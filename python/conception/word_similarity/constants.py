import os
from enum import Enum

_BASE_DIR = '../../../'
_RES_DIR = os.path.join(_BASE_DIR, 'resources')
_TMP_DIR = os.path.join(_BASE_DIR, 'tmp')
_FIGURES_DIR = os.path.join(_BASE_DIR, 'figures')
_LOGS_DIR = os.path.join(_BASE_DIR, 'logs')

_WORDSIM_DATA_DIR = os.path.join(_RES_DIR, 'word_similarity')
_WORDSIM_TMP_DIR = os.path.join(_TMP_DIR, 'evaluation/word_similarity')

_NASARI_DIR = os.path.join(_RES_DIR, 'NASARI_remapped')
_CONCEPTION_DIR = os.path.join(_TMP_DIR, 'vectors')
_VOCABULARIES_DIR = os.path.join(_WORDSIM_TMP_DIR, 'vocabs')
_WORD_SYNSETS_DIR = os.path.join(_WORDSIM_TMP_DIR, 'word_synsets')

_MUSE_DIR = '../../../../embeddings/muse/'
_GEOMM_DIR = '../../../../embeddings/geomm/'
_NB_DIR = '../../../../embeddings/numberbatch/'


class Language(Enum):
    AR = 'AR'
    DE = 'DE'
    EN = 'EN'
    ES = 'ES'
    FA = 'FA'
    FR = 'FR'
    IT = 'IT'
    NL = 'NL'
    PT = 'PT'
    RU = 'RU'
    SV = 'SV'
    ZH = 'ZH'


class Nasari(Enum):
    EN = os.path.join(_NASARI_DIR, 'NASARI_lexical_english.txt')
    FR = os.path.join(_NASARI_DIR, 'NASARI_lexical_french.txt')
    DE = os.path.join(_NASARI_DIR, 'NASARI_lexical_german.txt')
    IT = os.path.join(_NASARI_DIR, 'NASARI_lexical_italian.txt')
    ES = os.path.join(_NASARI_DIR, 'NASARI_lexical_spanish.txt')
    Unified = os.path.join(_RES_DIR, 'NASARI_unified', 'NASARI_unified_english.remapped.txt')


class Conception(Enum):
    Selection = os.path.join(_CONCEPTION_DIR, 'selection/selection.tsv')
    Expansion = os.path.join(_CONCEPTION_DIR, 'expansion/expansion.tsv')
    Connection = os.path.join(_CONCEPTION_DIR, 'connection/connection.tsv')


class Embeddings(Enum):
    Muse_EN = os.path.join(_MUSE_DIR, 'wiki.multi.en.vec')
    Muse_ES = os.path.join(_MUSE_DIR, 'wiki.multi.es.vec')
    Muse_DE = os.path.join(_MUSE_DIR, 'wiki.multi.de.vec')
    Muse_IT = os.path.join(_MUSE_DIR, 'wiki.multi.it.vec')
    Geomm_EN = os.path.join(_GEOMM_DIR, 'vecmap-en.vec')
    Geomm_ES = os.path.join(_GEOMM_DIR, 'vecmap-es.vec')
    Geomm_DE = os.path.join(_GEOMM_DIR, 'vecmap-de.vec')
    Geomm_IT = os.path.join(_GEOMM_DIR, 'vecmap-it.vec')
    Numberbatch_19 = os.path.join(_NB_DIR, 'numberbatch-19.08.txt')


class WordPairs():

    def __init__(
        self,
        name,
        path,
        first_language,
        second_language,
    ):
        self.name = name
        self.path = path
        self.first_language = first_language
        self.second_language = second_language


class MC(Enum):
    MC_AR = WordPairs('MC_AR', os.path.join(_WORDSIM_DATA_DIR, 'mc_ar.tsv'), Language.AR, Language.AR)
    MC_DE = WordPairs('MC_DE', os.path.join(_WORDSIM_DATA_DIR, 'mc_de.tsv'), Language.DE, Language.DE)
    MC_EN = WordPairs('MC_EN', os.path.join(_WORDSIM_DATA_DIR, 'mc_en.tsv'), Language.EN, Language.EN)
    MC_ES = WordPairs('MC_ES', os.path.join(_WORDSIM_DATA_DIR, 'mc_es.tsv'), Language.ES, Language.ES)
    MC_FA = WordPairs('MC_FA', os.path.join(_WORDSIM_DATA_DIR, 'mc_fa.tsv'), Language.FA, Language.FA)
    MC_FR = WordPairs('MC_FR', os.path.join(_WORDSIM_DATA_DIR, 'mc_fr.tsv'), Language.FR, Language.FR)
    MC_IT = WordPairs('MC_IT', os.path.join(_WORDSIM_DATA_DIR, 'mc_it.tsv'), Language.IT, Language.IT)
    MC_NL = WordPairs('MC_NL', os.path.join(_WORDSIM_DATA_DIR, 'mc_nl.tsv'), Language.NL, Language.NL)
    MC_PT = WordPairs('MC_PT', os.path.join(_WORDSIM_DATA_DIR, 'mc_pt.tsv'), Language.PT, Language.PT)
    MC_RU = WordPairs('MC_RU', os.path.join(_WORDSIM_DATA_DIR, 'mc_ru.tsv'), Language.RU, Language.RU)
    MC_SV = WordPairs('MC_SV', os.path.join(_WORDSIM_DATA_DIR, 'mc_sv.tsv'), Language.SV, Language.SV)
    MC_ZH = WordPairs('MC_ZH', os.path.join(_WORDSIM_DATA_DIR, 'mc_zh.tsv'), Language.ZH, Language.ZH)


