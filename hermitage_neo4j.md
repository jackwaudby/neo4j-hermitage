# Testing Neo4j transaction isolation levels #

These tests were run with Neo4j 4.0.4 community edition.
Initially tests where run by opening multiple Cypher shells. However, it was difficult to display certain anomalies as Cypher statements _seemed_ to be issued only when you enter `:commit` i.e. there was no round tripping between the client (cypher shell) and the database (Neo4j). To work around this I switched to using Neo4j Embedded in a Java application and created a number of threads to simulate the clients. To control the order of events threads merely sleep between operations within a transaction. It's a bit of a hack but it works surprising well. 


```
# install neo4j 4.0.4
export NEO4J_HOME=

# Run test
./run_test.sh <anomaly> <delay-between-ops>
```

Below is a description of each test.

### G0 - Dirty Write ###
Informally, if two transactions write multiple of the _same_ objects they do so in the same order.
```
:begin -- t1
:begin -- t2
MATCH (n1:Test {id:1}) SET n1.value = 11 -- t1
MATCH (n1:Test {id:1}) SET n1.value = 12 -- t2
MATCH (n2:Test {id:2}) SET n2.value = 21 -- t1
:commit -- t1
:begin
MATCH (n) RETURN n; -- t1. Shows 1 => 11, 2 => 21
:commit
MATCH (n2:Test {id:2}) SET n2.value = 22 -- t2
:commit
MATCH (n) RETURN n; -- t1/t2. Shows 1 => 12, 2 => 22
```
Neo4j prevents *dirty writes*.

### G1a - Aborted Reads ###
Informally, a transaction never sees the effects of an aborted transaction. 
```
:begin -- t1
:begin -- t2
MATCH (n1:Test {id:1}) SET n1.value = 101;  -- t1
MATCH (n:Test) WHERE n.id = 1 RETURN n.value AS val; -- t2. 
:rollback -- t1
MATCH (n:Test) WHERE n.id = 1 RETURN n.value AS val; -- t2. 
:commit -- t2. Does not include aborted write by t1
```
Neo4j prevents *reading aborted transaction's writes*.

### G1b - Intermediate Reads ###
Informally, if a transaction makes multiple updates to an object, other transactions never see the intermediate updates. 
```
:begin -- t1
:begin -- t2
MATCH (n1:Test {id:1}) SET n1.value = 101;  -- t1
MATCH (n:Test) WHERE n.id = 1 RETURN n.value AS val; -- t2. 
MATCH (n1:Test {id:1}) SET n1.value = 11; -- t1
:commit -- t1
MATCH (n:Test) WHERE n.id = 1 RETURN n.value AS val; -- t2. 
:commit -- t2. Reads 10 then reads 11, never 101
```
Neo4j prevents *reading intermediate versions of transaction's writes*.

### G1c - Circular Information Flow ###
Informally, if transaction 1 affects transaction 2, then transction 2 cannot affect transaction 1 - you don't read what I've wrote if I've read what you wrote. 
```
:begin -- t1
:begin -- t2
MATCH (n1:Test {id:1}) SET n1.value = 11; -- t1
MATCH (n2:Test {id:2}) SET n2.value = 22; -- t2
MATCH (n2:Test) WHERE n2.id = 2 RETURN n2.value AS val; -- t1
MATCH (n1:Test) WHERE n1.id = 1 RETURN n1.value AS val; -- t2
:commit -- t1 // doesn't see t2 write
:commit -- t2  // sees t1 write
```

### OTV - Observed Transaction Vanishes ###
Informally, transaction 3 sees a write by transaction 1 of X but then fails to observe the write of Y by transaction 1 -- as if, by magic, transaction 1 has disappeared

