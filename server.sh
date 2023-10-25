#!/bin/bash

mvn exec:java -Dexec.mainClass="choral.examples.futures.Server" -Dexec.args="'$*'"