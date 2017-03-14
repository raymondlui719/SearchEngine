
import java.io.*;
import java.util.*;
import java.net.URL;
import java.net.URLConnection;	
import org.htmlparser.util.ParserException;
import jdbm.RecordManager;
import jdbm.RecordManagerFactory;

public class Page {
	private String url;
	private String title;
	private String pageId;
	private long lastModification;
	private long size;
	private DataManager wordsFreqList;
	private Vector<String> childLinks;

	public Page(RecordManager recman, String _url, String _pageId) throws ParserException, IOException {
		url = _url;
		pageId = _pageId;
		String objectName = "termFreq" + pageId;
		wordsFreqList = new DataManager(recman, objectName);
		initialize();
	}

	public Page(RecordManager recman, String _url) throws ParserException, IOException {
		url = _url;
		pageId = null;
		wordsFreqList = new DataManager(recman, "termFreq");
		initialize();
	}

	public void initialize() throws ParserException, IOException {
		title = Indexer.extractTitle(url);
		URL m_url = new URL(url);
		URLConnection uc = m_url.openConnection();	
		lastModification = uc.getLastModified();
		if(lastModification == 0)
			lastModification = uc.getDate();
		if(uc.getContentLengthLong() > 0)
			size = uc.getContentLengthLong();
		indexWordsFromPage();
		childLinks = Indexer.extractLinks(url);
	}

	public void indexWordsFromPage() throws ParserException, IOException {
		Vector<String> words = Indexer.extractWords(this.url);
		for(String w: words) {
			String stemmedWord = StopStem.processWord(w);
			if(stemmedWord == null || stemmedWord.equals(""))
				continue;
			int tf = 0;
			if(wordsFreqList.getEntry(stemmedWord) != null)
				tf = ((int) wordsFreqList.getEntry(stemmedWord)) + 1;
			else
				tf = 1;
			wordsFreqList.addEntry(stemmedWord, tf);
		}
	}

	public long getLastModificationLong() {
		return lastModification;
	}
	public Date getLastModification() {
		if(lastModification == 0) return null;
		return new Date(lastModification);
	}
	public String getURL() {
		return url;
	}
	public String getPageTitle() {
		return title;
	}
	public long getPageSize() {
		return size;
	}
	public String getPageID() {
		return pageId;
	}
	public String getPageLink() {
		return url;
	}
	public DataManager getWordFreqList() {
		return wordsFreqList;
	}
	public Vector<String> getChildLinks() {
		return childLinks;
	}

}