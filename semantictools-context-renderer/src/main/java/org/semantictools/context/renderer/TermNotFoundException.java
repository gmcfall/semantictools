package org.semantictools.context.renderer;

public class TermNotFoundException extends RuntimeException {
  private static final long serialVersionUID = 1L;
  
  public TermNotFoundException(String uri) {
    super("The JSON-LD context does not contain a term for the URI: " + uri);
  }

}
