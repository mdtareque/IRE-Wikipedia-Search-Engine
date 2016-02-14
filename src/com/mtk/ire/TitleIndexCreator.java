package com.mtk.ire;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;

public class TitleIndexCreator {

	static BufferedWriter bw = null;
	public static void main(String[] args) throws Exception {
		String f = "/home/mtk/Downloads/dc/titles-prod.txt";
		String s;
		bw = new BufferedWriter(new FileWriter("/home/mtk/title.index", true));
		BufferedReader br = new BufferedReader(new FileReader(f));
		Page p= null;
		int i=0;
		while( (s = br.readLine()) != null ) {
			p = new Page();
			p.docId = Long.parseLong(s.split(" ")[0]);
			p.title = s.split(" ")[1];
			PageProcessor.processPage(p);
			i++;
			if(i%50000 == 0) System.out.println(i + " done");
		}
		Indexer.writeIndexToFile(bw); // for titles		
		bw.close();
	}
}
