package org.semantictools.json;

public class UnsupportedDatetypeException extends RuntimeException {
  private static final long serialVersionUID = 1L;
  
  public UnsupportedDatetypeException(String uri) {
    super("Unsupported datatype: " + uri);
  }

}
