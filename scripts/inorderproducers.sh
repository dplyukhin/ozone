#!/bin/bash
trap "kill 0" EXIT

# mvn compile

for requestsPerSecond in 20 40 60 80 100 120 140 160; do
    systemArgs="-DrequestsPerSecond=$requestsPerSecond"

    echo "Running with requestsPerSecond=$requestsPerSecond."

    mvn -q -e exec:java -Dexec.mainClass="choral.examples.ozone.inorderproducers.Server" $systemArgs &
    sleep 2
    mvn -q -e exec:java -Dexec.mainClass="choral.examples.ozone.inorderproducers.Worker2" $systemArgs &
    mvn -q -e exec:java -Dexec.mainClass="choral.examples.ozone.inorderproducers.Worker1" $systemArgs &

    wait
done
