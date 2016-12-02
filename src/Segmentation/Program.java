package Segmentation;

import java.io.*;
import java.util.*;

public class Program {

	public static void main(String[] args) {
		CGSegmenter seg = new CGSegmenter(false);

		try {
			InputStream r = System.in;
			InputStreamReader re = new InputStreamReader(r);
			BufferedReader reader = new BufferedReader(re);
			String content = reader.readLine();
			System.out.println("---------------");
			List<String> list = seg.segmentSentence(content.toCharArray());
			for(String i : list){
				System.out.println(i);
			}
			System.out.println("---------------");
			
		} catch (IOException e) {
			
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
