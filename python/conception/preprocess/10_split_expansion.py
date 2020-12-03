import os

from preprocess_paths import Conception, ConceptionSplit


def split(input_path, output_dir, buffer_size=100_000):
    limit = buffer_size
    buffer = []
    if not os.path.exists(output_dir):
        os.makedirs(output_dir)

    with open(input_path.value) as f_in:
        for line in f_in:
            synset = int(line[0:8])
            if synset > limit:
                output_path = os.path.join(
                    output_dir, 'expansion_{}_{}.tsv'.format(limit - buffer_size, limit))
                
                print('  Writing vectors to {}'.format(output_path))
                
                with open(output_path, 'w') as f_out:
                    for e in buffer:
                        f_out.write(e)
                
                limit += buffer_size
                buffer = []
                
            buffer.append(line)


if __name__ == "__main__":
    print("  Splitting {}...".format(Conception.Expansion.name))
    split(Conception.Expansion, ConceptionSplit.Expansion.value)
    print("  Done!\n")