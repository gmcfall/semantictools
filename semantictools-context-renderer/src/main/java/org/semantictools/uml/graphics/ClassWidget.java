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
package org.semantictools.uml.graphics;

import java.awt.Color;
import java.awt.Graphics2D;

import org.semantictools.graphics.ConnectableWidget;
import org.semantictools.graphics.Label;
import org.semantictools.graphics.Style;
import org.semantictools.graphics.VerticalPanel;

public class ClassWidget extends ConnectableWidget {
  
  private VerticalPanel panel;
  
  public ClassWidget(Label className, Style style) {
    super(new VerticalPanel(), style);
    panel = (VerticalPanel) getBody();
    panel.add(className);
  }
  
  public void addField(Label label) {
    panel.add(label);
  }
  
  @Override
  public void paint(Graphics2D g) {
    super.paint(g);
    if (panel.getChildren().size()>1) {
      int height = panel.getWidget(0).getBounds().getHeight();
      int width = panel.getWidth();
      

      Color borderColor = getStyle().getBorderColor();
      if (borderColor != null) {
        g.setColor(borderColor);
        g.drawLine(0, height, width, height);
      }
      
    }
    
  }
  

}
