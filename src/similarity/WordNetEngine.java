package similarity;

import java.lang.reflect.InvocationTargetException;
import edu.cmu.lti.lexical_db.ILexicalDatabase;
import edu.cmu.lti.lexical_db.NictWordNet;
import edu.cmu.lti.lexical_db.data.Concept;
import edu.cmu.lti.ws4j.Relatedness;
import edu.cmu.lti.ws4j.RelatednessCalculator;

/**
 * This is just a wrapper for the WS4J WordNet library.
 * 
 * @author Salim
 * 
 */
public class WordNetEngine {

	private static final ILexicalDatabase db = new NictWordNet();

	public WordNetEngine() {
	}

	public static double wordSimilarity(String word1, String word2,
			SimilarityMethod method) throws InstantiationException,
			IllegalAccessException, IllegalArgumentException,
			InvocationTargetException, NoSuchMethodException, SecurityException {
		RelatednessCalculator rc = (RelatednessCalculator) method.rc
				.getConstructor(ILexicalDatabase.class).newInstance(db);
		return Math.min(1, rc.calcRelatednessOfWords(word1, word2));
	}

	public static Relatedness synsetSimilarity(Concept synset1,
			Concept synset2, SimilarityMethod method)
			throws InstantiationException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException,
			NoSuchMethodException, SecurityException {
		RelatednessCalculator rc = (RelatednessCalculator) method.rc
				.getConstructor(ILexicalDatabase.class).newInstance(db);
		return rc.calcRelatednessOfSynset(synset1, synset2);
	}

}
