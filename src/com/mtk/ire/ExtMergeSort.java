package com.mtk.ire;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Comparator;
import java.util.PriorityQueue;

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
		BufferedWriter bw = new BufferedWriter(new FileWriter("/media/mtk/soft/tmp/main/dev-split/merged-no-sort"));
//		BufferedWriter bw = new BufferedWriter(new FileWriter("/media/mtk/soft/tmp/main/dev-split/merged-sorted-byDocId"));
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
				if(pq.size() > 1) {
					next = pq.peek();
					if (next.w.equals(word)) {
						next = pq.remove();
						sb.append(next.posting);
						tmp = br[next.i].readLine();
						if(tmp == null) {
							done++;
							break;
						}
						else {
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
			
			bw.write(word + ":" + sb.toString() + "\n");
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
