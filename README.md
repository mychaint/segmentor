# Chinese Segmentor (Tokenizer)
Since word is the minimium unit to express meaning in various and complicated sentences. When conduct natural language processing in different languages, extracting words in a sentence is always an essential task at beginning. Unlike English, in which words are naturally splitted by spaces, Chinese has no interval between each two characters, so that Chinese segementor needs to identify correct combinations of consecutive characters. 

There are two challenges, ambiguity and non-volcabulary.
For ambiguity, the common case in Chinese is no matter you split or combine consequential characters, they are both make senses individually, but segmentor should only select one which is globally understandable. Such as "组合成分子", the correct result should be 组合/成/分子, however 组合,合成,成分,分子, they are all valid words in Chinese. Also like in two sentences "熟悉他的人才能够了解他的想法" and "公司在寻找人才加入我们的团队", "人才" needs to be considered differently, the first one must be "人/才" while the second one is "人才".
For non-volcabulary, such places' names like "黄山", "钓鱼台", "新加坡", human's names like "比尔盖茨", "周星驰", terminologies like "中国人民解放军", "中科院上海技术物理研究院", "电商" they need to be detected as a single word. However not all of them ever appeared in a dictionary, even anywhere for a new word.

### Introduction of Principle
This segmentor is based on First-Order Hidden Markov Model, and I used 4-tag model to learn observation
 sequence. I would like to briefly introduce the principal adopted in this segmentor.
#### Hidden Markov Model
As for a sequence of observation, I assumed there are only four different positions of each character in a word should be, Beginning of a word - "B" ("早/B上"), Middle of a word - "M" (中国/M人民解放军),End of a word - "E" (目的地/E), Single-character word - "S" (的/S). The probability of position of each character is able to be statisticed from corpus. If ![N|Solid](http://august-charter-92912.appspot.com/Resources/image/seqofOB.jpg) means the sequence of observation, each character should have its own probability of each position P(X). 

However, this probability is affected by relationship between current character and the previous one or serval characters which is conditional probability P(X|Y), and obviously it is a hidden feature of the observation sequence, thus we can say observation sequence can be interprated by Hidden Markov Model(HMM), meanwhile we denote overall probability of an observation sequence as ![N|Solid](http://august-charter-92912.appspot.com/Resources/image/overallP.jpg), where P(X) is possible to be learnt from corpus. Finally a combination result of predicted sequence with the optimal overall P must be the most probable result of segmentation. As mentioned above, Y is the position tags of characters before X. Conventionally, use ![N|Solid](http://august-charter-92912.appspot.com/Resources/image/preconditionY.jpg) is enough, because as for most popular corpus of Chinese, more than 90% Chinese words consist of only one or two characters, and if considering words of 
three characters, this percentage will increase up to around 95%. Thus, using Bigram or Trigram is able to theoratically provide a markable accrency. 
#### Machine Learning - Process the corpus 
Corpus is provided by Microsoft. The content of corpus is manually tagged with spaces to split different words.
It is shown below.

![N|Solid](http://august-charter-92912.appspot.com/Resources/image/corpus.jpg) 

I code a program to pre-process corpus with 4 tags I defined. Pre-processed corpus is showed below.

![N|Solid](http://august-charter-92912.appspot.com/Resources/image/corpus-preprocessed.jpg)

With this 4-tag model, I can start the program to statistic the probability of each tag of each character under current context. As described by Hidden Markov Random Field, I assume the only the character before current one will influence the probability of tag that current one should have, which we call Bigram Model. Afterward, program will output a file with statistic result showed below.

![N|Solid](http://august-charter-92912.appspot.com/Resources/image/corpus-sta.jpg)
#### Segmentation - Obtain optimal result using Dynamic Programming
To obtain optimal result, I used vertibi algorithms which is the typical method to get optimal result. I optimised this algorithm in serval aspects, such as ignore the irrational tag combinations like B-S, M-S, E-M, M-B, and provide a default probability for strange character combinations which cannot be found in corpus, such as non-vocabularies, person name, place name etc.. This operation makes segmentor assume the strange combinations are always reasonable if rest of sequence can provide a highest overall probability. So that this segmentor is able to detect non-vocabularies.
#### Sample
This is the input content.

![N|Solid](http://august-charter-92912.appspot.com/Resources/image/segNETin.jpg)

This is the segmentation result

![N|Solid](http://august-charter-92912.appspot.com/Resources/image/segNETres.jpg)

---
### References
- [1]Huang Chang-ning, Zhao Hai. Chinese Word Segmentation: A Decade Review. Journal of Chinese Information Processing, May 2007 : 8-19 
- [2]L.R.Rabiner, B.H.Juang. An Introduction to Hidden Markov Models. IEEE ASSP Magazine, Jan 1986 : 4-16 


### Project Files List
* src/ : java source code.
* tf/ : text files used to initialised tokenizer.
* tm/ : training materials, corpus from Microsoft Research Asia.

