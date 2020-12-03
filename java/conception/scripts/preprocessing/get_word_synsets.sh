#!/bin/bash

echo "Compiling code..."
javac \
	-cp BabelNet-API-4.0.1/babelnet-api-4.0.1.jar:BabelNet-API-4.0.1/lib/*:config \
	src/preprocess/WordSynsetGetter.java \
	-d classes
echo "Compiling code... Done!"

echo "Deleting previous outputs..."
rm ../../tmp/word_synsets/en_word_synsets.tsv
rm ../../tmp/word_synsets/it_word_synsets.tsv
rm ../../tmp/word_synsets/de_word_synsets.tsv
rm ../../tmp/word_synsets/es_word_synsets.tsv
rm ../../tmp/word_synsets/fr_word_synsets.tsv
echo "Deleting previous outputs... Done!"

echo "Running code..."
java \
	-cp BabelNet-API-4.0.1/babelnet-api-4.0.1.jar:BabelNet-API-4.0.1/lib/*:config:classes \
    preprocess.WordSynsetGetter \
    ../../tmp/vocabs/en_word_vocabulary.tsv \
    ../../tmp/word_synsets/en_word_synsets.tsv \
    EN \
    100000

java \
	-cp BabelNet-API-4.0.1/babelnet-api-4.0.1.jar:BabelNet-API-4.0.1/lib/*:config:classes \
    preprocess.WordSynsetGetter \
    ../../tmp/vocabs/it_word_vocabulary.tsv \
    ../../tmp/word_synsets/it_word_synsets.tsv \
    IT \
    100000

java \
	-cp BabelNet-API-4.0.1/babelnet-api-4.0.1.jar:BabelNet-API-4.0.1/lib/*:config:classes \
    preprocess.WordSynsetGetter \
    ../../tmp/vocabs/de_word_vocabulary.tsv \
    ../../tmp/word_synsets/de_word_synsets.tsv \
    DE \
    100000

java \
	-cp BabelNet-API-4.0.1/babelnet-api-4.0.1.jar:BabelNet-API-4.0.1/lib/*:config:classes \
    preprocess.WordSynsetGetter \
    ../../tmp/vocabs/es_word_vocabulary.tsv \
    ../../tmp/word_synsets/es_word_synsets.tsv \
    ES \
    100000

java \
	-cp BabelNet-API-4.0.1/babelnet-api-4.0.1.jar:BabelNet-API-4.0.1/lib/*:config:classes \
    preprocess.WordSynsetGetter \
    ../../tmp/vocabs/fr_word_vocabulary.tsv \
    ../../tmp/word_synsets/fr_word_synsets.tsv \
    FR \
    100000
echo "Running code... Done!"