package relatedness;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Properties;
import java.util.Vector;

import corpus.AbstractCorpus;
import data.DataUnit;
import data.QASet;
import edu.stanford.nlp.ling.CoreAnnotations.LemmaAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;
import tools.Spelling;

public class LSASpelledRelatedness extends AbstractRelatednessScorer {
	
	LSALoader lsaModel;
	StanfordCoreNLP pipeline;
	Spelling speller;

	public LSASpelledRelatedness(String address, boolean forceTrain,Spelling speller) {
		super(address, forceTrain);
		this.speller = speller;
		Properties props = new Properties();
		props.put("annotators", "tokenize, ssplit, pos, lemma");
		pipeline = new StanfordCoreNLP(props);
		
		try {
			System.out.println("Loading LSA space model.");
			lsaModel = new LSALoader(new File("data/lsa"));
			System.out.println("LSA space model loaded successfully.");
		} catch (IOException ex) {
			System.out.println("Failed to load the LSA model.");
		}
	}

	@Override
	protected void doTrain(AbstractCorpus[] corpora, QASet dataset) {
		// do nothing
	}
	
	public double score(DataUnit[] references, DataUnit answer) {
		double max = -99999999;
		for (DataUnit ref : references) {
			String preparedRef = prepareText(ref.getText());
			String preparedAnswer = prepareText(answer.getText());
			String[] refList = preparedRef.split(" ");
			String[] answerList = preparedAnswer.split(" ");
			List<Vector<Double>> refVectors = new Vector<>(refList.length);
			List<Vector<Double>> answerVectors = new Vector<>(answerList.length);
			for (int i = 0; i < refList.length; i ++) {
				refVectors.add(lsaModel.getVector(refList[i]));
			}
			for (int i = 0; i < answerList.length; i ++) {
				answerVectors.add(lsaModel.getVector(answerList[i]));
			}
			Vector<Double> refVector = lsaModel.sumVectors(refVectors);
			Vector<Double> answerVector = lsaModel.sumVectors(answerVectors);			
			double relatedness = lsaModel.cosineSimilarity(refVector, answerVector);
			if (relatedness > max) {
				max = relatedness;
			}
		}
		return max;
	}
	
	private String prepareText(String words) {
		String pretext = words.trim().replaceAll("[^\\x00-\\x7F]", "").replaceAll("[^\\w\\.\\s\\-]", "").replaceAll("[0-9]","").replaceAll("\\.", "").replaceAll("\\s+", " ");
		StringBuilder prebuilder = new StringBuilder();
		for (String word : pretext.split(" ")) {
			prebuilder.append(speller.correct(word));
			prebuilder.append(" ");
		}
		String text = prebuilder.toString().trim();
		Annotation document = new Annotation(text);
		pipeline.annotate(document);
		List<CoreMap> sentences = document.get(SentencesAnnotation.class);
		StringBuilder builder = new StringBuilder();
		for(CoreMap sentence: sentences) {
			for (CoreLabel token: sentence.get(TokensAnnotation.class)) {
				String word = token.get(LemmaAnnotation.class);
				builder.append(word.toLowerCase());
				builder.append(" ");
			}
		}
		return builder.toString().trim();
	}

	@Override
	public String getName() {
		return "LSA Similarity (own corpus)";
	}

	@Override
	protected void doLoad(String address) {
	}

	@Override
	protected void doSave(String address) {
		// do nothing
	}

}
