package preprocess;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import it.uniroma1.lcl.babelnet.BabelNet;
import it.uniroma1.lcl.babelnet.BabelNetQuery;
import it.uniroma1.lcl.babelnet.BabelSynset;
import it.uniroma1.lcl.babelnet.data.BabelLemmaType;
import it.uniroma1.lcl.babelnet.data.BabelSenseSource;
import it.uniroma1.lcl.jlt.util.Language;

public class WordSynsetGetter {

	private static final List<BabelSenseSource> sources = Arrays.asList(BabelSenseSource.WN, BabelSenseSource.IWN,
			BabelSenseSource.WONEF, BabelSenseSource.WIKI, BabelSenseSource.OMWIKI, BabelSenseSource.MSTERM,
			BabelSenseSource.VERBNET, BabelSenseSource.FRAMENET, BabelSenseSource.GEONM, BabelSenseSource.OMWN_IT,
			BabelSenseSource.OMWN_ZH, BabelSenseSource.OMWN_JA, BabelSenseSource.OMWN_PT, BabelSenseSource.OMWN_AR,
			BabelSenseSource.OMWN_FA, BabelSenseSource.MCR_ES, BabelSenseSource.OMWN_FR, BabelSenseSource.OMWN_KO);

	public static void main(String[] args) throws IOException {
		System.out.println("Word -> Synsets");
		if (args.length != 4) {
			System.out.println("This program requires 4 arguments:");
			System.out.println("  - path/to/word/vocabulary.txt");
			System.out.println("  - path/to/output.txt");
			System.out.println("  - vocabulary language (EN, IT, DE, FR, ES)");
			System.out.println("  - buffer size");
			return;
		}

		String inputPath = args[0];
		String outputPath = args[1];
		Language language = Language.valueOf(args[2]);
		int bufferSize = Integer.parseInt(args[3]);

		System.out.println("  input path:  " + inputPath);
		System.out.println("  output path: " + outputPath);
		System.out.println("  language:    " + language.getName());
		System.out.println("  buffer size: " + bufferSize);

		getSynsets(inputPath, outputPath, language, bufferSize);
	}

	private static void getSynsets(String inputPath, String outputPath, Language language, int bufferSize) throws IOException {
		Stream<String> inputLines;
		List<String> outputLines;
		int lineNumber = 0;

		do {
			System.out.println("  Reading line " + lineNumber);
			inputLines = Files.lines(Paths.get(inputPath)).skip(lineNumber).limit(bufferSize);
			outputLines = inputLines.parallel().map(line -> getWordSynsets(line, language)).collect(Collectors.toList());

			Files.write(Paths.get(outputPath), outputLines, Charset.forName("UTF-8"), StandardOpenOption.CREATE,
					StandardOpenOption.APPEND);

			lineNumber += bufferSize;
		} while (!outputLines.isEmpty());
	}

	private static String getWordSynsets(String line, Language language) {
		String[] parts = line.split("\t", 2);
		String word = parts[0];
		String occurrences = parts[1];

		BabelNet bn = BabelNet.getInstance();
		BabelNetQuery query = new BabelNetQuery.Builder(word)
				.sources(WordSynsetGetter.sources)
				.from(language)
				.toSameLanguages()
				.filterSynsets(synset -> synset.getSenseSources().size() > 1 || synset.getSenseSources().contains(BabelSenseSource.WN))
				.filterSynsets(synset -> synset.getLemmas(language, BabelLemmaType.HIGH_QUALITY).stream()
						.anyMatch(lemma -> lemma.getLemma().toLowerCase().replace("_", " ").equals(word)))
				.build();
		List<BabelSynset> synsets = bn.getSynsets(query);

		List<String> ids = synsets.stream()
				.map(synset -> synset.getID().toString())
				.collect(Collectors.toList());
		String outputLine = occurrences + "\t" + word + "\t" + ids.size() + "\t" + String.join("\t", ids);
		
		return outputLine;
	}
}
