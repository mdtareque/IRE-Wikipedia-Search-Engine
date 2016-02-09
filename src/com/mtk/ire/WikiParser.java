package com.mtk.ire;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Holds the parsing logic for wikipedia xml pages to get references,
 * infoboxes, categories, external links, citations
 * 
 * @author mtk
 *
 */
public class WikiParser {
	
	static Pattern					doubleCurly, ref, htmlComment, cite;
	static private Pattern[]		citesPattern;
	static private StringBuilder	tmp;
	static private String			out;
	static private final String		INFOBOX_CONST_STR	= "{{infobox";
	static private final String		CITE_START			= "{{cite";
	static private final String		REF1				= "==references==";
	static private final String		REF2				= "== references ==";
	static private final String		EXT_LINK1			= "==external links==";
	static private final String		EXT_LINK2			= "== external links ==";
	static private final String		DEFAULT_SORT_STR	= "defaultsort";
	static private final String		CATEGORY_STR		= "[[category";

	static private final String		DOUBLE_EQUALS		= "==";
	static private int				startPos, lastStartPos, endPos, bracketCount, st, indexTmp;

	static private Pattern			p;
	static private Matcher			m;

	static {
		tmp = new StringBuilder();
		doubleCurly = Pattern.compile("\\{\\{.*?\\}\\}", Pattern.DOTALL);
		ref = Pattern.compile("<ref>.*?</ref>", Pattern.DOTALL);
		htmlComment = Pattern.compile("<!--.*?-->", Pattern.DOTALL);
		cite = Pattern.compile("\\{\\{ ?cite(.*?)\\}\\}", Pattern.DOTALL);
		// citeContents: lastName, firstName, title, publisher, place, location,
		// encyclopedia
		citesPattern = new Pattern[13];
		citesPattern[0] = Pattern.compile("last ?=(.*?)(\\||\\})", Pattern.DOTALL);
		citesPattern[1] = Pattern.compile("first ?=(.*?)(\\||\\})", Pattern.DOTALL);
		citesPattern[2] = Pattern.compile("title ?=(.*?)(\\||\\})", Pattern.DOTALL);
		citesPattern[3] = Pattern.compile("publisher ?=(.*?)(\\||\\})", Pattern.DOTALL);
		citesPattern[4] = Pattern.compile("place ?=(.*?)(\\||\\})", Pattern.DOTALL);
		citesPattern[5] = Pattern.compile("location ?=(.*?)(\\||\\})", Pattern.DOTALL);
		citesPattern[6] = Pattern.compile("encyclopedia ?=(.*?)(\\||\\})", Pattern.DOTALL);
		citesPattern[7] = Pattern.compile("authorlink ?=(.*?)(\\||\\})", Pattern.DOTALL);
		citesPattern[8] = Pattern.compile("year ?=(.*?)(\\||\\})", Pattern.DOTALL);
		citesPattern[9] = Pattern.compile("quote ?=(.*?)(\\||\\})", Pattern.DOTALL);
		citesPattern[10] = Pattern.compile("journal ?=(.*?)(\\||\\})", Pattern.DOTALL);
		citesPattern[11] = Pattern.compile("author ?=(.*?)(\\||\\})", Pattern.DOTALL);
		citesPattern[12] = Pattern.compile("work ?=(.*?)(\\||\\})", Pattern.DOTALL);
		p = Pattern.compile("\\[\\[:?category:(.*?)\\]\\]");
	}

	private static String removeCite(String text) {

		startPos = text.indexOf(CITE_START);
		if (startPos < 0) return text;
		bracketCount = 2;
		endPos = startPos + CITE_START.length();
		for (; endPos < text.length(); endPos++) {
			switch (text.charAt(endPos)) {
				case '}':
					bracketCount--;
					break;
				case '{':
					bracketCount++;
					break;
				default:
			}
			if (bracketCount == 0) break;
		}
		text = text.substring(0, startPos - 1) + text.substring(endPos);
		return removeCite(text);
	}

