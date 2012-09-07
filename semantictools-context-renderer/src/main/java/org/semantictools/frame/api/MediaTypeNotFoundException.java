package org.semantictools.frame.api;

public class MediaTypeNotFoundException extends RuntimeException {
  private static final long serialVersionUID = 1L;
  
  public MediaTypeNotFoundException(String mediaType) {
    super("Media type not found: " + mediaType);
  }

}
