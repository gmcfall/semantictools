package org.semantictools.context.renderer.model;

public enum Container {
  
  LIST("@list"),
  SET("@set"),
  NONE("");
  
  private String text;
  
  private Container(String text) {
    this.text = text;
  }
  
  public String getText() {
    return text;
  }

}
