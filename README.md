# Hermitage Style Neo4j Isolation Tests

The [Neo4j docs](https://neo4j.com/docs/java-reference/current/transaction-management/introduction/) state:
- _"All database operations that access the graph, indexes, or the schema must be performed in a transaction."_
- _"The default isolation level is `READ_COMMITTED` - which means they will see data as soon as it has been committed and will not see data in other transactions that have not yet been committed."_
- Achieving Serializability is also possible - _"one can manually acquire write locks on nodes and relationships to achieve higher level of isolation `SERIALIZABLE`."_
- _"Non-repeatable reads may occur (i.e., only write locks are acquired and held until the end of the transaction)."_
- _"Data retrieved by traversals is not protected from modification by other transactions."_
- Lock granularity: _"Locks are acquired at the Node and Relationship level."_
- _"All modifications performed in a transaction are kept in memory."_

In summary at `READ_COMMITTED` we would expect Neo4j prevents G0 and G1{a-c}. 
It acknowledges lost update and non-repeatable reads are possible.

Summary of test results
-----------------------

| DBMS          | So-called isolation level    | Actual isolation level | G0 | G1a | G1b | G1c | OTV | PMP | P4 | G-single | G2-item | G2   |
|:--------------|:-----------------------------|:-----------------------|:--:|:---:|:---:|:---:|:---:|:---:|:--:|:--------:|:-------:|:----:|
| Neo4j         | "read committed" ★           | monotonic atomic view  | ✓  | ✓   | ✓   | ✓   | ✓   |  -  |    some |     -     |   -      |      |
|               | "serializable"               | serializable           | ✓  | ✓   | ✓   | ✓   | ✓   |  ✓   |    ✓ |     ✓    |    ✓     |   ✓   |


Legend:

* ★ = default configuration
* ✓ = isolation level prevents this anomaly from occurring
* — = isolation level does not prevent this anomaly, so it can occur
* some = isolation level prevents this anomaly in some cases, but not in others (see test cases for details)
* anomalies
  - G0: Write Cycles (dirty writes)
  - G1a: Aborted Reads (dirty reads, cascaded aborts)
  - G1b: Intermediate Reads (dirty reads)
  - G1c: Circular Information Flow (dirty reads)
  - OTV: Observed Transaction Vanishes
  - PMP: Predicate-Many-Preceders
  - P4: Lost Update
  - G-single: Single Anti-dependency Cycles (read skew)
  - G2-item: Item Anti-dependency Cycles (write skew on disjoint read)
  - G2: Anti-Dependency Cycles (write skew on predicate read)
