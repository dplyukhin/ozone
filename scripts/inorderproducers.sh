#!/bin/bash
trap "kill 0" EXIT

# mvn compile

mvn exec:java -Dexec.mainClass="choral.examples.futures.inorderproducers.Server" &
sleep 1
mvn exec:java -Dexec.mainClass="choral.examples.futures.inorderproducers.Worker2" &
mvn exec:java -Dexec.mainClass="choral.examples.futures.inorderproducers.Worker1" &

wait