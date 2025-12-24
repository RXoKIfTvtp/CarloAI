package net.carlo;

import java.io.IOException;
import java.util.ArrayList;

import net.carlo.ai.GuidedNGramModel;
import net.carlo.util.Text;

public class Main {
	
	/**
	 * Loads the help text from an embedded text file resource.
	 * @return The help text of the embedded resource
	 */
	private static String help() {
		return String.join(
			System.getProperty("line.separator"),
			Text.loadResourceText("res/help.txt")
		);
	}
	
	public static void main(String[] args) {
		// Program arguments with their default values
		ArrayList<String[]> texts = new ArrayList<String[]>();
		ArrayList<Double> weight = new ArrayList<Double>();
		int ngramSize = 3;
		int length = 12;
		int count = 1;
		int retries = 100;
		
		// Parse optional arguments and set variables
		for (int i = 0; i < args.length; i++) {
			String flag = args[i].toLowerCase();
			try {
				if (flag.equals("-g") || flag.equals("-ng") || flag.equals("-ngram")) {
					ngramSize = Integer.parseInt(args[++i]);
				} else if (flag.equals("-l") || flag.equals("-len") || flag.equals("-length")) {
					length = Integer.parseInt(args[++i]);
				} else if (flag.equals("-c") || flag.equals("-count")) {
					count = Integer.parseInt(args[++i]);
				} else if (flag.equals("-r") || flag.equals("-retries")) {
					retries = Integer.parseInt(args[++i]);
				} else if (flag.equals("-b") || flag.equals("-bible")) {
					String[] t = (Text.loadBible());
					if (t == null) {
						System.err.println("Failed to load embedded resource.");
						return;
					}
					texts.add(t);
				} else if (flag.equals("-f") || flag.equals("-file")) {
					String[] t = (Text.loadFileText(args[++i]));
					if (t == null) {
						System.err.println("Failed to load file " + args[i]);
						return;
					}
					texts.add(t);
				} else if (flag.equals("-u") || flag.equals("-url")) {
					String[] t = (Text.loadURLText(args[++i]));
					if (t == null) {
						System.err.println("Failed to load URL resource.");
						return;
					}
					texts.add(t);
				} else if (flag.equals("-t") || flag.equals("-text")) {
					String[] t = (args[++i].split("\\s+"));
					if (t == null) {
						System.err.println("Failed to load text.");
						return;
					}
					texts.add(t);
				} else if (flag.equals("-w") || flag.equals("-weight")) {
					weight.add(Double.parseDouble(args[++i]));
				} else if (flag.equals("-stdin")) {
					try {
						if (System.in.available() > 0) {
							texts.add(Text.readLines(System.in));
						}
					} catch (IOException e) {
						System.err.println("Error reading stdin: " + e.getMessage());
					}
				} else if (flag.equals("-h") || flag.equals("-help")) {
					System.out.println(help());
					return;
				}
			} catch (NumberFormatException e) {
				System.err.println("\"" + args[i] + "\" is not a valid argument for " + args[i - 1]);
				return;
			} catch (IndexOutOfBoundsException e) {
				System.err.println("Expected parameter after " + args[i - 1]);
				return;
			}
		}
		
		// If no text was specified attempt to read from standard input stream
		if (texts.isEmpty()) {
			try {
				if (System.in.available() > 0) {
					texts.add(Text.readLines(System.in));
				}
			} catch (IOException e) {
				System.err.println("Error reading stdin: " + e.getMessage());
			}
		}
		
		// If input text is still empty, no text can be generated
		// Print help or error
		if (texts.isEmpty()) {
			if (args.length == 0) {
				System.out.println(help());
			} else {
				System.err.println("No text was specified!");
			}
			return;
		}
		
		// It's not possible to generate text if the n-gram size is less than 2
		if (ngramSize < 2) {
			System.err.println("Warning: ngram size set to 2.");
			ngramSize = 2;
		}
		
		// a length of less than 1 would just result in blank lines
		if (length < 1) {
			System.err.println("Warning: length set to 1.");
			length = 1;
		}
		
		// if the length is less than the n-gram size then initialization would already exceed the target
		if (length < ngramSize) {
			System.err.println("Warning: length is less than " + ngramSize + ".");
		}
		
		// If the count of lines is less than 1, no lines would be generated
		if (count < 1) {
			System.err.println("Warning: count set to 1.");
			count = 1;
		}
		
		// Combine all the texts and re-weight all the texts if weights are specified.
		String[] corpus = Text.createWeightedCorpus(texts, weight);

		// It's possible that the files are empty
		// If the files don't contain any (or enough) text on a line to make an n-gram then
		// It's effectively blank
		if (corpus.length == 0) {
			System.err.println("The specified text" + (texts.size() == 1 ? " does't" : "s don't") + " contain any lines of text!");
			return;
		}
		
		// Create an n-gram model of ngramSize
		GuidedNGramModel gngc = new GuidedNGramModel(ngramSize);
		
		// Build the n-gram model using the aggregate corpus
		gngc.train(corpus);
		
		// Generate count lines of text
		for (int i = 0; i < count; i++) {
			System.out.println(gngc.generate(length, retries).text);
		}
	}
}
