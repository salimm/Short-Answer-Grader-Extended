package runners;
import java.io.IOException;

import relatedness.AbstractRelatednessScorer;
import relatedness.CosineSimilarityRelatedness;
import relatedness.FMeasureRelatedness;
import relatedness.LSASpelledRelatedness;
import relatedness.LeskSimilarity;
import relatedness.NGramCharRelatednessScorer;
import relatedness.NGramRelatednessScorer;
import relatedness.PrecisionRelatedness;
import relatedness.RecallRelatedness;
import tools.Spelling;

public class DefaultScorerList {
	public static AbstractRelatednessScorer[] DEFAULT_SCORERS() throws IOException{
		Spelling spelling = new Spelling("data/big.txt");
		return new AbstractRelatednessScorer[] {	
				new NGramRelatednessScorer(
						"data/languageModels/word-based/corpus-ngram-model-3.arpa",
						false, spelling),
				new NGramRelatednessScorer(
						"data/languageModels/word-based/corpus-ngram-model-4.arpa",
						false, spelling),
				new NGramRelatednessScorer(
						"data/languageModels/word-based/train-ngram-model-3.arpa",
						false, spelling),
				new NGramRelatednessScorer(
						"data/languageModels/word-based/train-ngram-model-4.arpa",
						false, spelling),
				new NGramCharRelatednessScorer(
						"data/languageModels/char-based/char-corpus-ngram-model-4.arpa",
						false),
				new NGramCharRelatednessScorer(
						"data/languageModels/char-based/char-corpus-ngram-model-5.arpa",
						false),
				new NGramCharRelatednessScorer(
						"data/languageModels/char-based/char-corpus-ngram-model-6.arpa",
						false),
				new NGramCharRelatednessScorer(
						"data/languageModels/char-based/char-corpus-ngram-model-7.arpa",
						false),
				new NGramCharRelatednessScorer(
						"data/languageModels/char-based/char-corpus-ngram-model-8.arpa",
						false),
				new NGramCharRelatednessScorer(
						"data/languageModels/char-based/char-train-ngram-model-4.arpa",
						false),
				new NGramCharRelatednessScorer(
						"data/languageModels/char-based/char-train-ngram-model-5.arpa",
						false),
				new NGramCharRelatednessScorer(
						"data/languageModels/char-based/char-train-ngram-model-6.arpa",
						false),
				new NGramCharRelatednessScorer(
						"data/languageModels/char-based/char-train-ngram-model-7.arpa",
						false),
				new NGramCharRelatednessScorer(
						"data/languageModels/char-based/char-train-ngram-model-8.arpa",
						false),
				new LSASpelledRelatedness(null, false, spelling),
				new CosineSimilarityRelatedness(null, false),
				new FMeasureRelatedness("", false),
				new RecallRelatedness(null, false),
				new PrecisionRelatedness(null, false),
				new LeskSimilarity(null, false) };

		
	}
}
