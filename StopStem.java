
import IRUtilities.*;
import java.io.*;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Vector;
import java.util.Scanner;
import java.lang.NullPointerException;

@SuppressWarnings("unchecked")
public class StopStem {
  static Porter porter;
  static Vector<String> stopWords;

  static {
    porter = new Porter();
    stopWords = new Vector<String>();
    readStopWords(stopWords);
  }

  private static boolean isStopWord(String str) {
    return stopWords.contains(str);
  }

  // read stop words from file
  private static void readStopWords(Vector<String> stopWords) {
    File f = new File("./stopwords.txt");
    if(!f.exists()) {
      System.out.println("File not exist.");
      return;
    }
    try {
      Scanner in = new Scanner(f);
      while(in.hasNext()) {
        String word = in.next();
        stopWords.add(word);
      }
    } catch(FileNotFoundException e) {
      e.printStackTrace();
    }
  }

  // avoid handling numeric words
  private static boolean isNumber(String str) {
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

  // utility function for stemming words in any scenario
  public static String processWord(String str) {
    if(isNumber(str) || isStopWord(str)) {
      return "";
    }
    else {
      String s = stem(str);
      if(isNumber(s))
        return "";
      else
        return s;
    }
  }

}
