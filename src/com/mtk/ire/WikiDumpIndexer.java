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
				System.exit(-1);
			}
			if (argv.length == 0) {
				argv = new String[2];
				argv[0] = "wiki-search-small.xml";
				argv[1] = System.getProperty("user.home") + "/ire-index";
			}
			indexInFile = argv[0];
			indexOutfile = argv[1];
			
			System.out.println("Reading wikidump from input file [" + indexInFile + "]");
			System.out.println("Will write index file to [" +indexOutfile +"]");
			
			InputStream xmlInput = new FileInputStream(indexInFile);
			SAXParser saxParser = factory.newSAXParser();
			WikiSaxHandler handler = new WikiSaxHandler();
			saxParser.parse(xmlInput, handler);
			
			Indexer.writeIndexToFile(indexOutfile);
		} catch (Throwable err) {
			err.printStackTrace();
		} finally {
			System.out.println("Runtime " + (System.currentTimeMillis() - start) / 1000 + "s");
		}
	}
}
