package samples;

import java.io.IOException;

import tools.Spelling;

/**
 * A simple sample to show how to do spell checking
 * 
 * @author Salim
 * 
 */
public class SpellChecking {
	public static void main(String[] args) throws IOException {
		Spelling s = new Spelling("data/big.txt");
		System.out.println(s.correct("natonal"));
	}
}
