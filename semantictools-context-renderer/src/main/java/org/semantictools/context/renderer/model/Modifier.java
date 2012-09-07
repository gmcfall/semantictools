package org.semantictools.context.renderer.model;

public enum Modifier {
  NONE(""),
  OPTIONAL("?"),
  REPEATABLE("+");
  
  private String symbol;
  
  private Modifier(String symbol) {
    this.symbol = symbol;
  }
  
  public String getSymbol() {
    return symbol;
  }
  
  
}
