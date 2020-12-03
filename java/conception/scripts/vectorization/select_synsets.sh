#!/bin/bash
echo "Compiling code..."
javac \
	-cp BabelNet-API-4.0.1/babelnet-api-4.0.1.jar:BabelNet-API-4.0.1/lib/*:config \
	src/vectorization/SynsetSelection.java \
	-d classes
echo "Compiling code... Done!"

echo

echo "Deleting previous outputs..."
rm ../../tmp/vectors/selection/selection.tsv
echo "Deleting previous outputs... Done!"

echo

echo "Running code..."
java \
    -Xmx55g -Xms8g \
	-cp BabelNet-API-4.0.1/babelnet-api-4.0.1.jar:BabelNet-API-4.0.1/lib/*:config:classes \
    vectorization.SynsetSelection \
    ../../resources/NASARI_remapped/split \
    ../../tmp/word_synsets/ \
    ../../tmp/vectors/selection/selection.tsv \
    100000
echo "Running code... Done!"