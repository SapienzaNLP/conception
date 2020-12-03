package evaluation;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import it.uniroma1.lcl.babelnet.BabelNet;
import it.uniroma1.lcl.babelnet.BabelNetQuery;
import it.uniroma1.lcl.babelnet.BabelSense;
import it.uniroma1.lcl.babelnet.BabelSenseComparator;
import it.uniroma1.lcl.babelnet.BabelSynset;
import it.uniroma1.lcl.babelnet.BabelSynsetComparator;
import it.uniroma1.lcl.babelnet.data.BabelLemmaType;
import it.uniroma1.lcl.babelnet.data.BabelSenseSource;
import it.uniroma1.lcl.babelnet.data.BabelSynsetRelationComparator;

import it.uniroma1.lcl.jlt.util.Language;
import it.uniroma1.lcl.kb.SynsetType;

import com.babelscape.util.UniversalPOS;


public class WordSynsetGetter {

    private static final List<BabelSenseSource> sources = Arrays.asList(BabelSenseSource.WN, BabelSenseSource.IWN,
            BabelSenseSource.WONEF, BabelSenseSource.WIKI, BabelSenseSource.OMWIKI, BabelSenseSource.MSTERM,
            BabelSenseSource.VERBNET, BabelSenseSource.FRAMENET, BabelSenseSource.GEONM, BabelSenseSource.OMWN_IT,
            BabelSenseSource.OMWN_ZH, BabelSenseSource.OMWN_JA, BabelSenseSource.OMWN_PT, BabelSenseSource.OMWN_AR,
            BabelSenseSource.OMWN_FA, BabelSenseSource.MCR_ES, BabelSenseSource.OMWN_FR, BabelSenseSource.OMWN_KO,
            BabelSenseSource.OMWN_NL, BabelSenseSource.OMWN_CWN, BabelSenseSource.SALDO, BabelSenseSource.IWN,
            BabelSenseSource.MCR_PT);

    public static void main(String[] args) throws IOException {
        System.out.println("Word -> Synsets");
        if (args.length != 3) {
            System.out.println("This program requires 3 arguments:");
            System.out.println("  - path/to/word/vocabulary.txt");
            System.out.println("  - path/to/output.txt");
            System.out.println("  - buffer size");
            return;
        }

        String inputPath = args[0];
        String outputPath = args[1];
        int bufferSize = Integer.parseInt(args[2]);

        System.out.println("  input path:  " + inputPath);
        System.out.println("  output path: " + outputPath);
        System.out.println("  buffer size: " + bufferSize);

        getSynsets(inputPath, outputPath, bufferSize);
    }

    private static void getSynsets(String inputPath, String outputPath, int bufferSize)
            throws IOException {
        Stream<String> inputLines;
        List<String> outputLines;
        int lineNumber = 0;

        do {
            System.out.println("  Reading line " + lineNumber);
            inputLines = Files.lines(Paths.get(inputPath)).skip(lineNumber).limit(bufferSize);
            outputLines = inputLines.parallel().map(line -> getWordSynsets(line)).collect(Collectors.toList());

            Files.write(Paths.get(outputPath), outputLines, Charset.forName("UTF-8"), StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND);

            lineNumber += bufferSize;
        } while (!outputLines.isEmpty());
    }

    private static String getWordSynsets(String line) {
        String[] parts = line.split("_");
        Language language = Language.valueOf(parts[0]);
        String word = parts[1];

        List<String> ids = getHighQualityWordSynsets(word, language);
        if (ids.isEmpty()) {
            ids = getAutomaticWordSynsets(word, language);
        }
        // if (ids.isEmpty()) {
        //     ids = getWordSynsetsFromEnglish(word);
        // }
        // if (ids.isEmpty()) {
        //     ids = getWordSynsetsFromWordSplits(word, language, " ");
        // }
        // if (ids.isEmpty()) {
        //     ids = getWordSynsetsFromWordSplits(word, language, "-");
        // }
        // if (ids.isEmpty()) {
        //     ids = getWordSynsetsFromTruncatedWord(word, language);
        // }

        String outputLine;
        if (language.equals(Language.AR) || language.equals(Language.FA)) {
            outputLine = parts[0] + "_" + "\u202B" + word + "\u202C" + "\t" + ids.size() + "\t" + String.join("\t", ids);
        } else {
            outputLine = parts[0] + "_" + word + "\t" + ids.size() + "\t" + String.join("\t", ids);
        }

        return outputLine;
    }

    private static List<String> getHighQualityWordSynsets(String word, Language language) {

        BabelNet bn = BabelNet.getInstance();
        BabelNetQuery query = new BabelNetQuery.Builder(word).sources(WordSynsetGetter.sources).from(language)
                .toSameLanguages()
                .filterSynsets(synset -> synset.getSenseSources().size() > 1
                        || synset.getSenseSources().contains(BabelSenseSource.WN))
                .filterSynsets(synset -> synset.getLemmas(language, BabelLemmaType.HIGH_QUALITY).stream()
                        .anyMatch(lemma -> lemma.getLemma().toLowerCase().replace("_", " ").equals(word)))
                .build();

        List<BabelSynset> synsets;
        try {
            synsets = bn.getSynsets(query);
            Collections.sort(synsets, new BabelSynsetRelationComparator(word, language));
        } catch (Exception e) {
            synsets = bn.getSynsets(query);
            Collections.sort(synsets, new BabelSynsetComparator(word, language));
        }

        List<String> ids = synsets.stream().map(
                synset -> {
                        String strSynset = synset.getID().toString();
                        String strType = synset.getType().equals(SynsetType.CONCEPT) ? "C" : "E";
                        return strSynset + "::" + strType;
                }).collect(Collectors.toList());
        
        return ids;
    }

