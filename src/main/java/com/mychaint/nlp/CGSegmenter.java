package com.mychaint.nlp;

import java.io.*;
import java.util.*;

/**
 * 
 * @author gong.chen
 * First Order HMM tokenizer, contains training methods
 *
 */
public class CGSegmenter {

	private String TrainingMaterialPath;
	private String TaggedTrainingMaterialPath;
	private String FinalTagFilePathForRelation;
	private String FinalTagFilePathForSingle;
	private String StatisticSumOfCharactersPath;
	private String DefaultFileFormat = "UTF-8";

	private THash _thash;
	private HashMap<String, Double> _charHash;

	private HashMap<String, HashMap<String, Double>> _tagHashForRelation;
	private HashMap<String, Double> _tagHashForSingle;

	private Double StrangeCombinationDefaultPossibility = 0.00035;
	private Double StrangeSingleDefaultPossibility = 0.0005;

	private List<String> resultlist;

	public void setDefaultStrangeCombinationPossibility(Double possibility) {
		this.StrangeCombinationDefaultPossibility = possibility;
	}

	public Double getStrangeCombinationDefaultPossibility() {
		return StrangeCombinationDefaultPossibility;
	}

	public void setStrangeSingleDefaultPossibility(
			Double strangeSingleDefaultPossibility) {
		StrangeSingleDefaultPossibility = strangeSingleDefaultPossibility;
	}

	public Double getStrangeSingleDefaultPossibility() {
		return StrangeSingleDefaultPossibility;
	}

	public CGSegmenter(boolean NeedInitialiseFinalTagFile) {
		this.FinalTagFilePathForRelation = "tf/tag-re.txt";
		this.FinalTagFilePathForSingle = "tf/tag-si.txt";
		this.TrainingMaterialPath = "tm/msr-in.txt";
		this.TaggedTrainingMaterialPath = "tm/msr-out.txt";
		this.StatisticSumOfCharactersPath = "tf/sum-si.txt";
		this.resultlist = new LinkedList<String>();
		if (NeedInitialiseFinalTagFile) {
			this.processTrainingMaterial();
			this.statisticTaggedTrainingMaterial();
		}
		this.initialiseTagHashMap();
	}