```
:begin -- t1
:begin -- t2
:begin -- t3
MATCH (n1:Test {id:1}) SET n1.value = 11; -- t1
MATCH (n2:Test {id:2}) SET n2.value = 19; -- t1
MATCH (n1:Test {id:1}) SET n1.value = 12; -- t2
:commit -- t1
MATCH (n1:Test {id:1}) RETURN n1.value AS val; -- t3
MATCH (n2:Test {id:2}) SET n2.value = 18; -- t2
MATCH (n2:Test {id:2}) RETURN n2.value AS val; -- t3
:commit -- t2
MATCH (n2:Test {id:2}) RETURN n2.value AS val; -- t3
MATCH (n1:Test {id:1}) RETURN n1.value AS val; -- t3
:commit -- t3 => none of t1 writes are read! 
```
Interestingly, Neo4j seems to prevent OTV (which is allowed by `READ_COMMITTED`). Neo4j _probably_ provides `MONOTONIC_ATOMIC_VIEW` by default.

### PMP - Predicate-Many-Preceders ###
Informally, a transaction performs 2+ predicate reads, where their logical ranges overlap. A concurrent transaction performs an operation that changes the results in the overlap, but the matches in each predicate read differs.
```
:begin -- t1
:begin -- t2
MATCH (n:Test) WHERE n.value = 30 RETURN n; -- t1
CREATE (n:Test {id: 3, value: 30}); -- t2
:commit; -- t2
MATCH (n:Test) WHERE n.value % 3 = 0 RETURN n; -- t1 (Expected to return the newly inserted node)
:commit -- t1
```
In this test Neo4j fails to prevent PMP under `READ_COMMITTED`. 

### P4 - Lost Update ### 
Informally, two concurrent transactions, t1 and t2, try update the same object. t2 never sees the update from t1, it overwrites it and t1's update is committed but "lost".
```
:begin -- t1
:begin -- t2
MATCH (n:Test {id:1}) SET n.value = n.value + 1; -- t1
MATCH (n:Test {id:1}) SET n.value = n.value + 1; -- t2
:commit
:commit
```
Interestingly, in this test Neo4j seems to prevent P4 (which is allowed by `READ_COMMITTED`). However, you can 

Simulate 2 concurrent clients incrementing a counter `100` times. `n.value` should be 210. 
```
# run from 2 terminals
./lost-update-test.sh
```
In this test Neo4j fails to prevent P4 under `READ_COMMITTED`. However, by taking an explicit write lock (`SERIALIZABILITY`) you can prevent this error. 
```
# run from 2 terminals
./lost-update-lock-test.sh
```

### G-single - Read Skew ###
Informally, some transaction both observes and misses modifications of another transaction.
```
:begin -- t1
:begin -- t2
MATCH (n1:Test {id:1}) RETURN n1; -- t1 (Sees 1 => 10)
MATCH (n1:Test {id:1}) SET n1.value = 12; -- t2
MATCH (n2:Test {id:2}) SET n2.value = 18; -- t2 
:commit -- t2
MATCH (n2:Test {id:2}) RETURN n2; -- t1 (Sees 2 => 18)
:commit -- t1 (Seen one of t2s writes but not the other) 
```
In this test Neo4j fails to prevent G-Single under `READ_COMMITTED`. 

### G2-item - Write Skew ###
Informally, transactions read sets overlap but their write sets are disjoint.
```
:begin -- t1
:begin -- t2
MATCH (n:Test) WHERE n.id in [1,2] RETURN n.value AS val; -- t1
MATCH (n:Test) WHERE n.id in [1,2] RETURN n.value AS val; -- t2
MATCH (n1:Test {id:1}) SET n1.value = 11; -- t1
MATCH (n2:Test {id:2}) SET n2.value = 21; -- t2
:commit -- t1
:commit -- t2
```
In this test Neo4j fails to prevent G2-item under `READ_COMMITTED`. 

### G2 - Anti-Dependency Cycles ###
Informally, transactions predicate read sets overlap but their write sets are disjoint.

```
:begin -- t1
:begin -- t2
MATCH (n:Test) WHERE n.value % 3 = 0 RETURN n; -- t1
MATCH (n:Test) WHERE n.value % 3 = 0 RETURN n; -- t2
CREATE (n:Test {id: 3, value: 30}); -- t1
CREATE (n:Test {id: 4, value: 42}); -- t2
:commit -- t1
:commit -- t2
:begin -- t3
MATCH (n:Test) WHERE n.value % 3 = 0 RETURN n; -- t3
:commit -- t3
```

In this test Neo4j fails to prevent G2 under `READ_COMMITTED`. 
