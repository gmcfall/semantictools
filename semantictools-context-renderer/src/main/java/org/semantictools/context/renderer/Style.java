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
package org.semantictools.context.renderer;

import java.awt.Color;
import java.awt.Font;

import org.semantictools.graphics.Padding;

public class Style {
  private Color boxBorderColor;
  private Color nameTextColor;
  private Color typeTextColor;
  private Color nameBgColor;
  private Color typeBgColor;
  private Color modifierTextColor;
  private Color arcColor;
  private Font nameFont;
  private Font typeFont;
  private Font modifierFont;
  private Font labelFont;
  
  private Padding namePadding;
  private Padding typePadding;
  private int verticalSpacing;
  private int horizontalSpacing;
  private int modifierDiameter;
  
  public Style(boolean useDefaultStyle) {
    if (useDefaultStyle) {
      boxBorderColor = new Color(0.5f, 0.5f, 0.5f);
      nameTextColor = Color.black;
      typeTextColor = Color.black;
      nameBgColor = Color.white;
      typeBgColor = new Color(0.83f, 0.83f, 0.83f);
      arcColor = new Color(0.2f, 0.4f, 0.6f);
      modifierTextColor = arcColor;
      
      nameFont = new Font("Arial", Font.BOLD, 14);
      typeFont = new Font("Arial", Font.PLAIN, 12);
      modifierFont = new Font("Arial", Font.PLAIN, 14);
      labelFont = new Font("Garamond", Font.PLAIN, 16);
      
      namePadding = new Padding(5, 5, 2, 5);
      typePadding = new Padding(2, 5, 2, 5);
      
      verticalSpacing = 15;
      horizontalSpacing = 100;
      
    }
    
  }

  
  
  public Font getLabelFont() {
    return labelFont;
  }



  public void setLabelFont(Font labelFont) {
    this.labelFont = labelFont;
  }



  public int getModifierDiameter() {
    return modifierDiameter;
  }


  public void setModifierDiameter(int diameter) {
    this.modifierDiameter = diameter;
  }


  public int getVerticalSpacing() {
    return verticalSpacing;
  }


  public void setVerticalSpacing(int verticalSpacing) {
    this.verticalSpacing = verticalSpacing;
  }


  public int getHorizontalSpacing() {
    return horizontalSpacing;
  }


  public void setHorizontalSpacing(int horizontalSpacing) {
    this.horizontalSpacing = horizontalSpacing;
  }


  public Color getBoxBorderColor() {
    return boxBorderColor;
  }

  public void setBoxBorderColor(Color boxOutline) {
    this.boxBorderColor = boxOutline;
  }

  public Color getNameTextColor() {
    return nameTextColor;
  }

  public void setNameTextColor(Color nameTextColor) {
    this.nameTextColor = nameTextColor;
  }

  public Color getTypeTextColor() {
    return typeTextColor;
  }

  public void setTypeTextColor(Color typeTextColor) {
    this.typeTextColor = typeTextColor;
  }

  public Color getNameBgColor() {
    return nameBgColor;
  }

  public void setNameBgColor(Color nameBgColor) {
    this.nameBgColor = nameBgColor;
  }

  public Color getTypeBgColor() {
    return typeBgColor;
  }

  public void setTypeBgColor(Color typeBgColor) {
    this.typeBgColor = typeBgColor;
  }

  public Color getModifierTextColor() {
    return modifierTextColor;
  }

  public void setModifierTextColor(Color modifierTextColor) {
    this.modifierTextColor = modifierTextColor;
  }

  public Color getArcColor() {
    return arcColor;
  }

  public void setArcColor(Color arcColor) {
    this.arcColor = arcColor;
  }

  public Font getNameFont() {
    return nameFont;
  }

  public void setNameFont(Font nameFont) {
    this.nameFont = nameFont;
  }

  public Font getTypeFont() {
    return typeFont;
  }

  public void setTypeFont(Font typeFont) {
    this.typeFont = typeFont;
  }

  public Font getModifierFont() {
    return modifierFont;
  }

  public void setModifierFont(Font modifierFont) {
    this.modifierFont = modifierFont;
  }

  public Padding getNamePadding() {
    return namePadding;
  }

  public void setNamePadding(Padding namePadding) {
    this.namePadding = namePadding;
  }

  public Padding getTypePadding() {
    return typePadding;
  }

  public void setTypePadding(Padding typePadding) {
    this.typePadding = typePadding;
  }
  
  
  
  

}
