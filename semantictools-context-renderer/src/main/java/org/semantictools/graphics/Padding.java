package org.semantictools.graphics;

public class Padding {
  private int padTop;
  private int padBottom;
  private int padLeft;
  private int padRight;
  private int maxDescent;
  
  
  public Padding(int top, int left, int bottom, int right) {
    padTop = top;
    padBottom = bottom;
    padLeft = left;
    padRight = right;
  }
  
  public Padding() {
    
  }
  
  public int getPadTop() {
    return padTop;
  }
  public void setPadTop(int padTop) {
    this.padTop = padTop;
  }
  public int getPadBottom() {
    return padBottom;
  }
  public void setPadBottom(int padBottom) {
    this.padBottom = padBottom;
  }
  public int getPadLeft() {
    return padLeft;
  }
  public void setPadLeft(int padLeft) {
    this.padLeft = padLeft;
  }
  public int getPadRight() {
    return padRight;
  }
  public void setPadRight(int padRight) {
    this.padRight = padRight;
  }

  @Deprecated
  public int getMaxDescent() {
    return maxDescent;
  }

  @Deprecated
  public void setMaxDescent(int maxDescent) {
    this.maxDescent = maxDescent;
  }
  
  

}
