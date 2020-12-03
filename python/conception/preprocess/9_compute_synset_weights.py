from preprocess_paths import Conception, Synsets

def compute_weights(log_interval=100_000):
    weights = {}
    with open(Conception.Expansion.value) as f_in:
        for line_number, line in enumerate(f_in, 1):
            if line_number % log_interval == 0:
                print('  Reading line {}...'.format(line_number))
            
            _, _, *components = line.strip().split('\t')
            synset_scores = []
            score_sum = 0.0

            for c in components:
                synset, score = c.split('::')
                score = float(score)
                synset_scores.append((synset, score))
                score_sum += score
            
            for synset, score in synset_scores:
                if synset not in weights:
                    weights[synset] = score / score_sum
                else:
                    weights[synset] += score / score_sum
    
    weights = sorted(weights.items(), key=lambda kv: kv[1])
    with open(Synsets.WEIGHTS.value, 'w') as f_out:
        for synset, weight in weights:
            if weight < 1e-8:
                weight = 1e-8
            f_out.write('{}\t{:0.8f}\n'.format(synset, weight))


if __name__ == '__main__':
    print('  Computing synset weights...')
    compute_weights()
    print('  Computing synset weights... Done!\n')
