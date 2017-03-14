
import java.io.*;
import java.util.*;
import java.net.URL;
import java.net.URLConnection;	
import org.htmlparser.util.ParserException;
import jdbm.RecordManager;
import jdbm.RecordManagerFactory;

public class Page implements Serializable {
	private String url;
	private String title;
	private String pageId;
	private long lastModification;
	private long size;
	private HashMap<String, Integer> word_tf;
	private Vector<String> childLinks;

	private static final long serialVersionUID = 3849687432103791608L;

	public Page(RecordManager recman, String _url, String _pageId) throws ParserException, IOException {
		url = _url;
		pageId = _pageId;
		word_tf = new HashMap<String, Integer>();
		initialize();
	}

	public Page(RecordManager recman, String _url) throws ParserException, IOException {
		url = _url;
		pageId = null;
		word_tf = new HashMap<String, Integer>();
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
		else
			size = 0;
		calculateWordTF();
		childLinks = Indexer.extractLinks(url);
	}

	public void calculateWordTF() throws ParserException, IOException {
		Vector<String> words = Indexer.extractWords(this.url);
		if(size == 0) {
			size = words.size();
		}
		for(String w: words) {
			String stemmedWord = StopStem.processWord(w);
			if(stemmedWord == null || stemmedWord.equals(""))
				continue;
			int tf = 0;
			if(word_tf.get(stemmedWord) != null)
				tf = word_tf.get(stemmedWord) + 1;
			else
				tf = 1;
			word_tf.put(stemmedWord, tf);
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
	public HashMap<String, Integer> getWordTF() {
		return word_tf;
	}
	public Vector<String> getChildLinks() {
		return childLinks;
	}

}