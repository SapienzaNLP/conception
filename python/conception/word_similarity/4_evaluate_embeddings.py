import argparse

from constants import Conception, Embeddings
import utils
import setup


if __name__ == '__main__':
    parser = argparse.ArgumentParser()
    parser.add_argument('embedding_1', help='embedding name (Muse_EN, etc.)', type=str)
    parser.add_argument('embedding_2', help='embedding name (Muse_EN, etc.)', type=str)
    parser.add_argument('-v', '--verbose', help='Detailed scores pair by pair.', action='store_true')
    args = parser.parse_args()

    path_1 = Embeddings[args.embedding_1].value
    path_2 = Embeddings[args.embedding_2].value

    print('\nEvaluating Conception\n')

    print('  Loading word pairs...')
    word_pairs = utils.load_word_pairs(append_language_code=False)
    print('  Loading word pairs... Done!\n')

    vocabulary_1 = set()
    vocabulary_2 = set()
    for dataset_class in word_pairs:
        for dataset_name in word_pairs[dataset_class]:
            for pair in word_pairs[dataset_class][dataset_name]:
                vocabulary_1.add(pair['word_1'])
                vocabulary_2.add(pair['word_2'])

    print('  Loading embeddings...')
    embeddings_1 = utils.load_embeddings(path_1, vocabulary_1)
    embeddings_2 = utils.load_embeddings(path_2, vocabulary_2)
    print('  Loading embeddings vectors... Done! [{} vectors, {} vectors]\n'.format(len(embeddings_1), len(embeddings_2)))

    utils.evaluate_embeddings(
        '{}-{}'.format(args.embedding_1, args.embedding_2),
        word_pairs,
        embeddings_1,
        embeddings_2,
        verbose=args.verbose)
