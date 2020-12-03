import os
import sys
import warnings

if not sys.warnoptions:
    warnings.simplefilter("ignore")


from constants import Language, Dataset, AuxiliaryData

from scipy.stats import spearmanr
from scipy.stats import pearsonr

import numpy as np

import matplotlib.pyplot as plt


def load_word_pairs(append_language_code=True):
    word_pairs = {}

    for dataset_class in Dataset:
        
        word_pairs[dataset_class] = {}

        for dataset in dataset_class.value:
            dataset = dataset.value
            word_pairs[dataset_class][dataset.name] = []

            with open(dataset.path) as f:
                for line in f:
                    word_1, word_2, score = line.strip().split('\t')
                    if dataset.first_language == Language.AR or dataset.first_language == Language.FA:
                        word_1 = '\u202b{}\u202c'.format(word_1)
                    if dataset.second_language == Language.AR or dataset.second_language == Language.FA:
                        word_2 = '\u202b{}\u202c'.format(word_2)
                    if append_language_code:
                        word_1 = '{}_{}'.format(dataset.first_language.value, word_1)
                        word_2 = '{}_{}'.format(dataset.second_language.value, word_2)

                    word_pairs[dataset_class][dataset.name].append({
                        'word_1': word_1.lower(),
                        'word_2': word_2.lower(),
                        'score': float(score),
                    })
    
    return word_pairs


def load_word_synsets(pos=None, include_named_entities=False):
    word_synsets_dictionary = {}
    synset_vocabulary = set()

    with open(AuxiliaryData.WORD_SYNSETS.value) as f:
        for line in f:
            word, _, *other = line.strip().split('\t')
            word = word.lower()
            word_synsets = []
            for e in other:
                synset, synset_type = e.split('::')
                if pos and synset.strip()[-1] != pos:
                    continue
                if not include_named_entities and synset_type == 'E':
                    continue
                if not include_named_entities and int(synset[3:11]) > 117_650:
                    continue

                synset = synset[3:11]
                word_synsets.append(synset)

            word_synsets_dictionary[word] = word_synsets
            synset_vocabulary.update(word_synsets)
    
    return word_synsets_dictionary, synset_vocabulary


def load_synset_weights(synsets):
    synset_weights = {}
    min_weight = None

    with open(AuxiliaryData.SYNSET_WEIGHTS.value) as f:
        for line in f:
            synset, weight = line.strip().split('\t')
            weight = float(weight)
            if synset in synsets:
                synset_weights[synset] = weight
                if not min_weight or weight < min_weight:
                    min_weight = weight

    for synset in synsets:
        if synset not in synset_weights:
            synset_weights[synset] = min_weight

    return synset_weights


def load_vectors(vector_path, synsets, normalize=True, min_length=30, max_length=300, component_separator='::', skip_dims=2, int_synset=True):
    vectors = {}

    with open(vector_path) as f:
        for line in f:
            if int_synset:
                synset = line[0:8]
            else:
                synset = line[3:11]
            
            if synset not in synsets:
                continue
            
            vector = {}
            parts = line.strip().split('\t')[skip_dims:]
            if max_length:
                if max_length <= 1.0:
                    limit = int(max_length * len(parts))
                    parts = parts[:limit]
                else:
                    parts = parts[:max_length]
            score_sum = 0.0
            vector_length = 0
            for part in parts:
                component, score = part.split(component_separator)
                score = float(score)
                score_sum += score
                vector[component] = {
                    'rank': len(vector) + 1,
                    'score': score,
                }
                vector_length += 1

            if normalize:
                for c in vector:
                    vector[c]['score'] /= score_sum
            
            if min_length and len(vector) < min_length:
                continue
            
            vectors[synset] = vector
    
    return vectors


def load_embeddings(embeddings_path, vocabulary, numberbatch=False):
    embeddings = {}
    
    with open(embeddings_path) as f:
        for line in f:
            parts = line.strip().split(' ')
            if len(parts) < 5:
                continue
            # word_parts = parts[0].split('/')
            # word = '{}_{}'.format(word_parts[2], word_parts[3].replace('_', ' '))
            word = parts[0]
            word = word.lower()
            word = word.replace('_', ' ')
            if word not in vocabulary:
                continue
            embedding = [float(p) for p in parts[1:]]
            embeddings[word] = np.array(embedding)

    return embeddings


