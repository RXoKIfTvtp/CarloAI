package net.carlo.ai;

/**
 * A class for generating n-grams from a line of text.
 */
public class Ngram {

	/**
	 * Processes a line of text into an array of n-grams.
	 * @param line The line of text to process in to n-grams
	 * @param ngramSize The size each n-gram should be
	 * @return An array of all n-grams that appear in the text.
	 * 			The returned array is in the same order as the
	 * 			n-grams appeared in the line and duplicates are
	 * 			not removed!
	 */
	public static String[][] gramsFromLine(String line, int ngramSize) {
		String[] words = line.split("\\s+");
		int grams = words.length - ngramSize + 1;
		
		if (grams <= 0) {
			return new String[0][];
		}
		
		String[][] ret = new String[grams][ngramSize];
		
		for (int i = 0; i < grams; i++) {
			ret[i] = new String[ngramSize];
			for (int j = 0; j < ngramSize; j++) {
				ret[i][j] = words[i + j];
			}
		}
		return ret;
	}
}