	public void statisticSumOfCharacters() throws Exception {
		File f = new File(this.getClass().getClassLoader().getResource(this.TrainingMaterialPath).getFile());
		if (!f.exists()) {
			System.err.println("未找到中文语料库文件： " + this.TrainingMaterialPath);
		} else {
			HashMap<Character, Integer> hashMap = new HashMap<>();
			char[] charArray = null;
			try {
				FileInputStream fis = new FileInputStream(f);
				InputStreamReader re = new InputStreamReader(fis,
						this.DefaultFileFormat);
				BufferedReader reader = new BufferedReader(re);
				String string = null;
				while ((string = reader.readLine()) != null) {
					charArray = string.toCharArray();
					for (char i : charArray) {
						if (i > 0x3400 && i < 0x9F00) {
							if (!hashMap.containsKey(i)) {
								hashMap.put(i, 0);
							}
						}
					}
				}
				File file = new File(this.getClass().getClassLoader().getResource(this.StatisticSumOfCharactersPath).getFile());
				if (!file.exists()) {
					System.err.println("为找到存储文件："
							+ this.StatisticSumOfCharactersPath);
				}
				FileOutputStream out = new FileOutputStream(file);
				OutputStreamWriter writer = new OutputStreamWriter(out,
						this.DefaultFileFormat);
				writer.write(new String("Total : " + hashMap.keySet().size() + "\r\n"));
				int n = 0;
				for (char i : hashMap.keySet()) {
					writer.write(i);
					n++;
					if (n % 50 == 0)
						writer.write("\r\n");
				}
				writer.flush();
				writer.close();
				System.out.println("文字统计完成。");
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	// 预处理中文语料库
	protected void processTrainingMaterial() {
		File f = new File(this.getClass().getClassLoader().getResource(this.TrainingMaterialPath).getFile());
		if (!f.exists()) {
			System.err.println("未找到中文语料库文件： " + this.TrainingMaterialPath);
		} else {
			try {
				if (!f.exists()) {
					f.createNewFile();
				}
				FileInputStream fis = new FileInputStream(f);
				InputStreamReader re = new InputStreamReader(fis,
						this.DefaultFileFormat);
				BufferedReader reader = new BufferedReader(re);
				String temp;
				PrintWriter writer = new PrintWriter(
						this.TaggedTrainingMaterialPath, this.DefaultFileFormat);
				System.out.println(new Date().toString() + " 开始预处理中文语料库。");
				while ((temp = reader.readLine()) != null) {
					char[] chararr = temp.toCharArray();
					StringBuilder sb = new StringBuilder();
					int i = 0;
					if (chararr[i] == 65279)
						i = 1;
					int j = i;
					while (j <= chararr.length - 1) {
						while (j < chararr.length - 1 && chararr[j] == ' ')
							j++;
						i = j;
						while (j < chararr.length - 1 && chararr[j] != 32)
							j++;
						if (j - i == 1) {
							sb.append(chararr[i] + "S");
						} else if (j - i == 2) {
							sb.append(chararr[i] + "B");
							sb.append(chararr[j - 1] + "E");
						} else if (j - i > 2) {
							sb.append(chararr[i++] + "B");
							while (i != j - 1) {
								sb.append(chararr[i++] + "M");
							}
							sb.append(chararr[i] + "E");
						}
						if (j >= chararr.length - 1)
							break;
					}
					writer.println(sb.toString());
				}
				System.out.println(new Date().toString() + " 完成中文语料库预处理。");
				writer.close();
				fis.close();
				re.close();
				reader.close();
			} catch (IOException e) {
				System.err
						.println("Error in method processTrainingMaterial() : "
								+ e.getMessage());
			}
		}
	}

	// 统计学习
	protected void statisticTaggedTrainingMaterial() {
		this.learningSingleTag(); // 状态独立概率P(x)
		this.learningRelationTag(); // 状态转移概率P(X |　Y)
	}

	// 学习状态独立概率
	protected void learningSingleTag() {
		File f = new File(this.getClass().getClassLoader().getResource(this.TaggedTrainingMaterialPath).getFile());
		if (!f.exists()) {
			System.err.println("未找到训练语料文件" + this.TaggedTrainingMaterialPath);
		} else {
			try {
				this._charHash = new HashMap<>();

				// 学习独立概率
				Double total = 0.0;
				FileInputStream fis = new FileInputStream(f);
				InputStreamReader re = new InputStreamReader(fis,
						this.DefaultFileFormat);
				BufferedReader reader = new BufferedReader(re);
				String temp = null;
				System.out.println(new Date().toString() + " 开始学习独立概率。");
				while ((temp = reader.readLine()) != null) {
					char[] chararr = temp.toCharArray();
					if (chararr.length == 0)
						continue;
					int i = 0;
					if ((int) chararr[i] == 65279)
						i++;
					while (i < chararr.length) {
						StringBuilder charsb = new StringBuilder();
						charsb.append(chararr[i]);
						charsb.append(chararr[i + 1]);
						if (this._charHash.containsKey(charsb.toString())) {
							Double _t = this._charHash.get(charsb.toString());
							_t = _t + 1.0;
							this._charHash.put(charsb.toString(), _t);
						} else
							this._charHash.put(charsb.toString(), 1.0);
						total += 1.0;
						i += 2;
					}
				}

				File _f = new File(this.getClass().getClassLoader().getResource(this.FinalTagFilePathForSingle).getFile());
				if (!_f.exists())
					_f.createNewFile();
				PrintWriter writer_char = new PrintWriter(
						this.FinalTagFilePathForSingle, this.DefaultFileFormat);

				for (String key : this._charHash.keySet()) {
					writer_char.print(key);
					writer_char.println(this._charHash.get(key) / total);
				}
				System.out.println(new Date().toString() + " 完成独立概率学习。");
				writer_char.close();
				fis.close();
				re.close();
				reader.close();
			} catch (IOException e) {
				System.err.println("Error in method learningSingleTag()");
			}
		}
	}

	// 学习状态转移概率
	protected void learningRelationTag() {
		File f = new File(this.getClass().getClassLoader().getResource(this.TaggedTrainingMaterialPath).getFile());
		if (!f.exists()) {
			System.err.println("未找到训练语料库" + this.TaggedTrainingMaterialPath);
		} else {
			try {
				// HMM学习
				this._thash = new THash();
				FileInputStream fis = new FileInputStream(f);
				InputStreamReader re = new InputStreamReader(fis,
						this.DefaultFileFormat);
				BufferedReader reader = new BufferedReader(re);
				String temp = null;
				System.out.println(new Date().toString() + " 开始学习状态转移概率。");
				while ((temp = reader.readLine()) != null) {
					char[] chararr = temp.toCharArray();
					int i = 0;
					while (i < chararr.length) {
						if (i == 0)
							this._thash.PutValue('~', '~', chararr[i],
									chararr[i + 1]);
						else
							this._thash.PutValue(chararr[i - 2],
									chararr[i - 1], chararr[i], chararr[i + 1]);
						i += 2;
					}
				}
				fis.close();
				re.close();
				reader.close();
				this._thash.calculatePossibilityForAllCombinations(
						this.FinalTagFilePathForRelation,
						this.DefaultFileFormat);
				System.out.println(new Date().toString() + " 完成状态转移概率学习。");
				System.out.println(new Date().toString() + " 训练语料库学习完毕。");
			} catch (IOException e) {
				System.err.println("Error in method learningRelationTag()");
			}
		}
	}

	// 初始化标注器表
	protected void initialiseTagHashMap() {
		File f = new File(this.getClass().getClassLoader().getResource(this.FinalTagFilePathForRelation).getFile());
		if (!f.exists()) {
			System.out
					.println("未找到标注器初始化文件" + this.FinalTagFilePathForRelation);
		} else {
			try {
				this._tagHashForRelation = new HashMap<>();
				FileInputStream fis = new FileInputStream(f);
				InputStreamReader re = new InputStreamReader(fis,
						this.DefaultFileFormat);
				BufferedReader reader = new BufferedReader(re);
				String temp = null;
				while ((temp = reader.readLine()) != null) {
					char[] chararr = temp.toCharArray();
					StringBuilder pri_key_sb = new StringBuilder();
					StringBuilder sec_key_sb = new StringBuilder();
					pri_key_sb.append(chararr[0]);
					pri_key_sb.append(chararr[1]);
					sec_key_sb.append(chararr[2]);
					sec_key_sb.append(chararr[3]);
					int j = 6;
					char[] pos_chararr = new char[7];
					for (int n = 0; n < 7; n++, j++) {
						pos_chararr[n] = chararr[j];
					}
					Double pos = 0.1 * this.convertStringtoDouble(pos_chararr,
							0);
					HashMap<String, Double> _hash;
					if (this._tagHashForRelation.containsKey(pri_key_sb
							.toString())) {
						_hash = this._tagHashForRelation.get(pri_key_sb
								.toString());
						_hash.put(sec_key_sb.toString(), pos);
					} else {
						_hash = new HashMap<>();
						_hash.put(sec_key_sb.toString(), pos);
						this._tagHashForRelation.put(pri_key_sb.toString(),
								_hash);
					}
				}
				fis.close();
				re.close();
				reader.close();
			} catch (IOException e) {
				System.out.println("Error in method initialise -> relation");
			}
		}

		f = new File(this.getClass().getClassLoader().getResource(this.FinalTagFilePathForSingle).getFile());
		if (!f.exists()) {
			System.out.println("为找到标注器初始化文件" + this.FinalTagFilePathForSingle);
		} else {
			try {
				this._tagHashForSingle = new HashMap<>();
				FileInputStream fis = new FileInputStream(f);
				InputStreamReader re = new InputStreamReader(fis,
						this.DefaultFileFormat);
				BufferedReader reader = new BufferedReader(re);
				String temp = null;
				while ((temp = reader.readLine()) != null) {
					char[] chararr = temp.toCharArray();
					StringBuilder sb = new StringBuilder();
					sb.append(chararr[0]);
					sb.append(chararr[1]);
					char[] pos_chararr = new char[5];
					for (int j = 0, i = 4; j < 5; j++, i++) {
						pos_chararr[j] = chararr[i];
					}
					Double pos = 0.1 * this.convertStringtoDouble(pos_chararr,
							0);
					pos = pos + chararr[2] - 48;
					if (chararr[chararr.length - 2] == '-') {
						int n = chararr[chararr.length - 1] - 48;
						for (int i = 0; i < n; i++) {
							pos *= 0.1;
						}
					}
					this._tagHashForSingle.put(sb.toString(), pos);
				}
				fis.close();
				re.close();
				reader.close();
			} catch (IOException e) {
				System.err.println("Error in method initialise -> single");
			}
		}
	}

	protected double convertStringtoDouble(char[] chararr, int position) {
		if (position == chararr.length - 1)
			return chararr[0] - 48;
		else
			return chararr[position] - 48 + 0.1
					* convertStringtoDouble(chararr, position + 1);
	}

	// 拆分句子，分离标点，符号，数字和英文字符等
	protected List<String> segmentSentence(char[] sentence) {
		int i = 0;
		int j = i;
		try {
			while (j < sentence.length) {

				if (sentence[j] == '。' || sentence[j] == '，'
						|| sentence[j] == '？' || sentence[j] == '：'
						|| sentence[j] == '！' || sentence[j] == ' '
						|| sentence[j] == '（' || sentence[j] == '）') {
					if (i < j)
						this.segmentWords(sentence, i, j - 1);
					i = ++j;
					continue;
				}

				if ((sentence[j] >= 65 && sentence[j] <= 90)
						|| (sentence[j] >= 97 && sentence[j] <= 122)) {
					if (i != j)
						this.segmentWords(sentence, i, j - 1);
					i = j;
					while (j < sentence.length
							&& ((sentence[j] >= 65 && sentence[j] <= 90) || (sentence[j] >= 97 && sentence[j] <= 122))) {
						j++;
					}
					StringBuilder sb = new StringBuilder();
					while (i < j) {
						sb.append(sentence[i]);
						i++;
					}
					this.resultlist.add(sb.toString());
					i = j;
					continue;
				}

				if (sentence[j] < 127) {
					if (i < j) {
						segmentWords(sentence, i, j - 1);
						i = j;
					}
					while (j < sentence.length && sentence[j] < 127) {
						j++;
					}
					StringBuilder sb = new StringBuilder();
					while (i < j) {
						sb.append(sentence[i]);
						i++;
					}
					this.resultlist.add(sb.toString());
					i = j;
					continue;
				}

				j++;
			}
			if (i < j)
				this.segmentWords(sentence, i, j - 1);
			return resultlist;
		} catch (OutOfMemoryError | ArrayIndexOutOfBoundsException e) {
			System.out.println(sentence.length);
			System.out.println(sentence[i]);
			System.out.println(sentence[j]);
			return this.resultlist;
		}
	}

	// 分词操作
	protected void segmentWords(char[] sentence, int start, int end) {
		int length = end - start + 1;
		GNode[][] graph = new GNode[length][4];

		for (int i = 0; i < length; i++) {
			for (int j = 0; j < 4; j++) {
				graph[i][j] = new GNode();
			}
		}

		// 初始化状态矩阵
		for (int j = 0; j < 4; j++) {
			graph[0][j].CurTag = getTag(j);
			if (j == 0 || j == 3) {
				StringBuilder sb = new StringBuilder();
				sb.append(sentence[start]);
				sb.append(graph[0][j].CurTag);
				graph[0][j].MaxPos = this.getPossiblity("~~", sb.toString());
			} else
				graph[0][j].MaxPos = 0.0;
		}

		// 动态规划过程
		for (int i = 1; i < length; i++) {
			for (int j = 0; j < 4; j++) {
				graph[i][j].CurTag = this.getTag(j);
				StringBuilder sec_key_sb = new StringBuilder();
				sec_key_sb.append(sentence[i + start]);
				sec_key_sb.append(graph[i][j].CurTag);
				for (int n = 0; n < 4; n++) {
					if (!this.checkLogicalCombination(graph[i - 1][n].CurTag,
							graph[i][j].CurTag))
						continue;
					StringBuilder pri_key_sb = new StringBuilder();
					pri_key_sb.append(sentence[i + start - 1]);
					pri_key_sb.append(graph[i - 1][n].CurTag);
					Double _pos = this.getPossiblity(pri_key_sb.toString(),
							sec_key_sb.toString());
					if (this._tagHashForSingle.containsKey(pri_key_sb
							.toString()))
						_pos *= this._tagHashForSingle.get(pri_key_sb
								.toString());
					else
						_pos *= this.StrangeSingleDefaultPossibility;
					_pos *= graph[i - 1][n].MaxPos;
					if (_pos >= graph[i][j].MaxPos) {
						graph[i][j].MaxPos = _pos;
						graph[i][j].PreTag = graph[i - 1][n].CurTag;
					}
				}
			}
		}

		// 筛选最优解
		int m = 0;
		Double _maxpos = 0.0;
		for (int j = 0; j < 4; j++) {
			if (graph[length - 1][j].MaxPos >= _maxpos) {
				_maxpos = graph[length - 1][j].MaxPos;
				m = j;
			}
		}

		char[] chararr = new char[length * 2];
		for (int i = end - start, j = chararr.length - 1, n = end; i >= 0
				&& j > 0; i--, j -= 2, n--) {
			chararr[j] = graph[i][m].CurTag;
			chararr[j - 1] = sentence[n];
			m = this.getInt(graph[i][m].PreTag);
		}

		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < chararr.length; i += 2) {
			sb.append(chararr[i]);
			if (chararr[i + 1] == 'E' || chararr[i + 1] == 'S') {
				this.resultlist.add(sb.toString());
				sb = new StringBuilder();
			} else if (i == chararr.length - 2)
				this.resultlist.add(sb.toString());
		}
	}

	protected char getTag(int value) {
		switch (value) {
		case 0:
			return 'B';
		case 1:
			return 'M';
		case 2:
			return 'E';
		case 3:
			return 'S';
		default:
			return 'S';
		}
	}

	protected int getInt(char tag) {
		switch (tag) {
		case 'B':
			return 0;
		case 'M':
			return 1;
		case 'E':
			return 2;
		case 'S':
			return 3;
		default:
			return 3;
		}
	}

	// 筛选可能搭配
	protected boolean checkLogicalCombination(char i, char j) {
		if ((i == 'B' || i == 'M') && (j == 'M' || j == 'E'))
			return true;
		if ((i == 'E' || i == 'S') && (j == 'B' || j == 'S'))
			return true;
		else
			return false;
	}

	protected Double getPossiblity(String pri_key, String sec_key) {
		if (this._tagHashForRelation.containsKey(pri_key)) {
			HashMap<String, Double> _hash = this._tagHashForRelation
					.get(pri_key);
			if (_hash.containsKey(sec_key))
				return _hash.get(sec_key);
		}
		return this.StrangeCombinationDefaultPossibility;
	}
}
