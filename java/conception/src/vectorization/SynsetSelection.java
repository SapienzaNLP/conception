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
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class SynsetSelection {

    public static void main(String[] args) throws IOException {
        System.out.println("Conception selection");
        if (args.length != 4) {
            System.out.println("This program requires 4 arguments:");
            System.out.println("  - path/to/NASARI/dir/");
            System.out.println("  - path/to/word/synsets/dir/");
            System.out.println("  - path/to/output/vectors.tsv");
            System.out.println("  - buffer size");
            return;
        }

        String nasariDir = args[0];
        String wordSynsetsDir = args[1];
        String outputPath = args[2];
        int bufferSize = Integer.parseInt(args[3]);

        select(nasariDir, wordSynsetsDir, outputPath, bufferSize);
    }

    private static void select(String nasariDir, String wordSynsetsDir, String outputPath, int bufferSize) throws IOException {
        
        System.out.println("  Reading word -> synsets dictionaries...");
        List<Path> wordSynsetsPaths = Files.walk(Paths.get(wordSynsetsDir), 1)
                .filter(f -> f.getFileName().toString().endsWith(".tsv"))
                .collect(Collectors.toList());
        
        Map<String, Map<String, Set<Integer>>> wordSynsets = new HashMap<>();
        for (Path wordSynsetsPath : wordSynsetsPaths) {
            String languageCode = wordSynsetsPath.getFileName().toString().substring(0, 2);
            System.out.print("    Reading " + languageCode + "...");

            Map<String, Set<Integer>> _wordSynsets = Files.lines(wordSynsetsPath)
                    .map(line -> line.split("\t")).collect(Collectors.toMap(
                        (String[] parts) -> parts[1],
                        (String[] parts) -> Arrays.stream(parts).skip(3).map(p -> Integer.parseInt(p.substring(3, 11)))
                                                    .collect(Collectors.toSet())));

            wordSynsets.put(languageCode, _wordSynsets);
            System.out.println(" " + _wordSynsets.size() + " entries.");
        }

        List<Path> nasariLanguages = Files.walk(Paths.get(nasariDir), 1)
                .filter(Files::isDirectory)
                .collect(Collectors.toList());

        for (int start = 0; start < 22_000_000; start += bufferSize) {
            final int limit = start + bufferSize;
            Map<String, Map<Integer, Map<String, Double>>> nasariVectors = new HashMap<>();
            Map<Integer, Integer> synsetSources = new HashMap<>();

            for (Path nasariLanguage : nasariLanguages) {
                String fileName = "nasari_" + start + "_" + limit + ".tsv";
                Path filePath = Paths.get(nasariLanguage.toString(), fileName);
                String languageCode = nasariLanguage.getFileName().toString().substring(0, 2);
                if (Files.notExists(filePath)) {
                    continue;
                }

                System.out.print("  Reading from " + fileName + " in " + languageCode + "...");

                Map<Integer, Map<String, Double>> _nasariVectors = Files.lines(filePath)
                        .parallel()
                        .map(line -> line.split("\t")).collect(
                                Collectors.toMap(
                                    (String[] parts) -> Integer.parseInt(parts[0].substring(3, 11)),
                                    (String[] parts) -> parseValues(parts, wordSynsets.get(languageCode))));

                nasariVectors.put(languageCode, _nasariVectors);

                System.out.println(" " + _nasariVectors.size() + " entries.");

                for (int synset : _nasariVectors.keySet()) {
                    int currentSources = synsetSources.getOrDefault(synset, 0);
                    synsetSources.put(synset, currentSources + 1);
                }
            }

            System.out.print("  Filtering vectors...");

            Map<Integer, Map<Integer, Double>> selectionVectors = synsetSources.entrySet().parallelStream()
                    // .filter(e -> e.getValue() > 1)
                    .collect(Collectors.toMap(
                        e -> e.getKey(),
                        e -> selectSynsets(e.getKey(), nasariVectors, wordSynsets)));
            
            System.out.println(" Built " + selectionVectors.size() + " vectors.");
            
            Comparator<Map.Entry<Integer, Double>> comparator = (o1,
                    o2) -> ((Comparable<Double>) ((Map.Entry<Integer, Double>) (o2)).getValue())
                            .compareTo(((Map.Entry<Integer, Double>) (o1)).getValue());

            List<String> outputLines = new ArrayList<>();
            List<Integer> synsets = new ArrayList<>(selectionVectors.keySet());
            Collections.sort(synsets);
            for (int synset : synsets) {
                Map<Integer, Double> selectionVector = selectionVectors.get(synset);

                List<Map.Entry<Integer, Double>> sortedVector = new ArrayList<>(selectionVector.entrySet());
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

            System.out.println("  Vectors written to file!");
        }
    }

    private static Map<String, Double> parseValues(String[] lineParts, Map<String, Set<Integer>> wordSynsets) {
        return Arrays.stream(lineParts).skip(2)
                .map(part -> part.split("_"))
                .filter(parts -> wordSynsets.containsKey(parts[0]))
                .collect(
                    Collectors.toMap(
                        (String[] value) -> value[0],
                        (String[] value) -> Double.parseDouble(value[1]),
                        (existing, replacement) -> existing + replacement));
    }

    private static Map<Integer, Double> selectSynsets(int rootSynset,
            Map<String, Map<Integer, Map<String, Double>>> nasariVectors,
            Map<String, Map<String, Set<Integer>>> wordSynsets) {
        
        Map<Integer, Integer> hits = new HashMap<>();

        for (String languageCode : nasariVectors.keySet()) {
            Map<Integer, Map<String, Double>> nasariVectorsForLanguage = nasariVectors.get(languageCode);
            if (!nasariVectorsForLanguage.containsKey(rootSynset)) {
                continue;
            }

            Map<String, Double> nasariVector = nasariVectorsForLanguage.get(rootSynset);
            Map<String, Set<Integer>> wordSynsetsForLanguage = wordSynsets.get(languageCode);
            for (Map.Entry<String, Double> e : nasariVector.entrySet()) {
                String word = e.getKey();
                if (!wordSynsetsForLanguage.containsKey(word)) {
                    continue;
                }

                Set<Integer> synsets = wordSynsetsForLanguage.get(word);
                for (int synset : synsets) {
                    int count = hits.getOrDefault(synset, 0);
                    hits.put(synset, count + 1);
                }
            }
        }

        Map<Integer, Double> selectedSynsets = new HashMap<>();
        for (String languageCode : nasariVectors.keySet()) {
            Map<Integer, Map<String, Double>> nasariVectorsForLanguage = nasariVectors.get(languageCode);
            if (!nasariVectorsForLanguage.containsKey(rootSynset)) {
                continue;
            }

            Map<String, Double> nasariVector = nasariVectorsForLanguage.get(rootSynset);
            Map<String, Set<Integer>> wordSynsetsForLanguage = wordSynsets.get(languageCode);
            for (Map.Entry<String, Double> e : nasariVector.entrySet()) {
                String word = e.getKey();
                if (!wordSynsetsForLanguage.containsKey(word)) {
                    continue;
                }

                Set<Integer> synsets = wordSynsetsForLanguage.get(word);
                boolean monosemous = synsets.size() == 1;
                int maxHits = 0;
                int numCandidates = 0;
                for (int synset : synsets) {
                    int count = hits.get(synset);
                    maxHits = Math.max(maxHits, count);
                }

                for (int synset : synsets) {
                    int count = hits.get(synset);
                    numCandidates += (count >= 2 && count >= maxHits - 1) ? 1 : 0;
                }

                if (numCandidates > 3) {
                    continue;
                }

                for (int synset : synsets) {
                    int count = hits.get(synset);
                    if (monosemous || (count >= 2 && count >= maxHits - 1)) {
                        double score = selectedSynsets.getOrDefault(synset, 0d);
                        selectedSynsets.put(synset, score + e.getValue());
                    }
                }
            }
        }
        return selectedSynsets;
    }
}