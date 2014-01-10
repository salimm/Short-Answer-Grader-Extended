package similarity;



import edu.cmu.lti.lexical_db.ILexicalDatabase;
import edu.cmu.lti.lexical_db.NictWordNet;
import edu.cmu.lti.ws4j.impl.Lin;

/**
 * 
 */
public class LinTest {
	public static void main(String[] args) {
		ILexicalDatabase db = new NictWordNet();
		Lin rc = new Lin(db);
		System.out.println(rc.calcRelatednessOfWords("help" + "#n", "help"
				+ "#n"));
		;
	}
}
