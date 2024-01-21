#!/bin/bash

# mvn compile

mvn exec:java -Dexec.mainClass="choral.examples.ozone.inorderproducers.Server" &
sleep 3
mvn exec:java -Dexec.mainClass="choral.examples.ozone.inorderproducers.Worker2" &
mvn exec:java -Dexec.mainClass="choral.examples.ozone.inorderproducers.Worker1" &

wait