class RG(Enum):
    RG_AR = WordPairs('RG_AR', os.path.join(_WORDSIM_DATA_DIR, 'rg_ar.tsv'), Language.AR, Language.AR)
    RG_DE = WordPairs('RG_DE', os.path.join(_WORDSIM_DATA_DIR, 'rg_de.tsv'), Language.DE, Language.DE)
    RG_EN = WordPairs('RG_EN', os.path.join(_WORDSIM_DATA_DIR, 'rg_en.tsv'), Language.EN, Language.EN)
    RG_ES = WordPairs('RG_ES', os.path.join(_WORDSIM_DATA_DIR, 'rg_es.tsv'), Language.ES, Language.ES)
    RG_FA = WordPairs('RG_FA', os.path.join(_WORDSIM_DATA_DIR, 'rg_fa.tsv'), Language.FA, Language.FA)
    RG_FR = WordPairs('RG_FR', os.path.join(_WORDSIM_DATA_DIR, 'rg_fr.tsv'), Language.FR, Language.FR)
    RG_IT = WordPairs('RG_IT', os.path.join(_WORDSIM_DATA_DIR, 'rg_it.tsv'), Language.IT, Language.IT)
    RG_NL = WordPairs('RG_NL', os.path.join(_WORDSIM_DATA_DIR, 'rg_nl.tsv'), Language.NL, Language.NL)
    RG_PT = WordPairs('RG_PT', os.path.join(_WORDSIM_DATA_DIR, 'rg_pt.tsv'), Language.PT, Language.PT)
    RG_RU = WordPairs('RG_RU', os.path.join(_WORDSIM_DATA_DIR, 'rg_ru.tsv'), Language.RU, Language.RU)
    RG_SV = WordPairs('RG_SV', os.path.join(_WORDSIM_DATA_DIR, 'rg_sv.tsv'), Language.SV, Language.SV)
    RG_ZH = WordPairs('RG_ZH', os.path.join(_WORDSIM_DATA_DIR, 'rg_zh.tsv'), Language.ZH, Language.ZH)


class WS353(Enum):
    WS_AR = WordPairs('WS353_AR', os.path.join(_WORDSIM_DATA_DIR, 'ws353_ar.tsv'), Language.AR, Language.AR)
    WS_DE = WordPairs('WS353_DE', os.path.join(_WORDSIM_DATA_DIR, 'ws353_de.tsv'), Language.DE, Language.DE)
    WS_EN = WordPairs('WS353_EN', os.path.join(_WORDSIM_DATA_DIR, 'ws353_en.tsv'), Language.EN, Language.EN)
    WS_ES = WordPairs('WS353_ES', os.path.join(_WORDSIM_DATA_DIR, 'ws353_es.tsv'), Language.ES, Language.ES)
    WS_FA = WordPairs('WS353_FA', os.path.join(_WORDSIM_DATA_DIR, 'ws353_fa.tsv'), Language.FA, Language.FA)
    WS_FR = WordPairs('WS353_FR', os.path.join(_WORDSIM_DATA_DIR, 'ws353_fr.tsv'), Language.FR, Language.FR)
    WS_IT = WordPairs('WS353_IT', os.path.join(_WORDSIM_DATA_DIR, 'ws353_it.tsv'), Language.IT, Language.IT)
    WS_NL = WordPairs('WS353_NL', os.path.join(_WORDSIM_DATA_DIR, 'ws353_nl.tsv'), Language.NL, Language.NL)
    WS_PT = WordPairs('WS353_PT', os.path.join(_WORDSIM_DATA_DIR, 'ws353_pt.tsv'), Language.PT, Language.PT)
    WS_RU = WordPairs('WS353_RU', os.path.join(_WORDSIM_DATA_DIR, 'ws353_ru.tsv'), Language.RU, Language.RU)
    WS_SV = WordPairs('WS353_SV', os.path.join(_WORDSIM_DATA_DIR, 'ws353_sv.tsv'), Language.SV, Language.SV)
    WS_ZH = WordPairs('WS353_ZH', os.path.join(_WORDSIM_DATA_DIR, 'ws353_zh.tsv'), Language.ZH, Language.ZH)


