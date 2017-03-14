
import java.io.*;
import java.util.*;
import java.net.URL;
import java.net.URLConnection;	

public class Page {
	private String url;
	private String title;
	private String pageId;
	private long lastModification;
	private long size;

	public Page(String url, String pageId) throws ParserException, IOException {
		this.url = url;
		this.pageId = pageId;
		initialize();
	}

	public void initialize() throws ParserException, IOException {
		title = Crawler.extractTitle(url);
		URL m_url = new URL(url);
		URLConnection uc = m_url.openConnection();
		lastModification = uc.getLastModified();
		if(lastModification == 0)
			lastModification = hc.getDate();
		if(hc.getContentLengthLong() > 0)
			size = hc.getContentLengthLong();
	}

	public long getLastModificationLong() {
		return lastModification;
	}
	public Date getLastModification() {
		if(lastModification == null) return null;
		return new Date(lastModification);
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

}