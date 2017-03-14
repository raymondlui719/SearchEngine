
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


public class Indexer {

  private Page page;

  public static Vector<String> extractWords(String url) throws ParserException {
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

  public static Vector<String> extractLinks(String url) throws ParserException  {
    Vector<String> links = new Vector<String>();
    LinkBean lb = new LinkBean();
    lb.setURL(url);
    URL[] URL_array = lb.getLinks();
    for (URL s: URL_array) {
      links.add(s.toString());
    }
    return links;
  }

  public static String extractTitle(String url) throws ParserException {
    Parser parser = new Parser();
    parser.setURL(url);
    Node node = (Node)parser.extractAllNodesThatMatch(new TagNameFilter ("title")).elementAt(0);
    if(node != null) {
      return node.toPlainTextString();
    }
    else {
      return null;
    }
  }

}
