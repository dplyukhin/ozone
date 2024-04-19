#!/bin/bash

trap "kill 0" EXIT

echo "Building Ozone..."
mvn clean > /dev/null
mvn compile > /dev/null

echo "Done. Now running benchmarks."

echo "Running producer microbenchmarks (Choral version)..."
bash scripts/inorderproducers.sh > /dev/null || true
echo "Results written to /data/inorderproducers."
echo "Running producer microbenchmarks (Ozone version)..."
bash scripts/concurrentproducers.sh > /dev/null || true
echo "Results written to /data/concurrentproducers."

echo "Running sender microbenchmarks (Choral version)..."
bash scripts/inordersend.sh > /dev/null || true
echo "Results written to /data/inordersend."
echo "Running sender microbenchmarks (Ozone version)..."
bash scripts/concurrentsend.sh > /dev/null || true
echo "Results written to /data/concurrentsend."

echo "Running model serving benchmark (Choral and Ozone versions)..."
bash scripts/modelserving.sh > /dev/null || true
echo "Results written to /data/modelserving."
echo "Running model serving benchmark (Akka version)..."
bash scripts/akkamodelserving.sh > /dev/null || true
echo "Results written to /data/modelserving."

echo "Done. Now plotting results."

python3 scripts/plot.py