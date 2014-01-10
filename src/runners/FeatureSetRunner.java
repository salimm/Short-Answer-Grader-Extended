package runners;
import io.parse.QAParser;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import data.QASet;

public class FeatureSetRunner {
	public static void main(String[] args) throws ParserConfigurationException,
			SAXException, IOException, InstantiationException,
			IllegalAccessException, IllegalArgumentException,
			InvocationTargetException, NoSuchMethodException, SecurityException {
		QAParser p = new QAParser();
		QASet test = p.parse(new File("data/train/beetle"));
		Runner.run(null, test, test, DefaultScorerList.DEFAULT_SCORERS(),
				"feature-set.csv");
	}
}
