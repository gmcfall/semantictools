package org.semantictools.context.view;

public class StringUtil {
  
  private static final String vowels = "aeiouAEIOU";
  
  public static String article(String word) {
    char c = word.charAt(0);
    return vowels.indexOf(c) < 0 ? "a " : "an ";
  }

}
