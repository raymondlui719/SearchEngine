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

public class TestProgram {
    private static RecordManager recman;
    private Indexer indexer;
    private DataManager pageID;  
    private DataManager pageInfo;
    private DataManager pageUrl;
    private DataManager childLinks;
    private int pageCount = 0;

 
    public TestProgram(String database) throws IOException {
        // Open the database
		recman = RecordManagerFactory.createRecordManager(database);
		indexer = new Indexer(recman);
		pageID = new DataManager(recman, "pageID");		// URL to pageID mapping
		pageInfo = new DataManager(recman, "pageInfo");	// pageID to page mapping
        pageUrl = new DataManager (recman, "pageUrl");
        childLinks = new DataManager(recman, "childLinks"); // parent page ID to list of child page ID
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
        HTree childLinksHashtable = childLinks.getHashTable();
	    FastIterator it = pageInfoHashtable.keys();
        String keyword = null;
        
        while((keyword = (String) it.next()) != null) 
		{   Page page = (Page) pageInfoHashtable.get(keyword);
            String url = (String) pageUrlHashtable.get(keyword);
            pageCount++;
            System.out.println(pageCount + ": " + page.getPageTitle());
            System.out.println("URL: " + url);
            System.out.println(page.getLastModification()+", Page Size: "+page.getPageSize());
            HashMap<String, Integer> word_tf = page.getWordTF();
            System.out.println(word_tf);

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
