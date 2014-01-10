package relatedness;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;

import org.tartarus.snowball.SnowballStemmer;
import org.tartarus.snowball.ext.englishStemmer;

import preProcess.StopWordTool;
import similarity.SimilarityMethod;
import similarity.WordNetEngine;
import tools.Spelling;
import corpus.AbstractCorpus;
import data.DataUnit;
import data.QASet;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.CoreAnnotations.LemmaAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.PartOfSpeechAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;
import edu.stanford.nlp.util.Pair;

/**
 * 
 * @author Salim
 * 
 */
public class WordNetBasedRelatedness extends AbstractRelatednessScorer {
	public static final int VALID_SIM_THRESHOLD = 0;
	private SimilarityMethod method;
	private Spelling spelling;
	// private IDFModel idfModel;
	StopWordTool stopWords = new StopWordTool();
	StanfordCoreNLP pipeline;

	public WordNetBasedRelatedness(String address, boolean forceTrain,
			SimilarityMethod method, Spelling spelling) {
		super(address, forceTrain);
		this.method = method;
		this.spelling = spelling;
		Properties props = new Properties();
		props.put("annotators", "tokenize, ssplit, pos,lemma");
		pipeline = new StanfordCoreNLP(props);
		// idfModel = new IDFModel("data/idfmodel", false);
		// idfModel.buildModel("data/corpus-xml/corpus-complete.xml");
	}

	@Override
	protected void doTrain(AbstractCorpus[] corpora, QASet dataset) {
	}

	@Override
	public double score(DataUnit[] references, DataUnit answer) {
		HashMap<String, ArrayList<String>> answerPosSets = getPosSets(answer);
		double max = -99999999;
		for (DataUnit ref : references) {
			HashMap<String, ArrayList<String>> refPosSets = getPosSets(ref);
			double sc1 = scoreSimilarity(refPosSets, answerPosSets);
			double sc2 = scoreSimilarity(answerPosSets, refPosSets);
			max = Math.max(max, (sc1 + sc2) / 2);
		}
		return max;
	}

	private double scoreSimilarity(HashMap<String, ArrayList<String>> posSets1,
			HashMap<String, ArrayList<String>> posSets2) {
		int count = 0;
		double sum = 0;
		for (Entry<String, ArrayList<String>> set : posSets1.entrySet()) {
			Pair<Double, Integer> res = score(set.getValue(),
					posSets2.get(set.getKey()), set.getKey());
			sum += res.first;
			count += res.second;

		}
		return sum / Math.max(1, count);
	}

	private Pair<Double, Integer> score(ArrayList<String> set1,
			ArrayList<String> set2, String type) {
		int count = 0;
		double sum = 0;
		if (set1 == null || set2 == null)
			return new Pair<Double, Integer>(sum, count);
		for (String w1 : set1) {
			w1 = spelling.correct(w1);
			double max = Double.MIN_VALUE;
			for (String w2 : set2) {
				if (!(type.equals("VB") || type.equals("NN"))) {
					if (w1.equals(w2)) {
						sum += 1;
						count += 1;
					}
					continue;
				}
				w2 = spelling.correct(w2);
				try {
					double sc = WordNetEngine.wordSimilarity(w1
							+ getWordNetTypeTag(type), w2
							+ getWordNetTypeTag(type), method);
					if (sc > VALID_SIM_THRESHOLD && sc > max) {
						max = sc;
					}
				} catch (InstantiationException | IllegalAccessException
						| IllegalArgumentException | InvocationTargetException
						| NoSuchMethodException | SecurityException e) {
					e.printStackTrace();
				}
			}
			if (max > 0) {
				// sum += max * idfModel.getIDF(w1);
				sum += max;
				// count+=idfModel.getIDF(w1);
				count += 1;
			}
		}
		return new Pair<Double, Integer>(sum, count);
	}

	private HashMap<String, ArrayList<String>> getPosSets(DataUnit answer) {
		HashMap<String, ArrayList<String>> sets = new HashMap<>();

		Annotation document = new Annotation(answer.getText());
		pipeline.annotate(document);
		List<CoreMap> sentences = document.get(SentencesAnnotation.class);
		for (CoreMap sentence : sentences) {
			for (CoreLabel token : sentence.get(TokensAnnotation.class)) {
				String lemma = token.get(LemmaAnnotation.class);
				lemma = lemma.toLowerCase();
				if (stopWords.isStopWord(lemma))
					continue;
				String pos = token.get(PartOfSpeechAnnotation.class);
				if (pos.equals("CD")) {
					pos = "CD";
				} else if (pos.startsWith("JJ")) {
					pos = "JJ";
				} else if (pos.startsWith("NN")) {
					pos = "NN";
				} else if (pos.startsWith("VB")) {
					pos = "VB";
				} else {
					continue;
				}
				if (!sets.containsKey(pos)) {
					sets.put(pos, new ArrayList<String>(3));
				}
				sets.get(pos).add(lemma);
			}

		}
		return sets;
	}

	private String getWordNetTypeTag(String type) {
		if (type.equals("VB"))
			return "#v";
		if (type.equals("NN"))
			return "#n";
		return "";
	}

	@Override
	protected void doLoad(String address) {

	}

	@Override
	protected void doSave(String address) {
	}

}
