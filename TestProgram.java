import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

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
    private int pageCount = 0;

 
    public TestProgram(String database) throws IOException {
        // Open the database
		recman = RecordManagerFactory.createRecordManager(database);
		indexer = new Indexer(recman);
		pageID = new DataManager(recman, "pageID");		// URL to pageID mapping
		pageInfo = new DataManager(recman, "pageInfo");	// pageID to page mapping
	}

    public void finalize() throws IOException {
		recman.commit();
		recman.close();
    }

    public void print() throws IOException {
        // Put the result into a txt file
        PrintStream pw = new PrintStream(new FileOutputStream("spider_result.txt"));
        System.setOut(pw);
	    /*FastIterator it = pageID.getIterator();
        String url = null;

        while((url = (String) it.next()) != null) 
		{
			//String pageid = (String) pageID.getEntry(url);
           
            pageCount++;
            System.out.println(pageCount +":URL " +url);
            System.out.println("-----------------------------------------");
   
		}*/
        pageInfo.printAll();
		pw.close();
    }

    public static void main(String[] args) throws IOException {
        String db = "Database";
        TestProgram tp = new TestProgram(db);
		tp.print();
        tp.finalize();
    }
}
