package org.semantictools.context.renderer;

public class TermNotFoundException extends RuntimeException {
  private static final long serialVersionUID = 1L;
  
  public TermNotFoundException(String uriOrSimpleName) {
    super("The JSON-LD context does not contain the requested term: " + uriOrSimpleName);
  }
  
 

}
