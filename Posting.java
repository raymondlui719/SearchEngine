import java.io.Serializable;
public class Posting implements Serializable
{
	private static final long serialVersionUID = -490076162276320538L;
	public String pageID;
	public int freq;
	Posting(String pageID, int freq)
	{
		this.pageID = pageID;
		this.freq = freq;
	}
	public String toString() {
		return pageID + ": " + freq + "; ";
	}
}