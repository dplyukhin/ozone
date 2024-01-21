#!/bin/bash

# mvn compile

mvn exec:java -Dexec.mainClass="choral.examples.ozone.concurrentproducers.Server" &
sleep 3
mvn exec:java -Dexec.mainClass="choral.examples.ozone.concurrentproducers.Worker2" &
mvn exec:java -Dexec.mainClass="choral.examples.ozone.concurrentproducers.Worker1" &

wait