    private static List<String> getAutomaticWordSynsets(String word, Language language) {

        BabelNet bn = BabelNet.getInstance();
        BabelNetQuery query = new BabelNetQuery.Builder(word).sources(WordSynsetGetter.sources).from(language)
                .toSameLanguages()
                .build();

        List<BabelSynset> synsets;
        try {
            synsets = bn.getSynsets(query);
            Collections.sort(synsets, new BabelSynsetRelationComparator(word, language));
        } catch (Exception e) {
            synsets = bn.getSynsets(query);
            Collections.sort(synsets, new BabelSynsetComparator(word, language));
        }

        List<String> ids = synsets.stream().map(
                synset -> {
                        String strSynset = synset.getID().toString();
                        String strType = synset.getType().equals(SynsetType.CONCEPT) ? "C" : "E";
                        return strSynset + "::" + strType;
                }).collect(Collectors.toList());
        
        return ids;
    }

    private static List<String> getWordSynsetsFromEnglish(String word) {

        BabelNet bn = BabelNet.getInstance();
        BabelNetQuery query = new BabelNetQuery.Builder(word).sources(WordSynsetGetter.sources).from(Language.EN)
                .toSameLanguages()
                .POS(UniversalPOS.NOUN)
                .build();
        
        List<BabelSynset> synsets;
        try {
            synsets = bn.getSynsets(query);
            Collections.sort(synsets, new BabelSynsetRelationComparator(word, Language.EN));
        } catch (Exception e) {
            synsets = bn.getSynsets(query);
            Collections.sort(synsets, new BabelSynsetComparator(word, Language.EN));
        }

        List<String> ids = synsets.stream().map(
                synset -> {
                        String strSynset = synset.getID().toString();
                        String strType = synset.getType().equals(SynsetType.CONCEPT) ? "C" : "E";
                        return strSynset + "::" + strType;
                }).collect(Collectors.toList());
        
        return ids;
    }

    private static List<String> getWordSynsetsFromWordSplits(String word, Language language, String split) {
        String[] parts = word.split(split);

        BabelNet bn = BabelNet.getInstance();
        List<String> idList = new ArrayList<>();

        for (String part : parts) {
            if (part.length() < 4) {
                continue;
            }

            BabelNetQuery query = new BabelNetQuery.Builder(part).sources(WordSynsetGetter.sources).from(language)
                    .toSameLanguages()
                    .filterSynsets(synset -> synset.getSenseSources().size() > 1
                            || synset.getSenseSources().contains(BabelSenseSource.WN))
                    .filterSynsets(synset -> synset.getLemmas(language, BabelLemmaType.HIGH_QUALITY).stream()
                            .anyMatch(lemma -> lemma.getLemma().toLowerCase().replace("_", " ").equals(part)))
                    .POS(UniversalPOS.NOUN, UniversalPOS.ADJ)
                    .build();

            List<BabelSynset> synsets;
            try {
                synsets = bn.getSynsets(query);
                Collections.sort(synsets, new BabelSynsetRelationComparator(part, language));
            } catch (Exception e) {
                synsets = bn.getSynsets(query);
                Collections.sort(synsets, new BabelSynsetComparator(part, language));
            }

            List<String> ids = synsets.stream().map(
                    synset -> {
                            String strSynset = synset.getID().toString();
                            String strType = synset.getType().equals(SynsetType.CONCEPT) ? "C" : "E";
                            return strSynset + "::" + strType;
                    }).collect(Collectors.toList());
            
            for (String id : ids) {
                if (!idList.contains(id)) {
                    idList.add(id);
                }
            }
        }
        
        return idList;
    }

    private static List<String> getWordSynsetsFromTruncatedWord(String word, Language language) {
        int originalLength = word.length();

        BabelNet bn = BabelNet.getInstance();

        for (int i = originalLength - 1; i > 0.5*originalLength && i >= 5; i--) {
            String subword = word.substring(0, i);
            BabelNetQuery query = new BabelNetQuery.Builder(subword).sources(WordSynsetGetter.sources).from(language)
                    .toSameLanguages()
                    .filterSynsets(synset -> synset.getSenseSources().size() > 1
                            || synset.getSenseSources().contains(BabelSenseSource.WN))
                    .filterSynsets(synset -> synset.getLemmas(language, BabelLemmaType.HIGH_QUALITY).stream()
                            .anyMatch(lemma -> lemma.getLemma().toLowerCase().replace("_", " ").equals(subword)))
                    .POS(UniversalPOS.NOUN)
                    .build();

            List<BabelSynset> synsets;
            try {
                synsets = bn.getSynsets(query);
                Collections.sort(synsets, new BabelSynsetRelationComparator(subword, language));
            } catch (Exception e) {
                synsets = bn.getSynsets(query);
                Collections.sort(synsets, new BabelSynsetComparator(subword, language));
            }

            List<String> ids = synsets.stream().map(
                    synset -> {
                            String strSynset = synset.getID().toString();
                            String strType = synset.getType().equals(SynsetType.CONCEPT) ? "C" : "E";
                            return strSynset + "::" + strType;
                    }).collect(Collectors.toList());
            
            if (!ids.isEmpty()) {
                return ids;
            }
        }
        
        return new ArrayList<>();
    }
}