package com.mtk.ire;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;

public class QueryProcessor {
	
	static long N = 16299475L;
	static ArrayList<QueryWord> qw = new ArrayList<QueryWord>();
	static class QueryWord {
		public QueryWord(String w2, Long idf2) {
			w = w2; idf = idf2;
		}
		public String w;
		long idf;
		public String toString() {
			return w + " " + idf;
		}
	}
	static IdfSorter idfSorter = new IdfSorter();
	static class IdfSorter implements Comparator<QueryWord>{
		@Override
		public int compare(QueryWord o1, QueryWord o2) {
			if(o1.idf > o2.idf) return 1;
			else if(o1.idf < o2.idf) return -1;
			else return 0;
		}
		
	}
	static class Log {
		static BufferedWriter bw = null;

		static {
			try {
				bw = new BufferedWriter(new FileWriter("/home/mtk/wikiSearchEngine.log"));
				bw.write("Wiki Search Engine and Query Processing starting " + (new Date()).toString() + "\n");
			} catch (Exception e) {
				System.err.println("Exception in initializing wiki search engine");
				e.printStackTrace();
			}
		}
	}

	static TreeMap<String, Long> secIndex = null;
	static TreeMap<Long, Long> titleSecIndex = null;

	static void initSearchEngine(String[] args) throws Exception {
		// readTeriataryIndex of mainIndex
		// readsecondaryIndex of titles
		secIndex = IndexGenerator.readSecondaryIndex();
		titleSecIndex = TitleIndexer.read2ndIndex();
		Log.bw.write("init done");
	}
	

	static class Page {
		Long did;
		String pos;
		long tf, idf;
		
		Double tf_idf;
		int[] counts = new int[6]; // title, body, category, extLinks, references, infobox
		
		public Page(Long d, long idff, String list) {
			did = d;
			idf = idff;
			pos = list;
			calculateTf();
			calculateTfIdf();
		}
		@Override
		public int hashCode() {
			return (int) (did%16000000);
		}
		@Override
		public boolean equals(Object obj) {
			return this.did.equals(((Page)obj).did);
		}
		private void calculateTf() {
			int num, j, i;
			for(i=0; i<pos.length();i++) {
				if( Character.isLetter(pos.charAt(i)) ) {
					num =0;
					j=i+1;
					while( j< pos.length() && Character.isDigit(pos.charAt(j))  )
						num = num*10 + Character.getNumericValue(pos.charAt(j++));
					switch(pos.charAt(i)) {
					case 'b': counts[0] = num*10; break;
					case 'c': counts[1] = num*50; break;
					case 'e': counts[2] = num; break;
					case 'i': counts[3] = num*20; break;
					case 'r': counts[4] = num; break;
					case 't': counts[5] = num*100; break;
					}
					
					i=j+1;
				}
			}
			int total=0;
			for(i=0; i<6; i++)
				total += counts[i];
			this.tf = total;
		}

		public void calculateTfIdf() {
			// tf-id : tf * Log(N/idf)
			tf_idf = tf * Math.log(N/idf);
		}
	}
	static TfIdSorter tfIdSorter = new TfIdSorter();
	static class TfIdSorter implements Comparator<Page> {
		public int compare(Page o1, Page o2) {
			if(o1.tf < o2.tf) return 1;
			else if(o1.tf > o2.tf) return -1;
			return 0;
		}
	}

	
	static List<Page> getSortedByTfAndGetList(String w, String pos, boolean rerun) {
//		List<Long> did = new ArrayList<Long>();
		List<Page> p = new ArrayList<Page>();
		Long idf = Long.parseLong(pos.split("\\|")[0]);
		if(!rerun)
			qw.add(new QueryWord(w, idf));

		String[] tokens = pos.split("\\|")[1].split(";"); // need one more split if documentFrequ is stored
		Long d;
		int i=0;
		String list = "";
		for(String t: tokens) {
			d = Long.parseLong(t.split("-")[0]); 
			list = t.split("-") [1];     
			p.add(new Page(d, idf, list));
		}
		Collections.sort(p,tfIdSorter);
		numOfResults = p.size();
		/*for(Page page : p) {
			did.add(page.did);
			i++;
			if(i>10) break;
		}*/
//		int limit = p.size();
//		if(limit > 10 ) limit = 10;
		return p; //.subList(0, limit);
	}

	

