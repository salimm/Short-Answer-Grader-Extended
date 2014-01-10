package similarity;

import edu.cmu.lti.ws4j.impl.JiangConrath;
import edu.cmu.lti.ws4j.impl.LeacockChodorow;
import edu.cmu.lti.ws4j.impl.Lesk;
import edu.cmu.lti.ws4j.impl.Lin;
import edu.cmu.lti.ws4j.impl.Resnik;
import edu.cmu.lti.ws4j.impl.WuPalmer;

/**
 * Different similarity methods.
 * 
 * @author Salim
 * 
 */
public enum SimilarityMethod {
	HIRST_ST_ONGE(Lin.class), LEACOCK_CHODOROW(LeacockChodorow.class), LESK(
			Lesk.class), WU_PALMER(WuPalmer.class), RESNIK(Resnik.class), JIANG_CONRATH(
			JiangConrath.class), LIN(Lin.class);

	public Class<?> rc;

	SimilarityMethod(Class<?> rc) {
		this.rc = rc;
	}

}
