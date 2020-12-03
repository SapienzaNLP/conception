#!/bin/bash
echo "Compiling code..."
javac \
	src/vectorization/MergeConnection.java \
	-d classes
echo "Compiling code... Done!"

echo

echo "Deleting previous outputs..."
rm ../../tmp/vectors/connection/connection.tsv
echo "Deleting previous outputs... Done!"

echo

echo "Running code..."
java \
    -Xmx55g -Xms8g \
	-cp classes \
    vectorization.MergeConnection \
    ../../tmp/vectors/connection/split \
    ../../tmp/vectors/connection/connection.tsv \
    100000
echo "Running code... Done!"