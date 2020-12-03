from preprocess_paths import Synsets

def clean_synsets():
    
    with open (Synsets.ALL_NEIGHBORS.value) as f_in, open(Synsets.CLEAN_NEIGHBORS.value, 'w') as f_out:
        for line in f_in:
            filtered_neighbors = []
            root, _, *neighbors = line.strip().split('\t')

            for neighbor in neighbors:
                synset, relation_type = neighbor.split('::')
                if relation_type != 'related':
                    filtered_neighbors.append(neighbor)
                else:
                    synset = int(synset[3:11])
                    if synset < 117_660:
                        filtered_neighbors.append(neighbor)
                    else:
                        break
            
            f_out.write('{}\t{}\t{}\n'.format(root, len(filtered_neighbors), '\t'.join(filtered_neighbors)))

if __name__ == '__main__':
    print('  Cleaning synset neighbors...')
    clean_synsets()
    print('  Cleaning synset neighbors... Done!\n')
