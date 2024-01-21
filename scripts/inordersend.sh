#!/bin/bash

# mvn compile

mvn exec:java -Dexec.mainClass="choral.examples.ozone.inordersend.Server" &
sleep 3
mvn exec:java -Dexec.mainClass="choral.examples.ozone.inordersend.Client" &
mvn exec:java -Dexec.mainClass="choral.examples.ozone.inordersend.Worker2" &
mvn exec:java -Dexec.mainClass="choral.examples.ozone.inordersend.Worker1" &

wait
