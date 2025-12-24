package net.carlo.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.carlo.Main;

/**
 * A helper class for processing text
 */
public class Text {
	
	/**
	 * Reads all lines from an InputStream 
	 * @param is the InputStream to read from
	 * @return The lines of text that were read from the input stream
	 * @throws IOException
	 */
	public static String[] readLines(InputStream is) throws IOException {
		List<String> lines = new ArrayList<String>();
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new InputStreamReader(is));
			String line;
			while ((line = reader.readLine()) != null) {
				lines.add(line);
			}
		} catch (IOException e) {
			throw e;
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					// Swallow
				}
			}
		}
		return lines.toArray(new String[lines.size()]);
	}
	
	/**
	 * Loads all lines of text from an embedded resource.
	 * @param path The path of the embedded resource, relative to the Main class
	 * @return The lines of text or null
	 */
	public static String[] loadResourceText(String path) {
		if (path == null) {
			return null;
		}
		InputStream is = Main.class.getResourceAsStream(path);
		if (is == null) {
			return null;
		}
		try {
			return readLines(is);
		} catch (IOException e) {
			return null;
		}
	}
	
	/**
	 * A wrapper method for loading the KJV bible.
	 * @return The lines of text from the embedded resource
	 */
	public static String[] loadBible() {
		return loadResourceText("res/kjv.txt");
	}
	
	/**
	 * Loads a text from a URL resource.
	 * @param path The URL to load a resource from
	 * @return
	 */
	public static String[] loadURLText(String path) {
		InputStream is = null;
		try {
			
			URL _url = new URL(path);
			HttpURLConnection con = (HttpURLConnection)_url.openConnection();
			con.setRequestMethod("GET");
			con.setConnectTimeout(5000);
			con.setReadTimeout(5000);
			
			is = con.getInputStream();
			
			return readLines(is);
		} catch (IOException e) {
			return null;
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (IOException e) {
					// Swallow
				}
			}
		}
	}
	
	/**
	 * Loads all lines of text from a file on the file system
	 * @param path The path of the file to load
	 * @return The lines of text in the file.
	 */
	public static String[] loadFileText(String path) {
		try {
			return readLines(new FileInputStream(new File(path)));
		} catch (IOException e) {
			return null;
		}
	}
	
	/**
	 * Performs re-weighting of texts to modify the amount of potential edges.
	 * 
	 * @param corpuses An ArrayList of the lines in the text.
	 * @param weight The desired weights for the texts
	 * @return A combined corpus of all the texts with their weights applied
	 */
	public static String[] createWeightedCorpus(ArrayList<String[]> corpuses, ArrayList<Double> weight) {
		ArrayList<String> corpus = new ArrayList<String>();
		if (weight.isEmpty() || corpuses.size() == 1) {
			for (int i = 0; i < corpuses.size(); i++) {
				String[] corp = corpuses.get(i);
				if (corp.length == 0) {
					continue;
				}
				Collections.addAll(corpus, corp);
			}
		} else {
			for (int i = weight.size(); i < corpuses.size(); i++) {
				weight.add(1.0D);
			}
			int max = corpuses.get(0).length;
			for (int i = 1; i < corpuses.size(); i++) {
				int t = corpuses.get(i).length;
				if (t > max) {
					max = t;
				}
			}
			for (int i = 0; i < corpuses.size(); i++) {
				String[] corp = corpuses.get(i);
				if (corp.length == 0) {
					continue;
				}
				if (weight.get(i) == 1.0D && corp.length == max) {
					Collections.addAll(corpus, corp);
				} else {
					int wlength = (int) Math.round(weight.get(i) * max);
					// There is nothing wrong with this, it does exactly
					// the same things as the for loop version. The for
					// loop version is a bit more readable
					/*while (wlength > corp.length) {
						Collections.addAll(corpus, corp);
						wlength -= corp.length;
					}
					int[] idx = Rand.autoSelect(corp.length, wlength);
					for (int j = 0; j < idx.length; j++) {
						corpus.add(corp[idx[j]]);
					}*/
					
					// Same thing as above
					int full = wlength / corp.length;
					for (int j = 0; j < full; j++) {
						Collections.addAll(corpus, corp);
					}
					int[] idx = Rand.autoSelect(corp.length, wlength % corp.length);
					for (int j = 0; j < idx.length; j++) {
						corpus.add(corp[idx[j]]);
					}
				}
			}
		}
		return corpus.toArray(new String[corpus.size()]);
	}
}
