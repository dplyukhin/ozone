#!/bin/bash

# mvn compile

mvn -q -e exec:java -Dexec.mainClass="choral.examples.ozone.concurrentproducers.Server" &
sleep 1
mvn -q -e exec:java -Dexec.mainClass="choral.examples.ozone.concurrentproducers.Worker2" &
mvn -q -e exec:java -Dexec.mainClass="choral.examples.ozone.concurrentproducers.Worker1" &

wait
