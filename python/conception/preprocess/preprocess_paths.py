import os
from enum import Enum

_BASE_DIR = '../../../'
_RESOURCES_DIR = os.path.join(_BASE_DIR, 'resources')
_TMP_DIR = os.path.join(_BASE_DIR, 'tmp')

_NASARI_ORIGINAL_DIR = os.path.join(_RESOURCES_DIR, 'NASARI_original')
_NASARI_REMAPPED_DIR = os.path.join(_RESOURCES_DIR, 'NASARI_remapped')
_NASARI_UNIFIED_DIR = os.path.join(_RESOURCES_DIR, 'NASARI_unified')
_NASARI_SPLIT_DIR = os.path.join(_NASARI_REMAPPED_DIR, 'split')
_VOCABULARIES_DIR = os.path.join(_TMP_DIR, 'vocabs')
_WORD_SYNSETS_DIR = os.path.join(_TMP_DIR, 'word_synsets')
_SYNSETS_DIR = os.path.join(_TMP_DIR, 'synsets')


class Mapping(Enum):
    bn3_to_bn4 = os.path.join(
        _NASARI_ORIGINAL_DIR, 'bn3_to_bn4.tsv')


class NasariOriginal(Enum):
    EN = os.path.join(_NASARI_ORIGINAL_DIR,
                      'NASARI_lexical_english.txt')
    FR = os.path.join(_NASARI_ORIGINAL_DIR,
                      'NASARI_lexical_french.txt')
    DE = os.path.join(_NASARI_ORIGINAL_DIR,
                      'NASARI_lexical_german.txt')
    IT = os.path.join(_NASARI_ORIGINAL_DIR,
                      'NASARI_lexical_italian.txt')
    ES = os.path.join(_NASARI_ORIGINAL_DIR,
                      'NASARI_lexical_spanish.txt')


class NasariRemapped(Enum):
    EN = os.path.join(_NASARI_REMAPPED_DIR,
                      'NASARI_lexical_english.txt')
    FR = os.path.join(_NASARI_REMAPPED_DIR,
                      'NASARI_lexical_french.txt')
    DE = os.path.join(_NASARI_REMAPPED_DIR,
                      'NASARI_lexical_german.txt')
    IT = os.path.join(_NASARI_REMAPPED_DIR,
                      'NASARI_lexical_italian.txt')
    ES = os.path.join(_NASARI_REMAPPED_DIR,
                      'NASARI_lexical_spanish.txt')


class NasariUnified(Enum):
    Original = os.path.join(_NASARI_UNIFIED_DIR,
                            'NASARI_unified_english.txt')
    Remapped = os.path.join(_NASARI_UNIFIED_DIR,
                            'NASARI_unified_english.remapped.txt')


class Conception(Enum):
    Selection = os.path.join(_TMP_DIR, 'vectors/selection/selection.tsv')
    Expansion = os.path.join(_TMP_DIR, 'vectors/expansion/expansion.tsv')
    Conception = os.path.join(_TMP_DIR, 'vectors/expansion/expansion.tsv')


class ConceptionSplit(Enum):
    Selection = os.path.join(_TMP_DIR, 'vectors/selection/split')
    Expansion = os.path.join(_TMP_DIR, 'vectors/expansion/split')
    Conception = os.path.join(_TMP_DIR, 'vectors/expansion/split')


class WordVocabulary(Enum):
    EN = os.path.join(_VOCABULARIES_DIR, 'en_word_vocabulary.tsv')
    FR = os.path.join(_VOCABULARIES_DIR, 'fr_word_vocabulary.tsv')
    DE = os.path.join(_VOCABULARIES_DIR, 'de_word_vocabulary.tsv')
    IT = os.path.join(_VOCABULARIES_DIR, 'it_word_vocabulary.tsv')
    ES = os.path.join(_VOCABULARIES_DIR, 'es_word_vocabulary.tsv')


class WordSynsets(Enum):
    EN = os.path.join(_WORD_SYNSETS_DIR, 'en_word_synsets.tsv')
    FR = os.path.join(_WORD_SYNSETS_DIR, 'fr_word_synsets.tsv')
    DE = os.path.join(_WORD_SYNSETS_DIR, 'de_word_synsets.tsv')
    IT = os.path.join(_WORD_SYNSETS_DIR, 'it_word_synsets.tsv')
    ES = os.path.join(_WORD_SYNSETS_DIR, 'es_word_synsets.tsv')


class WordSynsetsCleaned(Enum):
    EN = os.path.join(_WORD_SYNSETS_DIR, 'en_word_synsets_cleaned.tsv')
    FR = os.path.join(_WORD_SYNSETS_DIR, 'fr_word_synsets_cleaned.tsv')
    DE = os.path.join(_WORD_SYNSETS_DIR, 'de_word_synsets_cleaned.tsv')
    IT = os.path.join(_WORD_SYNSETS_DIR, 'it_word_synsets_cleaned.tsv')
    ES = os.path.join(_WORD_SYNSETS_DIR, 'es_word_synsets_cleaned.tsv')


class Synsets(Enum):
    VOCABULARY = os.path.join(_SYNSETS_DIR, 'synset_vocabulary.tsv')
    CATEGORIES = os.path.join(_SYNSETS_DIR, 'synset_categories.tsv')
    WEIGHTS = os.path.join(_SYNSETS_DIR, 'synset_weights.tsv')
    ALL_NEIGHBORS = os.path.join(_SYNSETS_DIR, 'all_synset_neighbors.tsv')
    CLEAN_NEIGHBORS = os.path.join(_SYNSETS_DIR, 'clean_synset_neighbors.tsv')
    EXPANDED_NEIGHBORS = os.path.join(_SYNSETS_DIR, 'expanded_synset_neighbors.tsv')
