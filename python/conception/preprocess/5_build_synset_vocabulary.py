from preprocess_paths import WordSynsetsCleaned, Synsets

def build_synset_vocabulary():
    synset_vocabulary = set()

    for path in WordSynsetsCleaned:
        print('  Reading word synsets from {}...'.format(path.name))

        with open(path.value) as f:
            for line in f:
                _, _, _, *synsets = line.strip().split('\t')
                synset_vocabulary.update(synsets)

    return synset_vocabulary

if __name__ == '__main__':
    print('Building synset vocabulary...')
    synset_vocabulary = build_synset_vocabulary()
    sorted_synsets = sorted(list(synset_vocabulary))
    with open(Synsets.VOCABULARY.value, 'w') as f:
        for synset in sorted_synsets:
            f.write('{}\n'.format(synset))
    
    print('Building synset vocabulary... Done!')