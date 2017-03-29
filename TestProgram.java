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
    private DataManager wordID;
    private DataManager idWord;
    private DataManager wordInfo;
    private DataManager pageWord;
    private int pageCount;

 
    public TestProgram(String database) throws IOException {
        // Open the database
		recman = RecordManagerFactory.createRecordManager(database);
		indexer = new Indexer(recman);
		pageID = new DataManager(recman, "pageID");		// URL to pageID mapping
		pageInfo = new DataManager(recman, "pageInfo");	// pageID to page mapping
        pageUrl = new DataManager (recman, "pageUrl");
        childLinks = new DataManager(recman, "childLinks"); // parent page ID to list of child page ID
        wordID = new DataManager(recman, "wordID");
        idWord = new DataManager(recman, "idWord");
        wordInfo = new DataManager(recman, "wordInfo");
        pageWord = new DataManager(recman, "pageWord");
        pageCount = 0;
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

            Vector<String> keywordIDs = (Vector<String>) pageWord.getEntry(pageID);
            for(String id: keywordIDs)
            {
                String word = String.valueOf(idWord.getEntry(id));
                Vector<Posting> pList = (Vector<Posting>) wordInfo.getEntry(id);
                for(Posting p: pList) {
                    if(p.pageID.equals(pageID)) {
                        System.out.print(word + " " + p.freq + "; ");
                    }
                }
            }
            System.out.println();

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
