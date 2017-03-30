
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
	private long size;	// page size

	private static final long serialVersionUID = 3849687432103791608L;

	public Page(RecordManager recman, String _url, String _pageId) throws ParserException, IOException {
		url = _url;
		pageId = _pageId;
		initialize();
	}

	public Page(RecordManager recman, String _url) throws ParserException, IOException {
		url = _url;
		pageId = null;
		initialize();
	}

	public void initialize() throws ParserException, IOException {
		title = Indexer.extractTitle(url);
		URL m_url = new URL(url);
		URLConnection uc =  m_url.openConnection();
		lastModification = uc.getLastModified();
		if(lastModification == 0) {
			lastModification = 0;
		}

		if(uc.getContentLengthLong() > 0)
			size = uc.getContentLengthLong();
		else {
			Vector<String> words = Indexer.extractWords(this.url);
			size = words.size();
		}
	}

	public long getLastModificationLong() {
		return lastModification;
	}
	public Date getLastModification() {
		if(lastModification == 0) return new Date(System.currentTimeMillis());
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

	public String toString() {
		return ("Title: " + title  + "\n" + "URL: " + url + "\n" + "Last Modification: " + getLastModification() + "\n" + "Page Size: " + size + "\n"+ "Page ID: " + pageId + "\n");
	}

}