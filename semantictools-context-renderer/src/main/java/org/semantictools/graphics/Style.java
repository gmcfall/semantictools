/*******************************************************************************
 * Copyright 2012 Pearson Education
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package org.semantictools.graphics;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;

public class Style {
  private Color color;
  private Color bgColor;
  private Color borderColor;
  private Font font;
  private FontMetrics metrics;
  private int padTop;
  private int padBottom;
  private int padRight;
  private int padLeft;
  private int marginLeft;
  private int marginRight;
  private int marginBottom;
  private int marginTop;
  
  private TextAlign textAlign = TextAlign.LEFT;
  
  public Color getColor() {
    return color;
  }
  public void setColor(Color color) {
    this.color = color;
  }
  public Color getBgColor() {
    return bgColor;
  }
  public void setBgColor(Color bgColor) {
    this.bgColor = bgColor;
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
  public int getPadRight() {
    return padRight;
  }
  public void setPadRight(int padRight) {
    this.padRight = padRight;
  }
  public int getPadLeft() {
    return padLeft;
  }
  public void setPadLeft(int padLeft) {
    this.padLeft = padLeft;
  }
  public int getMarginLeft() {
    return marginLeft;
  }
  public void setMarginLeft(int marginLeft) {
    this.marginLeft = marginLeft;
  }
  public int getMarginRight() {
    return marginRight;
  }
  public void setMarginRight(int marginRight) {
    this.marginRight = marginRight;
  }
  public int getMarginBottom() {
    return marginBottom;
  }
  public void setMarginBottom(int marginBottom) {
    this.marginBottom = marginBottom;
  }
  public int getMarginTop() {
    return marginTop;
  }
  public void setMarginTop(int marginTop) {
    this.marginTop = marginTop;
  }
  public TextAlign getTextAlign() {
    return textAlign;
  }
  public void setTextAlign(TextAlign textAlign) {
    this.textAlign = textAlign;
  }
  public Color getBorderColor() {
    return borderColor;
  }
  public void setBorderColor(Color borderColor) {
    this.borderColor = borderColor;
  }
  
  
  

}
