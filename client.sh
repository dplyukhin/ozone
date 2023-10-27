#!/bin/bash

mvn exec:java -Dexec.mainClass="choral.examples.futures.hello.Client" -Dexec.args="'$*'"