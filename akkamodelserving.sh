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
    echo mvn -q -e scala:run -Dlauncher=$role -Pchoral-nocompile $systemArgs
    mvn -q -e scala:run -Dlauncher=$role -Pchoral-nocompile $systemArgs &
  fi
  sleep 10
done

wait
