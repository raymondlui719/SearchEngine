
import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Vector;

import org.htmlparser.util.ParserException;

import jdbm.RecordManager;
import jdbm.RecordManagerFactory;
import jdbm.helper.FastIterator;
import jdbm.htree.HTree;

public class Spider {
	private RecordManager recman;
	private Indexer indexer;
	//private StopStem stopStem;
	private DataManager pageID;
	private int pageCount;

	public Spider(String recordmanager) throws IOException {
		recman = RecordManagerFactory.createRecordManager(recordmanager);
		indexer = new Indexer();
		pageID = new DataManager(recman, "pageID");
	}

	public void finalize() throws IOException {
		recman.commit();
		recman.close();
	}

	public void indexing(String url, int maxPage) throws ParserException, IOException {
		Queue<String> linksList = new LinkedList<String>();
		Queue<String> processedLinks = new LinkedList<String>();
		linksList.add(url);
		while(linksList.size() != 0 && processedLinks.size() < maxPage)
		{
			String pageUrl = linksList.remove();
			String id = "";

			if(!processedLinks.contains(pageUrl))
			{
				if(pageID.getEntry(pageUrl) != null)
				{
					id = String.valueOf(pageID.getEntry(pageUrl));
				}
				else
				{
					id = String.format("%04d", pageCount++);
				}
				pageID.addObject(pageUrl, id);
				Page page = new Page(recman, pageUrl, id);
				recman.commit();

				// start printing stuff
				System.out.println(page.getPageTitle());
				System.out.println(page.getURL());
				System.out.print(page.getLastModification() + ", ");
				System.out.println(page.getPageSize());
				page.getWordFreqList().printAll();
				System.out.println();
				for(String childLink: page.getChildLinks())
				{
					linksList.add(childLink);
					// System.out.println(childLink);
				}
				System.out.println("-------------------------------------");

				processedLinks.add(pageUrl);
			}
		}
	}

	public static void main(String[] arg) throws IOException, ParserException {
		String db = "Database";
		String startUrl = "http://www.ust.hk";
		final int maxPage = 30;

		Spider spider = new Spider(db);

		spider.indexing(startUrl, maxPage);

		spider.finalize();

	}
}