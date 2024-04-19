#trap "kill 0" EXIT

batchSize=10

for useOzone in false true; do
    requestsPerSecondValues=(25 50 75 100 125 150 175 225 275 350) # Skip rates that map to the same effective request rate

    for requestsPerSecond in ${requestsPerSecondValues[@]}; do
        systemArgs="-DbatchSize=$batchSize -DrequestsPerSecond=$requestsPerSecond -Djava.awt.headless=true"

        mvn -q -e scala:run -Dlauncher=client -Pchoral-nocompile $systemArgs &
        sleep 5
        mvn -q -e scala:run -Dlauncher=batcher -Pchoral-nocompile $systemArgs &
        sleep 5
        mvn -q -e scala:run -Dlauncher=worker1 -Pchoral-nocompile $systemArgs &
        sleep 5
        mvn -q -e scala:run -Dlauncher=worker2 -Pchoral-nocompile $systemArgs &
        sleep 5
        mvn -q -e scala:run -Dlauncher=model1 -Pchoral-nocompile $systemArgs &
        sleep 5
        mvn -q -e scala:run -Dlauncher=model2 -Pchoral-nocompile $systemArgs &

        wait
    done
done