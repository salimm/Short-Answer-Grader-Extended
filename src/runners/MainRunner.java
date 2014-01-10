package runners;
import io.parse.QAParser;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;
import data.QASet;
import relatedness.AbstractRelatednessScorer;
import weka.classifiers.trees.RandomForest;

public class MainRunner {
	public static void main(String[] args) throws ParserConfigurationException,
			SAXException, IOException, InstantiationException,
			IllegalAccessException, IllegalArgumentException,
			InvocationTargetException, NoSuchMethodException,
			SecurityException, ClassNotFoundException {
		String runner = "crossvalidation";
		String train = "data/train/beetle";
		String test = "data/train/beetle";
		String out = "out";
		String model = "finalmodel.model";

		for (int i = 0; i < args.length; i += 2) {
			if (args[i].equals("-r")) {
				runner = args[i + 1];
			} else if (args[i].equals("-t")) {
				train = args[i + 1];
			} else if (args[i].equals("-s")) {
				test = args[i + 1];
			} else if (args[i].equals("-o")) {
				out = args[i + 1];
			} else if (args[i].equals("-m")) {
				model = args[i + 1];
			} else {
				printHelp();
			}
		}
		AbstractRelatednessScorer[] scorers = DefaultScorerList
				.DEFAULT_SCORERS();

		QAParser p = new QAParser();
		if (runner.equals("crossvalidation")) {
			QASet trainset = p.parse(new File(train));
			Runner.run(null, trainset, scorers, new RandomForest());
		} else if (runner.equals("trainer")) {
			QASet trainset = p.parse(new File(train));
			Runner.train(null, trainset, scorers, out);
		} else if (runner.equals("evaluate")) {
			QASet trainset = p.parse(new File(train));
			Runner.finalEvaluate(trainset, scorers, model, out);
		} else if (runner.equals("featureset")) {
			QASet testset = p.parse(new File(test));
			Runner.run(null, testset, testset, scorers, out);
		} else {
			printHelp();
		}

	}

	private static void printHelp() {
		System.out.println("HELP:");
		System.out.println("\t Java -jar baseline.jar -r [");
		System.out.println("\t\t crossvalidation -t <train-test-data-path>|");
		System.out
				.println("\t\t trainer -t <path-to-train-dir> -o <path-to-output-model>|");
		System.out
				.println("\t\t evaluate -s <path-to-test-dir> -o <path-to-output-csv>|");
		System.out
				.println("\t\t featureset -t <path-to-data-dir> -o <path-to-csv-output>");
		throw new Error(" Wrong Arguments...");
	}
}
