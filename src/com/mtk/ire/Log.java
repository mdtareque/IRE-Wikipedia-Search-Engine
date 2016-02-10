package com.mtk.ire;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;

public class Log {

	static long appStart = 0;
	static BufferedWriter bw = null;

	static {
		Date date = new Date();
		appStart = System.currentTimeMillis();
		try {
			bw = new BufferedWriter(new FileWriter(INFO.GET_LOG_FILE, true));
			bw.write("\nApp Start Time : " + date.toString());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void i(String s) {
		try {
			bw.write(s + "\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void flush() {
		try {
			bw.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void i(String s, boolean b) {
		if (b) {
			System.out.println(s);
		}
		i(s);
	}

	public static void close() {
		Date date = new Date();
		Log.i("App End time : " + date.toString());
		Log.i("Application RunTime " + (System.currentTimeMillis() - appStart));
		try {
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
