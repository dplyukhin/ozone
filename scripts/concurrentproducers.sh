#!/bin/bash
trap "kill 0" EXIT

# mvn compile

mvn exec:java -Dexec.mainClass="choral.examples.futures.concurrentproducers.Server" &
sleep 1
mvn exec:java -Dexec.mainClass="choral.examples.futures.concurrentproducers.Worker2" &
mvn exec:java -Dexec.mainClass="choral.examples.futures.concurrentproducers.Worker1" &

wait