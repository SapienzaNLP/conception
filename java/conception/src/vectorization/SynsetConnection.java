package vectorization;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

public class SynsetConnection {

    public static void main(String[] args) throws IOException {
        System.out.println("Conception connection");
        if (args.length != 5) {
            System.out.println("This program requires 4 arguments:");
            System.out.println("  - path/to/expansion/vectors/split/");
            System.out.println("  - path/to/synset/weights.tsv");
            System.out.println("  - path/to/synset/types.tsv");
            System.out.println("  - path/to/connection/vectors/split/");
            System.out.println("  - buffer size");
            return;
        }

        String expansionDir = args[0];
        String synsetWeightsPath = args[1];
        String synsetTypesPath = args[2];
        String outputDir = args[3];
        int bufferSize = Integer.parseInt(args[4]);

        connect(expansionDir, synsetWeightsPath, synsetTypesPath, outputDir, bufferSize);
    }

    private static void connect(String expansionDir, String synsetWeightsPath, String synsetTypesPath, String outputDir, int bufferSize) throws IOException {
        
        System.out.println("  Reading synset -> weight dictionary...");

        Map<Integer, Double> synsetWeights = Files.lines(Paths.get(synsetWeightsPath))
                    .map(line -> line.split("\t")).collect(Collectors.toMap(
                        (String[] parts) -> Integer.parseInt(parts[0]),
                        (String[] parts) -> Math.pow(Double.parseDouble(parts[1]), 0.5)));
        
        double minWeight = 0d;
        for (double w : synsetWeights.values()) {
            if (w < minWeight || minWeight == 0d) {
                minWeight = w;
            }
        }

        System.out.println("  " + synsetWeights.size() + " entries.\n");

        System.out.println("  Reading synset -> type dictionary...");

        Map<Integer, Boolean> synsetTypes = Files.lines(Paths.get(synsetTypesPath))
                    .map(line -> line.split("\t")).collect(Collectors.toMap(
                        (String[] parts) -> Integer.parseInt(parts[0].substring(3, 11)),
                        (String[] parts) -> parts[1].equals("C")));

        System.out.println("  " + synsetTypes.size() + " entries.\n");

        for (int start = 0; start < 22_000_000; start += bufferSize) {
            final int limit = start + bufferSize;
            String fileName = "expansion_" + start + "_" + limit + ".tsv";
            Path filePath = Paths.get(expansionDir, fileName);

            Map<Integer, Map<Integer, Double>> expansionVectors = Files.lines(filePath)
                    .parallel()
                    .map(line -> line.split("\t")).collect(
                            Collectors.toMap(
                                (String[] parts) -> Integer.parseInt(parts[0].substring(0, 8)),
                                (String[] parts) -> parseValues(parts),
                                (v1, v2) -> {
                                    v2.forEach( (k, v) -> v1.put(k, v + v1.getOrDefault(k, 0d)));
                                    return v1;
                                }));

            System.out.println("  Loaded " + expansionVectors.size() + " vectors from " + start + " to " + limit + ".");

            System.out.print("  Connecting vectors...");

            Map<Integer, Map<Integer, Double>> connectionVectors = connectExpansionVectors(expansionVectors, synsetWeights, minWeight, synsetTypes);
            
            System.out.println(" Built " + connectionVectors.size() + " vectors.");

            Comparator<Map.Entry<Integer, Double>> comparator = (o1,
                    o2) -> ((Comparable<Double>) ((Map.Entry<Integer, Double>) (o2)).getValue())
                            .compareTo(((Map.Entry<Integer, Double>) (o1)).getValue());

            List<Integer> synsets = new ArrayList<>(connectionVectors.keySet());
            Collections.sort(synsets);

            List<String> outputLines = synsets.parallelStream()
                        .map(synset -> {
                            List<Map.Entry<Integer, Double>> sortedVector = new ArrayList<>(connectionVectors.get(synset).entrySet());
                            Collections.sort(sortedVector, comparator);

                            StringBuffer outputBuffer = new StringBuffer().append(String.format("%08d", synset)).append("\t").append(sortedVector.size());
                            for (Map.Entry<Integer, Double> synsetScore : sortedVector) {
                                String strSynset = String.format("%08d", synsetScore.getKey());
                                double score = 100*synsetScore.getValue();
                                String strScore;
                                if (score > 1e-5) {
                                    strScore = String.format(Locale.US, "%.5f", score);
                                } else {
                                    strScore = "0.00001";
                                }
                                outputBuffer.append("\t").append(strSynset).append("::").append(strScore);
                            }

                            return outputBuffer.toString();
                        })
                        .collect(Collectors.toList());

            // List<String> outputLines = new ArrayList<>();
            // for (int synset : synsets) {
            //     Map<Integer, Double> connectedVector = connectionVectors.get(synset);

            //     List<Map.Entry<Integer, Double>> sortedVector = new ArrayList<>(connectedVector.entrySet());
            //     Collections.sort(sortedVector, comparator);

            //     StringBuffer outputBuffer = new StringBuffer().append(String.format("%08d", synset)).append("\t").append(sortedVector.size());
            //     for (Map.Entry<Integer, Double> synsetScore : sortedVector) {
            //         String strSynset = String.format("%08d", synsetScore.getKey());
            //         double score = 100*synsetScore.getValue();
            //         String strScore;
            //         if (score > 1e-5) {
            //             strScore = String.format(Locale.US, "%.5f", score);
            //         } else {
            //             strScore = "0.00001";
            //         }
            //         outputBuffer.append("\t").append(strSynset).append("::").append(strScore);
            //     }
            //     outputLines.add(outputBuffer.toString());
            // }

            String outputName = "connection_" + start + "_" + limit + ".tsv";
            Path outputPath = Paths.get(outputDir, outputName);
            Files.write(outputPath, outputLines, Charset.forName("UTF-8"), StandardOpenOption.CREATE);

            System.out.println("  Vectors written to file!\n");
        }
    }

