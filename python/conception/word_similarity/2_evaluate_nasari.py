import argparse

from constants import Nasari
import utils
import setup


if __name__ == '__main__':
    parser = argparse.ArgumentParser()
    parser.add_argument('nasari_language', help='NASARI vectors language (EN, ES, DE, IT, FR, Unified)', type=str)
    parser.add_argument('-v', '--verbose', help='Detailed scores pair by pair.', action='store_true')
    args = parser.parse_args()

    nasari_vectors = Nasari[args.nasari_language]

    print('\nEvaluating NASARI\n')

    print('  Loading word pairs...')
    word_pairs = utils.load_word_pairs()
    print('  Loading word pairs... Done!\n')
    
    print('  Loading word synsets and building synset vocabulary...')
    word_synsets, synsets = utils.load_word_synsets(
        pos=setup.POS,
        include_named_entities=setup.INCLUDE_NAMED_ENTITIES)
    print('  Loading word synsets and building synset vocabulary... Done! [{} synsets]\n'.format(len(synsets)))

    print('  Loading synset weights...')
    synset_weights = utils.load_synset_weights(synsets)
    print('  Loading synset weights... Done!\n')

    print('  Loading NASARI vectors...')
    vectors = utils.load_vectors(
        nasari_vectors.value,
        synsets,
        normalize=setup.NORMALIZE_SCORES,
        min_length=setup.MIN_VECTOR_LENGTH,
        max_length=setup.MAX_VECTOR_LENGTH,
        skip_dims=1,
        component_separator='_',
        int_synset=False)
    print('  Loading NASARI vectors... Done! [{} vectors]\n'.format(len(vectors)))

    utils.evaluate(
        'NASARI_{}'.format(args.nasari_language),
        word_pairs,
        word_synsets,
        synset_weights,
        vectors,
        setup.SIMILARITY_FUNCTION,
        setup.SCORING_FUNCTION,
        verbose=args.verbose)
