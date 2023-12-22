#!/bin/bash
trap "kill 0" EXIT

# mvn compile

mvn exec:java -Dexec.mainClass="choral.examples.futures.inordersend.Server" &
sleep 1
mvn exec:java -Dexec.mainClass="choral.examples.futures.inordersend.Client" &
mvn exec:java -Dexec.mainClass="choral.examples.futures.inordersend.Worker2" &
mvn exec:java -Dexec.mainClass="choral.examples.futures.inordersend.Worker1" &

wait