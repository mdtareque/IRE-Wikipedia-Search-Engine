package com.mtk.ire;

import java.io.FileInputStream;
import java.io.InputStream;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

public class WikiDumpIndexer {

	static long start = System.currentTimeMillis();
	
	public static void main(String[] argv) {

		String indexInFile, indexOutfile;
		SAXParserFactory factory = SAXParserFactory.newInstance();
		try {
			if(argv.length != 2) {
				System.out.println("Usage> java WikiDumpIndexer <wiki-dump-xml> <output index filename>");
//				System.exit(-1);
			}
			if (argv.length == 0) {
				argv = new String[2];
				argv[0] = INFO.MINI_100MB_FILE; // mini-wiki.xml
				argv[1] = INFO.MAIN_INDEX_FILES_DIR; 
			}
			indexInFile = argv[0];
			indexOutfile = argv[1];
			Indexer.setOutFile(indexOutfile);
			Log.i("Reading wikidump from input file [" + indexInFile + "]", true);
			Log.i("Will write index file to [" +indexOutfile +"<counter>]", true);
			Log.flush();
			InputStream xmlInput = new FileInputStream(indexInFile);
			SAXParser saxParser = factory.newSAXParser();
			WikiSaxHandler handler = new WikiSaxHandler();
			saxParser.parse(xmlInput, handler);
			PageProcessor.writeStats();
			Indexer.writeIndexToFile(); // residue
		} catch (Throwable err) {
			err.printStackTrace();
		} finally {
			Log.i("Runtime " + (System.currentTimeMillis() - start) / 1000 + "s", true);
			Log.close();
		}
	}
}
