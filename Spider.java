
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
	private DataManager childLinks;
	private int pageCount;

	public Spider(String recordmanager) throws IOException {
		recman = RecordManagerFactory.createRecordManager(recordmanager);
		indexer = new Indexer();
		pageID = new DataManager(recman, "pageID");		// URL to pageID mapping
		pageInfo = new DataManager(recman, "pageInfo");	// pageID to page mapping
		childLinks = new DataManager(recman, "childLinks"); // parent page ID to list of child page ID
		pageCount = 0;
	}

	public void finalize() throws IOException {
		recman.commit();
		recman.close();
	}

	// get pre-crawled links in database
	public void getData(Queue<String> processed) throws IOException {
		HTree hashtable = pageID.getHashTable();
		FastIterator it = hashtable.keys();
		String url = null;
		while( (url = (String)it.next()) != null) {
			processed.add(url);
			pageCount++;
			System.out.println(pageCount + ". URL " + url + " initially processed");
		}
	}

	public void indexing(String url, int maxPage) throws ParserException, IOException {
		Queue<String> linksList = new LinkedList<String>();
		Queue<String> processedLinks = new LinkedList<String>();
		getData(processedLinks);
		linksList.add(url);
		int idCount = 0;
		while(linksList.size() != 0 && processedLinks.size() < maxPage)
		{
			String pageUrl = linksList.remove();
			String id = "";

			/*
				For phase 2:
				Before a page is fetched into the system, it must perform several checks:
				If the URL doesn’t exist in the index, go ahead to retrieve the URL (DONE)
				If the URL already exists in the index but the last modification date of the URL is later than that recorded in the index, 
				go ahead to retrieve the URL; otherwise, ignore the URL (TODO)
			*/
			if(processedLinks.contains(pageUrl))
				continue;

			// get or assign pageID of/to pageUrl
			if(pageID.getEntry(pageUrl) != null)
			{
				id = String.valueOf(pageID.getEntry(pageUrl));
			}
			else
			{
				id = String.format("%04d", pageCount+1);
			}
			// URL <==> pageID
			pageID.addEntry(pageUrl, id);
			Page page = new Page(recman, pageUrl, id);

			// if we can actually connect to the page then do the following:
			if(page.getPageTitle() != null)
			{
				// pageID ==> pageInfo
				pageInfo.addEntry(id, page);
				//recman.commit();

				/*
					TODO: index the word to word ID and word ID to posting list table
				*/
				
				Vector<String> links = Indexer.extractLinks(pageUrl);
				Vector<String> links_without_dup = new Vector<String>(new LinkedHashSet<String>(links));
				Vector<String> childLinks = new Vector<String>();
				for(String link: links_without_dup)
				{
					// TODO: check if the child link was processed before,
					// i.e. check if the child link is instanceof processedLinks
					// i.e. check if the child link is the anchored url of some links in processedLinks
					if(!processedLinks.contains(link)) {
						linksList.add(link);
						childLinks.add(link);
					}
				}

				// index the child links of the given page
				addChildLink(pageUrl, childLinks);

				// mark link as processed
				processedLinks.add(pageUrl);
				System.out.println((pageCount + 1) + ". Processed " + pageUrl);
				pageCount++;
			}
			else
			{
				System.out.println("Failed to connect page with https: " + pageUrl);
				page = null;
			}
		}
	}

	// parent id --> Vector<String> child id (currently child url, need change)
	public void addChildLink(String parentLink, Vector<String> links) throws IOException {
		String parentID = String.valueOf(pageID.getEntry(parentLink));
		if(parentID != null) {
			childLinks.addEntry(parentID, links);
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
		DataManager childLinks = new DataManager(recman, "childLinks");
		
		spider.finalize();

		//pageID.printAll();
		//pageInfo.printAll();
		// String startUrlID = String.valueOf(pageID.getEntry(startUrl));
		// Vector<String> links = (Vector<String>) childLinks.getEntry(startUrlID);
		// System.out.println(links);

		//HTree hashtable;
		//FastIterator iter;
		//String keyword;

		//hashtable = pageInfo.getHashTable();
		//iter = hashtable.keys();
		//keyword = null;
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
	}
}