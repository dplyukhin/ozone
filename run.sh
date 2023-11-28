#!/bin/bash
trap "kill 0" EXIT

mvn exec:java -Dexec.mainClass="choral.examples.futures.$1" -Dexec.args="'${@:2}'"