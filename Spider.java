
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
	private DataManager pageUrl;
	private DataManager pageInfo;	// page information of the indexed pages (those 30 pages)
	private DataManager childLinks;
	private int pageCount;
	private DataManager wordID;
    	private DataManager wordInfo;
    	private int wordCount;

	public Spider(String recordmanager) throws IOException {
		recman = RecordManagerFactory.createRecordManager(recordmanager);
		indexer = new Indexer(recman);
		pageID = new DataManager(recman, "pageID");		// URL to pageID mapping
		pageUrl = new DataManager(recman, "pageUrl");	// page ID to URL mapping
		pageInfo = new DataManager(recman, "pageInfo");	// pageID to page mapping
		childLinks = new DataManager(recman, "childLinks"); // parent page ID to list of child page ID
		wordID = new DataManager(recman, "wordID");	// word to wordID mapping
       		wordInfo = new DataManager(recman, "wordInfo");	// wordID to  mapping
		pageCount = 0;
		wordCount = 0;
		
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
			//System.out.println(pageCount + ". URL " + url + " initially processed");
		}
	}

	public void indexing(String url, int maxPage) throws ParserException, IOException {
		Queue<String> linksList = new LinkedList<String>();
		Queue<String> processedLinks = new LinkedList<String>();
		getData(processedLinks);
		linksList.add(url);
		while(linksList.size() != 0 && processedLinks.size() < maxPage)
		{
			String onePage = linksList.remove();
			String id = "";


			// get or assign pageID of/to onePage
			if(pageID.getEntry(onePage) != null)
			{
				id = String.valueOf(pageID.getEntry(onePage));
			}
			else
			{
				id = String.format("%04d", pageCount++);
			}

			/*
				If the URL already exists in the index but the last modification date 
				of the URL is later than that recorded in the index, 
				go ahead to retrieve the URL; otherwise, ignore the URL
			*/

			Page page = new Page(recman, onePage, id);

			Page oldPage = null;
			if(pageInfo.getEntry(id) != null) {
				oldPage = (Page) pageInfo.getEntry(id);
			}

			if(processedLinks.contains(onePage)
				&& oldPage != null
				&& oldPage.getLastModificationLong() != 0 
				&& oldPage.getLastModificationLong() >= page.getLastModificationLong()) {
				continue;
			}

			// if we can actually connect to the page then do the following:
			if(page.getPageTitle() != null)
			{

				// URL <==> pageID
				pageID.addEntry(onePage, id);
				pageUrl.addEntry(id, onePage);

				// pageID ==> pageInfo
				pageInfo.addEntry(id, page);
				//recman.commit();

				/*
					TODO: index the word to word ID and word ID to posting list table
				*/
				indexer.indexNewPage(onePage, id);

				
				Vector<String> links = Indexer.extractLinks(onePage);
				Vector<String> links_without_dup = new Vector<String>(new LinkedHashSet<String>(links));
				Vector<String> childIDs = new Vector<String>();
				for(String link: links_without_dup)
				{
					// TODO: check if the child link was processed before,
					// i.e. check if the child link is instanceof processedLinks
					// i.e. check if the child link is the anchored url of some links in processedLinks
					if(!processedLinks.contains(link)) {
						// add the child link to queue
						linksList.add(link);
						// index the child links
						if(pageID.getEntry(link) != null) {
							id = String.valueOf(pageID.getEntry(link));
						}
						else {
							id = String.format("%04d", pageCount++);
						}
						// URL <==> pageID
						pageID.addEntry(link, id);
						pageUrl.addEntry(id, link);
						childIDs.add(id);
						
						
						Vector<String> words = Indexer.extractWords(onePage);	
						for(String w : words){
						    String stemmedWord = StopStem.processWord(w);
						    String word_id = "";
						    if(stemmedWord == null || stemmedWord.equals("")){
							continue;
						    }
							// check if the word is in wordID already

							if(wordID.getEntry(stemmedWord) ==null){
							    word_id = String.format("%04d", wordCount++);

							}else{
							    word_id = String.valueOf(wordID.getEntry(stemmedWord));
							}
							// word ==> wordID
						    wordID.addEntry(stemmedWord,word_id);
						    HashMap<String, Integer> word_tf = page.getWordTF();
						    int tf = word_tf.get(stemmedWord);
						}
					}
				}

				// index the child links of the given page
				addChildLink(onePage, childIDs);

				// mark link as processed
				processedLinks.add(onePage);
				System.out.println(processedLinks.size() + ". Processed " + onePage);
			}
			else
			{
				System.out.println("Failed to connect page with https: " + onePage);
			}
		}
	}

	// parent id --> Vector<String> child id
	public void addChildLink(String parentLink, Vector<String> ids) throws IOException {
		String parentID = String.valueOf(pageID.getEntry(parentLink));
		if(parentID != null) {
			childLinks.addEntry(parentID, ids);
		}
	}

	public static void main(String[] arg) throws IOException, ParserException {
		String db = "Database";
		String startUrl = "http://www.cse.ust.hk";
		final int maxPage = 30;

		Spider spider = new Spider(db);

		spider.indexing(startUrl, maxPage);

		DataManager pageID = new DataManager(recman, "pageID");
		DataManager pageUrl = new DataManager(recman, "pageUrl");
		DataManager pageInfo = new DataManager(recman, "pageInfo");
		DataManager childLinks = new DataManager(recman, "childLinks");
		DataManager wordID = new DataManager(recman, "wordID");
       		DataManager WordInfo = new DataManager(recman, "wordInfo");
		
		
		childLinks.printAll();
		
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
