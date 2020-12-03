#!/bin/bash

echo "Compiling code..."
javac \
	-cp BabelNet-API-4.0.1/babelnet-api-4.0.1.jar:BabelNet-API-4.0.1/lib/*:config \
	src/preprocess/SynsetNeighborGetter.java \
	-d classes
echo "Compiling code... Done!"

echo

echo "Deleting previous outputs..."
rm ../../tmp/synsets/synset_neighbors.tsv
echo "Deleting previous outputs... Done!"

echo

echo "Running code..."
java \
	-cp BabelNet-API-4.0.1/babelnet-api-4.0.1.jar:BabelNet-API-4.0.1/lib/*:config:classes \
    preprocess.SynsetNeighborGetter \
    ../../tmp/synsets/synset_neighbors.tsv \
    100000
echo "Running code... Done!"

echo