    private static Map<Integer, Double> parseValues(String[] lineParts) {
        int limit = (int)(lineParts.length * 0.90);
        if (limit > 500) {
            limit = 500;
        } else if (limit < 25) {
            limit = 25;
        }

        Map<Integer, Double> values = new HashMap<>();
        for (int i = 2; i < limit && i < lineParts.length; i++) {
            int synset = Integer.parseInt(lineParts[i].substring(0, 8));
            double score = Math.pow(1d / (i - 1), 0.8);
            values.put(synset, score);
        }

        return values;

        // Map<Integer, Double> values = Arrays.stream(lineParts).skip(2).limit(limit)
        //         .map(part -> part.split("::"))
        //         .collect(
        //             Collectors.toMap(
        //                 (String[] value) -> Integer.parseInt(value[0]),
        //                 (String[] value) -> Double.parseDouble(value[1]),
        //                 (existing, replacement) -> existing + replacement));
        
        // double sum = 0d;
        // for (double value : values.values()) {
        //     sum += value;
        // }

        // final double fSum = sum;

        // return values.entrySet().stream().collect(
        //         Collectors.toMap(
        //             (Map.Entry<Integer, Double> e) -> e.getKey(),
        //             (Map.Entry<Integer, Double> e) -> e.getValue() / fSum));
    }

    private static Map<Integer, Map<Integer, Double>> connectExpansionVectors(Map<Integer, Map<Integer, Double>> vectors, Map<Integer, Double> weights, double minWeight, Map<Integer, Boolean> types) {
        Map<Integer, Map<Integer, Double>> outVectors = new HashMap<>();

        for (Map.Entry<Integer, Map<Integer, Double>> vectorEntry : vectors.entrySet()) {
            int rootSynset = vectorEntry.getKey();
            double rootWeight = weights.getOrDefault(rootSynset, minWeight);
            boolean rootType = types.getOrDefault(rootSynset, false);
            Map<Integer, Double> rootVector = vectorEntry.getValue();
            if (!outVectors.containsKey(rootSynset)) {
                outVectors.put(rootSynset, new HashMap<>());
            }
            Map<Integer, Double> outRootVector = outVectors.get(rootSynset);

            for (Map.Entry<Integer, Double> componentEntry : rootVector.entrySet()) {
                int synset = componentEntry.getKey();
                double score = componentEntry.getValue();
                double inScore = outRootVector.getOrDefault(synset, 0d) + score;
                outRootVector.put(synset, inScore);

                boolean synsetType = types.getOrDefault(synset, false);
                if (synsetType && !rootType) {
                    continue;
                }

                double weight = weights.getOrDefault(synset, minWeight);
                double outScore = (rootWeight / weight) * score;
                if (outScore >= 5e-3) {
                    if (!outVectors.containsKey(synset)) {
                        outVectors.put(synset, new HashMap<>());
                    }
                    Map<Integer, Double> synsetVector = outVectors.get(synset);
                    outScore += synsetVector.getOrDefault(rootSynset, 0d);
                    synsetVector.put(rootSynset, outScore);
                }
            }
        }

        return outVectors;
    }
}