This is a Chinese Segmentor based on Hidden Markov Model. 

Basic principal:
I defined four positions of each Chinese character in a word. /B denotes Beginning, /M denotes Middle, /E denotes End, /S denotes Single   charater word. In the way we get examples like, 开/B始/E, 战/B斗/M力/E, 的/S. Since position of current character should depend on several   characters before it, I consider this as the hidden condition and try to adopt statistical learning. So final probility of position of current character P(Y) equals to probility of position of current character under condition of previous several characters with their positions times independent probility of current character with specific position. If we could find out the maximium probility of whole sequence, that could be the best segmentation result.

noted: 
1. I only consider the status of previous one character, which is called bigram. 
2. In order to process human names as well as place names which cannot be covered perfectly in corpus, I use a pre-defined probility for them, which allows segmentor to accept un volcabulary.
