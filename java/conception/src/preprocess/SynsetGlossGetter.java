package preprocess;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import it.uniroma1.lcl.babelnet.BabelNet;
import it.uniroma1.lcl.babelnet.BabelNetQuery;
import it.uniroma1.lcl.babelnet.BabelSynset;
import it.uniroma1.lcl.babelnet.data.BabelExample;
import it.uniroma1.lcl.babelnet.data.BabelGloss;
import it.uniroma1.lcl.babelnet.data.BabelLemmaType;
import it.uniroma1.lcl.babelnet.data.BabelSenseSource;
import it.uniroma1.lcl.jlt.util.Language;

import com.babelscape.util.UniversalPOS;
import it.uniroma1.lcl.kb.SynsetType;

public class SynsetGlossGetter {

	private static final List<BabelSenseSource> sources = Arrays.asList(BabelSenseSource.WN, BabelSenseSource.IWN,
			BabelSenseSource.WONEF, BabelSenseSource.WIKI, BabelSenseSource.OMWIKI, BabelSenseSource.MSTERM,
			BabelSenseSource.VERBNET, BabelSenseSource.FRAMENET, BabelSenseSource.GEONM, BabelSenseSource.OMWN_IT,
			BabelSenseSource.OMWN_ZH, BabelSenseSource.OMWN_JA, BabelSenseSource.OMWN_PT, BabelSenseSource.OMWN_AR,
			BabelSenseSource.OMWN_FA, BabelSenseSource.MCR_ES, BabelSenseSource.OMWN_FR, BabelSenseSource.OMWN_KO);

	public static void main(String[] args) throws IOException {
		System.out.println("Word -> Synsets");
		if (args.length != 3) {
			System.out.println("This program requires 4 arguments:");
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

	private static void getSynsets(String inputPath, String outputPath, int bufferSize) throws IOException {
		Stream<String> inputLines;
		List<String> outputLines;
		int lineNumber = 0;

		do {
			System.out.println("  Reading line " + lineNumber);
			inputLines = Files.lines(Paths.get(inputPath)).skip(lineNumber).limit(bufferSize);
			outputLines = inputLines.parallel().map(line -> getWordSynsets(line)).filter(line -> !line.isEmpty()).collect(Collectors.toList());

			Files.write(Paths.get(outputPath), outputLines, Charset.forName("UTF-8"), StandardOpenOption.CREATE,
					StandardOpenOption.APPEND);

			lineNumber += bufferSize;
		} while (!outputLines.isEmpty());
	}

	private static String getWordSynsets(String line) {
        String[] parts = line.split("\t");
        Language language = Language.valueOf(parts[0]);
        String word = parts[1];
        UniversalPOS pos = UniversalPOS.valueOf(parts[2]);

		BabelNet bn = BabelNet.getInstance();
		BabelNetQuery query = new BabelNetQuery.Builder(word)
				// .sources(SynsetGlossGetter.sources)
				.from(language)
                // .to(Language.EN)
                // .POS(pos)
                .filterSynsets(synset -> synset.getType().equals(SynsetType.CONCEPT))
				// .filterSynsets(synset -> synset.getSenseSources().size() > 1 || synset.getSenseSources().contains(BabelSenseSource.WN))
				// .filterSynsets(synset -> synset.getLemmas(language, BabelLemmaType.HIGH_QUALITY).stream()
				// 		.anyMatch(lemma -> lemma.getLemma().toLowerCase().replace("_", " ").equals(word)))
				.build();
		List<BabelSynset> synsets = bn.getSynsets(query);

        StringBuffer outputLine = new StringBuffer();
        for (int i = 0; i < synsets.size(); i++) {
            BabelSynset synset = synsets.get(i);
            outputLine.append(language).append("\t");
            if (language.equals(Language.AR)) {
                outputLine.append("\u202B" + word + "\u202C");
            } else {
                outputLine.append(word);
            }
            outputLine.append("\t").append(synset.getPOS()).append("\t").append(synset.getID());
            List<BabelGloss> glosses = synset.getGlosses(Language.EN);
            if (!glosses.isEmpty()) {
				String gloss = glosses.get(0).getGloss();
				outputLine.append("\t").append(gloss);
            }

			List<BabelExample> examples = synset.getExamples(Language.EN);
			if (!examples.isEmpty()) {
				String example = examples.get(0).getExample();
				outputLine.append("\t").append(example);
			}
			
            if (i != synsets.size() - 1) {
                outputLine.append("\n");
            }
        }
		
		return outputLine.toString();
	}
}
