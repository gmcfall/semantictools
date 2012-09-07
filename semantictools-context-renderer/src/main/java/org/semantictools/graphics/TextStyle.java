package org.semantictools.graphics;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;


public class TextStyle {

  private Color color;
  private Font font;
  private FontMetrics metrics;
  private Padding padding;
  
 
  public Color getColor() {
    return color;
  }
  public void setColor(Color color) {
    this.color = color;
  }
  public Font getFont() {
    return font;
  }
  public void setFont(Font font) {
    this.font = font;
  }
  public FontMetrics getMetrics() {
    return metrics;
  }
  public void setMetrics(FontMetrics metrics) {
    this.metrics = metrics;
  }
  public Padding getPadding() {
    return padding;
  }
  public void setPadding(Padding padding) {
    this.padding = padding;
  }
}
