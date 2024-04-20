#!/bin/bash
#trap "kill 0" EXIT

# mvn compile

batchSize=10

for useOzone in false true; do
    requestsPerSecondValues=(25 50 75 100 125 150 175 225 275 350) # Skip rates that map to the same effective request rate 

    for requestsPerSecond in ${requestsPerSecondValues[@]}; do
        systemArgs="-DbatchSize=$batchSize -DrequestsPerSecond=$requestsPerSecond -DuseOzone=$useOzone -Djava.awt.headless=true"

        echo "Running with useOzone=$useOzone and requestsPerSecond=$requestsPerSecond."

        mvn -q -e exec:java -Dexec.mainClass="choral.examples.ozone.modelserving.Client" -Dexec.args="img.jpg" $systemArgs &
        sleep 5
        mvn -q -e exec:java -Dexec.mainClass="choral.examples.ozone.modelserving.Batcher" $systemArgs &
        sleep 5
        mvn -q -e exec:java -Dexec.mainClass="choral.examples.ozone.modelserving.Worker" -Dexec.args="--workerID 1" $systemArgs &
        sleep 5
        mvn -q -e exec:java -Dexec.mainClass="choral.examples.ozone.modelserving.Worker" -Dexec.args="--workerID 2" $systemArgs &
        sleep 5
        mvn -q -e exec:java -Dexec.mainClass="choral.examples.ozone.modelserving.Model" -Dexec.args="--modelID 1" $systemArgs &
        sleep 5
        mvn -q -e exec:java -Dexec.mainClass="choral.examples.ozone.modelserving.Model" -Dexec.args="--modelID 2" $systemArgs &

        wait
    
    done
done
