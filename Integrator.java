
import java.io.*;
import java.util.*;
import java.util.Vector;

public class Spider {
	private Indexer indexer;
	private StopStem stopStem;
	private DataManager index;
	private Vector<Document> childLinks;

	public Spider(String fileName, String url) {
		crawler = new Crawler(url);
		stopStem = new StopStem(fileName);
		index = new InvertedIndex("phase1", "ht1");
		Vector<String> = crawler.extractLinks();
		
	}
}