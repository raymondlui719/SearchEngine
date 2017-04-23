import java.util.Vector;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Enumeration;
import java.util.Collections;
import java.net.URL;

import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;

import jdbm.RecordManager;
import jdbm.RecordManagerFactory;
import jdbm.helper.FastIterator;
import jdbm.htree.HTree;

import org.htmlparser.Parser;
import org.htmlparser.util.ParserException;

@SuppressWarnings("unchecked")
public class Query {
    private RecordManager recman;
    private DataManager wordID;
    private DataManager word;
    private DataManager bodyWord;
    private DataManager titleWord;
    private DataManager pageBodyWord;
    private DataManager pageTitleWord;
    private DataManager pageBodyMaxTF;
    private DataManager pageTitleMaxTF;
    private DataManager pageUrl;
    private TermWeight termWeight;
    private Hashtable<String, Score> pageScore;

    public Query(RecordManager _recman) throws IOException {
        recman = _recman;
        wordID = new DataManager(recman, "wordID");     // word --> word-id
        word = new DataManager(recman, "word");     // word-id --> word
        bodyWord = new DataManager(recman, "bodyWord"); // word-id --> {page-id, tf}
        titleWord = new DataManager(recman, "titleWord");   
        pageBodyWord = new DataManager(recman, "pageBodyWord"); // forward index (page-id --> {keywords})
        pageTitleWord = new DataManager(recman, "pageTitleWord");
        pageUrl = new DataManager(recman, "pageUrl");
        termWeight = new TermWeight(recman);
        pageScore = new Hashtable<String, Score>();
    }

    public String pageIDtoURL(String pageID) throws IOException
    {
        return String.valueOf(pageUrl.getEntry(pageID));
    }

    public void calculatePartialScore(String oneTerm) throws IOException {
        oneTerm = StopStem.processWord(oneTerm);
        if(oneTerm == null || oneTerm.equals(""))
            return;

        String word_id = (String) wordID.getEntry(oneTerm);

        if(word_id == null || word_id.equals(""))
            return;

        Vector<Posting> bodyList = (Vector<Posting>) bodyWord.getEntry(word_id);
        Vector<Posting> titleList = (Vector<Posting>) titleWord.getEntry(word_id);

        if(bodyList != null)
        {
            for(Posting p : bodyList)
            {
                String pageID = p.pageID;
                double partialScore = termWeight.getTermWeight(pageID, word_id, true);
                if (pageScore.get(pageID) != null) {
                    partialScore = pageScore.get(pageID).body + partialScore;
                    double title = pageScore.get(pageID).title;
                    pageScore.put(pageID, new Score(partialScore, title));
                }
                else
                    pageScore.put(pageID, new Score(partialScore, 0));
            }
        }
        if(titleList != null)
        {
            for(Posting p : titleList)
            {
                String pageID = p.pageID;
                double partialScore = termWeight.getTermWeight(pageID,word_id, false);
                if (pageScore.get(pageID) != null) {
                    partialScore = pageScore.get(pageID).title + partialScore;
                    double body = pageScore.get(pageID).body;
                    pageScore.put(pageID, new Score(body, partialScore));
                }
                else
                    pageScore.put(pageID, new Score(0, partialScore));
            }
        }
    }

    public Vector<Score> getScore(String query) throws IOException {
        pageScore = new Hashtable<String, Score>();
        String mQuery = query.toLowerCase().replaceAll("[^a-z\"\']", " ");
        String[] terms = mQuery.split("[\"\']");
        Vector<Score> searchResult = new Vector<Score>();
        
        for(int i = 0; i < terms.length; i++)
        {
            if(i % 2 == 0 || !terms[i].trim().contains(" "))
            {
                if (terms[i].equals("") || terms[i] == null)
                    continue;

                String[] words = terms[i].split(" ");
                for(int j = 0; j < words.length; j++) {
                    if(words[j].equals("") || words[j] == null)
                        continue;
                    calculatePartialScore(words[j]);
                }
                //double bodyTermWeight = termWeight.getTermWeight(pageId,word_id, true);
                //termWeightList.add(bodyTermWeight);
            }
            else {
                if (terms[i].equals("") || terms[i] == null)
                    continue;

                Phrase phrase = new Phrase(recman, terms[i]);
                Hashtable<String, Double> bodyPhWeights = phrase.getWeight(true);
                Hashtable<String, Double> titlePhWeights = phrase.getWeight(false);

                Enumeration<String> be = bodyPhWeights.keys();
                String pageID;
                while(be.hasMoreElements())
                {
                    pageID = be.nextElement();
                    double partialScore = bodyPhWeights.get(pageID);
                    if (pageScore.get(pageID) != null) {
                        partialScore = pageScore.get(pageID).body + partialScore;
                        double title = pageScore.get(pageID).title;
                        pageScore.put(pageID, new Score(partialScore, title));
                    }
                    else
                        pageScore.put(pageID, new Score(partialScore, 0));
                }
                Enumeration<String> te = titlePhWeights.keys();
                while(te.hasMoreElements())
                {
                    pageID = te.nextElement();
                    double partialScore = titlePhWeights.get(pageID);
                    if(pageScore.get(pageScore) != null) {
                        partialScore = pageScore.get(pageID).title + partialScore;
                        double body = pageScore.get(pageID).body;
                        pageScore.put(pageID, new Score(body, partialScore));
                    }
                    else
                        pageScore.put(pageID, new Score(0, partialScore));
                }
                String[] words = terms[i].split(" ");
                for(int j = 0; j < words.length; j++) {
                    if(words[j].equals("") || words[j] == null)
                        continue;
                    calculatePartialScore(words[j]);
                }
            }
        }
        Enumeration<String> temp = pageScore.keys();
        String pageID;
        while(temp.hasMoreElements()) {
            pageID = temp.nextElement();
            searchResult.add(new Score(pageID, pageScore.get(pageID).body, pageScore.get(pageID).title));
        }

        Collections.sort(searchResult);
        return searchResult;
    }

    public void printScore() throws IOException {
        Enumeration<String> result = pageScore.keys();
        String pageID;
        while(result.hasMoreElements()) {
            pageID = result.nextElement();
            System.out.println(pageIDtoURL(pageID) + ": " + pageScore.get(pageID).body + ", " + pageScore.get(pageID).title);
        }
    }

    public static void main(String[] args) throws IOException {
        RecordManager recman = RecordManagerFactory.createRecordManager("Database");
        Query query = new Query(recman);
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        System.out.println("Enter your query: ");
        String q = br.readLine();
        Vector<Score> result = query.getScore(q);
        for(Score s: result)
        {
            System.out.println(query.pageIDtoURL(s.pageID) + ": " + s.body + ", " + s.title);
        }
        // DataManager pageUrl = new DataManager(recman, "pageUrl");
        // FastIterator iter = pageUrl.getIterator();
        // String pageID;
        // while((pageID = (String) iter.next()) != null) {
        //     double score = query.getScore(q, pageID);
        //     if(score != -1.0)
        //         System.out.println(pageUrl.getEntry(pageID) + ": " + score);
        // }
    }
}