	static List<Page> singleWordQueryProcessor(String s, boolean rerun) {
		// get posting list, by some formula
		List<Page> dids = new ArrayList<Page>();
		if (s.indexOf(":") > 0) { // field query detected

		} else {
			try {
				Long firstSeekPos = secIndex.floorEntry(s).getValue(), nextSeekPos;
//				System.out.println("firstSeekPos " + firstSeekPos);
				String primIndexFile = "/home/mtk/ireIndex/merged-prod1-300.justTerms";
				RandomAccessFile raf = new RandomAccessFile(primIndexFile, "r");
				raf.seek(firstSeekPos);
				String tmp;
				int j=0;
				while (true) {
					tmp = raf.readLine();
					if(j>4000) return null;
					j++;
					// System.out.println(tmp);
					if (tmp.split("-")[0].equals(s)) {
						nextSeekPos = Long.parseLong(tmp.split("-")[1]);
//						System.out.println("Got " + nextSeekPos + " nextSeekPos from secondary index");
						break;
					}
				}

				String posList = "/home/mtk/ireIndex/merged-prod1-300.justPostingList";
				raf = new RandomAccessFile(posList, "r");
//				System.out.println("nextSeekPos " + nextSeekPos);
				raf.seek(nextSeekPos);
				tmp = raf.readLine();
//				System.out.println(tmp);
//				tmp = tmp.split("\\|")[1];
				dids = getSortedByTfAndGetList(s, tmp, rerun);
				
				/*String[] toks = (tmp.split("\\|")[1]).split(";");
				for (String p : toks) {
					// System.out.println(p);
					dids.add(Long.parseLong(p.split("-")[0]));
				}*/
//				System.out.println("dids " + dids);
			} catch (Exception e) {
				e.printStackTrace();
			}

		}
//		int limit = dids.size();
//		if(dids.size() > 10) limit = 10;
		return dids; //.subList(0, limit);
	}

	public static ArrayList<Page> intersection(ArrayList<Page> a,ArrayList<Page> b){
		ArrayList<Page> result = new ArrayList<Page>();
		int i = 0;
	    int j = 0;
	    if(a == null || b == null) return null;
	    while(i < a.size() && j < b.size()){
	        if(a.get(i).did < b.get(j).did)
	            ++i;
	        else if (a.get(i).did > b.get(j).did)
	            ++j;
	        else{
	            result.add(a.get(i));
	            ++i;
	            ++j;
	        }
	    }
	    return result;		
	}

	static TfIdfSorter tfIdfSorter = new TfIdfSorter();
	static class TfIdfSorter implements Comparator<Page> {
		public int compare(Page o1, Page o2) {
			if(o1.tf < o2.tf) return 1;
			else if(o1.tf > o2.tf) return -1;
			return 0;
		}
	
	}
	static List<Page> multiWordQueryProcessor(String s) {
		ArrayList<Page> commonDocs = new ArrayList<Page>();
//		System.out.println(s + " len of Common docIds " + commonDocs.size() );
		if(commonDocs == null) commonDocs = new ArrayList<Page>();
		String[] words = s.split(" ");
		String[] toks = null;
		int flag=0;
		for(int i=words.length; i>0; i--) {
			flag=0;
			for(int j=0; j<=words.length-i; j++) {
				toks = new String[i];
//				System.out.println(" creating tok list from " +j + " of window " + i);
				for(int k=j; k<j+i; k++) {
					toks[k-j] = words[k];
//					System.out.println(toks[k-j]);
				}
				commonDocs= (ArrayList<Page>) multiWordQueryProcessor0(toks);
				if(commonDocs != null && commonDocs.size() > 0 ) { flag=1; break;}
			}
			if(flag == 1) break;
		}			
		
		if(commonDocs.size() == 0) {
//			for(QueryWord q : qw) System.out.println(q);
			Collections.sort(qw, idfSorter);
//			for(QueryWord q : qw) System.out.println(q);
			for (QueryWord q : qw) {
				commonDocs.addAll( (ArrayList<Page>) singleWordQueryProcessor(q.w, true) );
			}
		}
//		List<Page> al = new ArrayList<Page>();
		// add elements to al, including duplicates
		
		Set<Page> commonDocsHash = new HashSet<Page>();
		commonDocsHash.addAll(commonDocs);
		commonDocs.clear();
		commonDocs.addAll(commonDocsHash);
		Collections.sort(commonDocs, tfIdfSorter);
		numOfResults = commonDocs.size();
		int limit = commonDocs.size();
		if(limit > 10) limit = 10;
		return commonDocs.subList(0, limit);
	}
	
