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

import it.uniroma1.lcl.kb.SynsetType;

public class SynsetTypeGetter {

    public static void main(String[] args) throws IOException {
        System.out.println("Synset -> Types");
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

        getTypes(outputPath, bufferSize);
    }

    private static void getTypes(String outputPath, int bufferSize) throws IOException {
        Iterator<BabelSynset> it = BabelNet.getInstance().iterator();

        int currentBufferSize = 0;
        List<String> outputLines = new ArrayList<>();
        while (it.hasNext()) {
            BabelSynset synset = it.next();
            String outputLine = getSynsetType(synset);
            
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

        System.out.println("  Writing types to file...");
        Files.write(Paths.get(outputPath), outputLines, Charset.forName("UTF-8"), StandardOpenOption.CREATE,
                StandardOpenOption.APPEND);
    }

    private static String getSynsetType(BabelSynset synset) {
        String strSynset = synset.getID().toString();
        String strType = synset.getType().equals(SynsetType.CONCEPT) ? "C" : "E";
        String outputLine = strSynset + "\t" + strType;
        return outputLine;
    }
}