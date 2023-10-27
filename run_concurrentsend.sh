#!/bin/bash
trap "kill 0" EXIT

mvn compile

mvn exec:java -Dexec.mainClass="choral.examples.futures.concurrentsend.Server" &
sleep 1
mvn exec:java -Dexec.mainClass="choral.examples.futures.concurrentsend.Client" &
mvn exec:java -Dexec.mainClass="choral.examples.futures.concurrentsend.Worker2" &
mvn exec:java -Dexec.mainClass="choral.examples.futures.concurrentsend.Worker1" &

wait