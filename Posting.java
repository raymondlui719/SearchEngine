import java.io.Serializable;
import java.util.Vector;
public class Posting implements Serializable
{
	private static final long serialVersionUID = -490076162276320538L;
	public String pageID;
	public int freq;
	public Vector<Integer> word_pos;
	Posting(String pageID, int freq, Vector<Integer> word_pos)
	{
		this.pageID = pageID;
		this.freq = freq;
		this.word_pos = word_pos;
	}
	public String toString() {
		String result = pageID + ": <";
		for(Integer pos: word_pos) {
			result += pos + " ";
		}
		result += ">;";
		return result;
	}
}