import os

from preprocess_paths import NasariRemapped

def merge(input_path):
    tmp_path = input_path + ".tmp"

    with open(input_path) as f_in, open(tmp_path, "w") as f_out:
        line_buffer = []
        previous_synset = ""
        for line in f_in:
            current_synset = line[:12]
            if current_synset != previous_synset:
                if len(line_buffer) > 1:
                    components = {}
                    score_sum = 0.0
                    for saved_line in line_buffer:
                        line_parts = saved_line.split("\t")
                        for line_part in line_parts:
                            word, score = line_part.split("_")
                            score = float(score)
                            score_sum += score
                            if word not in components:
                                components[word] = score
                            else:
                                components[word] += score
                    sorted_components = sorted(components.items(), key=lambda kv: -kv[1])
                    sorted_components = ["{}_{:0.4f}".format(c[0], 100.0 * c[1]/score_sum) for c in sorted_components]
                    sorted_components = "\t".join(sorted_components)
                    new_line = "{}\t{}\n".format(previous_synset, sorted_components)
                    f_out.write(new_line)
                elif line_buffer:
                    f_out.write("{}\t{}\n".format(previous_synset, line_buffer[0]))

                line_buffer = [line[13:].strip()]
                previous_synset = current_synset
            else:
                line_buffer.append(line[13:].strip())
    
    with open(tmp_path) as f_in, open(input_path, "w") as f_out:
        for line in f_in:
            f_out.write(line)
    
    os.remove(tmp_path)
    

if __name__ == "__main__":
    for path in NasariRemapped:
        print("  Merging {}...".format(path.name))
        merge(path.value)
    
    print("  Done!\n")