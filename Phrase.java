
import jdbm.RecordManager;
import java.util.Vector;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Enumeration;

import jdbm.RecordManager;
import jdbm.RecordManagerFactory;

@SuppressWarnings("unchecked")
public class Phrase {

	private RecordManager 	recman;
	private Vector<String> 	words;
	private DataManager 	pageInfo;
	private DataManager 	wordID;
	private DataManager 	bodyWord;
	private DataManager 	titleWord;
	private DataManager		pageUrl;

	// save important information of the phrase, e.g. phrase frequency, phrase weight
	private Vector<Vector<Posting>>		bPosts;
	private Vector<Vector<Posting>>		tPosts;
	private Vector<Integer>				wordPosInQuery;
	private Hashtable<String, Integer> 	bodyPfs;
	private Hashtable<String, Integer> 	titlePfs;
	private Hashtable<String, Double>	bodyPhWeights;
	private Hashtable<String, Double>	titlePhWeights;

	private Hashtable<String, Integer>	bodyDFs;
	private Hashtable<String, Integer>	titleDFs;

	public String pageIDtoURL(String pageID) throws IOException
	{
		return String.valueOf(pageUrl.getEntry(pageID));
	}

	public Phrase(String phrase) throws IOException {
		recman = RecordManagerFactory.createRecordManager("Database");

		wordPosInQuery = new Vector<Integer>();
		pageInfo 	= new DataManager(recman, "pageInfo");
		wordID 		= new DataManager(recman, "wordID");
		bodyWord 	= new DataManager(recman, "bodyWord");
		titleWord 	= new DataManager(recman, "titleWord");
		pageUrl		= new DataManager(recman, "pageUrl");

		bodyPfs		= new Hashtable<String, Integer>();
		titlePfs 	= new Hashtable<String, Integer>();
		bodyPhWeights	= new Hashtable<String, Double>();
		titlePhWeights	= new Hashtable<String, Double>();

		bodyDFs 	= new Hashtable<String, Integer>();
		titleDFs	= new Hashtable<String, Integer>();

		words = new Vector<String>();
		String[] temp = phrase.split(" ");
		boolean noSuchKeywordInDb = false;
		for(int i = 0; i < temp.length; i++) {
			String stem = StopStem.processWord(temp[i]);
			if(stem == null || stem.equals("")) continue;
			String word = String.valueOf(wordID.getEntry(stem));
			if(word == null || word.equals("")) noSuchKeywordInDb = true;
			words.add(word);
			wordPosInQuery.add(i);
		}
		if(!noSuchKeywordInDb) {
			bPosts = initializePosting(bodyWord);
			tPosts = initializePosting(titleWord);

			calculateDF(bodyWord, bodyDFs);
			calculateDF(titleWord, titleDFs);

			calculatePhraseFreq(bPosts, bodyPfs);
			calculatePhraseFreq(tPosts, titlePfs);

			calculateWeight(bodyPhWeights, bodyDFs, bodyPfs);
			calculateWeight(titlePhWeights, titleDFs, titlePfs);
		}
	}

	public Hashtable<String, Double> getWeight(boolean isBody)
	{
		if(isBody) return bodyPhWeights;
		else return titlePhWeights;
	}

	public Vector<Vector<Posting>> initializePosting(DataManager wordInfo) throws IOException {
		Vector<Vector<Posting>> posts = new Vector<Vector<Posting>>();
		Vector<Posting> p;
		// initialize posting lists from phrase words
		for(String w: words) {
			p = (Vector<Posting>) wordInfo.getEntry(w);
			if(p != null) {
				posts.add(p);
			}
		}
		return posts;
	}

	public void calculateDF(DataManager wordInfo, Hashtable<String, Integer> dfs) throws IOException {
		for(String w: words) {
			Vector<Posting> p = (Vector<Posting>) wordInfo.getEntry(w);
			if(p != null)
				dfs.put(w, p.size());
			else
				dfs.put(w, 0);
		}
	}

