package net.carlo.util;

/**
 * A String helper class for functionality that is not built into the string class in java
 * @see String
 */
public class Strings {

	/**
	 * Characters that can safely be removed without affecting the meaning of text.
	 */
	public static String[] UNNECESSARY = new String[] {
		"[", "]", ":", "(", ")", ":", "—"
	};
	
	/**
	 * In order to increase the potential edges punctuation can be removed.
	 */
	public final static String[] PUNCTUATION = new String[] {
		";", ",", ".", "?", "!"
	};
	
	/**
	 * Removes all occurrences of each needle from the haystack.
	 * @param haystack The string to search
	 * @param needle The strings that should be removed from the haystack.
	 * @return
	 */
	public static String remove(String haystack, String... needle) {
		if (needle == null || needle.length == 0) {
			return haystack;
		}
		for (int i = 0; i < needle.length; i++) {
			haystack = haystack.replace(needle[i], "");
		}
		return haystack;
	}
	
	/**
	 * Creates a joined string from a subset of an array of strings
	 * @param separator
	 * @param word
	 * @param start
	 * @param length
	 * @return
	 * @See {@link String#join(CharSequence, CharSequence...)}
	 */
	public final static String join(String separator, String[] word, int start, int length) {
		if (word == null || word.length < 1 || length < 1) {
			return null;
		}
		if (word.length == 1) {
			return word[0];
		}
		StringBuilder sb = new StringBuilder();
		sb.append(word[start]);
		for (int i = start + 1; i < start + Math.min(length, word.length); i++) {
			sb.append(separator);
			sb.append(word[i]);
		}
		return sb.toString();
	}
}
