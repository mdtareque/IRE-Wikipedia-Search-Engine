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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;

public class QueryProcessor {
	
	static long N = 16299475L;
	static ArrayList<String> postingLists = new ArrayList<String>();
	static class WordDocTuple {
		String w;
		Long did;
		public WordDocTuple(String w, Long did) {
			this.w = w;
			this.did = did;
		}
		@Override
		public int hashCode() {
			return w.hashCode()+did.hashCode();
		}
		@Override
		public boolean equals(Object obj) {
			WordDocTuple two = (WordDocTuple) obj;
			return w.equals(two.w) && did == two.did;
		}
		@Override
		public String toString() {
			return w + " " + did;
		}
	}
	
//	static HashMap<WordDocTuple, Page> hm = new HashMap<WordDocTuple, Page>();
	static ArrayList<QueryWord> qw = new ArrayList<QueryWord>();
	static class QueryWord {
		public String w;
		long idf;
		public QueryWord(String w2, Long idf2) {
			w = w2; idf = idf2;
		}
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
		secIndex = IndexGenerator.readSecondaryIndex("1-300");
		titleSecIndex = TitleIndexer.read2ndIndex();
		Log.bw.write("init done");
	}
	

	static class Page {
		Long did;
		String pos;
		long tf, idf;
		
		Double tf_idf, okapiValue, score;
		int[] counts = new int[6]; // title, body, category, extLinks, references, infobox
		
