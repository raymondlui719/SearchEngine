
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
    private DataManager word;
    private DataManager bodyWord;
    private DataManager titleWord;
    private DataManager pageBodyWord;
    private DataManager pageTitleWord;
    private DataManager pageBodyMaxTF;
    private DataManager pageTitleMaxTF;

    public Indexer(RecordManager _recman) throws IOException {
        // TODO: index new page here in phase 2
        recman = _recman;
        wordID = new DataManager(recman, "wordID");     // word --> word-id
        word = new DataManager(recman, "word");     // word-id --> word
        bodyWord = new DataManager(recman, "bodyWord"); // word-id --> {page-id, tf}
        titleWord = new DataManager(recman, "titleWord");   
        pageBodyWord = new DataManager(recman, "pageBodyWord"); // forward index (page-id --> {keywords})
        pageTitleWord = new DataManager(recman, "pageTitleWord");
        pageBodyMaxTF = new DataManager(recman, "pageBodyMaxTF");   // forward index (page-id --> max tf)
        pageTitleMaxTF = new DataManager(recman, "pageTitleMaxTF");
    }

    public void indexPageTitle(String pageUrl, String pageID) throws IOException {
        int maxTF = 1;
        String title = Indexer.extractTitle(pageUrl);
        Vector<String> titles = new Vector<String>();
        Vector<String> keywords = new Vector<String>();
        StringTokenizer st = new StringTokenizer(title);
        while (st.hasMoreTokens()) {
            titles.add(st.nextToken());
        }

        for(int i = 0; i < titles.size(); i++)
        {
            String stem = StopStem.processWord(titles.get(i));
            String word_id;
            if(stem == null || stem.equals(""))
                continue;

            if(wordID.getEntry(stem) == null)
            {
                word_id = String.format("%04d", wordID.getTableSize() + 1);
                keywords.add(word_id);
                // word --> word ID
                wordID.addEntry(stem, word_id);
                // word ID --> word
                word.addEntry(word_id, stem);
                Vector<Posting> pList = new Vector<Posting>();
                Vector<Integer> word_pos = new Vector<Integer>();
                word_pos.add(i);
                pList.add(new Posting(pageID, 1, word_pos));
                // word ID --> Posting list
                titleWord.addEntry(word_id, pList);
            }
            else
            {
                word_id = String.valueOf(wordID.getEntry(stem));
                if(!keywords.contains(word_id)) {
                    keywords.add(word_id);
                }
                if(titleWord.getEntry(word_id) != null)
                {
                    Vector<Posting> pList = (Vector<Posting>) titleWord.getEntry(word_id);
                    boolean processed = false;
                    for(Posting p: pList) 
                    {
                        if(p.pageID.equals(pageID))
                        {
                            if(++p.freq > maxTF) {
                                maxTF = p.freq;
                            }
                            p.word_pos.add(i);
                            processed = true;
                            break;
                        }
                    }
                    if(!processed) {
                        Vector<Integer> word_pos = new Vector<Integer>();
                        word_pos.add(i);
                        pList.add(new Posting(pageID, 1, word_pos));
                    }
                    titleWord.addEntry(word_id, pList);
                }
                else
                {
                    Vector<Posting> pList = new Vector<Posting>();
                    Vector<Integer> word_pos = new Vector<Integer>();
                    word_pos.add(i);
                    pList.add(new Posting(pageID, 1, word_pos));
                    titleWord.addEntry(word_id, pList);
                }
            }
        }
        pageTitleWord.addEntry(pageID, keywords);
        pageTitleMaxTF.addEntry(pageID, maxTF);
    }

    public void indexPageBody(String pageUrl, String pageID) throws IOException {
        int maxTF = 1;
        Vector<String> words = Indexer.extractWords(pageUrl);
        Vector<String> keywords = new Vector<String>();
        for(int i = 0; i < words.size(); i++)
        {
            String stem = StopStem.processWord(words.get(i));
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
                // word ID --> word
                word.addEntry(word_id, stem);
                Vector<Posting> pList = new Vector<Posting>();
                Vector<Integer> word_pos = new Vector<Integer>();
                word_pos.add(i);
                pList.add(new Posting(pageID, 1, word_pos));
                // word ID --> Posting list
                bodyWord.addEntry(word_id, pList);
            }
            else
            {
                word_id = String.valueOf(wordID.getEntry(stem));
                if(!keywords.contains(word_id)) {
                    keywords.add(word_id);
                }
                if(bodyWord.getEntry(word_id) != null)
                {
                    Vector<Posting> pList = (Vector<Posting>) bodyWord.getEntry(word_id);
                    boolean processed = false;
                    for(Posting p: pList) 
                    {
                        if(p.pageID.equals(pageID))
                        {
                            if(++p.freq > maxTF) {
                                maxTF = p.freq;
                            }
                            p.word_pos.add(i);
                            processed = true;
                            break;
                        }
                    }
                    if(!processed) {
                        Vector<Integer> word_pos = new Vector<Integer>();
                        word_pos.add(i);
                        pList.add(new Posting(pageID, 1, word_pos));
                    }
                    bodyWord.addEntry(word_id, pList);
                }
                else
                {
                    Vector<Posting> pList = new Vector<Posting>();
                    Vector<Integer> word_pos = new Vector<Integer>();
                    word_pos.add(i);
                    pList.add(new Posting(pageID, 1, word_pos));
                    bodyWord.addEntry(word_id, pList);
                }
            }
        }
        pageBodyWord.addEntry(pageID, keywords);
        pageBodyMaxTF.addEntry(pageID, maxTF);
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
