package preprocess;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import it.uniroma1.lcl.babelnet.data.BabelDomain;
import it.uniroma1.lcl.babelnet.data.BabelPointer;
import it.uniroma1.lcl.babelnet.BabelNet;
import it.uniroma1.lcl.babelnet.BabelSynset;
import it.uniroma1.lcl.babelnet.BabelSynsetID;

public class SynsetNeighborGetter {

    private static final List<BabelPointer> sources = Arrays.asList(BabelPointer.ANTONYM, BabelPointer.ATTRIBUTE,
            BabelPointer.DERIVATIONALLY_RELATED, BabelPointer.ENTAILMENT, BabelPointer.PERTAINYM,
            BabelPointer.HOLONYM_MEMBER, BabelPointer.HOLONYM_PART, BabelPointer.HOLONYM_SUBSTANCE,
            BabelPointer.HYPERNYM, BabelPointer.HYPONYM, BabelPointer.MERONYM_MEMBER,
            BabelPointer.MERONYM_PART, BabelPointer.MERONYM_SUBSTANCE, BabelPointer.SIMILAR_TO, BabelPointer.TOPIC,
            BabelPointer.TOPIC_MEMBER, BabelPointer.VERB_GROUP, BabelPointer.WIKIDATA_HYPONYM,
            BabelPointer.WIKIDATA_HYPERNYM, BabelPointer.WIKIDATA_MERONYM, BabelPointer.SEMANTICALLY_RELATED);

    public static void main(String[] args) throws IOException {
        System.out.println("Synset -> Neighbors");
        if (args.length != 2) {
            System.out.println("This program requires 2 arguments:");
            System.out.println("  - path/to/output.txt");
            System.out.println("  - buffer size");
            return;
        }

        String outputPath = args[0];
        int bufferSize = Integer.parseInt(args[1]);

        System.out.println("  output path: " + outputPath);
        System.out.println("  buffer size: " + bufferSize);

        getNeighbors(outputPath, bufferSize);
    }

    private static void getNeighbors(String outputPath, int bufferSize) throws IOException {
        Iterator<String> it = BabelNet.getInstance().getOffsetIterator();

        int currentBufferSize = 0;
        List<String> outputLines = new ArrayList<>();
        while (it.hasNext()) {
            String offset = it.next();
            String outputLine = getSynsetCategories(offset);
            
            outputLines.add(outputLine);
            
            currentBufferSize++;
            if (currentBufferSize == bufferSize) {
                System.out.println("  Writing neighbors to file...");
                Files.write(Paths.get(outputPath), outputLines, Charset.forName("UTF-8"), StandardOpenOption.CREATE,
                        StandardOpenOption.APPEND);
                currentBufferSize = 0;
                outputLines.clear();
            }
        }

        System.out.println("  Writing neighbors to file...");
        Files.write(Paths.get(outputPath), outputLines, Charset.forName("UTF-8"), StandardOpenOption.CREATE,
                StandardOpenOption.APPEND);
    }

    private static String getSynsetCategories(String offset) {
        String strSynset = offset.trim();
        BabelSynsetID synset = new BabelSynsetID(strSynset);

        List<String> neighbors = synset.getOutgoingEdges().stream()
                .filter(edge -> !edge.getPointer().isAutomatic() && sources.contains(edge.getPointer()))
                .map(edge -> edge.getTarget() + "::" + edge.getPointer().getShortName()).collect(Collectors.toList());

        String outputLine = strSynset + "\t" + neighbors.size() + "\t" + String.join("\t", neighbors);

        return outputLine;
    }
}