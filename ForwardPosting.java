import java.io.Serializable;
public class ForwardPosting implements Serializable, Comparable<ForwardPosting>
{
	public String wordID;
	public int freq;
	ForwardPosting(String wordID, int freq)
	{
		this.wordID = wordID;
		this.freq = freq;
	}

	@Override
	public int compareTo(ForwardPosting fp)
	{
		if(this.freq > fp.freq)
			return -1;
		else if(this.freq == fp.freq)
			return 0;
		else
			return 1;
	}
}