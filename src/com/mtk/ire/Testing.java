package com.mtk.ire;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class Testing {
	public static void main(String[] args) {
		HashMap<Integer, Double> map = new HashMap<Integer, Double>();
		ValueComparator bvc = new ValueComparator(map);
		TreeMap sorted_map = new TreeMap(bvc);

		map.put(123, 99.5);
		map.put(12, 7.4);
		map.put(23, 67.4);
		map.put(126, 67.3);

		System.out.println("unsorted map: " + map);
		sorted_map.putAll(map);
		System.out.println("results: " + sorted_map);
		for(Object i : sorted_map.keySet()) {
			System.out.println(i + " " + sorted_map.get(i));
		}
	}
}

class ValueComparator implements Comparator {
	Map base;

	public ValueComparator(Map base) {
		this.base = base;
	}

	// Note: this comparator imposes orderings that are inconsistent with
	// equals.
	public int compare(Object a, Object b) {
		if ((Double)base.get(a) >= (Double)base.get(b)) {
			return -1;
		} else {
			return 1;
		} // returning 0 would merge keys
	}
}
