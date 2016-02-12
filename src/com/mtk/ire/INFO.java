package com.mtk.ire;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;

public class INFO {

	public static final String INDIVIDUAL_FILE_DIR = "/media/mtk/soft/tmp/i/";
	public static final String TEMP_INDEX_FILES_DIR = "/media/mtk/soft/tmp/main/dev/i"; // i is prefix filename
	public static final String MAIN_INDEX_FILES_DIR = "/media/mtk/soft/tmp/main/prod/i"; // i is prefix filename

	public static final String STOP_WORD_FILE = "docs/stop-word-list.txt";
	public static final String GET_LOG_FILE = "/media/mtk/soft/tmp/log.txt";
	public static final String TITLES_FILE_PROD = "/media/mtk/soft/tmp/main/titles-prod";
	public static final String TITLES_FILE_DEV = "/media/mtk/soft/tmp/main/titles-dev";
	
	
	public static final String MINI_100MB_FILE = "docs/wiki-search-small.xml";
	public static final String MAIN_DUMP = "/home/mtk/enwiki-latest-pages-articles.xml";
	public static long PRINT_BLOCK;// = 30000; // number of files
	public static long INDEX_SPLIT_BLOCK;// = 80000;
	public static final boolean WRITE_TITLES = true;
	public static final boolean WRITE_MAIN_INDEX = true;
	public static long FREE_MEM_THRESHOLD = 0;

	
	static {
		System.out.println("Getting config values");
		try {
			Log.i("Reading configuration values from ire.config");
			BufferedReader br = new BufferedReader(new FileReader("/home/mtk/ire.config"));
			PRINT_BLOCK = Long.parseLong(br.readLine().split(":")[1]);
			INDEX_SPLIT_BLOCK = Long.parseLong(br.readLine().split(":")[1]);
//			FREE_MEM_THRESHOLD= Long.parseLong(br.readLine().split(":")[1]);
			
			Log.i("INDEX_SPLIT_BLOCK " + INDEX_SPLIT_BLOCK+ " PRINT_BLOCK " + PRINT_BLOCK);
//			Log.i("FREE_MEM_THRESHOLD " + FREE_MEM_THRESHOLD);
			Log.flush();
			br.close();
		} catch (Exception e) {
			PRINT_BLOCK = 30000;
			INDEX_SPLIT_BLOCK = 60000;
			FREE_MEM_THRESHOLD=1024*1024*300;
			e.printStackTrace();
		}
		
	}
}
