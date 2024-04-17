#!/bin/bash
trap "kill 0" EXIT

# mvn compile

for requestsPerSecond in 160; do
    systemArgs="-DrequestsPerSecond=$requestsPerSecond"

    echo "Running with requestsPerSecond=$requestsPerSecond."

    mvn -q -e exec:java -Dexec.mainClass="choral.examples.ozone.inorderproducers.Server" $systemArgs &
    sleep 1
    mvn -q -e exec:java -Dexec.mainClass="choral.examples.ozone.inorderproducers.Worker2" $systemArgs &
    mvn -q -e exec:java -Dexec.mainClass="choral.examples.ozone.inorderproducers.Worker1" $systemArgs &

    wait
done
