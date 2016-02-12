package com.mtk.ire;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
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

	static int NUM_FILES=2;
	static String ROOT_DIR="/media/mtk/soft/tmp/main/dev-split/";
	
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
	static class FileWordComparator implements Comparator<FileWord>{

		@Override
		public int compare(FileWord o1, FileWord o2) {
			return o1.w.compareTo(o2.w);
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
					case 'c': counts[1] = num; break;
					case 'e': counts[2] = num; break;
					case 'i': counts[3] = num; break;
					case 'r': counts[4] = num; break;
					case 't': counts[5] = num; break;
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
	static class TfIdSorter implements Comparator<Page> {
		public int compare(Page o1, Page o2) {
			if(o1.tf < o2.tf) return 1;
			else if(o1.tf > o2.tf) return -1;
			return 0;
		}
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
	static class DocIdSorter implements Comparator<Page>{
		public int compare(Page o1, Page o2) {
			if(o1.did > o2.did) return 1;
			else if(o1.did > o2.did) return -1;
			else return 0;
		}
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
		Collections.sort(p, new DocIdSorter());
		StringBuilder sb = new StringBuilder();
		for(Page page: p) {
//			sb.append(page.did + "-" + page.pos+";"); // normal
			sb.append(page.did + page.pos+";"); // size reduced
		}
		sb.deleteCharAt(sb.length()-1);
		return sb.toString();
	}
	
	
	static void externalMergeSort(int start, int stop)throws Exception {
		int size = stop-start + 1;
		if(start == 0 && stop == 0 ) {
			size=2;
		}// special last merge
		System.out.println("Size of BufferedReader " + size);
		
		BufferedReader[] br = new BufferedReader[size];
		Comparator<FileWord> cmp = new FileWordComparator();
        PriorityQueue<FileWord> pq =  new PriorityQueue<FileWord>(2*NUM_FILES, cmp);
		for(int i=0; i<NUM_FILES; i++) {
//			System.out.println("Reading " + ROOT_DIR + "/i" + (i+1));
			br[i] = new BufferedReader(new FileReader(ROOT_DIR + "/i" + (i+1)));
			pq.add(new FileWord(i, br[i].readLine()));
		}
		
		System.out.println("Initialized all FileWords and pq");
		FileWord fwRemoved = null;
		String word, tmp, longestPostingWord = null;
		int done=0;
		FileWord next;
		StringBuilder sb = new StringBuilder();
//		BufferedWriter bw = new BufferedWriter(new FileWriter("/media/mtk/soft/tmp/main/dev-split/merged-no-sort"));
//		BufferedWriter bw = new BufferedWriter(new FileWriter("/media/mtk/soft/tmp/main/dev-split/merged-sorted-byDocId"));
//		BufferedWriter bw = new BufferedWriter(new FileWriter("/media/mtk/soft/tmp/main/dev-split/merged-sorted-byTf"));
//		BufferedWriter bw = new BufferedWriter(new FileWriter("/media/mtk/soft/tmp/main/dev-split/merged-sorted-byTf-RemoveUselessWords"));
		BufferedWriter bw = new BufferedWriter(new FileWriter("/media/mtk/soft/tmp/main/dev-split/merged-sorted-byTf-RemoveUselessWords-sizeReduced"));
		
		long longestPostingList = 0;
		
		while(true) {
			sb.setLength(0);
			sb.trimToSize();
			fwRemoved = pq.remove();
			word = fwRemoved.w;
			sb.append(fwRemoved.posting);
			tmp =br[fwRemoved.i].readLine();
			if(tmp == null) {
				done++;
			}
			else {
				fwRemoved.setNew(tmp);
				pq.add(fwRemoved);
			}
			do {
				if(pq.size() > 0) {
					next = pq.peek();
					if (next.w.equals(word)) {
						next = pq.remove();
						sb.append(next.posting);
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
				longestPostingList = sb.length();
				longestPostingWord = word;
			}
//			System.out.println(getSortedByDocId(sb.toString()));
			if(!isUselessWord(word))
				bw.write(word + ":" + getSortedByTf(sb.toString()) + "\n");
//			bw.write(word + ":" + sb.toString() + "\n");
			if(done == NUM_FILES) break;
			
		}
//		if(done < NUM_FILES) {
		if(pq.size() > 0) {
			while( (next=pq.peek()) != null ){
				next = pq.remove();
				bw.write(next.w + ":" + next.posting +"\n");
			}
		}
		System.out.println("longestPostingList "  + longestPostingList + " for " + longestPostingWord);
		bw.close();
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
		return false;
	}


	public static void main(String[] args) throws Exception{
		externalMergeSort(1, 26);
		/*externalMergeSort(1, 512);
		externalMergeSort(513, 1186);
		externalMergeSort(0,0);*/
	}
	
	
}
