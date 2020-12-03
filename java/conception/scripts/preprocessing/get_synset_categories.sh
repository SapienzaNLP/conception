#!/bin/bash

echo "Compiling code..."
javac \
	-cp BabelNet-API-4.0.1/babelnet-api-4.0.1.jar:BabelNet-API-4.0.1/lib/*:config \
	src/preprocess/SynsetCategoryGetter.java \
	-d classes
echo "Compiling code... Done!"

echo "Deleting previous outputs..."
rm ../../tmp/synsets/synset_categories.tsv
echo "Deleting previous outputs... Done!"

echo "Running code..."
java \
	-cp BabelNet-API-4.0.1/babelnet-api-4.0.1.jar:BabelNet-API-4.0.1/lib/*:config:classes \
    preprocess.SynsetCategoryGetter \
    ../../tmp/synsets/synset_vocabulary.tsv \
    ../../tmp/synsets/synset_categories.tsv \
    100000
echo "Running code... Done!"