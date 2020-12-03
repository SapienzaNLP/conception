from preprocess_paths import WordSynsets, WordSynsetsCleaned

def clean(input_path, output_path, occurrence_threshold=1):
    '''
        Reads a word -> synsets file from input_path and
        writes a cleaned word -> synsets file to output_path
        where each entry word has at least a synset and
        the word appears at least occurrence_threshold times
        in the NASARI vectors.
    '''
    with open(input_path) as f_in, open(output_path, 'w') as f_out:
        
        for line in f_in:
            occurrences, word, synset_count, *_ = line.strip().split('\t')
            
            occurrences = int(occurrences)
            if occurrences < occurrence_threshold:
                break

            synset_count = int(synset_count)
            if synset_count > 0:
                f_out.write(line)


if __name__ == '__main__':
    print('Cleaning word -> synsets files')
    for input_path, output_path in zip(WordSynsets, WordSynsetsCleaned):
        input_path = input_path.value
        output_path = output_path.value
        print('  from {}...'.format(input_path))
        clean(input_path, output_path)
    print('Done!\n')