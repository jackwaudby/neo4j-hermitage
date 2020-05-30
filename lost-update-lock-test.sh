#!/bin/bash

for i in {1..100}
do
     -- curl -s -X POST -H 'Content-type: application/json' http://neo4j:admin@localhost:7474/db/data/transaction/commit -d '{"statements": [{"statement": "MATCH (n {id : 1}) set n._LOCK = true SET n.value = n.value + 1 remove n._LOCK return n.value;"}]}' | jq -r '(.results[0]) | .columns,.data[].row | @csv'

done
