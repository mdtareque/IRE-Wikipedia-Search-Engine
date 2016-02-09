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
	public List<Page>					pages			= new ArrayList<Page>();
	private static IPageParsedListener	listener;
	private Stack<String>				elementStack	= new Stack<String>();
	private Stack<Object>				objectStack		= new Stack<Object>();

	StringBuilder						tmp;

	static {
		listener = new PageParsedListener();
	}

	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		this.elementStack.push(qName);

		if ("page".equals(qName)) {
			Page page = new Page();
			this.objectStack.push(page);
			this.pages.add(page);
		}
		if ("text".equals(qName)) {
			tmp = new StringBuilder();
		}
	}

	public void endElement(String uri, String localName, String qName) throws SAXException {
		this.elementStack.pop();
		if ("page".equals(qName)) {
			if (pages.size() >= BUNDLE_SIZE) {
				fireBunchOfPagesParsedEvent();
			}
			this.objectStack.pop();
		}
		if ("mediawiki".equals(qName)) { // last residue pages
			if (pages.size() > 0) fireBunchOfPagesParsedEvent();
		}

		if ("text".equals(qName)) {
			Page page = (Page) this.objectStack.peek();
			page.text = tmp.toString();
		}
	}

	public void characters(char ch[], int start, int length) throws SAXException {

		String value = new String(ch, start, length).trim();
		if (value.length() == 0) {
			return;
		}

		if ("id".equals(currentElement()) && "page".equals(currentElementParent())) {
			Page page = (Page) this.objectStack.peek();
			page.docId = Integer.parseInt(value);
		} else if ("text".equals(currentElement()) && "revision".equals(currentElementParent())) {
			tmp.append(value);
		} else if ("title".equals(currentElement()) && "page".equals(currentElementParent())) {
			Page page = (Page) this.objectStack.peek();
			page.title = value;
		}
	}

	private String currentElement() {
		return this.elementStack.peek();
	}

	private String currentElementParent() {
		if (this.elementStack.size() < 2) return null;
		return this.elementStack.get(this.elementStack.size() - 2);
	}

	private void fireBunchOfPagesParsedEvent() {
		listener.dispatch(pages);
		pages.clear();
	}

}
