import os

from preprocess_paths import NasariRemapped, _NASARI_SPLIT_DIR


def split(path, output_dir, buffer_size=100_000):
    language = path.name.lower()
    path = path.value
    output_dir = os.path.join(output_dir, language)
    if not os.path.exists(output_dir):
        os.makedirs(output_dir)

    limit = buffer_size
    buffer = []
    with open(path) as f_in:
        for line in f_in:
            synset = int(line[3:11])
            if synset > limit:
                output_path = os.path.join(
                    output_dir, 'nasari_{}_{}.tsv'.format(limit - buffer_size, limit))
                
                print('  Writing vectors to {}'.format(output_path))
                
                with open(output_path, 'w') as f_out:
                    for e in buffer:
                        f_out.write(e)
                
                limit += buffer_size
                buffer = []
                
            buffer.append(line)


if __name__ == "__main__":
    for path in NasariRemapped:
        print("  Splitting {}...".format(path.name))
        split(path, _NASARI_SPLIT_DIR)
    
    print("  Done!\n")