def weighted_overlap(v1, v2):
    overlap = [w for w in v1 if w in v2]
    if not overlap:
        return 0.0

    n = sum(1/(v1[w]['rank'] + v2[w]['rank']) for w in overlap)
    d = sum(1/(2*i) for i in range(1, len(overlap) + 1))
    score = (n/d)**0.5
    return score


def score_overlap(v1, v2):
    overlap = [w for w in v1 if w in v2]
    if not overlap:
        return 0.0

    n = sum(v1[w]['score'] + v2[w]['score'] for w in overlap)
    d = 2.0
    score = (n/d)**0.5
    return score


def max_similarity(word_1_synsets, word_2_synsets, vectors, scoring_function, synset_weights={}, fallback_score=0.5):
    max_similarity = -1
    best_match = [0, 0]
    fallback = False

    for synset_1 in word_1_synsets:
        if synset_1 not in vectors:
            continue
        vector_1 = vectors[synset_1]

        for synset_2 in word_2_synsets:
            if synset_2 not in vectors:
                continue
            vector_2 = vectors[synset_2]

            similarity = scoring_function(vector_1, vector_2)
            if max_similarity == -1:
                max_similarity = 0.0
            if similarity > max_similarity:
                max_similarity = similarity
                best_match = [synset_1, synset_2]
    
    if max_similarity == -1:
        max_similarity = fallback_score
        fallback = True

    return max_similarity, best_match, fallback


def weight_similarity(word_1_synsets, word_2_synsets, vectors, scoring_function, synset_weights={}, fallback_score=0.5):
    max_similarity = -1
    best_match = [0, 0]
    fallback = False

    sorted_word_1_synsets = sorted(word_1_synsets, key=lambda s: -synset_weights[s])
    word_1_synset_ranks = {s: i for i, s in enumerate(sorted_word_1_synsets, 1)}

    sorted_word_2_synsets = sorted(word_2_synsets, key=lambda s: -synset_weights[s])
    word_2_synset_ranks = {s: i for i, s in enumerate(sorted_word_2_synsets, 1)}

    for synset_1 in word_1_synsets:
        if synset_1 not in vectors:
            continue
        vector_1 = vectors[synset_1]

        for synset_2 in word_2_synsets:
            if synset_2 not in vectors:
                continue
            vector_2 = vectors[synset_2]

            similarity = scoring_function(vector_1, vector_2)
            similarity = similarity * (2 / (word_1_synset_ranks[synset_1] + word_2_synset_ranks[synset_2]))**0.5

            if similarity > max_similarity:
                max_similarity = similarity
                best_match = [synset_1, synset_2]
    
    if max_similarity == -1:
        max_similarity = fallback_score
        fallback = True

    return max_similarity, best_match, fallback


def ranked_similarity(word_1_synsets, word_2_synsets, vectors, scoring_function, synset_weights={}, fallback_score=0.5):
    max_similarity = -1
    best_match = [0, 0]
    fallback = False
    similarities = []

    for synset_1 in word_1_synsets:
        if synset_1 not in vectors:
            continue
        vector_1 = vectors[synset_1]
        local_max_similarity = -1

        for synset_2 in word_2_synsets:
            if synset_2 not in vectors:
                continue
            vector_2 = vectors[synset_2]

            similarity = scoring_function(vector_1, vector_2)
            similarities.append(similarity)
            if similarity > local_max_similarity:
                local_max_similarity = similarity

            if similarity > max_similarity:
                max_similarity = similarity
                best_match = [synset_1, synset_2]
        
        # if local_max_similarity != -1:
        #     similarities.append(similarity)
    
    if max_similarity == -1:
        max_similarity = fallback_score
        fallback = True
    else:
        similarities.sort(reverse=True)
        n = sum([score/rank for rank, score in enumerate(similarities[:3], 1)])
        d = sum([1./rank for rank in range(1, len(similarities[:3]) + 1)])
        max_similarity = (n/d)**0.5 #**(1-(n/d))

    return max_similarity, best_match, fallback


