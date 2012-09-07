package org.semantictools.json;

public class UnsupportedDatatypeException extends RuntimeException {
  private static final long serialVersionUID = 1L;
  
  public UnsupportedDatatypeException(String uri) {
    super("Unsupported datatype: " + uri);
  }

}
