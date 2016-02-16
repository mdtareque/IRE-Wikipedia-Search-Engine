package com.mtk.ire;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.PriorityQueue;

/**
 * 
 * DONE:
 * 1. remove useless words like all composed on only 1 distinct character
 * 2. remove useless words like all composed on only 2 distinct character and length > 4
 * 
 * TODO: truncate posting list
 * 
 * @author mtk
 *
 */
public class ExtMergeSort {
	static class Log {
		static BufferedWriter bw = null;
		static {
			try {
				bw = new BufferedWriter(new FileWriter("/home/mtk/merge.log", true));
				bw.write("\n\nMerging process starting .." + (new Date()).toString() + "\n");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	static class TfIdSorter implements Comparator<Page> {
		public int compare(Page o1, Page o2) {
			if(o1.tf < o2.tf) return 1;
			else if(o1.tf > o2.tf) return -1;
			return 0;
		}
	}

	static DocIdSorter didcmp = new DocIdSorter();
	static class DocIdSorter implements Comparator<Page>{
		public int compare(Page o1, Page o2) {
			if(o1.did > o2.did) return 1;
			else if(o1.did < o2.did) return -1;
			else return 0;
		}
	}

	static class FileWordComparator implements Comparator<FileWord>{

		@Override
		public int compare(FileWord o1, FileWord o2) {
			return o1.w.compareTo(o2.w);
		}
		
	}
	static String ROOT_DIR="/media/mtk/soft/tmp/main/prod/";  // dev-split 
	
	static class FileWord {
		String w;
		String posting;
		int i;
		
		FileWord() {}
		FileWord(int index, String line) {
			this.w = line.split(":")[0];
			this.posting = line.split(":")[1];
			this.i = index;
		}

		public String toString() {
			return i + "-" + w;
		}
		public void setNew(String line) {
			this.w = line.split(":")[0];
			this.posting = line.split(":")[1];
		}
	}
	
	static class Page {
		Long did;
		String pos;
		int tf;
		int[] counts = new int[6]; // title, body, category, extLinks, references, infobox
		
		public Page(Long d, String list) {
			did = d;
			pos = list;
			calculateTf();
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
					case 'b': counts[0] = num; break;
					case 'c': counts[1] = num*50; break;
					case 'e': counts[2] = num; break;
					case 'i': counts[3] = num*50; break;
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
	}
	

	static List<Page> getSortedByTfAndGetList(String pos) {
		List<Page> p = new ArrayList<Page>();
		String[] tokens = pos.split(";"); // need one more split if documentFrequ is stored
		Long d;
//		int i=0;
		String list = "";
		for(String t: tokens) {
			d = Long.parseLong(t.split("-")[0]);  // for mergin 1k files 
			list = t.split("-") [1];    // this is used 
/*			for(i=0;i<pos.length();i++) {
				if(Character.isLetter(t.charAt(i)))
						break;
			}
			d = Long.parseLong(t.substring(0, i));
			list = t.substring(i);*/
			p.add(new Page(d, list));
		}
		Collections.sort(p, new TfIdSorter());
		return p;
	}
	
	static String getSortedByTf(String pos) {
		List<Page> p = new ArrayList<Page>();
		String[] tokens = pos.split(";");
		Long d;
		String list = "";
		for(String t: tokens) {
			d = Long.parseLong(t.split("-")[0]);
			list = t.split("-") [1];
			p.add(new Page(d, list));
		}
		Collections.sort(p, new TfIdSorter());
		StringBuilder sb = new StringBuilder();
		for(Page page: p) {
//			sb.append(page.did + "-" + page.pos+";"); // normal
			sb.append(page.did + page.pos+";"); // size reduced
		}
		sb.deleteCharAt(sb.length()-1);
		return sb.toString();
	}
	
	/**
	 * COmpression:
	 * 1. Truncated posting list to 2000 max
	 * 2. Storing the original document Frequency
	 * 3. Removed delimiter from doc id and posting list
	 * 4. Used difference of docIds to store, to reduce space
	 * 5.  
	 * 
	 */
	static String customSort(String s, String word) throws IOException {
		List<Page> sortedByTf = getSortedByTfAndGetList(s);
		List<Page>  pruned;
		long oldSize = sortedByTf.size();
		if(sortedByTf.size() > 3500) {
//			Log.bw.write("Truncating for " + word + ", oldSize : " + sortedByTf.size() + "\n");
			pruned = sortedByTf.subList(0, 3500); // reduction three
		} else pruned = sortedByTf;
		Collections.sort(pruned, didcmp);
		StringBuilder sb = new StringBuilder();
		sb.append(oldSize+"|");
//		sb.append(pruned.get(0).did + pruned.get(0).pos + ";");
//		long prevId = pruned.get(0).did ;
//		for(int j=1; j<pruned.size(); j++){
		for(Page page : pruned) {
//			sb.append(page.did + "-" + page.pos+";"); // normal
//			Page page = pruned.get(j);
//			sb.append((page.did - prevId) + page.pos+";"); // size reduced, more reduction
			sb.append(page.did +"-" + page.pos+";"); // size reduced
//			prevId = page.did;
		}
		return sb.deleteCharAt(sb.length()-1).toString();
	}

	static String customSort2(String s, String word, long df) throws IOException {
		List<Page> sortedByTf = getSortedByTfAndGetList(s);
//		List<Page> pruned;
		long oldSize = df;
		/*if(sortedByTf.size() > 2800) {
//			Log.bw.write("Trunc " + word + ", oldSize : " + sortedByTf.size() + "\n");
			pruned = sortedByTf.subList(0, 2800); // reduction three
		}
		else 
			pruned = sortedByTf;*/
		Collections.sort(sortedByTf, didcmp);
		StringBuilder sb = new StringBuilder();
		sb.append(oldSize+"|");
//		sb.append(pruned.get(0).did + pruned.get(0).pos + ";");
//		long prevId = pruned.get(0).did ;
//		for(int j=1; j<pruned.size(); j++){
//			sb.append(page.did + "-" + page.pos+";"); // normal
		for(Page page: sortedByTf) {
//			Page page = pruned.get(j);
//			sb.append((page.did - prevId) + page.pos+";"); // size reduced, more reduction
			sb.append(page.did  +"-" +page.pos+";"); // size reduced
//			prevId = page.did;
		}
		sb.deleteCharAt(sb.length()-1);
		return sb.toString();
	}
	

	static String getSortedByDocId(String pos) {
		List<Page> p = new ArrayList<Page>();
		String[] tokens = pos.split(";");
		Long d;
		String list = "";
		for(String t: tokens) {
			d = Long.parseLong(t.split("-")[0]);
			list = t.split("-") [1];
			p.add(new Page(d, list));
		}
		Collections.sort(p, didcmp);
		StringBuilder sb = new StringBuilder();
		for(Page page: p) {
//			sb.append(page.did + "-" + page.pos+";"); // normal
			sb.append(page.did + page.pos+";"); // size reduced
		}
		sb.deleteCharAt(sb.length()-1);
		return sb.toString();
	}
	
	
//	static void externalMergeSort(int start, int stop)throws Exception {
	static void externalMergeSort(String base, String[] inps, String outf)throws Exception {
		int NUM_FILES = 0;
//		int size = stop-start + 1;
		int size = inps.length;
//		Log.bw.write("Merging for  "+ start + "-"+ stop + " size:" + size + "\n");
		NUM_FILES = size;
		Log.bw.write("Merging for  "+ base + "-"+ inps.length +"\n");
		Comparator<FileWord> cmp = new FileWordComparator();
		PriorityQueue<FileWord> pq =  new PriorityQueue<FileWord>(2*NUM_FILES, cmp);
		BufferedReader[]  br = null;
		br = new BufferedReader[size];
		Log.bw.write("Merging " + base + "\n");
		for(int i=0; i<NUM_FILES; i++) {
//			System.out.println("Reading " + ROOT_DIR + "/i" + (i+1));
			br[i] = new BufferedReader(new FileReader(base + inps[i]));
			pq.add(new FileWord(i, br[i].readLine()));
		}
		
		/*
		if(start == 0 && stop == 0 ) {
			System.out.println("Special case... "); 
			size=4;
			NUM_FILES  = size;
			br = new BufferedReader[size];
			br[0] = new BufferedReader(new FileReader("/media/mtk/soft/tmp/main/merged-prod1-300"));
			pq.add(new FileWord(0, br[0].readLine()));
			br[1] = new BufferedReader(new FileReader("/media/mtk/soft/tmp/main/merged-prod301-600"));
			pq.add(new FileWord(1, br[1].readLine()));
			br[2] = new BufferedReader(new FileReader("/media/mtk/soft/tmp/main/merged-prod601-900"));
			pq.add(new FileWord(2, br[2].readLine()));
			br[3] = new BufferedReader(new FileReader("/media/mtk/soft/tmp/main/merged-prod901-1186"));
			pq.add(new FileWord(3, br[3].readLine()));
		}// special last merge
		else {
			System.out.println("smaller merges...");
			NUM_FILES  = size;
			br = new BufferedReader[size];
			Log.bw.write("Mering " + ROOT_DIR + "i\n");
			for(int i=0; i<NUM_FILES; i++) {
	//			System.out.println("Reading " + ROOT_DIR + "/i" + (i+1));
				br[i] = new BufferedReader(new FileReader(ROOT_DIR + "/i" + (start+i)));
				pq.add(new FileWord(i, br[i].readLine()));
			}
		}
		*/
		System.out.println("Initialized all FileWords and pq");
		FileWord fwRemoved = null;
		String word, tmp, longestPostingWord = null, out;
		int done=0;
		FileWord next;
		StringBuilder sb = new StringBuilder();
//		BufferedWriter bw = new BufferedWriter(new FileWriter("/media/mtk/soft/tmp/main/dev-split/merged-no-sort"));
//		BufferedWriter bw = new BufferedWriter(new FileWriter("/media/mtk/soft/tmp/main/dev-split/merged-sorted-byDocId"));
//		BufferedWriter bw = new BufferedWriter(new FileWriter("/media/mtk/soft/tmp/main/dev-split/merged-sorted-byTf"));
//		BufferedWriter bw = new BufferedWriter(new FileWriter("/media/mtk/soft/tmp/main/dev-split/merged-sorted-byTf-RemoveUselessWords"));
//		BufferedWriter bw = new BufferedWriter(new FileWriter("/media/mtk/soft/tmp/main/dev-split/merged-sorted-byTf-RemoveUselessWords-sizeReduced"));
//		BufferedWriter bw = new BufferedWriter(new FileWriter("/media/mtk/soft/tmp/main/dev-split/merged-sorted-byTf-AndPrune"));
		
		BufferedWriter bwaf = new BufferedWriter(new FileWriter("/media/mtk/soft/tmp/main/merged-prod-a-f"+outf, true));
		BufferedWriter bwgp = new BufferedWriter(new FileWriter("/media/mtk/soft/tmp/main/merged-prod-g-p"+outf, true));
		BufferedWriter bwqz = new BufferedWriter(new FileWriter("/media/mtk/soft/tmp/main/merged-prod-q-z"+outf, true));
		BufferedWriter bwnum = new BufferedWriter(new FileWriter("/media/mtk/soft/tmp/main/merged-prod-numeric"+outf, true));
		
		
		long longestPostingList = 0;
		long df=0;
		while(true) {
			df=0;
			sb.setLength(0);
			sb.trimToSize();
			fwRemoved = pq.remove();
			word = fwRemoved.w;
			df += Long.parseLong(fwRemoved.posting.split("\\|")[0]);
			sb.append(fwRemoved.posting.split("\\|")[1]);
//			sb.append(fwRemoved.posting);
			tmp =br[fwRemoved.i].readLine();
			if(tmp == null) {
				done++;
			} else {
				fwRemoved.setNew(tmp);
				pq.add(fwRemoved);
			}
			do {
				if(pq.size() > 0) {
					next = pq.peek();
					if (next.w.equals(word)) {
						next = pq.remove();
//						System.out.println("merging" + word);
						df += Long.parseLong(next.posting.split("\\|")[0]);
						sb.append(next.posting.split("\\|")[1]);
						
						// need more robust merge mechanism to inser the hacked titles index, LAST RUNn 
						
//						sb.append(next.posting);
						tmp = br[next.i].readLine();
						if(tmp == null) {
							done++; // bug: the word can be last word in some file, don't break, continue
						} else {
							next.setNew(tmp);
							pq.add(next);
						}
					} else {
						break;
					}
				}
				else
					break;
			} while( true );
			if(sb.length() > longestPostingList) { 
				longestPostingList = sb.toString().getBytes().length ; // + (df + "").length();
				longestPostingWord = word;
				Log.bw.flush();
			}
//			System.out.println(getSortedByDocId(sb.toString()));
			if(!isUselessWord(word))
			{
				out = customSort2(sb.toString(), word, df);
//				out = customSort(sb.toString(), word);
				char c= word.charAt(0);
				if(c>= 'a' && c <='f')
					bwaf.write(word + ":" + out + "\n");
				else if(c>='g' && c<='p')
					bwgp.write(word + ":" + out + "\n");
				else if(c>='q' && c<='z')
					bwqz.write(word + ":" + out + "\n");
				else
					bwnum.write(word + ":" + out + "\n");
//				bw.write(word + ":" +  + "\n");
			}
//			bw.write(word + ":" + customSort2(sb.toString(), word, df) + "\n");
//			bw.write(word + ":" + sb.toString() + "\n");
			if(done == NUM_FILES) break;
			
		}
//		if(done < NUM_FILES) {
		if(pq.size() > 0) {
			Log.bw.write("Residual from pq");
			while( (next=pq.peek()) != null ){
				next = pq.remove();
//				bw.write(next.w + ":" + next.posting +"\n");
				out = next.posting;
				char c= next.w.charAt(0);
				if(c>= 'a' && c <='f')
					bwaf.write(word + ":" + out + "\n");
				else if(c>='g' && c<='p')
					bwgp.write(word + ":" + out + "\n");
				else if(c>='q' && c<='z')
					bwqz.write(word + ":" + out + "\n");
				else
					bwnum.write(word + ":" + out + "\n");
			}
		}
		Log.bw.write("longestPostingList "  + longestPostingList + " for " + longestPostingWord);
		bwaf.close();
		bwgp.close();
		bwqz.close();
		bwnum.close();
		
		for(int i=0; i<NUM_FILES; i++) {
			br[i].close();
		}
		
		System.out.println("done");
	}
	
	private static boolean isUselessWord(String w) {
		int[] map = new int[26];
		int i;
		for(i=0; i<w.length() ; i++) {
			if(Character.isLetter(w.charAt(i)))
				map[w.charAt(i) - 'a']++;
		}
		int unique=0;
		for(i=0; i<26; i++) {
			if(map[i] > 0) unique++;
		}
		if(unique < 3 && w.length()> 5) return true;
		if(unique == 1) return true;
		if(w.length() > 5 && w.length()/unique >= 2) return true;
		return false;
	}


	public static void main(String[] args) throws Exception{
		long start = System.currentTimeMillis();
		String[] inps = new String[300];
		/*
		for(int i=0; i<300; i++) inps[i] = (i+1)+"";
		externalMergeSort("/media/mtk/soft/tmp/main/prod/i", inps, "1-300");
// 		externalMergeSort(1, 300);
		Log.bw.write("1,300 merge took " + (System.currentTimeMillis() - start) + " ms\n"); 
		Log.bw.flush();
		
		start = System.currentTimeMillis();
//		externalMergeSort(301, 600);
		inps = new String[800-301];
		for(int i=301; i<800; i++) inps[i-301] = i+"";
		externalMergeSort("/media/mtk/soft/tmp/main/prod/i", inps, "300-800");
		Log.bw.write("301,800 merge took " + (System.currentTimeMillis() - start) + " ms\n");
		Log.bw.flush();

		start = System.currentTimeMillis();
		inps = new String[1187-800];
		for(int i=800; i<1187; i++) inps[i-800] = i+"";
		externalMergeSort("/media/mtk/soft/tmp/main/prod/i", inps, "800-1187");
//		externalMergeSort(601, 900);
		Log.bw.write("601,1186 merge took " + (System.currentTimeMillis() - start) + " ms\n");
		Log.bw.flush();
		*/
		
/*		start = System.currentTimeMillis();
		inps = new String[3];
		inps[0] = "merged-prod-a-f1-300";
		inps[1] = "merged-prod-a-f300-800";
		inps[2] = "merged-prod-a-f800-1187";
		externalMergeSort("/media/mtk/soft/tmp/main/", inps, "final");
		Log.bw.write("a-f merge took " + (System.currentTimeMillis() - start) + " ms\n");
		Log.bw.flush();

 
		start = System.currentTimeMillis();
		inps = new String[3];
		inps[0] = "merged-prod-g-p1-300";
		inps[1] = "merged-prod-g-p300-800";
		inps[2] = "merged-prod-g-p800-1187";
		externalMergeSort("/media/mtk/soft/tmp/main/", inps, "final");
		Log.bw.write("g-p merge took " + (System.currentTimeMillis() - start) + " ms\n");
		Log.bw.flush();
		*/

		/*
		start = System.currentTimeMillis();
		inps = new String[3];
		inps[0] = "merged-prod-q-z1-300";
		inps[1] = "merged-prod-q-z300-800";
		inps[2] = "merged-prod-q-z800-1187";
		externalMergeSort("/media/mtk/soft/tmp/main/", inps, "final");
		Log.bw.write("q-z merge took " + (System.currentTimeMillis() - start) + " ms\n");
		Log.bw.flush();

*/
		start = System.currentTimeMillis();
		inps = new String[3];
		inps[0] = "merged-prod-numeric1-300";
		inps[1] = "merged-prod-numeric300-800";
		inps[2] = "merged-prod-numeric800-1187";
		externalMergeSort("/media/mtk/soft/tmp/main/", inps, "final");
		Log.bw.write("q-z merge took " + (System.currentTimeMillis() - start) + " ms\n");
		Log.bw.flush();
		/* */
		
//		externalMergeSort(0,0);
		Log.bw.close();
	}
	
	
}
