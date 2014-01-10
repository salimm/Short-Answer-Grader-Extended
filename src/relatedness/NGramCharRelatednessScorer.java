package relatedness;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.tartarus.snowball.SnowballStemmer;
import org.tartarus.snowball.ext.englishStemmer;

import preProcess.StopWordTool;
import weka.core.Stopwords;
import corpus.AbstractCorpus;
import data.DataUnit;
import data.QASet;
import edu.berkeley.nlp.lm.ArrayEncodedProbBackoffLm;
import edu.berkeley.nlp.lm.ConfigOptions;
import edu.berkeley.nlp.lm.StringWordIndexer;
import edu.berkeley.nlp.lm.collections.Iterators;
import edu.berkeley.nlp.lm.io.IOUtils;
import edu.berkeley.nlp.lm.io.LmReaders;
import edu.cmu.lti.ws4j.util.StopWordRemover;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.CoreAnnotations.LemmaAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;
/**
 * 
 * @author Salim and Fataneh
 *
 */
public class NGramCharRelatednessScorer extends AbstractRelatednessScorer {
	ArrayEncodedProbBackoffLm<String> lm;
	StanfordCoreNLP pipeline;

	public NGramCharRelatednessScorer(String address, boolean forceTrain) {
		super(address, forceTrain);
	}

	@Override
	protected void doTrain(AbstractCorpus[] corpora, QASet dataset) {
	}

	@Override
	public double score(DataUnit[] references, DataUnit answer) {
		return score(answer.getText());
	}

	SnowballStemmer stemmer = new englishStemmer();

	private double score(String test) {

		float logScore = 0.0f;
		Annotation document = new Annotation(test);
		pipeline.annotate(document);
		List<CoreMap> sentences = document.get(SentencesAnnotation.class);
		StopWordTool stopWord = new StopWordTool();
		for (CoreMap sentence : sentences) {
			float sentScore = 0.0f;
			String newtext = "";
			List<Integer> sent = new ArrayList<>();
			sent.add(lm.getWordIndexer().getOrAddIndexFromString(
					lm.getWordIndexer().getStartSymbol()));
			for (CoreLabel token : sentence.get(TokensAnnotation.class)) {
				String lemma = token.get(LemmaAnnotation.class);
				newtext += lemma + " ";
			}

			char[] chars = newtext.toCharArray();
			String cline = "";
			for (char c : chars) {
				cline = cline + c + " ";
			}
			cline = cline.trim();
			cline = cline.replaceAll("\\s+", " ");
			for (String c : cline.split(" ")) {
				sent.add(lm.getWordIndexer().getIndexPossiblyUnk(c));
			}
			sent.add(lm.getWordIndexer().getOrAddIndexFromString(
					lm.getWordIndexer().getEndSymbol()));
			int[] sentArray = buildIntArray(sent);
			for (int i = 2; i <= Math.min(lm.getLmOrder(), sent.size()); ++i) {
				final float score = lm.getLogProb(sentArray, 0, i);
				sentScore += score;
			}
			for (int i = 1; i <= sent.size() - lm.getLmOrder(); ++i) {
				final float score = lm.getLogProb(sentArray, i,
						i + lm.getLmOrder());
				sentScore += score;
			}
			logScore += sentScore;
		}

		return logScore;
	}

	private int[] buildIntArray(List<Integer> integers) {
		int[] ints = new int[integers.size()];
		int i = 0;
		for (Integer n : integers) {
			ints[i++] = n;
		}
		return ints;
	}

	@Override
	protected void doLoad(String address) {
		this.lm = getLm(address, false);
		Properties props = new Properties();
		props.put("annotators", "tokenize, ssplit,pos,lemma");
		pipeline = new StanfordCoreNLP(props);
	}

	@Override
	protected void doSave(String address) {
	}

	/**
	 * @return
	 */
	private ArrayEncodedProbBackoffLm<String> getLm(String address,
			boolean unranked) {
		final File lmFile = FileUtils.getFile(address);
		final ConfigOptions configOptions = new ConfigOptions();
		configOptions.storeRankedProbBackoffs = !unranked;
		configOptions.unknownWordLogProb = 0.0f;
		final ArrayEncodedProbBackoffLm<String> lm = LmReaders
				.readArrayEncodedLmFromArpa(lmFile.getPath(), false,
						new StringWordIndexer(), configOptions,
						Integer.MAX_VALUE);
		return lm;
	}
	
	@Override
	public String getName() {
		return super.getName()+address;
	}

}
