
import IRUtilities.*;
import java.io.*;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Vector;

@SuppressWarnings("unchecked")
public class StopStem {
  static Porter porter;
  static Vector<String> stopWords;

  static {
    porter = new Porter();
    readStopWords(stopWords);
  }

  public static boolean isStopWord(String str) {
    return stopWords.contains(str);
  }

  private static void readStopWords(Vector<String> stopWords) {
    File f = new File("stopwords.txt");
    if(!f.exists()) {
      System.out.println("File not exist.");
      return;
    }
    try {
      Scanner in = new Scanner(f);
      while(in.hasNext()) {
        String word = in.next();
        stopwords.add(word);
      }
    } catch(FileNotFoundException e) {
      e.printStackTrace();
    }
  }

  public static boolean isNumber(String str) {
    try {
      double num = Double.parseDouble(str);
    } catch(NumberFormatException e) {
      return false;
    }
    return true;
  }

  public static String stem(String str) {
    return porter.stripAffixes(str);
  }

}
