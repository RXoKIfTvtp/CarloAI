package net.carlo.ai;

/**
 * This class is used to return the result of a text generation attempt
 */
public class Result {
	
	/**
	 * The generated text
	 */
	public final String text;
	/**
	 * The word count of the generated text. This may be different than the
	 * desired target length even if the ending is graceful
	 */
	public final int words;
	/**
	 * A graceful ending is when the ending n-gram is one that naturally appeared in the corpus
	 * It is possible for generations to prematurely end.
	 */
	public final boolean gracefulEnding;
	/**
	 * How many-th attempts were used in attempting to generate text with a graceful ending
	 */
	public final int tries;
	
	public Result(String text, int words, boolean gracefulEnding, int tries) {
		this.text = text;
		this.words = words;
		this.gracefulEnding = gracefulEnding;
		this.tries = tries;
	}
}
