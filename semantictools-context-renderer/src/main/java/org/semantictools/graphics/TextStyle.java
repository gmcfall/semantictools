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
