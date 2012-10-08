package org.semantictools.context.renderer;

public class TypeNotFoundException extends RuntimeException {
  private static final long serialVersionUID = 1L;
  
  public TypeNotFoundException(String uri) {
    super("RDF type not found: " + uri);
  }

}
