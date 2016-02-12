package com.mtk.ire;

import static java.nio.file.StandardOpenOption.READ;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.EnumSet;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

// build secondary index on titles
public class TitleIndexer {

	/*
	 * Got at 15 bytes 103 Got at 1901657 bytes 28167635 Total bytes are
	 * 555065000
	 * 
	 * real 1m7.725s user 1m2.592s sys 0m2.352s without 1 offset Got at 15 bytes
	 * 98 Got at 1901657 bytes 27121472
	 */
	static TreeMap<Long, Long> secondaryIndex = new TreeMap<Long, Long>();

	static void readIndex() throws Exception {
		String file = "/home/mtk/Downloads/dc/2nd-titles.index";
		@SuppressWarnings("resource")
		BufferedReader br = new BufferedReader(new FileReader(file));
		String t = null;
		long i = 0;
		Long bytes = 0L, id, seekPos;
		Charset utf8 = Charset.forName("UTF-8");
		while ((t = br.readLine()) != null) {
			id = Long.parseLong(t.split("-")[0]);
			seekPos = Long.parseLong(t.split("-")[1]);
			secondaryIndex.put(id, seekPos);
		}
		System.out.println("Secondary index size " + secondaryIndex.size());
		long start = System.currentTimeMillis();
		System.out.println("Testing");
		long testKey = 191242;
		// System.out.println(secondaryIndex.containsKey(testKey));
		// System.out.println(secondaryIndex.floorEntry(testKey));
		testKey = 1046163;
		System.out.println(secondaryIndex.containsKey(testKey));
		Entry<Long, Long> ans = secondaryIndex.floorEntry(testKey);
		System.out.println(ans);
		if (testKey == ans.getKey()) {
			System.out.println(seekByPos(testKey, ans.getValue()));
		} else {
			System.out.println(seekByPos(testKey, ans.getValue()));
			// System.out.println(seekByPos(ans.getValue()));
		}
		System.out.println("Time  " + (System.currentTimeMillis() - start));
	}

	static String seekByPos(Long key, Long pos) {
		System.out.println("Checking for " + key + " at pos " + pos);
		String f = "/Downloads/dc/titles-prod.txt";
		Path file = Paths.get(System.getProperty("user.home"), f);
		ByteBuffer buff = ByteBuffer.allocate(150);
		String encoding = System.getProperty("file.encoding");
		// We open the file in READ mode
		try (SeekableByteChannel sbc = Files.newByteChannel(file, EnumSet.of(READ))) {
			StringBuilder sb = new StringBuilder();
			System.out.println("\n\nchecking for position : " + pos);
			sbc.position(pos);
			sbc.read(buff);
			buff.flip();
			CharBuffer ans = Charset.forName(encoding).decode(buff);
			buff.rewind();
			String result = "";
			if (ans.toString().contains(key + " ")) {
				System.out.println("In if");
				int startIndex = ans.toString().indexOf(key + " ");
				result = ans.toString().substring(startIndex);
				// sb.append(result);
				if (result.contains("\n")) {
					System.out.print(result.substring(0, result.indexOf('\n')));
				} else {
					// now get till \n
					System.err.println("No end of line reached.. continue to read...");
					sb.append(result);
					ans = CharBuffer.wrap(result);
					while (!ans.toString().contains("\n")) {
						sbc.read(buff);
						buff.flip();
						ans = Charset.forName(encoding).decode(buff);
						buff.rewind();
						sb.append(ans);
						if (ans.toString().contains("\n")) {
							System.err.println("Got \\n");
							break;
						}
					}
					System.err.println("Answer");
					System.out.println(sb.toString().substring(0, sb.toString().indexOf('\n')));
				}
			} else {
				System.out.println("In else");
				while (!ans.toString().contains(key + " ")) {
					sbc.read(buff);
					buff.flip();
					ans = Charset.forName(encoding).decode(buff);
					buff.rewind();
					sb.append(ans);
					if (ans.toString().contains(key + " ")) {
						System.err.println("Got \\n");
						int startIndex = ans.toString().indexOf(key + " ");
						result = ans.toString().substring(startIndex); // remove
																		// anything
																		// before
																		// key,
																		// as it
																		// may
																		// contain
																		// \n
						// sb.append(result);
						if (result.contains("\n")) {
							System.out.print(result.substring(0, result.indexOf('\n')));
						} else {
							// now get till \n
							System.err.println("No end of line reached.. continue to read...");
							sb.append(result);
							ans = CharBuffer.wrap(result);
							while (!ans.toString().contains("\n")) {
								sbc.read(buff);
								buff.flip();
								ans = Charset.forName(encoding).decode(buff);
								buff.rewind();
								sb.append(ans);
								if (ans.toString().contains("\n")) {
									System.err.println("Got \\n");
									break;
								}
							}
							System.err.println("Answer");
							System.out.println(sb.toString().substring(0, sb.toString().indexOf('\n')));
						}
						break;
					}
				}
			}
			// Clear buffer
			buff.clear();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}

		return "";
	}

