package com.mtk.ire;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class Log {

	static BufferedWriter bw = null;
	static {
		try {
			bw = new BufferedWriter(new FileWriter(INFO.GET_LOG_FILE));
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
		if(b) {
			System.out.println(s);
		}
		i(s);
	}
	public static void close() {
		try {
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
