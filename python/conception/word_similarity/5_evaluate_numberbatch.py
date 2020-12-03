import argparse

from constants import Conception, Embeddings
import utils
import setup


if __name__ == '__main__':
    parser = argparse.ArgumentParser()
    parser.add_argument('embedding', help='embedding name (Numberbatch_19, etc.)', type=str)
    parser.add_argument('-v', '--verbose', help='Detailed scores pair by pair.', action='store_true')
    args = parser.parse_args()

    path = Embeddings[args.embedding].value

    print('\nEvaluating Conception\n')

    print('  Loading word pairs...')
    word_pairs = utils.load_word_pairs()
    print('  Loading word pairs... Done!\n')

    vocabulary = set()
    for dataset_class in word_pairs:
        for dataset_name in word_pairs[dataset_class]:
            for pair in word_pairs[dataset_class][dataset_name]:
                vocabulary.add(pair['word_1'])
                vocabulary.add(pair['word_2'])

    print('  Loading embeddings...')
    embeddings = utils.load_embeddings(path, vocabulary, numberbatch=True)
    print('  Loading embeddings vectors... Done! [{} vectors]\n'.format(len(embeddings)))

    utils.evaluate_embeddings(
        '{}'.format(args.embedding),
        word_pairs,
        embeddings,
        embeddings,
        verbose=args.verbose)
