
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
    private DataManager parentLinks;
	private int pageCount;

	public Spider(String recordmanager) throws IOException {
		recman = RecordManagerFactory.createRecordManager(recordmanager);
		indexer = new Indexer(recman);
		pageID = new DataManager(recman, "pageID");		// URL to pageID mapping
		pageUrl = new DataManager(recman, "pageUrl");	// page ID to URL mapping
		pageInfo = new DataManager(recman, "pageInfo");	// pageID to page mapping
		childLinks = new DataManager(recman, "childLinks"); // parent page ID to list of child page ID
        parentLinks = new DataManager(recman,"parentLinks"); // child page ID to list of parent page ID
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

			Page page = new Page(recman, onePage, id);

			Page oldPage = null;
			if(pageInfo.getEntry(id) != null) {
				oldPage = (Page) pageInfo.getEntry(id);
			}

			/*
				If the URL already exists in the index but the last modification date 
				of the URL is later than that recorded in the index, 
				go ahead to retrieve the URL; otherwise, ignore the URL
			*/
			if(processedLinks.contains(onePage)
				&& oldPage != null
				&& oldPage.getLastModificationLong() >= page.getLastModificationLong()) {
				continue;
			}

			// if we can actually connect to the page then do the following:
			if(page.getPageTitle() != null)
			{
                String cid = "";
				// URL <==> pageID
				pageID.addEntry(onePage, id);
				pageUrl.addEntry(id, onePage);

				// pageID ==> pageInfo
				pageInfo.addEntry(id, page);
				
				indexer.indexPageTitle(onePage, id);
				indexer.indexPageBody(onePage, id);

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
							cid = String.valueOf(pageID.getEntry(link));
						}
						else {
							cid = String.format("%04d", pageCount++);
						}
						// URL <==> pageID
						pageID.addEntry(link, cid);
						pageUrl.addEntry(cid, link);
						childIDs.add(cid);
						addParentLink(cid,id);		
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

    // child page ID -> Vector<String> parent id
    public void addParentLink(String childId, String parentId) throws IOException {
        Vector<String> parents;
        // childID is not existed in parentLinks
        if (parentLinks.getEntry(childId) == null)
        {
            parents = new Vector<String>();
            parents.add(parentId);

        } else {
            
            parents = (Vector<String>)parentLinks.getEntry(childId);
            if (!parents.contains(parentId)) {
                parents.add(parentId);
            }
            
        }
        parentLinks.addEntry(childId, parents);

    }
   	public static void main(String[] arg) throws IOException, ParserException {
		String db = "Database";
		String startUrl = "https://course.cse.ust.hk/comp4321/labs/TestPages/testpage.htm";
		final int maxPage = 300;

		Spider spider = new Spider(db);

		spider.indexing(startUrl, maxPage);
		
		spider.finalize();
	}
}
