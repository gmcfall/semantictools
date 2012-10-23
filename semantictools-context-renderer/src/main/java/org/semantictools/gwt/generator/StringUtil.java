package org.semantictools.gwt.generator;

public class StringUtil {

  public static final String capitalize(String text) {
    char first = text.charAt(0);
    if (Character.isUpperCase(first)) {
      return text;
    }
    first = Character.toUpperCase(first);
    StringBuilder builder = new StringBuilder();
    builder.append(first);
    builder.append(text.substring(1));
    return builder.toString();
  }
  
  public static final String getter(String fieldName) {
    StringBuilder builder = new StringBuilder();
    builder.append("get");
    builder.append(capitalize(fieldName));
    return builder.toString();
  }
  
  public static final String setter(String fieldName) {

    StringBuilder builder = new StringBuilder();
    builder.append("set");
    builder.append(capitalize(fieldName));
    return builder.toString();
  }
}
