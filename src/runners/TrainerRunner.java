package runners;
import io.parse.QAParser;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;
import data.QASet;

public class TrainerRunner {
	public static void main(String[] args) throws ParserConfigurationException,
			SAXException, IOException, InstantiationException,
			IllegalArgumentException, InvocationTargetException,
			NoSuchMethodException, SecurityException, ClassNotFoundException,
			IllegalAccessException {
		QAParser p = new QAParser();
		QASet train = p.parse(new File("data/train/beetle/"));
		Runner.train(null, train, DefaultScorerList.DEFAULT_SCORERS(),
				"finalmodel.model");
	}
}
