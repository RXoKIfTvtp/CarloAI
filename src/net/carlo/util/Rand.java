package net.carlo.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Supplier;

/**
 * A helper class for performing random selection and random number generation
 */
public class Rand {
	
	/**
	 * Generates a random integer between 0 and length - 1 (both inclusive).
	 * This method is thread safe.
	 */
	public static int nextInt(int length) {
		return ThreadLocalRandom.current().nextInt(length);
	}
	
	/**
	 * Generates a random double between 0 (inclusive) and 1 (exclusive).
	 * This method is thread safe.
	 */
	public static double nextDouble() {
		return ThreadLocalRandom.current().nextDouble();
	}
	
	/**
	 * Partial Durstenfeld Shuffle
	 * @param l The size of the list to select from
	 * @param n The count of indexes to select from an l size list
	 * @return an array of random indices from l
	 * @see https://en.wikipedia.org/wiki/Fisher%E2%80%93Yates_shuffle#JavaScript_implementation
	**/
	public static int[] select(int l, int n) {
		if (n < 1) {
			return new int[0];
		}
		if (n > l) {
			n = l;
		}
		
		int[] b = new int[l];
		int[] r = new int[n];
		int i;
		
		for (i = 0; i < b.length; i++) {
			b[i] = i;
		}
		
		int j;
		for (i = 0; i < n; i++) {
			j = i + nextInt(l - i);
			r[i] = b[j];
			b[j] = b[i];
		}
		
		return r;
	}
	
	/**
	 * A thread safe, re-useable buffer.
	 */
	private final static ThreadLocal<ArrayList<Integer>> bufI = ThreadLocal.withInitial(new Supplier<ArrayList<Integer>>() {
		@Override
		public ArrayList<Integer> get() {
			return new ArrayList<Integer>();
		}
	});
	
	/**
	 * Partial Durstenfeld Shuffle with reused buffers
	 * @param l The size of the list to select from
	 * @param n The count of indexes to select from an l size list
	 * @return an array of random indices from l
	 * @see https://en.wikipedia.org/wiki/Fisher%E2%80%93Yates_shuffle#JavaScript_implementation
	**/
	public static int[] select2(int l, int n) {
		if (n < 1) {
			return new int[0];
		}
		if (n > l) {
			n = l;
		}
		
		ArrayList<Integer> b = bufI.get();
		int[] r = new int[n];
		int i;
		
		b.clear();
		b.ensureCapacity(l);
		//for (i = 0; i < b.length; i++) {
		for (i = 0; i < l; i++) {
			//b[i] = i;
			b.add(i);
		}
		
		int j;
		for (i = 0; i < n; i++) {
			j = i + nextInt(l - i);
			//r[i] = b[j];
			r[i] = b.get(j);
			//b[j] = b[i];
			b.set(j, b.get(i));
		}
		
		return r;
	}
	
	/**
	 * A thread safe, re-useable buffer.
	 */
	private final static ThreadLocal<HashMap<Integer, Integer>> bufH = ThreadLocal.withInitial(new Supplier<HashMap<Integer, Integer>>() {
		@Override
		public HashMap<Integer, Integer> get() {
			return new HashMap<Integer, Integer>();
		}
	});
	
	/**
	 * Sparse Partial Durstenfeld Shuffle
	 * @param l The size of the list to select from
	 * @param n The count of indexes to select from an l size list
	 * @return an array of random indices from l
	 * @see https://en.wikipedia.org/wiki/Fisher%E2%80%93Yates_shuffle#JavaScript_implementation
	**/
	public static int[] sparseSelect(int l, int n) {
		if (n < 1) {
			return new int[0];
		}
		if (n > l) {
			n = l;
		}
		
		HashMap<Integer, Integer> b = bufH.get();
		b.clear();
		int[] r = new int[n];
		
		int i, j, t;
		for (i = 0; i < n; i++) {
			j = i + nextInt(l - i);
			t = b.getOrDefault(i, i);
			r[i] = b.getOrDefault(j, j);
			b.put(j, t);
		}
		
		return r;
	}
	
	/**
	 * Selects n amount of items from a list of size n.
	 * Used to randomly select unique indices without duplicates.
	 * Automatically selects the fastest select implementation based on the values of l and n
	 * @param l the size of the list of select from.
	 * @param n the amount of indices to select from the list. Can not be larger than l
	 * @return an array of random indices from l
	 */
	public static int[] autoSelect(int l, int n) {
		if (n < 1) {
			return new int[0];
		}
		if (n > l) {
			n = l;
		}
		
		if (l <= 512) {
			return select(l, n);
		} else if (n < (l >> 3)) { // same as (l / 8)
			return sparseSelect(l, n);
		} else {
			return select2(l, n);
		}
	}
}
