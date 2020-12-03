from collections import Counter

from preprocess_paths import NasariRemapped, WordVocabulary

def build_vocab(nasari_path):
    '''
        Returns a dictionary of the words used as components
        in the NASARI vectors where d[word] = word_count.
    '''
    words = []
    with open(nasari_path) as f:
        for line in f:
            _, *components = line.strip().split('\t')
            vector_words = [c[:c.find('_')] for c in components]
            words.extend(vector_words)
    
    return Counter(words)

def write_vocab(vocab, output_path):
    '''
        Writes the vocabulary to the given output_path.
    '''
    sorted_word_counts = sorted(vocab.items(), key=lambda kv: kv[1], reverse=True)
    with open(output_path, 'w') as f:
        for word, count in sorted_word_counts:
            f.write('{}\t{}\n'.format(word, count))

if __name__ == '__main__':
    print('Creating word vocabularies...')
    for nasari_path, output_path in zip(NasariRemapped, WordVocabulary):
        nasari_path = nasari_path.value
        output_path = output_path.value
        print('  from {}...'.format(nasari_path))
        vocab = build_vocab(nasari_path)
        write_vocab(vocab, output_path)

    print('Done!\n')