		public Page(Long d, long idff, String list, String w) {
			did = d;
			idf = idff;
			pos = list;
			calculateTf();
			calculateTfIdf();
//			hm.put(new WordDocTuple(w, did), this);
			calculateOkapi();
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
//			if(did == 57570) System.out.println("calculateTf " +pos);
			for(i=0; i<pos.length();i++) {
				if( Character.isLetter(pos.charAt(i)) ) {
//					if(did == 57570)System.out.println(i + "  in if");
					num =0;
					j=i+1;
					while( j< pos.length() && Character.isDigit(pos.charAt(j))  )
						j++;
					num = Integer.parseInt(pos.substring(i+1, j));
//					if(did == 57570) System.out.println("calculateTf " +num + " j is " + j);
//						num = num*10 + Character.getNumericValue(pos.charAt(j++));
					switch(pos.charAt(i)) {
					case 'b': counts[0] = num*10; break;
					case 'c': counts[1] = num*50; break;
					case 'e': counts[2] = num; break;
					case 'i': counts[3] = num*20; break;
					case 'r': counts[4] = num; break;
					case 't': counts[5] = num*100; break;
					}
					
					i=j-1;
//				} else {
//					if(did == 57570)System.out.println(i + "  in else");
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
		public void calculateOkapi() {
//			okapiValue = 
		}
		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			if (counts[0] > 0) sb.append("b"  + counts[0] );
			if (counts[1]  > 0) sb.append("c" + counts[1] );
			if (counts[2]  > 0) sb.append("e" + counts[2] );
			if (counts[3] > 0) sb.append("i"  + counts[3] );
			if (counts[4]  > 0) sb.append("r" + counts[4] );
			if (counts[5] > 0) sb.append("t"  + counts[5] );
			if (sb.length() > 0) sb.append(";");
			
			return did + " " + sb.toString(); 
		} 
	}
	static List<Page> fieldQueryProcessor(String w, boolean rerun, String pos)  {
		try {
			
			if(pos == null) return null;
			List<Page> p = new ArrayList<Page>();
			
			Long idf = Long.parseLong(pos.split("\\|")[0]);
			String[] tokens = pos.split("\\|")[1].split(";"); // need one more split if documentFrequ is stored
			Long d;
			
			String list = "";
			for(String t: tokens) {
				d = Long.parseLong(t.split("-")[0]); 
				list = t.split("-") [1];     
				p.add(new Page(d, idf, list, w));
			}
			char field = w.charAt(0);
			List<Page> res  = new ArrayList<Page>();
			int indexToSearch =-1;
			switch(field) {
				case 'b': indexToSearch=0; break;
				case 'c': indexToSearch=1; break;
				case 'e': indexToSearch=2; break;
				case 'i': indexToSearch=3; break;
				case 'r': indexToSearch=4; break;
				case 't': indexToSearch=5; break;
				default: System.out.println("Invalid field query"); return null;
			}
//			int i=0;
			for(Page pg : p) {
				if(pg.counts[indexToSearch] > 0) {
//					System.out.println(pg);
					res.add(pg);
//					i++;
//					if(i==10) break;
				}
			}
			return res;
			
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		return null;
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
		String list = "";
		for(String t: tokens) {
			d = Long.parseLong(t.split("-")[0]); 
			list = t.split("-") [1];     
			p.add(new Page(d, idf, list, w ));
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

	
	@SuppressWarnings("resource")
	static String getPostingList(String s) throws Exception {
		if(s.indexOf(":") > 0) 
			s= s.substring(s.indexOf(":")+1);
		Long firstSeekPos = secIndex.floorEntry(s).getValue(), nextSeekPos;
	//	System.out.println("firstSeekPos " + firstSeekPos);
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
	//			System.out.println("Got " + nextSeekPos + " nextSeekPos from secondary index");
				break;
			}
		}
	
		String posList = "/home/mtk/ireIndex/merged-prod1-300.justPostingList";
		raf = new RandomAccessFile(posList, "r");
	//	System.out.println("nextSeekPos " + nextSeekPos);
		raf.seek(nextSeekPos);
		tmp = raf.readLine();
//		System.out.println(tmp);
		return tmp;
	}
	static List<Page> singleWordQueryProcessor(String s, boolean rerun) {
		// get posting list, by some formula
		List<Page> dids = new ArrayList<Page>();
		try {
			String posList = getPostingList(s);
			if(posList == null) return null;
//				System.out.println(tmp);
//				tmp = tmp.split("\\|")[1];
			dids = getSortedByTfAndGetList(s, posList, rerun);
			
			/*String[] toks = (tmp.split("\\|")[1]).split(";");
			for (String p : toks) {
				// System.out.println(p);
				dids.add(Long.parseLong(p.split("-")[0]));
			}*/
//				System.out.println("dids " + dids);
		} catch (Exception e) {
			e.printStackTrace();
		}
//		int limit = dids.size();
//		if(dids.size() > 10) limit = 10;
		return dids; //.subList(0, limit);
	}
	public static ArrayList<Page> union(ArrayList<Page> a,ArrayList<Page> b){
		ArrayList<Page> result = new ArrayList<Page>();
		int i = 0;
	    int j = 0;
	    if(a == null || b == null) return null;
	    while(i < a.size() && j < b.size()){
	        if(a.get(i).did < b.get(j).did) {
	        	result.add(a.get(i));
	        	result.add(b.get(j));
	            ++i;
	        }
	        else if (a.get(i).did > b.get(j).did) {
	        	result.add(a.get(i));
	        	result.add(b.get(j));
	            ++j;
	        }
	        else{
	            result.add(a.get(i));
	            ++i;
	            ++j;
	        }
	    }
	    return result;		
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
//			System.out.println(o1 + " " + o2);
			if(o1.score < o2.score) return 1;
			else if(o1.score > o2.score) return -1;
			return 0;
		}
	
	}
	
	static String plainQ = "";
	static String fieldQ = "";
	static void preprocess(String s) {
		StringBuilder sbp = new StringBuilder();
		StringBuilder sbf = new StringBuilder();
		for(String w: s.split(" ")){
			if(w.indexOf(":") == 1) {
				sbf.append(w + " ");
			} else {
				sbp.append(w + " ");
			}
		}
		plainQ = sbp.toString().trim();
		fieldQ = sbf.toString().trim();
	}
	static List<Page> multiWordQueryProcessor(String s) throws Exception {
		ArrayList<Page> commonDocs = new ArrayList<Page>();
//		System.out.println(s + " len of Common docIds " + commonDocs.size() );
//		if(commonDocs == null) commonDocs = new ArrayList<Page>();
		String[] words = s.split(" ");
		commonDocs = (ArrayList<Page>) multiWordQueryProcessor0(s.split(" "));
		/*String[] toks = null;
		int flag=0;
		for(int i=words.length; i>1; i--) {
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
//				if(getElapsedTime() > 800) {flag= 1; break;}
			}
			if(flag == 1) break;
		}			*/
		
		if(commonDocs == null) commonDocs = new ArrayList<Page>();
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
		// commondocs
		Collections.sort(commonDocs, tfIdSorter);
		ArrayList<Page> outputDocs = new ArrayList<Page>();
		double score=0;
		for(int i=0; i<commonDocs.size(); i++) {
			if(i>100) break;
			score =0;
			for(int j=0; j<words.length; j++) {
//				Page wdTuple = hm.get(new WordDocTuple(words[j], commonDocs.get(i).did ));
				String pos =postingLists.get(j);
//				System.out.println(words[j] + " " + commonDocs.get(i).did );
//				System.out.println(wdTuple);
				double tfidf = getTfIdf(pos, words[j]);
				score += tfidf;
			}
			commonDocs.get(i).score = score;
			outputDocs.add(commonDocs.get(i));
		}
//		System.out.println("After score");
		Collections.sort(outputDocs, tfIdfSorter);
		numOfResults = outputDocs.size();
//		int limit = commonDocs.size();
//		if(limit > 10) limit = 10;
		return outputDocs; //.subList(0, limit);
	}
	
	
	private static int calculateTf(String pos) {
		int counts[] = new int[6];
		int num, j, i;
//		if(did == 57570) System.out.println("calculateTf " +pos);
		for(i=0; i<pos.length();i++) {
			if( Character.isLetter(pos.charAt(i)) ) {
//				if(did == 57570)System.out.println(i + "  in if");
				num =0;
				j=i+1;
				while( j< pos.length() && Character.isDigit(pos.charAt(j))  )
					j++;
				num = Integer.parseInt(pos.substring(i+1, j));
//				if(did == 57570) System.out.println("calculateTf " +num + " j is " + j);
//					num = num*10 + Character.getNumericValue(pos.charAt(j++));
				switch(pos.charAt(i)) {
				case 'b': counts[0] = num*10; break;
				case 'c': counts[1] = num*50; break;
				case 'e': counts[2] = num; break;
				case 'i': counts[3] = num*20; break;
				case 'r': counts[4] = num; break;
				case 't': counts[5] = num*100; break;
				}
				i=j-1;
			}
		}
		int total=0;
		for(i=0; i<6; i++)
			total += counts[i];
		return total;
	}

	public static double calculateIdf(String w) {
		// tf-id : tf * Log(N/idf)
		return Math.log(N/Long.parseLong(w));
	}

	private static double getTfIdf(String pos, String w) {
		if(pos == null) return 0;
		return calculateTf(pos) * calculateIdf(pos.split("\\|")[0]);
	}


	static List<Page> multiWordQueryProcessor0(String[] words) throws Exception {
//		String[] words = s.split(" ");
		ArrayList<Page> commonDocs = new ArrayList<Page>();
		ArrayList<Page> singleDocIds = null;
		int i=0;
		String pos = "";
		for (String w : words) {
			pos = getPostingList(w);
			postingLists.add(pos);
			// get posting list, by some formula
			if(i==0) {
				commonDocs = (ArrayList<Page>) stemmedAndPlainSingleWordQueryProcessor(w, pos);
//				commonDocs = (ArrayList<Page>) singleWordQueryProcessor(w, false);
//				System.out.println(w + " len of docIds " + commonDocs.size() );
			} else {
				singleDocIds = (ArrayList<Page>) stemmedAndPlainSingleWordQueryProcessor(w, pos);
//				singleDocIds = (ArrayList<Page>) singleWordQueryProcessor(w, false);
//				System.out.println(w + " len of docIds " + singleDocIds.size() );
			}
			if(i>0) {
				commonDocs = union(singleDocIds, commonDocs); 
			}
			i++;
		}
//		int limit = commonDocs.size();
//		if(limit > 10) limit = 10;
		return commonDocs;//.subList(0, limit);
	}

	static List<Page> wikiSearch(String s) throws Exception {
		s=s.trim();
		ArrayList<Page> out = null;
		if (s.split(" ").length == 1) { // single word query
//			return singleWordQueryProcessor(s, false);
			String pos = getPostingList(s);
			return stemmedAndPlainSingleWordQueryProcessor(s, pos);
		} else if(s.indexOf("\"") >= 0){// phrase query, change to multiWord
			s=s.replaceAll("\"", " ").replaceAll("\\s+", " ");
			preprocess(s);
			out = (ArrayList<Page>) multiWordQueryProcessor(plainQ);
		} else { // multi word query
			preprocess(s);
			out = (ArrayList<Page>) multiWordQueryProcessor(plainQ);
		}
		if(!fieldQ.equals("")) {
			ArrayList<Page> commonDocs = new ArrayList<Page>();
			ArrayList<Page> singleDocIds = null;
			int i=0;
			for(String w: fieldQ.split(" ")) {
				// get posting list, by some formula
				String pos = getPostingList(w.split(":")[1]);
				if(i==0) {
					commonDocs = (ArrayList<Page>) stemmedAndPlainSingleWordQueryProcessor(w, pos);
				}
				else {
					singleDocIds = (ArrayList<Page>) stemmedAndPlainSingleWordQueryProcessor(w, pos);
				}
				if(i>0) {
					commonDocs = intersection(singleDocIds, commonDocs); 
				}
				i++;
			}
			List<Page> res = union(commonDocs, out);
			if(res == null || res.size() == 0) {
				return out;
			} else {
				return res;
			}
		}
		fieldQ = "";
		plainQ = "";
		return out;
	}
	static IREUtils.Stemmer stemmer = new IREUtils.Stemmer();
	private static String getStem(String t) {
		stemmer.reset();
		stemmer.add(t.toCharArray(), t.length());
		stemmer.stem();
		return stemmer.toString();
	}

	private static List<Page> stemmedAndPlainSingleWordQueryProcessor(String s, String pos) {
		ArrayList<Page> out = null; 
		
		if(s.indexOf(":") == 1) {
			try {
				
				out = (ArrayList<Page>) fieldQueryProcessor(s, false, pos);
				if(out != null  && out.size() > 0) return out;
				pos = getPostingList(getStem(s.split(":")[1]));
				out = (ArrayList<Page>) fieldQueryProcessor(s, false, pos);
			} catch (ArrayIndexOutOfBoundsException e) {
				System.out.println("Invalid field query");
			} catch(Exception e) {
				e.printStackTrace();
			}
		} else  {
			out = (ArrayList<Page>) singleWordQueryProcessor(s, false);
			if(out != null  && out.size() > 0) return out;
			String stemmed = getStem(s);
			out = (ArrayList<Page>) singleWordQueryProcessor(stemmed, false);
		}
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
		try {
			br = new BufferedReader(new InputStreamReader(System.in));
			String s = null;
			while (true) {
				postingLists.clear();
				System.out.print("query> ");
				s = br.readLine();
				if(s.length() == 0) continue;
				s=s.toLowerCase();
				if ("q".equals(s))
					break;
				startClock();
				List<Page> dids = wikiSearch(s);
				endNprintTime();
//				Collections.sort(dids, tfIdfSorter);
				int i=0;
				if(dids != null) {
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
