package com.mtk.ire;

import java.io.IOException;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.net.URLEncoder;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class WikipediaParser {
	private final String baseUrl;

	public WikipediaParser(String lang) {
		this.baseUrl = String.format("http://%s.wikipedia.org/wiki/", lang);
	}

	public String fetchFirstParagraph(String article) throws IOException {
		String url = baseUrl + URLEncoder.encode(article.replaceAll(" ", "_"));
//		System.out.println(url);
		Document doc = Jsoup.connect(url)
				  .data("query", "Java")
				  .userAgent("Mozilla")
				  .cookie("auth", "token")
				  .timeout(3000)
				  .post();
		Elements paragraphs = doc.select(".mw-content-ltr p");

		Element firstParagraph = paragraphs.first();
		return firstParagraph.text();
	}

	public static String getSummary(String title) {
		System.setProperty("http.proxyHost", "proxy.iiit.ac.in");
		System.setProperty("http.proxyPort", "8080");
//	    Authenticator.setDefault(new DummyAuthenticator());
		WikipediaParser parser = new WikipediaParser("en");
		String firstParagraph = "";
		try {
			firstParagraph = parser.fetchFirstParagraph(title);
		} catch (IOException e) {
//			e.printStackTrace();
			return "";
		}
		String[] toks = firstParagraph.split(" ");
		int i=0;
		StringBuilder sb = new StringBuilder();
		int c=0, wc=0;
		for(String t : toks) {
			sb.append(t + " ");
			c+= t.length();
			wc++;
			c++;
			if(c > 90) { sb.append("\n"); c=0;}
			if(wc>80) break;
		}
		return sb.toString();
	}
	
	public static void main(String[] args) throws IOException {
			}

	   private static class DummyAuthenticator extends Authenticator {
	      public PasswordAuthentication getPasswordAuthentication() {
	         return new PasswordAuthentication(
	               "tareque.mohd@students.iiit.ac.in", "8080".toCharArray()
	               );
	      }
	   }
}