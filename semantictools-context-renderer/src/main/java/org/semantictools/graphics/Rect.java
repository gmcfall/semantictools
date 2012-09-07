package org.semantictools.graphics;

public interface Rect {
  
  public int getLeft();
  public void setLeft(int left);
  
  public int getRight();
  public int getBottom();
  
  public int getTop();  
  public void setTop(int top);
  
  public int getHeight();
  public void setHeight(int height);
  
  public int getWidth();
  public void setWidth(int width);
  
  public void setPosition(int left, int top);
 
  

}
