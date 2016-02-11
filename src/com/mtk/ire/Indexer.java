package com.mtk.ire;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

public class Indexer {
	static int counter=1;

	static Map<String, TreeMap<Long, Indexer.Ocurrences>> words = new HashMap<String, TreeMap<Long, Indexer.Ocurrences>>();

	private static String outfile;

	static class Ocurrences {
		int bodyCount, titleCount, infoBoxCount, categoryCount, refCount, externalLinkCount;
		static StringBuilder sb = null; 
		@Override
		public String toString() {
			sb = new StringBuilder();
			if (bodyCount > 0) sb.append("b" + bodyCount);
			if (titleCount > 0) sb.append("t" + titleCount);
			if (infoBoxCount > 0) sb.append("i" + infoBoxCount);
			if (categoryCount > 0) sb.append("c" + categoryCount);
			if (refCount > 0) sb.append("r" + refCount);
			if (externalLinkCount > 0) sb.append("e" + externalLinkCount);
			if (sb.length() > 0) sb.append(";");
			return sb.toString();
		}

		public long getTotal() {
			return bodyCount + titleCount + infoBoxCount + categoryCount + refCount + externalLinkCount;
		}

		static void updateOccurrentObj(Indexer.Ocurrences oc, LOCATION e) {
			switch (e) {
				case TITLE:
					oc.titleCount++;
					break;
				case REF:
					oc.refCount++;
					break;
				case BODY:
					oc.bodyCount++;
					break;
				case INFOBOX:
					oc.infoBoxCount++;
					break;
				case CATEGORY:
					oc.categoryCount++;
					break;
				case EXTERNAL_LINKS:
					oc.externalLinkCount++;
					break;
			}
		}
	}

	static BufferedWriter bw = null;
	static SortedSet<String> keys = null;
	static TreeMap<Long, Indexer.Ocurrences> value = null; 
	public static void writeIndexToFile() {
		try {
			bw = new BufferedWriter(new FileWriter(outfile + counter));
			Map<String, TreeMap<Long, Indexer.Ocurrences>> ind = words;
			keys = new TreeSet<String>(ind.keySet());
			for (String k : keys) {
				/* Type1 Index */
				value = ind.get(k);
				bw.write(k + ":");// + value.size() +":");
				for (Long id : value.keySet()) {
					bw.write(id + "-" + value.get(id) + "");
				}
				bw.write("\n");
				/* Type2 Index */
				/*
				 * bw.write(k + ":");
				 * TreeMap<Long, Indexer.Ocurrences> value = ind.get(k);
				 * StringBuilder sb = new StringBuilder();
				 * long totalCnt =0;
				 * Indexer.Ocurrences o = null;
				 * bw.write(value.size() +":");
				 * for (Long id : value.keySet()) {
				 * o = value.get(id);
				 * totalCnt += o.getTotal();
				 * // sb.append();
				 * }
				 */
			}
			Log.i("Index written to file [" + (outfile +counter)+ "] word-count [" + words.size() + "]");
			counter++;
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				bw.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

	public static void setOutFile(String indexOutfile) {
		Indexer.outfile = indexOutfile;
	}

}
