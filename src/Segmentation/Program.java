package Segmentation;

import java.io.*;
import java.util.*;

public class Program {

	public static void main(String[] args) throws IOException {
		
		CGSegmenter seg = new CGSegmenter(false);
		InputStreamReader re = null;
		
		if (args.length > 0) {
			String filePath = args[0];
			File file = new File(filePath);
			if(!file.exists()){
				System.err.println("File not exists.");
			} else {
				FileInputStream fis = new FileInputStream(file);
				re = new InputStreamReader(fis);

			}
			
		} else {
			InputStream r = System.in;
			re = new InputStreamReader(r);
			
		}
		
		BufferedReader reader = new BufferedReader(re);
		String content = reader.readLine();
		System.out.println("Result:");
		List<String> list = seg.segmentSentence(content.toCharArray());
		for(String i : list){
			System.out.println(i);
		}
		
//		Stemmer stem = new Stemmer();
//		String string = "created";
//		String string1 = "avail";
//		stem.add(string.toCharArray(), string.toCharArray().length);
//		stem.stem();
//		System.out.println(stem.toString());
//		stem.add(string1.toCharArray(), string1.toCharArray().length);
//		stem.stem();
//		System.out.println(stem.toString());
	}
}
