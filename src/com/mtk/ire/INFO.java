package com.mtk.ire;

import java.io.Writer;

public class INFO {

	public static final String INDIVIDUAL_FILE_DIR = "/media/mtk/soft/tmp/i/";
	public static final String TEMP_INDEX_FILES_DIR = "/media/mtk/soft/tmp/main/dev/i"; // i if prefix filename
	public static final String MAIN_INDEX_FILES_DIR = "/media/mtk/soft/tmp/main/prod/i"; // i if prefix filename
	public static final String MINI_100MB_FILE = "docs/wiki-search-small.xml";
	public static final String STOP_WORD_FILE = "docs/stop-word-list.txt";
	public static final String GET_LOG_FILE = "/media/mtk/soft/tmp/log.txt";
	public static final String TITLES_FILE = "/media/mtk/soft/tmp/titles";
	
	
	public static final String MAIN_DUMP = "/home/mtk/enwiki-latest-pages-articles.xml";
	public static final long PRINT_BLOCK = 30000;
	public static final int INDEX_SPLIT_BLOCK = 30000;
	public static final boolean WRITE_TITLES = true;
	public static final boolean WRITE_MAIN_INDEX = true;

}
