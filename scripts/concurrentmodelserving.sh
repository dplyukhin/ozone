#!/bin/bash
trap "kill 0" EXIT

# mvn compile

batchSize=10
imagesPerSecond=200
useOzone=false
systemArgs="-DbatchSize=$batchSize -DimagesPerSecond=$imagesPerSecond -DuseOzone=$useOzone"

mvn -q -e exec:java -Dexec.mainClass="choral.examples.ozone.modelserving.Client" -Dexec.args="img.jpg" $systemArgs &
sleep 1
mvn -q -e exec:java -Dexec.mainClass="choral.examples.ozone.modelserving.Batcher" $systemArgs &
sleep 1
mvn -q -e exec:java -Dexec.mainClass="choral.examples.ozone.modelserving.Worker" -Dexec.args="--workerID 1" $systemArgs &
sleep 1
mvn -q -e exec:java -Dexec.mainClass="choral.examples.ozone.modelserving.Worker" -Dexec.args="--workerID 2" $systemArgs &
sleep 1
mvn -q -e exec:java -Dexec.mainClass="choral.examples.ozone.modelserving.Model" -Dexec.args="--modelID 1" $systemArgs &
sleep 1
mvn -q -e exec:java -Dexec.mainClass="choral.examples.ozone.modelserving.Model" -Dexec.args="--modelID 2" $systemArgs &

wait
