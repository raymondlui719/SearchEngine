
import jdbm.RecordManager;
import jdbm.RecordManagerFactory;
import jdbm.htree.HTree;
import jdbm.helper.FastIterator;
import java.util.Vector;
import java.io.IOException;
import java.io.Serializable;

// class Posting implements Serializable
// {
// 	public String keyword;
// 	public int freq;
// 	Posting(String keyword, int freq)
// 	{
// 		this.keyword = keyword;
// 		this.freq = freq;
// 	}
// }

public class DataManager
{

	private HTree hashtable;

	public DataManager(RecordManager recman, String objectname) throws IOException
	{
		//recman = RecordManagerFactory.createRecordManager(recordmanager);
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

	public void addEntry(String keyword, Object obj) throws IOException
	{
		hashtable.put(keyword, obj);
	}

	public void delEntry(String word) throws IOException
	{
		hashtable.remove(word);
	}

	public Object getEntry(String keyword) throws IOException
	{
		return hashtable.get(keyword);
	}

	public HTree getHashTable() throws IOException
	{
		return hashtable;
	}

	public int getTableSize() throws IOException
	{
		FastIterator iter = hashtable.keys();
		int count = 0;
		while(iter.next() != null) {
			count++;
		}
		return count;
	}

	public FastIterator getIterator() throws IOException
	{
		return getHashTable().keys();
	}

	public void printAll() throws IOException
	{
		FastIterator iter = hashtable.keys();
		String key;
		while((key = (String) iter.next()) != null)
		{
			System.out.println(key + ": " + hashtable.get(key).toString());
    			System.out.println("----------------------------------------------");
		}
	}	
	
}
