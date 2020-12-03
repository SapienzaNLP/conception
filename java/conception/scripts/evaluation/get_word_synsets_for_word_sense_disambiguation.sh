#!/bin/bash

echo "Compiling code..."
javac \
	-cp BabelNet-API-4.0.1/babelnet-api-4.0.1.jar:BabelNet-API-4.0.1/lib/*:config \
	src/evaluation/WordSynsetGetter.java \
	-d classes
echo "Compiling code... Done!"

echo "Deleting previous outputs..."
rm -rf ../../tmp/evaluation/word_sense_disambiguation/word_synsets
mkdir -p ../../tmp/evaluation/word_sense_disambiguation/word_synsets
echo "Deleting previous outputs... Done!"

echo "Running code..."
java \
	-cp BabelNet-API-4.0.1/babelnet-api-4.0.1.jar:BabelNet-API-4.0.1/lib/*:config:classes \
    evaluation.WordSynsetGetter \
    ../../tmp/evaluation/word_sense_disambiguation/vocabs/word_vocabulary.tsv \
    ../../tmp/evaluation/word_sense_disambiguation/word_synsets/word_synsets.tsv \
    100000
echo "Running code... Done!"