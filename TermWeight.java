import java.io.IOException;
import java.util.Vector;

import jdbm.RecordManager;
import jdbm.htree.HTree;

public class TermWeight {
    private RecordManager recman;
    private DataManager wordID;
    private DataManager word;
    private DataManager bodyWord;
    private DataManager titleWord;
    private DataManager pageBodyWord;
    private DataManager pageTitleWord;
    private DataManager pageBodyMaxTF;
    private DataManager pageTitleMaxTF;

    public TermWeight(RecordManager _recman) throws IOException {
        recman = _recman;
        wordID = new DataManager(recman, "wordID");     // word --> word-id
        word = new DataManager(recman, "word");     // word-id --> word
        bodyWord = new DataManager(recman, "bodyWord"); // word-id --> {page-id, tf}
        titleWord = new DataManager(recman, "titleWord");   
        pageBodyWord = new DataManager(recman, "pageBodyWord"); // forward index (page-id --> {keywords})
        pageTitleWord = new DataManager(recman, "pageTitleWord");
        pageBodyMaxTF = new DataManager(recman, "pageBodyMaxTF");   // forward index (page-id --> max tf)
        pageTitleMaxTF = new DataManager(recman, "pageTitleMaxTF");
    }
    
    public double getTermWeight (String page_Id,String word_Id,boolean isBody) throws IOException {
        int tf = 0;
        int df = 0;   
        // if the term is in the body     
        if(isBody) {
            Vector<Posting>bodypostingList = (Vector<Posting>) bodyWord.getEntry(word_Id);
            if(bodypostingList == null) {
                return 0;
            }
            for (Posting p:bodypostingList) {
                if(p.pageID.equals(page_Id)) {
                    tf = p.freq;
                    break;
                }
            }
            df = bodypostingList.size();
        }else { // if the term is in the title
            Vector<Posting>titlepostingList = (Vector<Posting>) titleWord.getEntry(word_Id);
            if(bodypostingList == null) {
                return 0;
            }
            for (Posting p:titlepostingList) {
                if(p.pageID.equals(page_Id)) {
                    tf = p.freq;
                    break;
                }
            }
            df = titlepostingList.size();
        }
        int maxTF = (int) pageBodyMaxTF.getEntry(page_Id); 
       
        // idf = log2 (N/df)
        double idf = Math.log((pageBodyMaxTF.getTableSize()*1.0)/df)/Math.log(2);
        return (tf*idf)/maxTF;   
    }   
}