	static void index() throws Exception {
		String file = "/home/mtk/Downloads/dc/titles-prod.txt";
		String out = "/home/mtk/Downloads/dc/2nd-titles.index";
		@SuppressWarnings("resource")
		BufferedReader br = new BufferedReader(new FileReader(file));
		String t = null;
		long i = 0;
		Long bytes = 0L, id;
		Charset utf8 = Charset.forName("UTF-8");
		while ((t = br.readLine()) != null) {
			id = Long.parseLong(t.split(" ")[0]);
			if (i % 4000 == 0) {
				secondaryIndex.put(id, bytes);
			}
			i++;
			bytes += t.getBytes(utf8).length + 1;
		}
		br.close();
		BufferedWriter bw = new BufferedWriter(new FileWriter(out));
		Set<Long> keys = secondaryIndex.keySet();
		for (Long l : keys) {
			bw.write(l + "-" + secondaryIndex.get(l) + "\n");
		}
		bw.close();
		System.out.println("Secondary index size " + secondaryIndex.size());
	}

	public static void read(String[] args) throws Exception {
		String file = "/home/mtk/Downloads/dc/titles-prod.txt";
		BufferedReader br = new BufferedReader(new FileReader(file));
		String t = null;
		long i = 0;
		long bytes = 0;
		Charset utf8 = Charset.forName("UTF-8");
		Charset utf16 = Charset.forName("UTF-16");
		while ((t = br.readLine()) != null) {
			if (Long.parseLong(t.split(" ")[0]) == 1901657L) {
				System.out.println("Got at  1901657 bytes " + bytes);
				break;
			}
			if (Long.parseLong(t.split(" ")[0]) == 12L) {
				System.out.println("Got at  12 bytes " + bytes);
				// break;
			}
			bytes += t.getBytes(utf8).length + 1;
			/*
			 * if(i%900 ==0 ) { // secondaryIndex.put(, value) }
			 */ }
		System.out.println("Total bytes are " + bytes);

	}

