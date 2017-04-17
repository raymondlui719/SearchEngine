import java.util.Vector;
import java.util.HashMap;
import org.htmlparser.Parser;
import org.htmlparser.util.ParserException;
import java.net.URL;
import java.io.IOException;

import java.util.*;

import jdbm.RecordManager;
import jdbm.RecordManagerFactory;
import jdbm.htree.HTree;

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
    private TermWeight termWeight;

    public Query(RecordManager _recman) throws IOException {
        recman = _recman;
        wordID = new DataManager(recman, "wordID");     // word --> word-id
        word = new DataManager(recman, "word");     // word-id --> word
        bodyWord = new DataManager(recman, "bodyWord"); // word-id --> {page-id, tf}
        titleWord = new DataManager(recman, "titleWord");   
        pageBodyWord = new DataManager(recman, "pageBodyWord"); // forward index (page-id --> {keywords})
        pageTitleWord = new DataManager(recman, "pageTitleWord");
        termWeight = new TermWeight(recman);
    }

    public double getScore(String query, String pageId) throws IOException {
        query = query.toLowerCase();
        String[] terms = query.trim().split("\\s+");
        Vector<Double> termWeightList = new Vector<Double>();
        
        for(int i = 0 ; i<terms.length; i++) {
            terms[i] = StopStem.processWord(terms[i]);
            String word_id = (String)wordID.getEntry(terms[i]);
            if(word_id.equals("") || word_id == null) {
                continue;
            }
            double bodyTermWeight = termWeight.getTermWeight(pageId,word_id, true);
            termWeightList.add(bodyTermWeight);

        }
        double summation = 0;
        for(int j = 0; j < termWeightList.size();j++) {
            summation = summation + termWeightList.get(j); 
        }
        return summation/(Math.sqrt(summation)*Math.sqrt(termWeightList.size()));
    }
}
