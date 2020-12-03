#!/bin/bash
echo "Compiling code..."
javac \
	src/vectorization/SynsetExpansion.java \
	-d classes
echo "Compiling code... Done!"

echo

echo "Deleting previous outputs..."
rm ../../tmp/vectors/expansion/expansion.tsv
echo "Deleting previous outputs... Done!"

echo

echo "Running code..."
java \
    -Xmx60g -Xms8g \
	-cp classes \
    vectorization.SynsetExpansion \
    ../../tmp/vectors/selection/split \
    ../../tmp/synsets/clean_synset_neighbors.tsv \
    ../../tmp/vectors/expansion/expansion.tsv \
    50000
echo "Running code... Done!"