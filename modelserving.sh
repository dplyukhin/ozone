#!/bin/bash
systemArgs=${@:1}
workers=("client" "batcher" "worker1" "worker2" "model1" "model2")
me=$(scontrol show aliases)
nodes=("node0" "node1" "node2" "node3" "node4" "node5")
len=${#nodes[@]}
leader=${nodes[0]}

for (( i=0; i<$len; i++ )); do
  if [ $me = ${nodes[$i]} ]; then
    role=${workers[i]}
    echo "I'm $me playing the role of $role. The leader is $leader."

    if [ "$role" = "client" ]; then
        mvn -e exec:java -Dexec.mainClass="choral.examples.ozone.modelserving.Client" -Dexec.args="img.jpg" $systemArgs &
    elif [ "$role" = "batcher" ]; then
	mvn -e exec:java -Dexec.mainClass="choral.examples.ozone.modelserving.Batcher" $systemArgs &
    elif [[ "$role" = "worker1" ]]; then
	mvn -e exec:java -Dexec.mainClass="choral.examples.ozone.modelserving.Worker" -Dexec.args="--workerID 1" $systemArgs &
    elif [[ "$role" = "worker2" ]]; then
	mvn -e exec:java -Dexec.mainClass="choral.examples.ozone.modelserving.Worker" -Dexec.args="--workerID 2" $systemArgs &
    elif [[ "$role" = "model1" ]]; then
	mvn -e exec:java -Dexec.mainClass="choral.examples.ozone.modelserving.Model" -Dexec.args="--modelID 1" $systemArgs &
    elif [[ "$role" = "model2" ]]; then
	mvn -e exec:java -Dexec.mainClass="choral.examples.ozone.modelserving.Model" -Dexec.args="--modelID 2" $systemArgs &
    else
        echo "Unknown role: $role"
        exit 1
    fi
  fi
  sleep 10
done

wait