class SemEval2017Mono(Enum):
    SemEval_DE = WordPairs('SemEval2017_DE', os.path.join(_WORDSIM_DATA_DIR, 'semeval2017_de.tsv'), Language.DE, Language.DE)
    SemEval_EN = WordPairs('SemEval2017_EN', os.path.join(_WORDSIM_DATA_DIR, 'semeval2017_en.tsv'), Language.EN, Language.EN)
    SemEval_ES = WordPairs('SemEval2017_ES', os.path.join(_WORDSIM_DATA_DIR, 'semeval2017_es.tsv'), Language.ES, Language.ES)
    SemEval_FA = WordPairs('SemEval2017_FA', os.path.join(_WORDSIM_DATA_DIR, 'semeval2017_fa.tsv'), Language.FA, Language.FA)
    SemEval_IT = WordPairs('SemEval2017_IT', os.path.join(_WORDSIM_DATA_DIR, 'semeval2017_it.tsv'), Language.IT, Language.IT)


class SemEval2017Cross(Enum):
    SemEval_DE_ES = WordPairs('SemEval2017_DE_ES', os.path.join(_WORDSIM_DATA_DIR, 'semeval2017_de-es.tsv'), Language.DE, Language.ES)
    SemEval_DE_FA = WordPairs('SemEval2017_DE_FA', os.path.join(_WORDSIM_DATA_DIR, 'semeval2017_de-fa.tsv'), Language.DE, Language.FA)
    SemEval_DE_IT = WordPairs('SemEval2017_DE_IT', os.path.join(_WORDSIM_DATA_DIR, 'semeval2017_de-it.tsv'), Language.DE, Language.IT)
    SemEval_EN_DE = WordPairs('SemEval2017_EN_DE', os.path.join(_WORDSIM_DATA_DIR, 'semeval2017_en-de.tsv'), Language.EN, Language.DE)
    SemEval_EN_ES = WordPairs('SemEval2017_EN_ES', os.path.join(_WORDSIM_DATA_DIR, 'semeval2017_en-es.tsv'), Language.EN, Language.ES)
    SemEval_EN_FA = WordPairs('SemEval2017_EN_FA', os.path.join(_WORDSIM_DATA_DIR, 'semeval2017_en-fa.tsv'), Language.EN, Language.FA)
    SemEval_EN_IT = WordPairs('SemEval2017_EN_IT', os.path.join(_WORDSIM_DATA_DIR, 'semeval2017_en-it.tsv'), Language.EN, Language.IT)
    SemEval_ES_FA = WordPairs('SemEval2017_ES_FA', os.path.join(_WORDSIM_DATA_DIR, 'semeval2017_es-fa.tsv'), Language.ES, Language.FA)
    SemEval_ES_IT = WordPairs('SemEval2017_ES_IT', os.path.join(_WORDSIM_DATA_DIR, 'semeval2017_es-it.tsv'), Language.ES, Language.IT)
    SemEval_IT_FA = WordPairs('SemEval2017_IT_FA', os.path.join(_WORDSIM_DATA_DIR, 'semeval2017_it-fa.tsv'), Language.IT, Language.FA)


class Card660(Enum):
    Card = WordPairs('Card660', os.path.join(_WORDSIM_DATA_DIR, 'card-660.tsv'), Language.EN, Language.EN)


class YP130(Enum):
    YP130 = WordPairs('YP130', os.path.join(_WORDSIM_DATA_DIR, 'yp-130.tsv'), Language.EN, Language.EN)


class SimVerb3500(Enum):
    SimVerb3500 = WordPairs('SimVerb3500', os.path.join(_WORDSIM_DATA_DIR, 'simverb-3500.tsv'), Language.EN, Language.EN) 


class Dataset(Enum):
    # MC = MC
    # RG = RG
    # WS353 = WS353
    # YP130 = YP130
    # SimVerb3500 = SimVerb3500
    SemEval2017Mono = SemEval2017Mono
    SemEval2017Cross = SemEval2017Cross
    Card660 = Card660


class AuxiliaryData(Enum):
    WORDS = os.path.join(_VOCABULARIES_DIR, 'word_vocabulary.tsv')
    WORD_SYNSETS = os.path.join(_WORD_SYNSETS_DIR, 'word_synsets.tsv')
    SYNSET_WEIGHTS = os.path.join(_TMP_DIR, 'synsets/synset_weights.tsv')
    SYNSET_NEIGHBORS = os.path.join(_TMP_DIR, 'synsets/clean_synset_neighbors.tsv')
    FIGURES = _FIGURES_DIR
    LOGS = _LOGS_DIR