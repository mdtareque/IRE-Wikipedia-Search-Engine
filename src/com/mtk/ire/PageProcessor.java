package com.mtk.ire;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class PageProcessor {

	static IREUtils.Stemmer s = new IREUtils.Stemmer();
	static WikiParser w = new WikiParser();
	static Set<String> stopWords = IREUtils.StopWords.stopword;
	static Map<String, TreeMap<Long, Indexer.Ocurrences>> dict = Indexer.words;
	

	public static void processPage(Page p) {
		Long id = p.docId;
//		p.text0 = p.text;
		p.text = p.text.toLowerCase();
		
		String text = p.text;
		text = text.replaceAll("&gt;", ">");
		text = text.replaceAll("&lt;", "<");
		// remove [http: ..] and get data
		text = text.replaceAll("\\[http:.*? (.*?)\\]", " $1 "); 
		text = WikiParser.ref.matcher(text).replaceAll(" ");
		text = WikiParser.htmlComment.matcher(text).replaceAll(" ");// remove tags
		p.text = text;
		
		// category
		p.categoriesText = w.getCategories(p.text);
//		p.categoriesText0 = p.categoriesText; 
		p.categoriesText = index(p.categoriesText.split(" "), id, LOCATION.CATEGORY);
		
		// external links
		p.externalLinkText = w.getExternalLinks(p.text);
//		p.externalLinkText0 = p.externalLinkText;
		p.externalLinkText = index(p.externalLinkText.split(" "), id, LOCATION.EXTERNAL_LINKS);
		
		// 	remove tags, remove [[File: and like 380 tags
		text = text.replaceAll("(</?.*?>)|(&nbsp;)|(\\'+)|(\\[\\[[A-Za-z]+:.*?\\]\\])", " "); 
		text = text.replaceAll("\\[\\[([^]]*?)\\|(.*?)\\]\\]", " $2 "); // keep
		// name from [][link|name]] 
		text = text.replaceAll("\\[\\[(.*?)\\]\\]", " $1 "); // rest all ignore
		p.text = text;

		// title
		p.title = p.title.replaceAll("([A-Z][^A-Z])", " $1");
//		p.title0 = p.title;
		p.title = index(p.title.toLowerCase().split("(\\P{Alnum})|(\\p{Blank})"), id, LOCATION.TITLE);
		
		// references
		p.referencedText = w.getRef(p.text);;
//		p.referencedText0 = p.referencedText;
		p.referencedText = index(p.referencedText.split(" "), id, LOCATION.REF);

		// infobox
		p.infoboxesText = w.getInfoBox(p.text);
//		p.infoboxesText0 = p.infoboxesText;
		p.infoboxesText = index(p.infoboxesText.split(" "), id, LOCATION.INFOBOX);

		// body
		p.text = WikiParser.doubleCurly.matcher(p.text).replaceAll(" ");
//		p.text0 = p.text;
		p.text = index(p.text.split("(\\P{Alpha}+)|(\\p{Blank})"), id, LOCATION.BODY);

		if(dict.size() > 100_000) {
			Indexer.writeIndexToFile();
			Indexer.words.clear();
		}
		writePageToFile(p);
	}

	private static String getStem(String t) {
		s.reset();
		s.add(t.toCharArray(), t.length());
		s.stem();
		return s.toString();
	}

	private static void writePageToFile(Page p) {
		BufferedWriter bw = null;
		try {
//			bw = new BufferedWriter(new FileWriter(System.getProperty("user.home") + "/ind/" + p.docId));
			bw = new BufferedWriter(new FileWriter(INFO.INDIVIDUAL_FILE_DIR + p.docId));
			bw.write(p.title + " " + p.docId + " \n\n");
//			bw.write("Original Text is \n\n" + p.text0 + "\n\n");
			bw.write("Filtered Text is \n\n" + p.text + "\n\n");

			//			bw.write("Original References\n\n" + p.referencedText0 + "\n\n");
			bw.write("Filtered References\n\n" + p.referencedText + "\n\n");
			
//			bw.write("Original Category\n\n" + p.categoriesText0+ "\n\n");
			bw.write("Filtered Category\n\n" + p.categoriesText+ "\n\n");
			
//			bw.write("Original ExternalLinks\n\n" + p.externalLinkText0 + "\n\n");
			bw.write("Filtered ExternalLinks\n\n" + p.externalLinkText + "\n\n");
			
//			bw.write("Original InfoBoxs\n\n" + p.infoboxesText0 + "\n\n");
			bw.write("Filtered InfoBoxs\n\n" + p.infoboxesText + "\n\n");
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				bw.close();
			} catch (IOException e) {
				System.err.println("Not able to write page " + p.docId);
				e.printStackTrace();
			}
		}

	}
	
	static String index(String[] tokens, long id, LOCATION e) {
		if(tokens.length == 0) return "";
		String stemmed;
		Indexer.Ocurrences oc = null;
		StringBuilder sb = new StringBuilder();
		for (String t : tokens) {
			if (t.length() < 3) // skip 0,1,2 length words
				continue;
			if (!stopWords.contains(t)) {
				stemmed = getStem(t);
				sb.append(stemmed + " ");
				if (dict.containsKey(stemmed)) {
					TreeMap<Long, Indexer.Ocurrences> ocl = dict.get(stemmed);
					if (ocl.containsKey(id)) {
						oc = ocl.get(id);
						Indexer.Ocurrences.updateOccurrentObj(oc, e);
					} else {
						oc = new Indexer.Ocurrences();
						Indexer.Ocurrences.updateOccurrentObj(oc, e);
						ocl.put(id, oc);
						dict.put(stemmed, ocl);
					}
				} else {
					// occurrence List
					TreeMap<Long, Indexer.Ocurrences> ocl = new TreeMap<Long, Indexer.Ocurrences>();
					oc = new Indexer.Ocurrences();
					Indexer.Ocurrences.updateOccurrentObj(oc, e);
					ocl.put(id, oc);
					dict.put(stemmed, ocl);
				}
			}
		}
		return sb.toString();
	}


}
