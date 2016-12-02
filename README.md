<b>File List</b>
<p>src/ : java source code.</p>
<p>tf/ : text files used to initialised tokenizer.
<p>tm/ : training materials, corpus from Microsoft Research Asia.
<hr>
<b>Introduction of Principle</b>
<p>This segmenter is based on First-Order Hidden Markov Random Field and I used 4-tag model to process the observation
 sequence. I would like to briefly introduce the principal I adopted in this segmenter.
</p>
<b>Hidden Markov Model</b><br/>
<p>As for a sequence of observation, I assumed there are only four different positions of each character should be, <em>Beginning - "B"</em>,
<em>Middle - "M"</em>, <em>End - "E"</em>, <em>Single - "S"</em>. Therefore the probability of position of each character is able to be
statisticed from corpus. If <img src="http://august-charter-92912.appspot.com/Resources/image/seqofOB.jpg"> means the sequence of observation, each character should have its own probability of each position P(X). 
However, this probability contains a relation between current character and the previous one or serval characters which is P(X|Y) and also 
this is a hidden property of the sequence, thus we call this sequence is based on Hidden Markov Model(HMM) and denote the over all probability
of this sequence of observation is <img src="http://august-charter-92912.appspot.com/Resources/image/overallP.jpg">. <br/>
P(X) is able to be learnt from corpus and if we get the optimal 
over all P must be the most probable result of segmentation.</p>
<p>As mentioned above, Y is the status of characters before X. Conventionally, using <img src="http://august-charter-92912.appspot.com/Resources/image/preconditionY.jpg"/> is enough, 
because as for most popular corpus of Chinese, more than 90% Chinese words consist of only one or two characters, and if considering words of 
three characters, this percentage will increase up to around 95%. Thus, using Bigram or Trigram is able to theoratically provid a relatively 
high accrency. </p>
<b>Machine Learning - Process the corpus</b> <br/>
<p>Corpus is provided by Microsoft. The content of corpus is formed as each two vocabularies have a space as
interval. Showed below.</p>
<img src="http://august-charter-92912.appspot.com/Resources/image/corpus.jpg"/><br/><br/>
<p>Processing the corpus with 4 tags showed above
will let me get a preprocessed corpus, showed below.</p>
<img src="http://august-charter-92912.appspot.com/Resources/image/corpus-preprocessed.jpg"/><br/><br/>
<p>With this 4-tag model, I can start the program
to statistic the probability of each tag for each character under current context. As described by Hidden Markov 
Random Field, I assume the only the character before current one will influence the probability of tag that the
current one should have, which we call Bigram Model. Afterward, program will output a file with content showed below.</p>
<img src="http://august-charter-92912.appspot.com/Resources/image/corpus-sta.jpg"/><br/><br/>
<b>Segment - Obtain optimal result using Dynamic Programming</b>
<p>For obtaining optimal result, I used vertibi algorithms which is the typical method to process the getting optimal result
 issue. I optimised this algorithm in serval aspects, such as ignore the irrational tag combinations like B-S, M-S, E-M, M-B, and
 provide a default probability for strange character combinations(cannot find in corpus, such as non-vocabularies, person name, 
 place name etc.).</p>
<b>Sample</b>
<p>This is the input content.<br/>
<img src="http://august-charter-92912.appspot.com/Resources/image/segNETin.jpg"/><br/>
This is the segmentation result<br/>
<img src="http://august-charter-92912.appspot.com/Resources/image/segNETres.jpg"/></p>
<hr>
<p>References</p>
<p>Huang Chang-ning, Zhao Hai. Chinese Word Segmentation: A Decade Review. Journal of Chinese Information Processing, May 2007 : 8-19 </p>
<p>L.R.Rabiner, B.H.Juang. An Introduction to Hidden Markov Models. IEEE ASSP Magazine, Jan 1986 : 4-16 </p>
</div>