def position_similarity(word_1_synsets, word_2_synsets, vectors, scoring_function, synset_weights={}, fallback_score=0.5):
    max_similarity = -1
    best_match = [0, 0]
    fallback = False

    word_1_synset_ranks = {s: i for i, s in enumerate(word_1_synsets, 1)}
    word_2_synset_ranks = {s: i for i, s in enumerate(word_2_synsets, 1)}

    for synset_1 in word_1_synsets:
        if synset_1 not in vectors:
            continue
        vector_1 = vectors[synset_1]

        for synset_2 in word_2_synsets:
            if synset_2 not in vectors:
                continue
            vector_2 = vectors[synset_2]

            similarity = scoring_function(vector_1, vector_2)
            similarity = similarity * (2 / (word_1_synset_ranks[synset_1] + word_2_synset_ranks[synset_2]))**0.5

            if similarity > max_similarity:
                max_similarity = similarity
                best_match = [synset_1, synset_2]
    
    if max_similarity == -1:
        max_similarity = fallback_score
        fallback = True

    return max_similarity, best_match, fallback


def evaluate(name, word_pairs, word_synsets, synset_weights, vectors, similarity_function, scoring_function, verbose=False):

    for dataset_class in word_pairs:
        pearson_results = []
        spearman_results = []

        for dataset in word_pairs[dataset_class]:
            print('  Evaluating {}...'.format(dataset))
            scores = []
            gold_scores = []
            pairs = []
            fallbacks = 0

            log_path = os.path.join(AuxiliaryData.LOGS.value, '{}_{}.log'.format(name, dataset))
            with open(log_path, 'w') as f_log:
                f_log.write('Evaluating {}...\n\n'.format(dataset))

                for pair in word_pairs[dataset_class][dataset]:
                    word_1 = pair['word_1']
                    word_2 = pair['word_2']
                    pairs.append('{}-{}'.format(word_1, word_2))

                    if word_1 not in word_synsets or word_2 not in word_synsets:
                        scores.append(0.5)
                        gold_scores.append(pair['score'])
                        fallbacks += 1
                        continue
                    
                    word_1_synsets = word_synsets[word_1]
                    word_2_synsets = word_synsets[word_2]
                    
                    score, best_match, fallback = similarity_function(
                        word_1_synsets, word_2_synsets, vectors, scoring_function, synset_weights=synset_weights)

                    if fallback:
                        fallbacks += 1
                    
                    if best_match:
                        if verbose:
                            print('{:16}\t{:16}\tP={:0.4f}\tG={:0.4f}\t{:8}\t{:8}\t{}'.format(word_1, word_2, score, pair['score'], str(best_match[0]), str(best_match[1]), not fallback))
                        f_log.write('{:16}\t{:16}\tP={:0.4f}\tG={:0.4f}\t{:8}\t{:8}\t{}\n'.format(word_1, word_2, score, pair['score'], str(best_match[0]), str(best_match[1]), not fallback))
                    else:
                        if verbose:
                            print('{:16}\t{:16}\tP={:0.4f}\tG={:0.4f}\t{}'.format(word_1, word_2, score, pair['score'], not fallback))
                        f_log.write('{:16}\t{:16}\tP={:0.4f}\tG={:0.4f}\t{}\n'.format(word_1, word_2, score, pair['score'], not fallback))
                    
                    scores.append(score)
                    gold_scores.append(pair['score'])

                pearson, _ = pearsonr(gold_scores, scores)
                spearman, _ = spearmanr(gold_scores, scores)
                print('    {} - Pearson: {:0.4f} - Spearman: {:0.4f}'.format(dataset, pearson, spearman))
                print('    # Fallbacks: {}/{}'.format(fallbacks, len(word_pairs[dataset_class][dataset])))
                print()

                f_log.write('\n\n{} - Pearson: {:0.4f} - Spearman: {:0.4f}\n'.format(dataset, pearson, spearman))
                f_log.write('# Fallbacks: {}/{}\n'.format(fallbacks, len(word_pairs[dataset_class][dataset])))

                pearson_results.append(pearson)
                spearman_results.append(spearman)

                fig = plt.figure()
                plt.title('{}_{}'.format(name, dataset))
                plt.xlabel('Gold')
                plt.ylabel('Predicted')
                plt.scatter(gold_scores, scores, alpha=0.5)
                # for i, pair in enumerate(pairs):
                #     if abs(gold_scores[i] - scores[i]) > 0.25:
                #         plt.annotate(pair, (gold_scores[i], scores[i]))

                fig.savefig(os.path.join(AuxiliaryData.FIGURES.value, '{}_{}.png'.format(name, dataset)), dpi=fig.dpi)
                plt.close(fig)

        
        avg_pearson = sum(pearson_results)/len(pearson_results)
        avg_spearman = sum(spearman_results)/len(spearman_results)
        print('\n\n  AVG - Pearson: {:0.4f} - Spearman: {:0.4f}\n\n\n'.format(avg_pearson, avg_spearman))

    
