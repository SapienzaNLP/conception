package vectorization;

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
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class SynsetExpansion {

    public static void main(String[] args) throws IOException {
        System.out.println("Conception expansion");
        if (args.length != 4) {
            System.out.println("This program requires 4 arguments:");
            System.out.println("  - path/to/selection/vectors/split/");
            System.out.println("  - path/to/synset/neighbors.tsv");
            System.out.println("  - path/to/output/vectors.tsv");
            System.out.println("  - buffer size");
            return;
        }

        String selectionDir = args[0];
        String synsetNeighborsPath = args[1];
        String outputPath = args[2];
        int bufferSize = Integer.parseInt(args[3]);

        expand(selectionDir, synsetNeighborsPath, outputPath, bufferSize);
    }

    private static void expand(String selectionDir, String synsetNeighborsPath, String outputPath, int bufferSize) throws IOException {
        
        System.out.println("  Reading synset -> neighbors dictionaries...");

        Map<Integer, Set<Integer>> synsetNeighbors = Files.lines(Paths.get(synsetNeighborsPath))
                    .map(line -> line.split("\t")).collect(Collectors.toMap(
                        (String[] parts) -> Integer.parseInt(parts[0].substring(3, 11)),
                        (String[] parts) -> Arrays.stream(parts).skip(2).map(p -> Integer.parseInt(p.substring(3, 11)))
                                                    .collect(Collectors.toSet())));
        

        System.out.println("  " + synsetNeighbors.size() + " entries.");

        for (int start = 0; start < 22_000_000; start += bufferSize) {
            final int limit = start + bufferSize;
            String fileName = "selection_" + start + "_" + limit + ".tsv";
            Path filePath = Paths.get(selectionDir, fileName);

            Map<Integer, Map<Integer, Double>> selectionVectors = Files.lines(filePath)
                    .parallel()
                    .map(line -> line.split("\t")).collect(
                            Collectors.toMap(
                                (String[] parts) -> Integer.parseInt(parts[0].substring(0, 8)),
                                (String[] parts) -> parseValues(parts),
                                (v1, v2) -> {
                                    v2.forEach( (k, v) -> v1.put(k, v + v1.getOrDefault(k, 0d)));
                                    return v1;
                                }));

            System.out.println("  Loaded " + selectionVectors.size() + " vectors from " + start + " to " + limit + ".");

            System.out.print("  Expanding vectors...");

            Map<Integer, Map<Integer, Double>> expandedVectors = IntStream.range(start, limit).boxed()
                    .parallel()
                    .filter(i -> selectionVectors.containsKey(i) || synsetNeighbors.containsKey(i))
                    .collect(Collectors.toMap(
                        i -> i,
                        i -> expandSynsets(i, selectionVectors, synsetNeighbors)));

            // Map<Integer, Map<Integer, Double>> expandedVectors = selectionVectors.entrySet()
            //         .parallelStream()
            //         .collect(Collectors.toMap(
            //             e -> e.getKey(),
            //             e -> expandSynsets(e.getKey(), selectionVectors.get(e.getKey()), synsetNeighbors)));
            
            System.out.println(" Built " + expandedVectors.size() + " vectors.");

            Comparator<Map.Entry<Integer, Double>> comparator = (o1,
                    o2) -> ((Comparable<Double>) ((Map.Entry<Integer, Double>) (o2)).getValue())
                            .compareTo(((Map.Entry<Integer, Double>) (o1)).getValue());

            List<String> outputLines = new ArrayList<>();
            List<Integer> synsets = new ArrayList<>(expandedVectors.keySet());
            Collections.sort(synsets);
            for (int synset : synsets) {
                Map<Integer, Double> expandedVector = expandedVectors.get(synset);

                List<Map.Entry<Integer, Double>> sortedVector = new ArrayList<>(expandedVector.entrySet());
                Collections.sort(sortedVector, comparator);

                StringBuffer outputBuffer = new StringBuffer().append(String.format("%08d", synset)).append("\t").append(sortedVector.size());
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

            Files.write(Paths.get(outputPath), outputLines, Charset.forName("UTF-8"), StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND);

            System.out.println("  Vectors written to file!\n");
        }
    }

    private static Map<Integer, Double> parseValues(String[] lineParts) {
        return Arrays.stream(lineParts).skip(2)
                .map(part -> part.split("::"))
                .collect(
                    Collectors.toMap(
                        (String[] value) -> Integer.parseInt(value[0]),
                        (String[] value) -> Double.parseDouble(value[1]),
                        (existing, replacement) -> existing + replacement));
    }

    private static Map<Integer, Double> expandSynsets(int rootSynset,
            Map<Integer, Map<Integer, Double>> selectionVectors,
            Map<Integer, Set<Integer>> synsetNeighbors) {

        Map<Integer, Double> selectionVector;
        if (selectionVectors.containsKey(rootSynset)) {
            selectionVector = selectionVectors.getOrDefault(rootSynset, new HashMap<>());
        } else {
            selectionVector = new HashMap<>();
            Set<Integer> neighbors = synsetNeighbors.get(rootSynset);
            for (int neighbor : neighbors) {
                selectionVector.put(neighbor, 1d / neighbors.size());
            }
            return selectionVector;
        }
        Set<Integer> selectedSynsets = selectionVector.keySet();
        Map<Integer, Double> expandedVector = new HashMap<>();
        Map<Integer, Integer> hits = new HashMap<>();
        double maxScore = 0d;
        final int minHits = Math.max(1, (selectionVector.size() / 250) + 1);

        for (Map.Entry<Integer, Double> component : selectionVector.entrySet()) {
            int selectedSynset = component.getKey();
            double score = component.getValue();
            double currentScore = expandedVector.getOrDefault(selectedSynset, 0d);
            expandedVector.put(selectedSynset, currentScore + score);
            int currentHits = hits.getOrDefault(selectedSynset, 0);
            hits.put(selectedSynset, currentHits + 1);
            maxScore = (score > maxScore) ? score : maxScore;

            if (synsetNeighbors.containsKey(selectedSynset)) {
                Set<Integer> neighbors = synsetNeighbors.get(selectedSynset);
                double neighborScore = 0.1 * score / neighbors.size();
                for (int neighbor : neighbors) {
                    double currentNeighborScore = expandedVector.getOrDefault(neighbor, 0d);
                    expandedVector.put(neighbor, currentNeighborScore + neighborScore);
                    int currentNeighborHits = hits.getOrDefault(neighbor, 0);
                    hits.put(neighbor, currentNeighborHits + 1);
                }
            }
        }

        if (synsetNeighbors.containsKey(rootSynset)) {
            Set<Integer> neighbors = synsetNeighbors.get(rootSynset);
            double neighborScore = 0.1 * maxScore / neighbors.size();
            for (int neighbor : neighbors) {
                double currentNeighborScore = expandedVector.getOrDefault(neighbor, 0d);
                expandedVector.put(neighbor, currentNeighborScore + neighborScore);
                int currentNeighborHits = hits.getOrDefault(neighbor, 0);
                hits.put(neighbor, currentNeighborHits + minHits + 1);
            }
        }

        Set<Integer> synsetsToInclude = new HashSet<>();
        for (int selectedSynset : selectedSynsets) {
            int currentHits = hits.get(selectedSynset);
            if (currentHits > minHits) {
                synsetsToInclude.add((selectedSynset));
                continue;
            }

            if (synsetNeighbors.containsKey(selectedSynset)) {
                Set<Integer> neighbors = synsetNeighbors.get(selectedSynset);
                for (int neighbor : neighbors) {
                    int currentNeighborHits = hits.get(neighbor);
                    if (currentNeighborHits > minHits) {
                        synsetsToInclude.add((selectedSynset));
                        break;
                    }
                }
            }
        }

        expandedVector = expandedVector.entrySet().stream()
                // .filter(e -> selectedSynsets.contains(e.getKey()) || hits.get(e.getKey()) > 1)
                .filter(e -> synsetsToInclude.contains(e.getKey()) || hits.get(e.getKey()) > minHits)
                .collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue()));

        return expandedVector;
    }

    // private static Map<Integer, Double> expandSynsets(int rootSynset,
    //         Map<Integer, Double> selectionVector,
    //         Map<Integer, Set<Integer>> synsetNeighbors) {
        
    //     Set<Integer> selectedSynsets = selectionVector.keySet();
    //     Map<Integer, Double> expandedVector = new HashMap<>();
    //     Map<Integer, Integer> hits = new HashMap<>();
    //     double maxScore = 0d;

    //     for (Map.Entry<Integer, Double> component : selectionVector.entrySet()) {
    //         int selectedSynset = component.getKey();
    //         double score = component.getValue();
    //         double currentScore = expandedVector.getOrDefault(selectedSynset, 0d);
    //         expandedVector.put(selectedSynset, currentScore + score);
    //         int currentHits = hits.getOrDefault(selectedSynset, 0);
    //         hits.put(selectedSynset, currentHits + 1);
    //         maxScore = (score > maxScore) ? score : maxScore;

    //         if (synsetNeighbors.containsKey(selectedSynset)) {
    //             Set<Integer> neighbors = synsetNeighbors.get(selectedSynset);
    //             double neighborScore = 0.1 * score / neighbors.size();
    //             for (int neighbor : neighbors) {
    //                 double currentNeighborScore = expandedVector.getOrDefault(neighbor, 0d);
    //                 expandedVector.put(neighbor, currentNeighborScore + neighborScore);
    //                 int currentNeighborHits = hits.getOrDefault(neighbor, 0);
    //                 hits.put(neighbor, currentNeighborHits + 1);
    //             }
    //         }
    //     }

    //     if (synsetNeighbors.containsKey(rootSynset)) {
    //         Set<Integer> neighbors = synsetNeighbors.get(rootSynset);
    //         double neighborScore = 0.1 * maxScore / neighbors.size();
    //         for (int neighbor : neighbors) {
    //             double currentNeighborScore = expandedVector.getOrDefault(neighbor, 0d);
    //             expandedVector.put(neighbor, currentNeighborScore + neighborScore);
    //             int currentNeighborHits = hits.getOrDefault(neighbor, 0);
    //             hits.put(neighbor, currentNeighborHits + 2);
    //         }
    //     }

    //     Set<Integer> synsetsToInclude = new HashSet<>();
    //     for (int selectedSynset : selectedSynsets) {
    //         int currentHits = hits.get(selectedSynset);
    //         if (currentHits > 1) {
    //             synsetsToInclude.add((selectedSynset));
    //             continue;
    //         }

    //         if (synsetNeighbors.containsKey(selectedSynset)) {
    //             Set<Integer> neighbors = synsetNeighbors.get(selectedSynset);
    //             for (int neighbor : neighbors) {
    //                 int currentNeighborHits = hits.get(neighbor);
    //                 if (currentNeighborHits > 1) {
    //                     synsetsToInclude.add((selectedSynset));
    //                     break;
    //                 }
    //             }
    //         }
    //     }

    //     expandedVector = expandedVector.entrySet().stream()
    //             // .filter(e -> selectedSynsets.contains(e.getKey()) || hits.get(e.getKey()) > 1)
    //             .filter(e -> synsetsToInclude.contains(e.getKey()) || hits.get(e.getKey()) > 1)
    //             .collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue()));

    //     return expandedVector;
    // }
}