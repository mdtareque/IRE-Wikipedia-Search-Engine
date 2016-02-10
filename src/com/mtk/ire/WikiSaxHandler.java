package com.mtk.ire;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Extract page.title, page.id, page.revision.text
 * 
 * @author mtk
 *
 */
public class WikiSaxHandler extends DefaultHandler {

	private static final int			BUNDLE_SIZE		= 10;
//	public List<Page>					pages			= new ArrayList<Page>();
	static Page p = new Page();
//	private static IPageParsedListener	listener;
	private Stack<String>				elementStack	= new Stack<String>();
//	private Stack<Object>				objectStack		= new Stack<Object>();
	static long counter = 0;
	StringBuilder						tmp;

//	static { listener = new PageParsedListener();	}

	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		this.elementStack.push(qName);

		if ("page".equals(qName)) {
			p.reset();
//			Page page = new Page();
//			this.objectStack.push(page);
//			this.pages.add(page);
		}
		if ("text".equals(qName)) {
			tmp = new StringBuilder();
		}
	}

	public void endElement(String uri, String localName, String qName) throws SAXException {
		this.elementStack.pop();
		if ("page".equals(qName)) {
			PageProcessor.processPage(p);
			counter++;
			if (counter % INFO.PRINT_BLOCK == 0) {
				Log.i("pages processed so far " + counter + ", last page id " + p.docId);
				Log.flush();
			}

			/*if (pages.size() >= BUNDLE_SIZE) {
				fireBunchOfPagesParsedEvent();
			}*/
//			this.objectStack.pop();
		}
		/*if ("mediawiki".equals(qName)) { // last residue pages
			if (pages.size() > 0) fireBunchOfPagesParsedEvent();
		}*/

		if ("text".equals(qName)) {
//			Page page = (Page) this.objectStack.peek();
//			page.text = tmp.toString();
			p.text = tmp.toString();
		}
	}
	String value = null;
	public void characters(char ch[], int start, int length) throws SAXException {

		value = new String(ch, start, length).trim();
		if (value.length() == 0) {
			return;
		}

		if ("id".equals(currentElement()) && "page".equals(currentElementParent())) {
//			Page page = (Page) this.objectStack.peek();
//			page.docId = Integer.parseInt(value);
			p.docId = Integer.parseInt(value);
		} else if ("text".equals(currentElement()) && "revision".equals(currentElementParent())) {
			tmp.append(value);
		} else if ("title".equals(currentElement()) && "page".equals(currentElementParent())) {
//			Page page = (Page) this.objectStack.peek();
//			page.title = value;
			p.title = value;
		}
	}

	private String currentElement() {
		return this.elementStack.peek();
	}

	private String currentElementParent() {
		if (this.elementStack.size() < 2) return null;
		return this.elementStack.get(this.elementStack.size() - 2);
	}

/*	private void fireBunchOfPagesParsedEvent() {
		listener.dispatch(pages);
		pages.clear();
	}
*/
}
