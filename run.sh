#!/bin/bash

mvn exec:java -Dexec.mainClass="choral.examples.futures.$1" -Dexec.args="'${@:2}'"