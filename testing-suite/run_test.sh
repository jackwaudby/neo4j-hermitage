#!/bin/bash
if [ -z $1 ]
then
  echo "Specify a test to continue. Options are: g0, g1a, g1b, g1c, otv, pmp, p4, g-single, g2-item, g2"
  exit 2
elif [ -n $1 ]
then
    mvn compile
    mvn exec:java -Dexec.mainClass=TestDriver  -Dexec.cleanupDaemonThreads=false -Dexec.args="$1 $2"
fi

# TODO
# pmp-write
# g-single-dependencies
# g-single-write-1
# g-single-write-2
# g2-two-edges