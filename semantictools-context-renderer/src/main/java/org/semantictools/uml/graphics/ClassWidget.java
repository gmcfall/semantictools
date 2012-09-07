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
