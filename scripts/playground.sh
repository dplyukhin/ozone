#!/bin/bash
trap "kill 0" EXIT

# mvn compile

mvn exec:java -Dexec.mainClass="choral.examples.ozone.playground.Server" &
sleep 3
mvn exec:java -Dexec.mainClass="choral.examples.ozone.playground.Client" &

wait
