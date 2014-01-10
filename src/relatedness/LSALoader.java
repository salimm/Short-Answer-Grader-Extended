package relatedness;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;
import java.lang.Math;

public class LSALoader {

	private HashMap<String,Vector<Double>> dictionary;
	private Vector<Double> emptyVector; 
	
	public LSALoader(File vectors) throws IOException {
		// Initializes the empty vector
		this.emptyVector = new Vector<Double>(300);
		for (int i = 0; i < 300; i++) {
			emptyVector.add(new Double(0));
		}
		
		// Initializes the vectors in String
		List<String> strings = new ArrayList<>();
		BufferedReader reader = new BufferedReader(new FileReader(vectors));
		String line;
		while((line = reader.readLine()) != null) {
			strings.add(line);
		}
		reader.close();
		
		//Populates the HashMap with the values
		this.dictionary = new HashMap<String,Vector<Double>>(strings.size());
		for (String string : strings) {
			String[] preliminary = string.split("\\|\\|\\|");
			if (preliminary.length == 2) {
				String key = preliminary[0];
				String[] intermediate = preliminary[1].split("\\|");
				if (intermediate.length == 300) {
					Vector<Double> values = new Vector<>();
					for (int i = 0; i < intermediate.length; i++) {
						values.add(new Double(intermediate[i]));
					}
					this.dictionary.put(key, values);
				}
			}
		}
	}
	
	public Vector<Double> getVector(String word) {
		if (this.dictionary.get(word) == null) {
			return this.emptyVector;
		} else {
			return this.dictionary.get(word);
		}
	}
	
	public Vector<Double> sumVectors(List<Vector<Double>> vectors) {
		
		// Initial check
		if (vectors == null || vectors.size() == 0) {
			return null;
		}
		int length = vectors.get(0).size();
		for (Vector<Double> vector : vectors) {
			if (vector.size() != length) {
				return null;
			}
		}
		
		// Summing the vectors
		Vector<Double> sum = new Vector<>(length);
		for (int i = 0; i < length; i++) {
			double temp = 0.0;
			for (Vector<Double> vector : vectors) {
				temp += vector.get(i);
			}
			sum.add(temp);
		}
		
		return sum;
	}
	
	public Vector<Double> sumTwoVectors(Vector<Double> x, Vector<Double> y) {
		if (x.size() != y.size()) {
			return null;
		} else {
			Vector<Double> z = new Vector<>(x.size());
			for (int i = 0; i < x.size(); i++) {
				z.add(x.get(i) + y.get(i));
			}
			return z;
		}
	}
	
	public double cosineSimilarity(Vector<Double> x, Vector<Double> y) {
		if (x.size() != y.size()) {
			return 0.0;
		}
		double similarity = 0.0;
		double numerator = 0.0;
		double denominator = 0.0;
		
		// Numerator
		for (int i = 0; i < x.size(); i++) {
			numerator += x.get(i) * y.get(i);
		}
		
		// Denominator
		double tempX = 0.0;
		double tempY = 0.0;
		for (int i = 0; i < x.size(); i++) {
			tempX += x.get(i) * x.get(i);
		}
		for (int i = 0; i < y.size(); i++) {
			tempY += y.get(i) * y.get(i);
		}
		tempX = Math.sqrt(tempX);
		tempY = Math.sqrt(tempY);
		denominator = tempX * tempY;
		
		// Similarity
		similarity = numerator / denominator;
		
		return similarity;
	}
	
}
