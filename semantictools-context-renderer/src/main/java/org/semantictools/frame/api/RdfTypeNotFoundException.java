package org.semantictools.frame.api;

public class RdfTypeNotFoundException extends RuntimeException {
  private static final long serialVersionUID = 1L;
  
  public RdfTypeNotFoundException(String typeURI) {
    super("RDF type not found: " + typeURI);
  }

}
