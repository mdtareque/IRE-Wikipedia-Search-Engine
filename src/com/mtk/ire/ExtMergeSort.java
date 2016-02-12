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
 * TODO:
 * 1. remove useless words like all composed on only 1 distinct character
 * 2. remove useless words like all composed on only 2 distinct character and length > 4
 * 
 * @author mtk
 *
 */
public class ExtMergeSort {

	static int NUM_FILES=26;
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
		public Page(Long d, String list) {
			did = d;
			pos = list;
		}
	}
	static class DocIdSorter implements Comparator<Page>{

		@Override
		public int compare(Page o1, Page o2) {
			if(o1.did > o2.did) return 1;
			else return -1;
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
			sb.append(page.did + "-" + page.pos+";");
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
		String word, tmp;
		int done=0;
		FileWord next;
		StringBuilder sb = new StringBuilder();
//		BufferedWriter bw = new BufferedWriter(new FileWriter("/media/mtk/soft/tmp/main/dev-split/merged-no-sort"));
		BufferedWriter bw = new BufferedWriter(new FileWriter("/media/mtk/soft/tmp/main/dev-split/merged-sorted-byDocId"));
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
//			System.out.println(getSortedByDocId(sb.toString())); 
			bw.write(word + ":" + getSortedByDocId(sb.toString()) + "\n");
//			bw.write(word + ":" + sb.toString() + "\n");
//			if(pq.size() < 2) break;
			if(done == NUM_FILES) break;
			
		}
//		if(done < NUM_FILES) {
		if(pq.size() > 0) {
			while( (next=pq.peek()) != null ){
				next = pq.remove();
				bw.write(next.w + ":" + next.posting +"\n");
			}
		}
		bw.close();
		for(int i=0; i<NUM_FILES; i++) {
			br[i].close();
		}
		System.out.println("done");
	}
	
	public static void main(String[] args) throws Exception{
		externalMergeSort(1, 26);
		/*externalMergeSort(1, 512);
		externalMergeSort(513, 1186);
		externalMergeSort(0,0);*/
	}
	
	
}