	public void calculatePhraseFreq(Vector<Vector<Posting>> posts, Hashtable<String, Integer> pfs) throws IOException {
		// convert from (vector of posting list) to 
		// (a hashtable with page id as keys and the posting list corresponds to the phrase word as values)
		Hashtable<String, Vector<Posting>> ht = new Hashtable<String, Vector<Posting>>();
		Vector<Posting> temp;
		for(Vector<Posting> pList: posts)
		{
			for(Posting p: pList)
			{
				String pageID = p.pageID;
				if(ht.get(pageID) != null)
					temp = ht.get(pageID);
				else
					temp = new Vector<Posting>();
				temp.add(p);
				ht.put(pageID, temp);
			}
		}

		// now we find the freq of the phrase of all pages
		Enumeration<String> e = ht.keys();
		while(e.hasMoreElements())
		{
			String pageID = e.nextElement();
			Vector<Posting> pList = ht.get(pageID);
			if(pList.size() == words.size()) {
				int pf = findFreq(pList);
				if(pf > 0)
					pfs.put(pageID, pf);
			}
		}
	}

	public void calculateWeight(Hashtable<String, Double> weight, Hashtable<String, Integer> dfs, Hashtable<String, Integer> pfs) throws IOException
	{
		double idf = 0.0;
		// for each term in phrase, calculate idf of the term, and then take the total sum of it
		for(String w: words)
		{
			int df = dfs.get(w);
			if(df == 0.0)
			{
				idf = 0.0;
				break;
			}
			idf += Math.log(((double) pageInfo.getTableSize()) / df) / Math.log(2);
		}

		Enumeration<String> e = pfs.keys();
		while(e.hasMoreElements())
		{
			String pageID = e.nextElement();
			weight.put(pageID, (idf * (double)pfs.get(pageID) / words.size()));
		}

	}

	public int findFreq(Vector<Posting> pList) throws IOException
	{
		int freq = 0;
		Vector<Vector<Integer>> wordPos = new Vector<Vector<Integer>>();
		Vector<Integer> indexes = new Vector<Integer>();	// pointers to word position list

		for(Posting p: pList)
		{
			wordPos.add(p.word_pos);
			indexes.add(0);
		}

		Vector<Integer> currWordPos;
		Vector<Integer> nextWordPos;
		Integer currWordIndex;
		Integer nextWordIndex;

		while(indexes.get(0) < wordPos.get(0).size())
		{
			boolean adjacent = false;
			for(int i = 0; i < indexes.size()-1; i++)
			{
				currWordPos = wordPos.get(i);
				nextWordPos = wordPos.get(i+1);
				currWordIndex = indexes.get(i);
				nextWordIndex = indexes.get(i+1);
				while(nextWordIndex < nextWordPos.size() && nextWordPos.get(nextWordIndex) < currWordPos.get(currWordIndex))
					nextWordIndex++;

				// set the pointer of the next phrase comparsion
				indexes.set(i+1, nextWordIndex);

				if(nextWordIndex >= nextWordPos.size())	// out of bound
					break;
				else if(nextWordPos.get(nextWordIndex) == currWordPos.get(currWordIndex) + wordPosInQuery.get(i+1) - wordPosInQuery.get(i))	// exact adjacency
					adjacent = true;
				else {
					adjacent = false;
					break;
				}
			}
			if(adjacent)	// the phrase passes all tests
				freq++;

			indexes.set(0, indexes.get(0) + 1);
		}

		return freq;
	}

	public static void main(String[] args) throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		System.out.println("Enter a phrase: ");
		String p = br.readLine();
		Phrase pp = new Phrase(p);
		Hashtable<String, Double> bodyPhWeights = pp.getWeight(true);
		Hashtable<String, Double> titlePhWeights = pp.getWeight(false);
		Enumeration<String> e = bodyPhWeights.keys();
		while(e.hasMoreElements())
		{
			String pageID = e.nextElement();
			System.out.println("Score: " + bodyPhWeights.get(pageID) + " --- " + pp.pageIDtoURL(pageID));
		}
	}
}