package com.mtk.ire;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.TreeMap;

public class IndexGenerator {
	static class Log {
		static BufferedWriter bw = null;

		static {
			try {
				bw = new BufferedWriter(new FileWriter("/home/mtk/indexGenerator.log", true));
				bw.write("Wiki index generator " + (new Date()).toString() + "\n");
			} catch (Exception e) {
				System.err.println("Exception in initializing index generator");
				e.printStackTrace();
			}
		}
	}

	static void createPrimaryIndex(String part) throws Exception {
		String inp = "/media/mtk/soft/tmp/main/merged-prod" + part ; //1-300";
		String posList = "/home/mtk/ireIndex/merged-prod"+part+".justPostingList";
		String terms = "/home/mtk/ireIndex/merged-prod"+part+".justTerms";
		BufferedWriter pibw = new BufferedWriter(new FileWriter(terms));
		BufferedWriter posbw = new BufferedWriter(new FileWriter(posList));
		BufferedReader br = new BufferedReader(new FileReader(inp));
		Log.bw.write("inp " + inp + "\n");
		Log.bw.write("terms " + terms+ "\n");
		Log.bw.write("posList " + posList + "\n");
		Log.bw.flush();
		String t = null, term, pos;
		long i = 0;
		Long bytes = 0L;
		Charset utf8 = Charset.forName("UTF-8");
		StringBuilder sb = new StringBuilder();
		while ((t = br.readLine()) != null) {
			sb.setLength(0);
			sb.trimToSize();
			String[] toks = t.split(":");
			term = toks[0];
			pos = toks[1];
			pibw.write(term + "-" + bytes + "\n"); // term-seekPosition
			posbw.write(pos+ "\n");
			i++;
			bytes += t.getBytes(utf8).length + 1 - term.toString().getBytes().length - 1;
		}
		pibw.close();
		posbw.close();
		br.close();
	}
	
	static void createSecondaryIndex(String part) throws Exception {
		String inp= "/home/mtk/ireIndex/merged-prod"+part+".justTerms";
		String secOut = "/home/mtk/ireIndex/merged-prod"+part+".justTerms.2nd";
		BufferedWriter sibw = new BufferedWriter(new FileWriter(secOut)); // secondary index bw
		BufferedReader br = new BufferedReader(new FileReader(inp));
		Log.bw.write("Writing secondary index");
		Log.bw.write("inp " + inp + "\n");
		Log.bw.write("secondaryIndex " + secOut + "\n");
		
		String t = null, term;
		long i=0;
		Long bytes = 0L;
		Charset utf8 = Charset.forName("UTF-8");
		StringBuilder sb = new StringBuilder();
		while ((t = br.readLine()) != null) {
			if(i%400 == 0) {
				sb.setLength(0);
				sb.trimToSize();
				term = t.split("-")[0];
				sibw.write(term + "-" + bytes + "\n");
			}
			bytes += t.getBytes(utf8).length + 1; // - id.toString().getBytes().length - 1;
			i++;
		}
		br.close();
		sibw.close();
	}
	
	static TreeMap<String,Long> readSecondaryIndex(String part) throws Exception {
		String inp = "/home/mtk/ireIndex/merged-prod"+part+".justTerms.2nd";
		BufferedReader br = new BufferedReader(new FileReader(inp));
		Log.bw.write("\nReading 2nd index in memory");
		Log.bw.write("inp " + inp + "\n");
		
		String t = null, term;
		long  seekPos, i=0;
		TreeMap<String, Long> secIndex = new TreeMap<String, Long>();
		while ((t = br.readLine()) != null) {
			term = t.split("-")[0];
			seekPos = Long.parseLong(t.split("-")[1]);
			secIndex.put(term, seekPos);
			i++;
		}
//		System.out.println("SecIndex size " + secIndex.size());
		return secIndex;
	}
	public static void main(String[] args) throws Exception {
		long start = System.currentTimeMillis();
//		createPrimaryIndex();
//		Log.bw.write("createPrimaryIndex time " + (System.currentTimeMillis() - start));
		
//		createSecondaryIndex();
//		Log.bw.write("createSecondaryIndex time " + (System.currentTimeMillis() - start));
		
		readSecondaryIndex("1-300");
		Log.bw.write("readSecondaryIndex time " + (System.currentTimeMillis() - start));
		
		Log.bw.close();
	}
}
