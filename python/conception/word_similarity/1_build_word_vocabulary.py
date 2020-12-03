from paths import Dataset, AuxiliaryData

def read_word_pairs(input_path, first_language, second_language):
    words = set()
    with open(input_path) as f_in:
        for line in f_in:
            first_word, second_word, _ = line.strip().split('\t')
            first_word = '{}_{}'.format(first_language.name, first_word.lower())
            second_word = '{}_{}'.format(second_language.name, second_word.lower())
            words.add(first_word)
            words.add(second_word)
    return words

def build_word_vocabulary():
    words = set()

    for dataset_class in Dataset:
        dataset_class = dataset_class.value

        for dataset in dataset_class:
            dataset = dataset.value
            print('    Reading {}...'.format(dataset.name))

            dataset_words = read_word_pairs(dataset.path, dataset.first_language, dataset.second_language)
            words.update(dataset_words)
    
    sorted_words = sorted(list(words))

    with open(AuxiliaryData.WORDS.value, 'w') as f_out:
        for word in sorted_words:
            f_out.write('{}\n'.format(word))


if __name__ == '__main__':
    print('  Building word vocabulary...')
    
    build_word_vocabulary()
    
    print('  Done!\n')
        