#!/bin/bash

echo "Compiling code..."
javac \
	-cp BabelNet-API-4.0.1/babelnet-api-4.0.1.jar:BabelNet-API-4.0.1/lib/*:config \
	src/preprocess/SynsetGlossGetter.java \
	-d classes
echo "Compiling code... Done!"

echo

echo "Deleting previous outputs..."
rm synset_glosses.tsv
echo "Deleting previous outputs... Done!"

echo

echo "Running code..."
java \
	-cp BabelNet-API-4.0.1/babelnet-api-4.0.1.jar:BabelNet-API-4.0.1/lib/*:config:classes \
    preprocess.SynsetGlossGetter \
    lemmas.tsv \
    synset_glosses.tsv \
    100000
echo "Running code... Done!"

echo