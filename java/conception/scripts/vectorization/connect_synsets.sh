#!/bin/bash
echo "Compiling code..."
javac \
	src/vectorization/SynsetConnection.java \
	-d classes
echo "Compiling code... Done!"

echo

echo "Deleting previous outputs..."
rm -rf ../../tmp/vectors/connection/split
mkdir -p ../../tmp/vectors/connection/split
echo "Deleting previous outputs... Done!"

echo

echo "Running code..."
java \
    -Xmx55g -Xms8g \
	-cp classes \
    vectorization.SynsetConnection \
    ../../tmp/vectors/expansion/split \
    ../../tmp/synsets/synset_weights.tsv \
    ../../tmp/synsets/synset_types.tsv \
    ../../tmp/vectors/connection/split \
    100000
echo "Running code... Done!"