	public static void seek(String[] args) {
		// The path we are going to open - the file containing the acrostic
		// Path acrostic = Paths.get(System.getProperty("user.home"),
		// "acrostic.txt");
		String f = "/Downloads/dc/titles-prod.txt";
		Path file = Paths.get(System.getProperty("user.home"), f);
		// The ByteBuffer is an array of 3 characters
		ByteBuffer buff = ByteBuffer.allocate(150);

		// Obtain encoding
		String encoding = System.getProperty("file.encoding");

		Long[] indexes = { 23L, 28216383L }; // , 57380912L, 28167375L};//,
												// 237876L, 23L};

		// We open the file in READ mode
		try (SeekableByteChannel sbc = Files.newByteChannel(file, EnumSet.of(READ))) {

			// We jump on every index using position , we read the characters
			// in the buffer and we print them on the screen.
			StringBuilder sb = new StringBuilder();
			for (Long idx : indexes) {
				System.out.println("\n\nchecking for position : " + idx);
				sbc.position(idx);
				sbc.read(buff);
				buff.flip();
				CharBuffer ans = Charset.forName(encoding).decode(buff);
				buff.rewind();
				if (ans.toString().contains("\n")) {
					System.out.println("In if");
					System.out.print(ans.toString().substring(0, ans.toString().indexOf('\n')));
				} else {
					System.err.println("No end of line reached.. continue to read...");
					sb.append(ans);
					while (!ans.toString().contains("\n")) {
						sbc.read(buff);
						buff.flip();
						ans = Charset.forName(encoding).decode(buff);
						buff.rewind();
						sb.append(ans);
						if (ans.toString().contains("\n")) {
							System.err.println("Got \\n");
							break;
						}
					}
					System.err.println("Answer");
					System.out.println(sb.toString().substring(0, sb.toString().indexOf('\n')));
				}

			}

			// Clear buffer
			buff.clear();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}

	public static void main(String[] args) throws Exception {
		// read(args);
		// seek(args);
		// index();
//		readIndex();
		
		createPrimaryIndex();
		
		int[] i = {10, 12, 18, 23};
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		while(true) {
			int i1 = Integer.parseInt(br.readLine());
			if(i1 ==0)break;
			System.out.println("readTitle " + i1);
			readTitleBySeek(i1);
		}
	}
	static TreeMap<Long, Long> getPrimaryIndex(String f) throws Exception {
		BufferedReader br = new BufferedReader(new FileReader(f));
		String t = null;
		long  id, seekPos;
		TreeMap<Long, Long> priIndex = new TreeMap<Long, Long>();
		while ((t = br.readLine()) != null) {
			id = Long.parseLong(t.split("-")[0]);
			seekPos = Long.parseLong(t.split("-")[1]);
			priIndex.put(id, seekPos);
		}
		System.out.println("Primary index read into memory, size is " + priIndex.size());
		return priIndex;
	}
	
	static void readTitleBySeek(long id) throws Exception {
		String primaryIndexFile = "/home/mtk/Downloads/dc/primaryIndex2.txt";
		
		TreeMap<Long, Long> primaryIndex = getPrimaryIndex(primaryIndexFile);
		Long firstSeekPos = primaryIndex.get(id);
		System.out.println("firstSeekPos " + firstSeekPos);
		
		String titles = "/home/mtk/Downloads/dc/justTitles2.txt";
		Path file = Paths.get(titles);
		ByteBuffer buff = ByteBuffer.allocate(50);
		String encoding = System.getProperty("file.encoding");
		try (SeekableByteChannel sbc = Files.newByteChannel(file, EnumSet.of(READ))) {

			System.out.println("\n\nchecking for position : " + id);
			sbc.position(firstSeekPos);
			sbc.read(buff);
			buff.flip();
			CharBuffer ans = Charset.forName(encoding).decode(buff);
			buff.rewind();
			String res = ans.toString();
			buff.clear();
			System.out.println("res is " + res + ".");
			System.out.println(res.substring(0, res.indexOf('\n')));
		
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}

	static void createPrimaryIndex() throws Exception {
		String file = "/home/mtk/Downloads/dc/titles-prod.txt";
		String primaryIndex = "/home/mtk/Downloads/dc/primaryIndex2.txt";
		String titles = "/home/mtk/Downloads/dc/justTitles2.txt";
		BufferedWriter pibw = new BufferedWriter(new FileWriter(primaryIndex));
		BufferedWriter titlesbw = new BufferedWriter(new FileWriter(titles));
		
		
		BufferedReader br = new BufferedReader(new FileReader(file));
		String t = null, title;
		long i = 0;
		Long bytes = 0L, id;
		Charset utf8 = Charset.forName("UTF-8");
		StringBuilder sb = new StringBuilder();
		while ((t = br.readLine()) != null) {
			sb.setLength(0);
			sb.trimToSize();
			String[] toks = t.split(" ");
			id = Long.parseLong(toks[0]);
			for(int j=1; j<toks.length; j++)
				sb.append(toks[j] + " ");
			title = sb.toString().trim();
//			seekPos = Long.parseLong(t.split("-")[1]);
			pibw.write(id + "-" + bytes + "\n");
			titlesbw.write(title + "\n");
//			secondaryIndex.put(id, bytes);
			i++;
			bytes += t.getBytes(utf8).length + 1 - id.toString().getBytes().length - 1;
		}
		pibw.close();
		titlesbw.close();
		br.close();

		/*Set<Long> keys = secondaryIndex.keySet();
		for (Long l : keys)

		{
			bw.write(l + "-" + secondaryIndex.get(l) + "\n");
		}
		bw.close();
		System.out.println("Secondary index size " + secondaryIndex.size());
*/	

	}
}
