#!/bin/bash

echo "Compiling code..."
javac \
	-cp BabelNet-API-4.0.1/babelnet-api-4.0.1.jar:BabelNet-API-4.0.1/lib/*:config \
	src/preprocess/SynsetTypeGetter.java \
	-d classes
echo "Compiling code... Done!"

echo

echo "Deleting previous outputs..."
rm ../../tmp/synsets/synset_types.tsv
echo "Deleting previous outputs... Done!"

echo

echo "Running code..."
java \
	-cp BabelNet-API-4.0.1/babelnet-api-4.0.1.jar:BabelNet-API-4.0.1/lib/*:config:classes \
    preprocess.SynsetTypeGetter \
    ../../tmp/synsets/synset_types.tsv \
    100000
echo "Running code... Done!"

echo