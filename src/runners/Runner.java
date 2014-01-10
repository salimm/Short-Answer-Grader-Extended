package runners;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Random;

import relatedness.AbstractRelatednessScorer;
import relatedness.FeatureSet;
import relatedness.RelatednessEngine;
import weka.classifiers.Classifier;
import weka.classifiers.evaluation.Evaluation;
import weka.classifiers.meta.FilteredClassifier;
import weka.classifiers.trees.RandomForest;
import weka.core.Instances;
import weka.filters.unsupervised.attribute.Remove;
import io.output.CSVWriter;
import corpus.AbstractCorpus;
import data.FeatureSetToInstancesConvertor;
import data.QASet;
import edu.stanford.nlp.util.Pair;

/**
 * 
 * @author Salim
 * 
 */
public abstract class Runner {
	public static void run(AbstractCorpus[] corpora, QASet train,
			AbstractRelatednessScorer[] scorers, Classifier classifier)
			throws InstantiationException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException,
			NoSuchMethodException, SecurityException {
		RelatednessEngine en = new RelatednessEngine(scorers);
		en.load();
		en.train(corpora, train);
		en.save();
		Pair<String[], List<FeatureSet>> features = en.score(train);
		Instances trainset = FeatureSetToInstancesConvertor.convert(features,
				train.getType());
		trainset.setClassIndex(trainset.numAttributes() - 1);
		FilteredClassifier fc = new FilteredClassifier();
		fc.setClassifier(classifier);
		Remove rem = new Remove();
		rem.setAttributeIndices("1,2");
		fc.setFilter(rem);
		try {
			fc.buildClassifier(trainset);
			Evaluation e = new Evaluation(trainset);
			e.crossValidateModel(fc, trainset, 10, new Random(1));
			System.out.println(e.toSummaryString());
			System.out.println(e.toClassDetailsString(""));
			System.out.println(e.toMatrixString(""));

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public static void train(AbstractCorpus[] corpora, QASet train,
			AbstractRelatednessScorer[] scorers, String modelPath)
			throws InstantiationException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException,
			NoSuchMethodException, SecurityException, ClassNotFoundException,
			IOException {

		RelatednessEngine en = new RelatednessEngine(scorers);
		en.load();
		en.train(corpora, train);
		en.save();
		Pair<String[], List<FeatureSet>> features = en.score(train);
		System.out.println("Features extracted...");
		Instances trainset = FeatureSetToInstancesConvertor.convert(features,
				train.getType());

		trainset.setClassIndex(trainset.numAttributes() - 1);
		System.out.println("Converted the train data to weka data format...");
		FilteredClassifier fc = new FilteredClassifier();
		fc.setClassifier(getClassifier());
		Remove rem = new Remove();
		rem.setAttributeIndices("1,2");
		fc.setFilter(rem);
		try {
			fc.buildClassifier(trainset);
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("Trained the classifier model...");
		ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(
				new File(modelPath)));
		oos.writeObject(fc);
		System.out.println("Classifier is written to file...\n Done!!!!");
		oos.flush();
		oos.close();
	}

	private static Classifier getClassifier() {
		return new RandomForest();
	}

	public static void runAndSaveResult(AbstractCorpus[] corpora, QASet train,
			QASet test, AbstractRelatednessScorer[] scorers,
			Classifier classifier, String outPath)
			throws InstantiationException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException,
			NoSuchMethodException, SecurityException {
		RelatednessEngine en = new RelatednessEngine(scorers);
		en.load();
		en.train(corpora, train);
		en.save();
		Pair<String[], List<FeatureSet>> features = en.score(test);
		Instances trainset = FeatureSetToInstancesConvertor.convert(features,
				train.getType());
		Instances testset = FeatureSetToInstancesConvertor.convert(features,
				test.getType());

		trainset.setClassIndex(trainset.numAttributes() - 1);
		testset.setClassIndex(testset.numAttributes() - 1);
		FilteredClassifier fc = new FilteredClassifier();
		fc.setClassifier(classifier);
		Remove rem = new Remove();
		rem.setAttributeIndices("1,2");
		fc.setFilter(rem);
		BufferedWriter bw = null;
		try {
			bw = new BufferedWriter(new FileWriter(new File(outPath)));
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		try {
			fc.buildClassifier(trainset);
			for (int i = 0; i < features.second.size(); i++) {
				double l = fc.classifyInstance(testset.instance(i));
				bw.write("" + features.second.get(i).getAnswer().getId()
						+ "\t1\t"
						+ features.second.get(i).getAnswer().getAccuracy()
						+ "\t" + test.getType().values[(int) l] + "\n");

			}
			bw.flush();
			bw.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public static void finalEvaluate(QASet test,
			AbstractRelatednessScorer[] scorers, String classifierAddress,
			String outPath) throws InstantiationException,
			IllegalAccessException, IllegalArgumentException,
			InvocationTargetException, NoSuchMethodException,
			SecurityException, ClassNotFoundException, IOException {

		RelatednessEngine en = new RelatednessEngine(scorers);
		en.load();
		Pair<String[], List<FeatureSet>> features = en.score(test);
		System.out.println("Features Extracted......");
		Instances testset = FeatureSetToInstancesConvertor.convert(features,
				test.getType());
		testset.setClassIndex(testset.numAttributes() - 1);
		System.out.println("Converted features to weka format....");
		FilteredClassifier fc = loadClassifier(classifierAddress);
		System.out.println("Classifier is loaded...");

		BufferedWriter bw = null;
		try {
			bw = new BufferedWriter(new FileWriter(new File(outPath)));
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		System.out.println("Classifying the test set...");
		double correct = 0;
		double total = 0;
		try {
			for (int i = 0; i < features.second.size(); i++) {
				double l = fc.classifyInstance(testset.instance(i));
				if (features.second.get(i).getAnswer().getAccuracy()
						.equals(test.getType().values[(int) l]))
					correct++;
				bw.write("" + features.second.get(i).getAnswer().getId()
						+ "\t1\t"
						+ features.second.get(i).getAnswer().getAccuracy()
						+ "\t" + test.getType().values[(int) l] + "\n");
				total++;
			}
			System.out.println(correct / total);
			bw.flush();
			bw.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("Done!!!!!");

	}

	private static FilteredClassifier loadClassifier(String address)
			throws ClassNotFoundException, IOException {
		@SuppressWarnings("resource")
		ObjectInputStream ois = new ObjectInputStream(new FileInputStream(
				new File(address)));
		return (FilteredClassifier) ois.readObject();
	}

	public static void run(AbstractCorpus[] corpora, QASet train, QASet test,
			AbstractRelatednessScorer[] scorers, String output) {
		RelatednessEngine en = new RelatednessEngine(scorers);
		en.load();
		en.train(corpora, train);
		en.save();
		Pair<String[], List<FeatureSet>> features = en.score(test);
		try {
			CSVWriter.write(features.first, features.second, scorers, output);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


}
