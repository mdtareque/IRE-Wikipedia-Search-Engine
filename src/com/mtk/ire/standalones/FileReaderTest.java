package com.mtk.ire.standalones;

import java.io.BufferedReader;
import java.io.FileReader;


public class FileReaderTest {
	
	public static void main(String[] args) throws Exception {

		BufferedReader br = new BufferedReader(new FileReader("docs/stop-word-list.txt"));
		String s;
		while((s= br.readLine()) != null ) 
			System.out.println(s);
		br.close();
	}
}
