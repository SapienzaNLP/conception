import subprocess

from preprocess_paths import Mapping, NasariOriginal, NasariRemapped, NasariUnified


def load_bn3_to_bn4_mapping():
    '''
        Returns a BabelNet3->BabelNet4 mapping.
        In general:
        - bn:x -> bn:y = OK
        - bn:x -> null = Synset bn:x does not exist in BabelNet 4
        - bn:x not in the mapping = Synset bn:x is the same in BabelNet 4
    '''
    bn3_to_bn4 = {}
    with open(Mapping.bn3_to_bn4.value) as f:
        for line in f:
            bn3, bn4 = line.strip().split('\t')
            bn3_to_bn4[bn3] = bn4

    return bn3_to_bn4


def remap_single_nasari(in_path, out_path, bn3_to_bn4m):
    '''
        Reads the original NASARI vectors from in_path and
        writes the remapped NASARI vectors to out_path.
        Each NASARI vector is remapped from BabelNet 3 to BabelNet 4.
        Returns the number of (remapped, null) instances.
    '''
    remapped_instances = 0
    null_instances = 0
    with open(in_path) as f_in, open(out_path, 'w') as f_out:
        for line in f_in:
            # Skip the Wikipedia page name.
            bn3, _, *components = line.strip().split('\t')
            assert len(bn3) == len('bn:12345678n')

            value_sum = 0.0
            word_value_pairs = []
            for c in components:
                word, value = c.split("_")
                value = float(value)
                word_value_pairs.append((word, value))
                value_sum += value
            word_value_pairs = ["{}_{:0.4f}".format(w, 100.0 * v/value_sum) for w, v in word_value_pairs]
            components = '\t'.join(word_value_pairs)

            if bn3 in bn3_to_bn4:
                bn4 = bn3_to_bn4[bn3]
                if bn4 == 'null':
                    null_instances += 1
                    continue
                remapped_instances += 1
            else:
                bn4 = bn3

            new_line = '{}\t{}\n'.format(bn4, components)
            f_out.write(new_line)
    return remapped_instances, null_instances


if __name__ == '__main__':
    bn3_to_bn4 = load_bn3_to_bn4_mapping()
    # Remap Nasari Lexical
    for original_path, remapped_path in zip(NasariOriginal, NasariRemapped):
        print('  Remapping {}...'.format(original_path.name))
        remapped, discarded = remap_single_nasari(
            original_path.value, remapped_path.value, bn3_to_bn4)
        print('    Remapped: {}\n    Discarded: {}'.format(remapped, discarded))
        
        sort_command = "sort -o {} {}".format(remapped_path.value, remapped_path.value)
        print('    Running {} ...'.format(sort_command))
        subprocess.run(sort_command, shell=True)
        print('    Done!\n')

    # Remap Nasari Unified
    print('  Remapping {}...'.format(NasariUnified.Original.value))
    remapped, discarded = remap_single_nasari(
        NasariUnified.Original.value, NasariUnified.Remapped.value, bn3_to_bn4)
    print('    Remapped: {}\n    Discarded: {}'.format(remapped, discarded))
    
    sort_command = "sort -o {} {}".format(NasariUnified.Original.value, NasariUnified.Remapped.value)
    print('    Running {} ...'.format(sort_command))
    subprocess.run(sort_command, shell=True)
    print('    Done!\n')

    print('  Done!\n')
