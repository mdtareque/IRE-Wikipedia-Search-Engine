package com.mtk.ire;
/**
 * Enum to keep track of what part of page is to be extracted
 * 
 * @author mtk
 *
 */
enum LOCATION {
	TITLE, REF, BODY, INFOBOX, CATEGORY, EXTERNAL_LINKS
}

/**
 * Simple Page Class encapsulating all contents of a wiki page
 * 
 * @author mtk
 *
 */
public class Page {
	long			docId;

	/*
	 * String title0;
	 * String text0;
	 * String categoriesText0;
	 * String infoboxesText0;
	 * String externalLinkText0;
	 * public String referencedText0;
	 */

	String			title;
	String			text;
	String			categoriesText;
	String			infoboxesText;
	String			externalLinkText;
	public String	referencedText;

	@Override
	public String toString() {
		return docId + " " + title;
	}

}
