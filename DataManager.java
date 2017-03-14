
import jdbm.RecordManager;
import jdbm.RecordManagerFactory;
import jdbm.htree.HTree;
import jdbm.helper.FastIterator;
import java.util.Vector;
import java.io.IOException;
import java.io.Serializable;

class Posting implements Serializable
{
	public String keyword;
	public int freq;
	Posting(String keyword, int freq)
	{
		this.keyword = keyword;
		this.freq = freq;
	}
}

public class DataManager
{

	private HTree hashtable;

	DataManager(RecordManager recman, String objectname) throws IOException
	{
		recman = RecordManagerFactory.createRecordManager(recordmanager);
		long recid = recman.getNamedObject(objectname);
			
		if (recid != 0)
		{
			hashtable = HTree.load(recman, recid);
		}
		else
		{
			hashtable = HTree.createInstance(recman);
			recman.setNamedObject(objectname , hashtable.getRecid());
		}
	}

	public void addEntry(String keyword, Object x) throws IOException
	{
		hashtable.put(keyword, x);
	}

	public void delEntry(String word) throws IOException
	{
		hashtable.remove(word);
	}

	public Object getEntry(String keyword) throws IOException
	{
		return hashtable.get(keyword);
	}

	public void printAll() throws IOException
	{
		FastIterator iter = hashtable.keys();
		String key;
		while((key = (String) iter.next()) != null)
		{
			System.out.print(key + " " + hashtable.get(key) + "; ");
		}
	}	
	
}
