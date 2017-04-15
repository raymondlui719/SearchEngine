import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.*;
import java.lang.Integer;

import java.util.*;

import org.htmlparser.util.ParserException;

import jdbm.RecordManager;
import jdbm.RecordManagerFactory;
import jdbm.helper.FastIterator;
import jdbm.htree.HTree;

@SuppressWarnings("unchecked")
public class TestProgram {
    private static RecordManager recman;
    private Indexer indexer;
    private DataManager pageID;  
    private DataManager pageInfo;
    private DataManager pageUrl;
    private DataManager childLinks;
    private DataManager parentLinks;
    private DataManager wordID;
    private DataManager word;
    private DataManager bodyWord;
    private DataManager titleWord;
    private DataManager pageBodyWord;
    private DataManager pageTitleWord;
    private DataManager pageBodyMaxTF;
    private DataManager pageTitleMaxTF;
    private int pageCount;
    private TermWeight termWeight;

 
    public TestProgram(String database) throws IOException {
        // Open the database
		recman = RecordManagerFactory.createRecordManager(database);
		indexer = new Indexer(recman);
		pageID = new DataManager(recman, "pageID");		// URL to pageID mapping
		pageInfo = new DataManager(recman, "pageInfo");	// pageID to page mapping
        pageUrl = new DataManager (recman, "pageUrl");
        childLinks = new DataManager(recman, "childLinks"); // parent page ID to list of child page ID
        parentLinks = new DataManager(recman,"parentLinks"); // child page ID to list of parent page ID
        wordID = new DataManager(recman, "wordID");     // word --> word-id
        word = new DataManager(recman, "word");     // word-id --> word
        bodyWord = new DataManager(recman, "bodyWord"); // word-id --> {page-id, tf}
        titleWord = new DataManager(recman, "titleWord");   
        pageBodyWord = new DataManager(recman, "pageBodyWord"); // forward index (page-id --> {keywords})
        pageTitleWord = new DataManager(recman, "pageTitleWord");
        pageBodyMaxTF = new DataManager(recman, "pageBodyMaxTF");   // forward index (page-id --> max tf)
        pageTitleMaxTF = new DataManager(recman, "pageTitleMaxTF");
        pageCount = 0;
        termWeight = new TermWeight(recman);
	}

    public void finalize() throws IOException {
		recman.commit();
		recman.close();
    }

    public void print() throws IOException {
        // Put the result into a txt file
        PrintStream pw = new PrintStream(new FileOutputStream("spider_result.txt"));
        System.setOut(pw);
        
        HTree pageInfoHashtable = pageInfo.getHashTable();
        HTree pageUrlHashtable = pageUrl.getHashTable(); 
	    FastIterator it = pageInfoHashtable.keys();
        String pageID = null;
        
        while((pageID = (String) it.next()) != null) 
		{
            Page page = (Page) pageInfoHashtable.get(pageID);
            pageCount++;
            System.out.println(pageCount + ": " + page.getPageTitle());
            System.out.println(page.getURL());
            System.out.println(page.getLastModification() + ", " + page.getPageSize());
            int bodyMaxTF = (int) pageBodyMaxTF.getEntry(pageID);
            System.out.println("Max term frequency (body): " + bodyMaxTF);
            int titleMaxTF = (int) pageTitleMaxTF.getEntry(pageID);
            System.out.println("Max term frequency (title): " + titleMaxTF);

            Vector<String> titleWordIDs = (Vector<String>) pageTitleWord.getEntry(pageID);
            for(String tid: titleWordIDs)
            {   String w = String.valueOf(word.getEntry(tid));
                Vector<Posting> pList = (Vector<Posting>) titleWord.getEntry(tid);
                
                for(Posting p: pList) {
                    if(p.pageID.equals(pageID)) {
                        System.out.print(w + " " + p.freq + "; ");
                    }
                }
                double mark = termWeight.getTermWeight(pageID,tid,false);
                System.out.println("Title Term Weight: "+mark);
            }
            System.out.println();

            Vector<String> bodyWordIDs = (Vector<String>) pageBodyWord.getEntry(pageID);
            for(String bid: bodyWordIDs)
            {   String w = String.valueOf(word.getEntry(bid));
                Vector<Posting> pList = (Vector<Posting>) bodyWord.getEntry(bid);
                for(Posting p: pList) {
                    if(p.pageID.equals(pageID)) {
                        System.out.print(w + " " + p.freq + "; ");
                    }
                }
                double mark = termWeight.getTermWeight(pageID,bid,true);
                System.out.println("Term Weight: "+mark);
            }
            System.out.println();

            Vector<String> parentsId = (Vector<String>) parentLinks.getEntry(pageID);
            System.out.println("Parents Links:");
            try {
	        for(String linkId: parentsId)
                {
                    String link = String.valueOf(pageUrl.getEntry(linkId));

                    System.out.println(link);
                }
	    } catch(NullPointerException ex) {
		System.out.println("None");
	    }
            Vector<String> childId = (Vector<String>) childLinks.getEntry(pageID);
            System.out.println("Children Links:");
            for(String linkId: childId)
            {
                String link = String.valueOf(pageUrl.getEntry(linkId));
                System.out.println(link);
            }
            System.out.println("---------------------------------------------------------------------");
        }
		pw.close();
    }

    public static void main(String[] args) throws IOException {
        String db = "Database";
        TestProgram tp = new TestProgram(db);
		tp.print();
        tp.finalize();
    }
}
