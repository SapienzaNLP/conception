package preprocess;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import it.uniroma1.lcl.babelnet.data.BabelDomain;
import it.uniroma1.lcl.babelnet.BabelNet;
import it.uniroma1.lcl.babelnet.BabelSynset;
import it.uniroma1.lcl.babelnet.BabelSynsetID;

public class SynsetCategoryGetter {

    public static void main(String[] args) throws IOException {
        System.out.println("Synset -> Categories");
        if (args.length != 3) {
            System.out.println("This program requires 4 arguments:");
            System.out.println("  - path/to/synset/vocabulary.txt");
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

        getCategories(inputPath, outputPath, bufferSize);
    }

    private static void getCategories(String inputPath, String outputPath, int bufferSize) throws IOException {
        Stream<String> inputLines;
        List<String> outputLines;
        int lineNumber = 0;

        do {
            System.out.println("  Reading line " + lineNumber);
            inputLines = Files.lines(Paths.get(inputPath)).skip(lineNumber).limit(bufferSize);
            outputLines = inputLines.parallel().map(line -> getSynsetCategories(line)).collect(Collectors.toList());

            Files.write(Paths.get(outputPath), outputLines, Charset.forName("UTF-8"), StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND);

            lineNumber += bufferSize;
        } while (!outputLines.isEmpty());
    }

    private static String getSynsetCategories(String line) {
        String strSynset = line.trim();
        BabelNet bn = BabelNet.getInstance();
        BabelSynset synset = bn.getSynset(new BabelSynsetID(strSynset));
        List<String> categories = synset.getDomains().keySet().stream()
                .map(domain -> ((BabelDomain) domain).getDomainString())
                .collect(Collectors.toList());

        String outputLine = strSynset + "\t" + String.join("\t", categories);

        return outputLine;
    }
}