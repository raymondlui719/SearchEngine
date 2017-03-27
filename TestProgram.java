import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;

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

 
    public TestProgram(String database) throws IOException {
		recman = RecordManagerFactory.createRecordManager(database);
		indexer = new Indexer();
		pageID = new DataManager(recman, "pageID");		// URL to pageID mapping
		pageInfo = new DataManager(recman, "pageInfo");	// pageID to page mapping
	}

    public void finalize() throws IOException {
		recman.commit();
		recman.close();
    }

    public void print() throws IOException {
        PrintWriter pw = new PrintWriter("spider_result.txt");
	    FastIterator it = pageID.getIterator();
        String url = null;

        while((url = (String) it.next()) != null) 
		{
			//get the page_id given an url.
			String pageid = (String) pageID.getEntry(url);
			//pw.println(url);
            pw.println(pageID.printAll());
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
