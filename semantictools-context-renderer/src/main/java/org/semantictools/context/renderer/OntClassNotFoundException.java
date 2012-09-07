package org.semantictools.context.renderer;

public class OntClassNotFoundException extends RuntimeException {
  private static final long serialVersionUID = 1L;
  
  public OntClassNotFoundException(String uri) {
    super("RDF Class not found: " + uri);
  }

}
