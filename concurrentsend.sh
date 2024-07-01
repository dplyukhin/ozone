#!/bin/bash
useOzone=$1
systemArgs=${@:2}
workers=("server" "client" "worker1" "worker2")
me=$(scontrol show aliases)
nodes=("node0" "node1" "node2" "node4")
len=${#nodes[@]}
leader=${nodes[0]}

if [ "$useOzone" = "true" ]; then
    benchmark="concurrentsend"
else
    benchmark="inordersend"
fi

for (( i=0; i<$len; i++ )); do
  if [ $me = ${nodes[$i]} ]; then
    role=${workers[i]}
    echo "I'm $me playing the role of $role. The leader is $leader."

    if [ "$role" = "server" ]; then
        mvn -e exec:java -Dexec.mainClass="choral.examples.ozone.$benchmark.Server" $systemArgs &
    elif [[ "$role" = "client" ]]; then
        mvn -e exec:java -Dexec.mainClass="choral.examples.ozone.$benchmark.Client" $systemArgs &
    elif [[ "$role" = "worker1" ]]; then
        mvn -e exec:java -Dexec.mainClass="choral.examples.ozone.$benchmark.Worker1" $systemArgs &
    elif [[ "$role" = "worker2" ]]; then
        mvn -e exec:java -Dexec.mainClass="choral.examples.ozone.$benchmark.Worker2" $systemArgs &
    else
        echo "Unknown role: $role"
        exit 1
    fi
  fi
  sleep 10
done

wait
