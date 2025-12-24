package net.carlo.ai;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import net.carlo.util.Rand;
import net.carlo.util.Strings;

/**
 * This class represents an n-gram model.
 */
public class GuidedNGramModel {

	/**
	 * The character to use to separate n-gram words when stored in the transition or end maps.
	 */
	public final static String KEY_SEPARATOR = " ";
	
	/**
	 * A list of n-grams that naturally appear at the start of a line
	 * in the texts.
	 */
	private final ArrayList<String> start = new ArrayList<String>();
	
	/**
	 * A list of all n-grams in the text.
	 */
	private final HashMap<String, List<String>> transition = new HashMap<String, List<String>>();
	
	/**
	 * A list of last n-grams, one's that appear at the end of a line.
	 */
	private final HashMap<String, List<String>> end = new HashMap<String, List<String>>();
	
	/**
	 * This shortest amount of n-grams for a given key, to reach a graceful end.
	 * 
	 */
	private final HashMap<String, Integer> distance = new HashMap<String, Integer>();
	
	/**
	 * The size of the n-grams used in this model
	 */
	private final int n;
	
	/**
	 * 
	 * @param n
	 */
	public GuidedNGramModel(int n) {
		// An n-gram size of less than 2 can't be used to generate text
		if (n < 2) {
			n = 2;
		}
		this.n = n;
	}
	
	/**
	 * A helper function to get or initialize, then get a transition list
	 * @param lut The HashMap used to look up the transitions list in
	 * @param key The key of the list to get from the lut HashMap
	 * @return The list for the key, if no list exists it is created
	 * @see HashMap
	 * @see List
	 * @see String
	 */
	private List<String> getOrCreateList(HashMap<String, List<String>> lut, String key) {
		List<String> list = lut.getOrDefault(key, null);
		if (list == null) {
			list = new ArrayList<String>();
			lut.put(key, list);
		}
		return list;
	}
	
	/**
	 * Builds the guided n-gram model from the given lines of text
	 * @param lines The lines of text to build the n-gram model from
	 */
	public void train(String... lines) {
		if (lines == null) {
			return;
		}
		for (int l = 0; l < lines.length; l++) {
			String line = lines[l];
			if (line == null || (line = line.trim()).isEmpty()) {
				continue;
			}
			
			// Normalize line
			line = line.toLowerCase();
			line = Strings.remove(line, Strings.PUNCTUATION);
			line = Strings.remove(line, Strings.UNNECESSARY);

			// Get all n-grams from the line
			String[][] grams = Ngram.gramsFromLine(line, n);
			if (grams.length < 1) {
				continue;
			}
			
			for (int i = 0; i < grams.length; i++) {
				String key = Strings.join(KEY_SEPARATOR, grams[i], 0, n - 1);
				
				// Store n-gram
				String value = grams[i][n - 1];
				getOrCreateList(transition, key).add(value);
				
				// Distance to end
				int dst = (grams.length - (i));
				int t = distance.getOrDefault(key, Integer.MAX_VALUE);
				if (dst < t) {
					distance.put(key, dst);
				}
				
				// If the n-gram is the first, store it in the start List
				// If the n-gram is the last, store it in the end List
				if (i == 0) {
					start.add(key);
				} else if (i == grams.length - 1) {
					getOrCreateList(end, key).add(value);
				}
			}
		}
	}
	
	/**
	 * Filters a list of possible transitions.
	 * If the distance-to-an-end is too far for a candidate then it is removed
	 * as a transition option for the current generation.
	 * @param prefix The current prefix to explore transitions for
	 * @param candidate The list of possible transition candidates
	 * @return
	 */
	private List<String> guideTowardsEnd(String prefix, List<String> candidate) {
		if (candidate == null) {
			return Collections.emptyList();
		}
		
		Integer currentDist = distance.get(prefix);
		if (currentDist == null) {
			return candidate;
		}
		
		// A list of transitions that have a minimum distance to a graceful ending that is
		// less than or equal to the target
		List<String> good = new ArrayList<String>();
		
		for (String next : candidate) {
			List<String> prefixWords = new ArrayList<>(Arrays.asList(prefix.split("\\s+")));
			prefixWords.remove(0);
			prefixWords.add(next);
			String nextPrefix = String.join(KEY_SEPARATOR, prefixWords);
			
			Integer nextDist = distance.get(nextPrefix);
			
			if (nextDist != null && nextDist <= currentDist) {
				good.add(next);
			}
		}
		
		// If all transitions are larger than the target, do not filter the list
		if (good.isEmpty()) {
			return candidate;
		}
		
		return good;
	}
	
	/**
	 * Attempts to generate a text with a given target length.
	 * The resulting text may be of different length than the target.
	 * @param length The target length to aim for during generation
	 * @return The Result of the generation attempt
	 * @see Result
	 */
	public Result generate(int length) {
		if (transition.isEmpty()) {
			return new Result("", 0, false, 0);
		}
		
		// Select a random starting n-gram
		String prefix = start.get(Rand.nextInt(start.size()));
		List<String> output = new ArrayList<String>(Arrays.asList(prefix.split("\\s+")));
		
		// Has a graceful ending been reached?
		boolean gracefulEnding = false;
		
		while (true) {
			// Check if we have reached the target length and whether or
			// nor the ending is graceful
			if (end.containsKey(prefix) && output.size() >= length - 1) {
				List<String> finals = end.get(prefix);
				
				// If the final transition contains any words, select a random one
				if (finals.isEmpty() == false) {
					output.add(finals.get(Rand.nextInt(finals.size())));
				}
				gracefulEnding = true;
				break;
			}
			
			// Get the list of transitions from the transition HashMap
			List<String> nextWords = transition.get(prefix);
			
			// If it does not contain any transitions we have reached a dead
			// end and the ending was not graceful
			if (nextWords == null || nextWords.isEmpty()) {
				break;
			}
			
			// If the length of the generated text is longer or equal to the
			// target length, filter the words to the guided end
			if (output.size() >= length) {
				nextWords = guideTowardsEnd(prefix, nextWords);
			}
			
			// Select a random transition word as the next word from the transition list
			output.add(nextWords.get(Rand.nextInt(nextWords.size())));
			
			// Update the prefix for the next iteration
			prefix = String.join(KEY_SEPARATOR, output.subList((output.size() - (n - 1)), output.size()));
		}
		return new Result(String.join(KEY_SEPARATOR, output), output.size(), gracefulEnding, 1);
	}
	
	/**
	 * Attempts to generate a text with a given target length.
	 * The resulting text may be of different length than the target.
	 * If the ending was not graceful generation is attempted again,
	 * up to tries amount of times. If no graceful ending is reached during
	 * any of the attempts then the best attempt is returned.
	 * @param length The target length to aim for during generation
	 * @param tries The amount of times to attempt to generate a text with a graceful ending
	 * @return The best generation result.
	 */
	public Result generate(int length, int tries) {
		if (tries < 1) {
			tries = 1;
		}
		Result best = null;
		Result result;
		for (int attempt = 1; attempt <= tries; attempt++) {
			result = generate(length);
			if (result.gracefulEnding) {
				return new Result(result.text, result.words, result.gracefulEnding, attempt);
			}
			//if (best == null || result.words > best.words && Math.abs(result.words - length) < Math.abs(best.words - length)) {
			if (best == null || result.words > best.words) {
				best = result;
			}
		}
		return new Result(best.text, best.words, best.gracefulEnding, tries);
	}
}
