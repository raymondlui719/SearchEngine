
import java.util.Vector;
import org.htmlparser.beans.StringBean;
import org.htmlparser.Node;
import org.htmlparser.NodeFilter;
import org.htmlparser.Parser;
import org.htmlparser.filters.AndFilter;
import org.htmlparser.filters.NodeClassFilter;
import org.htmlparser.filters.TagNameFilter;
import org.htmlparser.tags.LinkTag;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;
import java.util.StringTokenizer;
import org.htmlparser.beans.LinkBean;
import java.net.URL;
import java.io.IOException;

import jdbm.RecordManager;

@SuppressWarnings("unchecked")
public class Indexer {

    private RecordManager recman; // TODO: needed in phase 2
    private DataManager wordID;
    private DataManager idWord;
    private DataManager wordInfo;
    private DataManager pageWord;

    public Indexer(RecordManager _recman) throws IOException {
        // TODO: index new page here in phase 2
        recman = _recman;
        wordID = new DataManager(recman, "wordID");     // word --> word-id
        idWord = new DataManager(recman, "idWord");     // word-id --> word
        wordInfo = new DataManager(recman, "wordInfo"); // word-id --> {page-id, tf}
        pageWord = new DataManager(recman, "pageWord"); // forward index (page-id --> {keywords})
    }

    public void indexNewPage(String pageUrl, String pageID) throws IOException {
        Vector<String> words = Indexer.extractWords(pageUrl);
        Vector<String> keywords = new Vector<String>();
        for(String w : words)
        {
            String stem = StopStem.processWord(w);
            String word_id;
            if(stem == null || stem.equals(""))
                continue;

            // check if the word is in wordID already
            if(wordID.getEntry(stem) == null)
            {
                word_id = String.format("%04d", wordID.getTableSize() + 1);
                keywords.add(word_id);
                // word --> word ID
                wordID.addEntry(stem, word_id);
                idWord.addEntry(word_id, stem);
                Vector<Posting> pList = new Vector<Posting>();
                pList.add(new Posting(pageID, 1));
                // word ID --> Posting list
                wordInfo.addEntry(word_id, pList);
            }
            else
            {
                word_id = String.valueOf(wordID.getEntry(stem));
                if(!keywords.contains(word_id)) {
                    keywords.add(word_id);
                }
                if(wordInfo.getEntry(word_id) != null)
                {
                    Vector<Posting> pList = (Vector<Posting>) wordInfo.getEntry(word_id);
                    boolean processed = false;
                    for(Posting p: pList) 
                    {
                        if(p.pageID.equals(pageID))
                        {
                            p.freq++;
                            processed = true;
                            break;
                        }
                    }
                    if(!processed)
                        pList.add(new Posting(pageID, 1));
                    wordInfo.addEntry(word_id, pList);
                }
                else
                {
                    Vector<Posting> pList = new Vector<Posting>();
                    pList.add(new Posting(pageID, 1));
                    wordInfo.addEntry(word_id, pList);
                }
            }
        }
        pageWord.addEntry(pageID, keywords);
    }

    public static Vector<String> extractWords(String url) {
        Vector<String> words = new Vector<String>();
        StringBean sb = new StringBean();
        sb.setURL(url);
        sb.setLinks(false);
        String contents = sb.getStrings();
        StringTokenizer st = new StringTokenizer(contents);
        while (st.hasMoreTokens()) {
            words.add(st.nextToken());
        }
        return words;
    }

    public static Vector<String> extractLinks(String url) {
        Vector<String> links = new Vector<String>();
        LinkBean lb = new LinkBean();
        lb.setURL(url);
        URL[] URL_array = lb.getLinks();
        for (URL s: URL_array) {
            links.add(s.toString());
        }
        return links;
    }

    public static String extractTitle(String url) {
        Parser parser = new Parser();
        try {
            parser.setURL(url);
            Node node = (Node)parser.extractAllNodesThatMatch(new TagNameFilter ("title")).elementAt(0);
            if(node != null) {
                return node.toPlainTextString();
            }
            else {
                return null;
            }
        }
        catch(ParserException e) {
            return null;
        }
    }

}
