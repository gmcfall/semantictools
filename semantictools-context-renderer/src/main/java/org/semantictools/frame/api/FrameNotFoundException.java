package org.semantictools.frame.api;

public class FrameNotFoundException extends RuntimeException {
  private static final long serialVersionUID = 1L;
  
  public FrameNotFoundException(String typeURI) {
    super("Frame not found: " + typeURI);
  }

}