	static List<Page> multiWordQueryProcessor0(String[] words) {
//		String[] words = s.split(" ");
		ArrayList<Page> commonDocs = new ArrayList<Page>();
		ArrayList<Page> singleDocIds = null;
		int i=0;
		for (String w : words) {
			// get posting list, by some formula
			if(i==0) {
				commonDocs = (ArrayList<Page>) stemmedAndPlainSingleWordQueryProcessor(w);
				commonDocs = (ArrayList<Page>) singleWordQueryProcessor(w, false);
//				System.out.println(w + " len of docIds " + commonDocs.size() );
			}
			else {
				singleDocIds = (ArrayList<Page>) stemmedAndPlainSingleWordQueryProcessor(w);
//				singleDocIds = (ArrayList<Page>) singleWordQueryProcessor(w, false);
//				System.out.println(w + " len of docIds " + singleDocIds.size() );
			}
			if(i>0) {
				commonDocs = intersection(singleDocIds, commonDocs); 
			}
			i++;
		}
//		int limit = commonDocs.size();
//		if(limit > 10) limit = 10;
		return commonDocs;//.subList(0, limit);
	}

	static List<Page> wikiSearch(String s) throws Exception {
		if (s.split(" ").length == 1) { // single word query
//			return singleWordQueryProcessor(s, false);
			return stemmedAndPlainSingleWordQueryProcessor(s);
		} else if(s.indexOf("\"") >= 0){// phrase query, change to multiWord
			s=s.replaceAll("\"", " ").replaceAll("\\s+", " ");
			return multiWordQueryProcessor(s);
		} else { // multi word query
			return multiWordQueryProcessor(s);
		}
	}
	static IREUtils.Stemmer stemmer = new IREUtils.Stemmer();
	private static String getStem(String t) {
		stemmer.reset();
		stemmer.add(t.toCharArray(), t.length());
		stemmer.stem();
		return stemmer.toString();
	}

	private static List<Page> stemmedAndPlainSingleWordQueryProcessor(String s) {
		String stemmed = getStem(s);
		ArrayList<Page> out = (ArrayList<Page>) singleWordQueryProcessor(s, false);
		if(out != null  && out.size() > 0) return out;
		out = (ArrayList<Page>) singleWordQueryProcessor(stemmed, false);
		return out;
	}

	static long start=0, numOfResults=0;
	static void startClock() {
		start=System.currentTimeMillis();
	}
	static long getElapsedTime() {
		return System.currentTimeMillis() - start;
	}
	static void endNprintTime() {
		System.out.println("About " + numOfResults + " results (" + (System.currentTimeMillis() - start)/1000.0 + " sec)");
	}
	public static void main(String[] args) throws Exception {
		initSearchEngine(args);
		System.out.println("Wikipedia Search Engine Started...");
		BufferedReader br = null;
		long start=0;
		try {
			br = new BufferedReader(new InputStreamReader(System.in));
			String s = null;
			while (true) {
				System.out.print("query> ");
				s = br.readLine();
				if ("q".equals(s))
					break;
				startClock();
				List<Page> dids = wikiSearch(s);
				endNprintTime();
				int i=0;
				if(dids == null) {
//					 System.out.println(); 
				} else  {
					for (Page l : dids) {
						System.out.println(String.format("%-9d %s", l.did, TitleIndexer.readTitleBySeek(l.did, titleSecIndex)));
						if(i == 10) break;
						i++;
					}
				}
				Log.bw.flush();
			}
		} finally {
			br.close();
			Log.bw.close();
		}
	}
	
}
