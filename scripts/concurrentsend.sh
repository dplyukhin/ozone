#!/bin/bash

# mvn compile

mvn exec:java -Dexec.mainClass="choral.examples.ozone.concurrentsend.Server" &
sleep 1
mvn exec:java -Dexec.mainClass="choral.examples.ozone.concurrentsend.Client" &
mvn exec:java -Dexec.mainClass="choral.examples.ozone.concurrentsend.Worker2" &
mvn exec:java -Dexec.mainClass="choral.examples.ozone.concurrentsend.Worker1" &

wait