	String parseCite(String c) {
		tmp.setLength(0);
		tmp.trimToSize();
		m = null;
		for (Pattern p : citesPattern) {
			m = p.matcher(c);
			while (m.find()) {
				tmp.append(m.group(1).trim() + " ");
			}
		}
		return tmp.toString().trim();
	}

	String getRef(String text) {
		st = text.indexOf(REF1);
		if (st == -1) {
			st = text.indexOf(REF2);
		}
		if (st == -1) return "";
		st = st + REF1.length() - 1;
		tmp.setLength(0);
		tmp.trimToSize();
		int nextEqualTo = text.indexOf(DOUBLE_EQUALS, st);
		if (nextEqualTo == -1) {
			nextEqualTo = text.indexOf("defaultsort", st);
		}
		if (nextEqualTo == -1) {
			nextEqualTo = text.indexOf("[[category", st);
		}
		nextEqualTo = nextEqualTo == -1 ? text.length() : nextEqualTo;
		String refs = text.substring(st, nextEqualTo);
		refs = refs.replaceAll("(;notes)|(\\{\\{reflist.*?\\}\\})", "");
		m = cite.matcher(ref.toString());
		while (m.find()) {
			tmp.append(parseCite(m.group(1)));
		}
		refs = cite.matcher(refs).replaceAll("");
		// remove [http: ..] and get data
		tmp.append(refs.replaceAll("\\[http:.*? (.*?)\\]", " $1 "));
		return tmp.toString().replaceAll("(\\P{Alpha})", " ").replaceAll("\\p{Blank}", " ");
	}

	String getExternalLinks(String text) {
		st = text.indexOf(EXT_LINK1);
		if (st == -1) {
			st = text.indexOf(EXT_LINK2);
		}
		if (st == -1) return "";
		st = st + EXT_LINK1.length() - 1;
		int nextEqualTo = text.indexOf(DOUBLE_EQUALS, st);
		if (nextEqualTo == -1) {
			nextEqualTo = text.indexOf(DEFAULT_SORT_STR, st);
		}
		if (nextEqualTo == -1) {
			nextEqualTo = text.indexOf(CATEGORY_STR, st);
		}
		nextEqualTo = nextEqualTo == -1 ? text.length() : nextEqualTo;
		out = text.substring(st, nextEqualTo);
		return out.replaceAll("\\P{Alpha}", " ").replaceAll("\\p{Blank}", " ");
	}

	String getCategories(String text) {
		indexTmp = text.indexOf("[[Category");
		if(indexTmp == -1) return "";
		text = text.substring(indexTmp);
		m = p.matcher(text);
		tmp.setLength(0);
		tmp.trimToSize();
		while (m.find())
			tmp.append(m.group(0));
		out = tmp.toString().replaceAll("(\\[\\[:?category:)|(\\]\\])", " ");
		return out.replaceAll("(\\P{Alnum})", " ").replaceAll("\\p{Blank}", " ");
	}

	String getInfoBox(String text) {
		lastStartPos = 0;
		tmp.setLength(0);
		tmp.trimToSize();
		while (true) {
			startPos = text.indexOf(INFOBOX_CONST_STR, lastStartPos);
			if (startPos < 0) break;
			lastStartPos = startPos + INFOBOX_CONST_STR.length();
			bracketCount = 2;
			endPos = startPos + INFOBOX_CONST_STR.length();
			for (; endPos < text.length(); endPos++) {
				switch (text.charAt(endPos)) {
					case '}':
						bracketCount--;
						break;
					case '{':
						bracketCount++;
						break;
					default:
				}
				if (bracketCount == 0) break;
			}
			if (endPos + 1 >= text.length()) break;
			out = removeCite(text.substring(lastStartPos, endPos + 1));
			out = out.replaceAll("\\p{Punct}", " ");
			tmp.append(out);
		}
		return tmp.toString().replaceAll("(\\P{Alpha})", " ").replaceAll("\\p{Blank}", " ");
	}

}
