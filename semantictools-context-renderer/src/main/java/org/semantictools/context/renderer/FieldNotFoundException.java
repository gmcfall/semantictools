package org.semantictools.context.renderer;

public class FieldNotFoundException extends RuntimeException {
  private static final long serialVersionUID = 1L;
  
  public FieldNotFoundException(String frameName, String fieldName) {
    super("Field not found: " + frameName + "." + fieldName);
  }

}
