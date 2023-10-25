#!/bin/bash

mvn exec:java -Dexec.mainClass="choral.examples.futures.Client" -Dexec.args="'$*'"