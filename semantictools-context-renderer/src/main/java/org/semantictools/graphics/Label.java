package org.semantictools.graphics;

import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;

public class Label extends BaseRect implements Widget {
  private String text;
  private int textX;
  private int textY;
  private Style style;
  private Widget parent;
  

  public Label(String text, Style style) {
    this.text = text;
    this.style = style;
  }

  @Override
  public void layout() {
    
    FontMetrics metrics = style.getMetrics();
    int ascent = metrics.getMaxAscent();
    int padLeft = style.getPadLeft();
    int padTop = style.getPadTop();
    int padRight = style.getPadRight();
    int padBottom = style.getPadBottom();
    
    int textWidth = metrics.stringWidth(text);
    
    int height = padTop + ascent + padBottom;
    int width = padLeft + textWidth + padRight;
    
    setWidth(width);
    setHeight(height);
    
    textX = padLeft;
    textY = padTop + ascent;
    

  }

  @Override
  public void setPosition(int left, int top) {
    setLeft(left);
    setTop(top);

  }

  @Override
  public void paint(Graphics2D g) {
    
    
    
    
    Font font = getStyle().getFont();
    if (font != null) {
      g.setFont(font);
    }
    g.setColor(getStyle().getColor());
    g.drawString(text, textX, textY);

  }

  @Override
  public Rect getBounds() {
    return this;
  }

  @Override
  public Style getStyle() {
    return style;
  }

  @Override
  public Widget getParent() {
    return parent;
  }

  @Override
  public void setParent(Widget parent) {
    this.parent = parent;    
  }

  public String toString() {
    return "Label(text='" + text + "', x=" + textX + ", y=" + textY + ", left=" + getBounds().getLeft() + 
        ", top=" + getBounds().getTop() + ", width=" + getBounds().getWidth() + ", height=" +
        getBounds().getHeight() + ")";
  }

  @Override
  public void setStyle(Style style) {
    this.style = style;
  }

}
