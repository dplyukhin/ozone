#!/bin/bash
trap "kill 0" EXIT

# mvn compile

mvn exec:java -Dexec.mainClass="choral.examples.ozone.inorderproducers.Server" &
sleep 1
mvn exec:java -Dexec.mainClass="choral.examples.ozone.inorderproducers.Worker2" &
mvn exec:java -Dexec.mainClass="choral.examples.ozone.inorderproducers.Worker1" &

wait
