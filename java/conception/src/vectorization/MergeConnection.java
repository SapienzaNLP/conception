package vectorization;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

public class MergeConnection {

    public static void main(String[] args) throws IOException {
        System.out.println("Conception expansion");
        if (args.length != 3) {
            System.out.println("This program requires 3 arguments:");
            System.out.println("  - path/to/connection/vectors/split/");
            System.out.println("  - path/to/output/vectors.tsv");
            System.out.println("  - buffer size");
            return;
        }

        String splitDir = args[0];
        String outputPath = args[1];
        int bufferSize = Integer.parseInt(args[2]);

        merge(splitDir, outputPath, bufferSize);
    }

    private static void merge(String splitDir, String outputPath, int bufferSize) throws IOException {

        List<Path> paths = Files.walk(Paths.get(splitDir))
                .filter(Files::isRegularFile)
                .collect(Collectors.toList());

        List<BufferedReader> readers = new ArrayList<>();
        for (Path path : paths) {
            readers.add(Files.newBufferedReader(path, Charset.forName("UTF-8")));
        }

        List<Integer> synsets = new ArrayList<>();
        List<Map<Integer, Double>> vectors = new ArrayList<>();
        List<Boolean> valid = new ArrayList<>();
        for (BufferedReader reader : readers) {
            String[] parts = reader.readLine().trim().split("\t");
            
            int synset = Integer.parseInt(parts[0]);
            synsets.add(synset);

            Map<Integer, Double> vector = Arrays.stream(parts).skip(2)
                    .map(p -> p.split("::"))
                    .collect(Collectors.toMap(
                        (String[] p) -> Integer.parseInt(p[0]),
                        (String[] p) -> Double.parseDouble(p[1])));
            vectors.add(vector);

            valid.add(true);
        }

        Comparator<Map.Entry<Integer, Double>> comparator = (o1, o2) -> 
                ((Comparable<Double>) ((Map.Entry<Integer, Double>) (o2)).getValue())
                .compareTo(((Map.Entry<Integer, Double>) (o1)).getValue());

        List<String> outputLines = new ArrayList<>();

        for (int offset = 1; offset < 22_000_000; offset++) {
            List<Map<Integer, Double>> toMerge = new ArrayList<>();

            for (int i = 0; i < synsets.size(); i++) {
                int synset = synsets.get(i);

                if (synset == offset) {
                    if (!valid.get(i)) {
                        continue;
                    }

                    toMerge.add(vectors.get(i));

                    BufferedReader reader = readers.get(i);
                    String line = reader.readLine();
                    if (line == null) {
                        valid.set(i, false);
                        continue;
                    }

                    String[] parts = line.trim().split("\t");
                    
                    int nextSynset = Integer.parseInt(parts[0]);
                    synsets.set(i, nextSynset);

                    Map<Integer, Double> vector = Arrays.stream(parts).skip(2)
                            .map(p -> p.split("::"))
                            .collect(Collectors.toMap(
                                (String[] p) -> Integer.parseInt(p[0]),
                                (String[] p) -> Double.parseDouble(p[1])));
                    vectors.set(i, vector);
                }
            }

            if (!toMerge.isEmpty()) {
                Map<Integer, Double> merged = new HashMap<>();
                for (Map<Integer, Double> vector : toMerge) {
                    for (Map.Entry<Integer, Double> entry : vector.entrySet()) {
                        int synset = entry.getKey();
                        double score = entry.getValue();
                        double currentScore = merged.getOrDefault(synset, 0d);
                        merged.put(synset, currentScore + score);
                    }
                }

                List<Map.Entry<Integer, Double>> sortedVector = new ArrayList<>(merged.entrySet());
                Collections.sort(sortedVector, comparator);

                StringBuffer outputBuffer = new StringBuffer().append(String.format("%08d", offset)).append("\t").append(sortedVector.size());
                for (Map.Entry<Integer, Double> synsetScore : sortedVector) {
                    String strSynset = String.format("%08d", synsetScore.getKey());
                    double score = synsetScore.getValue();
                    String strScore;
                    if (score > 1e-5) {
                        strScore = String.format(Locale.US, "%.5f", synsetScore.getValue());
                    } else {
                        strScore = "0.00001";
                    }
                    outputBuffer.append("\t").append(strSynset).append("::").append(strScore);
                }

                outputLines.add(outputBuffer.toString());
            }

            if (offset % bufferSize == 0) {
                Files.write(Paths.get(outputPath), outputLines, Charset.forName("UTF-8"), StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND);

                System.out.println("  Vectors from " + (offset - bufferSize) + " to " + offset + " written to file!\n");
                outputLines.clear();
            }
        }

    }

}