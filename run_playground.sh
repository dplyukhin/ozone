#!/bin/bash
trap "kill 0" EXIT

# mvn compile

mvn exec:java -Dexec.mainClass="choral.examples.futures.playground.Server" &
sleep 1
mvn exec:java -Dexec.mainClass="choral.examples.futures.playground.Client" &

wait