def evaluate_embeddings(name, word_pairs, embeddings_1, embeddings_2, verbose=False):

    for dataset_class in word_pairs:
        pearson_results = []
        spearman_results = []

        for dataset in word_pairs[dataset_class]:
            print('  Evaluating {}...'.format(dataset))
            scores = []
            gold_scores = []
            pairs = []
            fallbacks = 0

            log_path = os.path.join(AuxiliaryData.LOGS.value, '{}_{}.log'.format(name, dataset))
            with open(log_path, 'w') as f_log:
                f_log.write('Evaluating {}...\n\n'.format(dataset))

                for pair in word_pairs[dataset_class][dataset]:
                    word_1 = pair['word_1']
                    word_2 = pair['word_2']
                    pairs.append('{}-{}'.format(word_1, word_2))

                    if word_1 not in embeddings_1 or word_2 not in embeddings_2:
                        fallbacks += 1
                        scores.append(0.5)
                        gold_scores.append(pair['score'])
                        continue

                    embedding_1 = embeddings_1[word_1]
                    embedding_2 = embeddings_2[word_2]

                    score = embedding_1.dot(embedding_2) / (np.linalg.norm(embedding_1) * np.linalg.norm(embedding_2))
                    
                    if verbose:
                        print('{:16}\t{:16}\tP={:0.4f}\tG={:0.4f}\t{}'.format(word_1, word_2, score, pair['score'], True))
                    f_log.write('{:16}\t{:16}\tP={:0.4f}\tG={:0.4f}\t{}\n'.format(word_1, word_2, score, pair['score'], True))
                    
                    scores.append(score)
                    gold_scores.append(pair['score'])

                pearson, _ = pearsonr(gold_scores, scores) if len(gold_scores) > 0 else [0.0, 0.0]
                spearman, _ = spearmanr(gold_scores, scores) if len(gold_scores) > 0 else [0.0, 0.0]
                print('    {} - Pearson: {:0.4f} - Spearman: {:0.4f}'.format(dataset, pearson, spearman))
                print('    # Fallbacks: {}/{}'.format(fallbacks, len(word_pairs[dataset_class][dataset])))
                print()

                f_log.write('\n\n{} - Pearson: {:0.4f} - Spearman: {:0.4f}\n'.format(dataset, pearson, spearman))
                f_log.write('# Fallbacks: {}/{}\n'.format(fallbacks, len(word_pairs[dataset_class][dataset])))

                pearson_results.append(pearson)
                spearman_results.append(spearman)

                fig = plt.figure()
                plt.title('{}_{}'.format(name, dataset))
                plt.xlabel('Gold')
                plt.ylabel('Predicted')
                plt.scatter(gold_scores, scores, alpha=0.5)
                # for i, pair in enumerate(pairs):
                #     if abs(gold_scores[i] - scores[i]) > 0.25:
                #         plt.annotate(pair, (gold_scores[i], scores[i]))

                fig.savefig(os.path.join(AuxiliaryData.FIGURES.value, '{}_{}.png'.format(name, dataset)), dpi=fig.dpi)
                plt.close(fig)

        
        avg_pearson = sum(pearson_results)/len(pearson_results)
        avg_spearman = sum(spearman_results)/len(spearman_results)
        print('\n\n  AVG - Pearson: {:0.4f} - Spearman: {:0.4f}\n\n\n'.format(avg_pearson, avg_spearman))