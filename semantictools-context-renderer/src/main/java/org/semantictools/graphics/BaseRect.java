package org.semantictools.graphics;

public class BaseRect implements Rect {

  private int left;
  private int top;
  private int width;
  private int height;
  
  @Override
  public int getLeft() {
    return left;
  }

  @Override
  public int getTop() {
    return top;
  }

  @Override
  public int getHeight() {
    return height;
  }

  @Override
  public int getWidth() {
    return width;
  }
  
  @Override
  public void setLeft(int left) {
    this.left = left;
  }

  @Override
  public void setTop(int top) {
    this.top = top;    
  }

  @Override
  public void setHeight(int height) {
    this.height = height;
  }

  @Override
  public void setWidth(int width) {
    this.width = width;
  }

  @Override
  public int getRight() {
    return left + width;
  }

  @Override
  public int getBottom() {
    return top + height;
  }
  public String toString() {
    return "BaseRect(left=" + left + ", top=" + top + ", width=" + width + ", height=" + height + ")";
  }

  @Override
  public void setPosition(int left, int top) {
    this.left = left;
    this.top = top;
  }


}
