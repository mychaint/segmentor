package Segmentation;

import java.util.*;
import java.io.*;

public class THash {

	private HashMap<Character, HashMap<Character, HashMap<Character, HashMap<Character, Double>>>> _myHashMap;

	private static Double INITIAL_VALUE = 1.0;
	private static Double POSSIBILITY_INTERPOLATE_VALUE = 1.02;

	public THash() {
		this._myHashMap = new HashMap<>();
	}

	public void PutValue(char pri_key, char pri_tag, char sec_key, char sec_tag) {
		if (!this._myHashMap.containsKey(pri_key)) {
			this._myHashMap.put(pri_key, new HashMap<Character, HashMap<Character, HashMap<Character, Double>>>());
		}
		HashMap<Character, HashMap<Character, HashMap<Character, Double>>> prihash = this._myHashMap
				.get(pri_key);

		if (!prihash.containsKey(pri_tag)) {
			prihash.put(pri_tag,
					new HashMap<Character, HashMap<Character, Double>>());
		}
		HashMap<Character, HashMap<Character, Double>> seccharhash = prihash
				.get(pri_tag);

		if (!seccharhash.containsKey(sec_key)) {
			seccharhash.put(sec_key, new HashMap<Character, Double>());
		}
		HashMap<Character, Double> sectaghash = seccharhash.get(sec_key);

		if (!sectaghash.containsKey(sec_tag)) {
			sectaghash.put(sec_tag, THash.INITIAL_VALUE);
		} else {
			Double _temp = sectaghash.get(sec_tag);
			_temp++;
		}
	}

	public void calculatePossibilityForAllCombinations(String path, String format) {
		File f = new File(path);
		try {
			if (!f.exists())
				f.createNewFile();
			PrintWriter writer = new PrintWriter(path, format);
			for(Character pri_key : this._myHashMap.keySet()){
				HashMap<Character, HashMap<Character, HashMap<Character, Double>>>
					_pritaghash = this._myHashMap.get(pri_key);
				for(Character pri_tag : _pritaghash.keySet()){
					HashMap<Character, HashMap<Character, Double>> 
					_sechash = _pritaghash.get(pri_tag);
					for(Character sec_key : _sechash.keySet()){
						HashMap<Character, Double> _sectaghash = _sechash.get(sec_key);
						Double total = 0.0;
						for(Character sec_tag : _sectaghash.keySet()){
							total += _sectaghash.get(sec_tag);
						}
						total *= THash.POSSIBILITY_INTERPOLATE_VALUE;
						for(Character sec_tag : _sectaghash.keySet()){
							StringBuilder sb = new StringBuilder();
							sb.append(pri_key);
							sb.append(pri_tag);
							sb.append(sec_key);
							sb.append(sec_tag);
							sb.append(_sectaghash.get(sec_tag) / total);
							writer.println(sb.toString());
						}
					}
				}
			}
			writer.close();
		} catch (IOException e) {
			System.err
					.println("Error in method calculatePossibilityForAllCombinations()");
		}

	}
}
