package tools;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;
import java.util.concurrent.ConcurrentNavigableMap;

import org.mapdb.DB;
import org.mapdb.DBMaker;

import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;

/**
 * 
 * @author Salim
 * 
 */
public class IDFModel {
	private ConcurrentNavigableMap<String, Double> treeMap;
	private DB db;

	public IDFModel(String address, boolean forceTrain) {
		File f = new File(address);
		if (f.exists() && forceTrain)
			f.delete();
		db = DBMaker.newFileDB(f).closeOnJvmShutdown().make();
		treeMap = db.getTreeMap("idf");
	}

	public void buildModel(String address) {
		if (treeMap.size() != 0)
			return;
		String tmp = "";
		try {
			tmp = new Scanner(new File(address)).useDelimiter("\\Z").next();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		String pattern = "<corpus>\n	<page>\n		<section type=\"col11406-p\">|		</section>\n		<section type=\"col11406-p\">|		</section>\n	</page>\n</corpus>";
		String[] paragraphs = tmp.split(pattern);
		if (paragraphs == null || paragraphs.length == 0) {
			throw new Error("Bad File");
		} else {
			Properties props = new Properties();
			props.put("annotators", "tokenize, ssplit");
			StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
			for (String p : paragraphs) {
				Annotation document = new Annotation(p);
				pipeline.annotate(document);
				List<CoreMap> sentences = document
						.get(SentencesAnnotation.class);
				HashMap<String, Boolean> map = new HashMap<>();
				for (CoreMap sentence : sentences) {
					for (CoreLabel token : sentence.get(TokensAnnotation.class)) {
						String word = token.get(TextAnnotation.class);
						if (map.get(word) != null)
							continue;
						map.put(word, true);
						Double d = treeMap.get(word);
						if (d == null) {
							d = 0.0;
						}
						d++;
						treeMap.put(word, d);
					}
				}
			}

			for (String s : treeMap.keySet()) {
				treeMap.put(s, ((double) paragraphs.length) / treeMap.get(s));
			}
			treeMap.put("totaltotaltotaltotaltotaltotaltotal",
					(double) paragraphs.length);
			db.commit();
		}

	}

	public double getIDF(String word) {
		if (word == null)
			return 0;
		Double d = treeMap.get(word);
		if (d == null)
			return treeMap.get("totaltotaltotaltotaltotaltotaltotal");
		return d;
	}

	// private void print() {
	// for (String s : treeMap.keySet()) {
	// System.out.println(s + "     ----      " + treeMap.get(s));
	// }
	// }

}
