
import java.io.IOException;
import java.util.*;

import org.htmlparser.util.ParserException;

import jdbm.RecordManager;
import jdbm.RecordManagerFactory;
import jdbm.helper.FastIterator;
import jdbm.htree.HTree;

public class Spider {
	private static RecordManager recman;
	private Indexer indexer;
	private DataManager pageID;
	private DataManager pageInfo;
	private int pageCount;

	public Spider(String recordmanager) throws IOException {
		recman = RecordManagerFactory.createRecordManager(recordmanager);
		indexer = new Indexer();
		pageID = new DataManager(recman, "pageID");		// URL to pageID mapping
		pageInfo = new DataManager(recman, "pageInfo");	// pageID to page mapping
	}

	public void finalize() throws IOException {
		recman.commit();
		recman.close();
	}

	// get pre-crawled links in database
	public void getData(Queue<String> processed) throws IOException {
		HTree hashtable = pageInfo.getHashTable();
		FastIterator it = hashtable.keys();
		String keyword = null;
		while( (keyword = (String)it.next()) != null) {
			processed.add(keyword);
		}
	}

	public void indexing(String url, int maxPage) throws ParserException, IOException {
		Queue<String> linksList = new LinkedList<String>();
		Queue<String> processedLinks = new LinkedList<String>();
		getData(processedLinks);
		linksList.add(url);
		while(linksList.size() != 0 && processedLinks.size() < maxPage)
		{
			String pageUrl = linksList.remove();
			String id = "";

			if(!processedLinks.contains(pageUrl))
			{
				System.out.println((pageCount + 1) + ". Processing " + pageUrl);

				// get or assign pageID of/to pageUrl
				if(pageID.getEntry(pageUrl) != null)
				{
					id = String.valueOf(pageID.getEntry(pageUrl));
				}
				else
				{
					id = String.format("%04d", pageCount++);
				}
				// URL <==> pageID
				pageID.addEntry(pageUrl, id);
				Page page = new Page(recman, pageUrl, id);
				// pageID ==> pageInfo
				pageInfo.addEntry(id, page);
				recman.commit();
				
				Vector<String> childLinks = Indexer.extractLinks(pageUrl);
				for(String childLink: childLinks)
				{
					// TODO: check if childLink was processed before,
					// i.e. check if childLink is instanceof processedLinks
					// i.e. check if childLink is the anchored url of some links in processedLinks 
					linksList.add(childLink);
				}

				// mark link as processed
				processedLinks.add(pageUrl);
			}
		}
	}

	public static void main(String[] arg) throws IOException, ParserException {
		String db = "Database";
		String startUrl = "http://www.cse.ust.hk";
		final int maxPage = 30;

		Spider spider = new Spider(db);

		spider.indexing(startUrl, maxPage);

		DataManager pageID = new DataManager(recman, "pageID");
		DataManager pageInfo = new DataManager(recman, "pageInfo");
		
		HTree hashtable;
		FastIterator iter;
		String keyword;

		hashtable = pageInfo.getHashTable();
		iter = hashtable.keys();
		keyword = null;
		// while((keyword = (String)iter.next()) != null) {
		// 	Page page = (Page) hashtable.get(keyword);
		// 	System.out.println(keyword);
		// 	System.out.println(page.getPageTitle());
		// 	System.out.print(page.getLastModification());
		// 	System.out.println(", " + page.getPageSize());
		// 	HashMap<String, Integer> word_tf = page.getWordTF();
		// 	System.out.println(word_tf);
		// 	Vector<String> childLinks = page.getChildLinks();
		// 	for(String childLink: childLinks) {
		// 		System.out.println(childLink);
		// 	}
		// }

		spider.finalize();

	}
}