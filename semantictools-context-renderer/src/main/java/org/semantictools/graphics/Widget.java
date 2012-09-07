package org.semantictools.graphics;

import java.awt.Graphics2D;


public interface Widget {
  
  /**
   * Initialize the layout of this widget at the given position
   */
  void layout();
  
  void setPosition(int left, int top);
  void paint(Graphics2D graphics);
  Rect getBounds();
  
  Style getStyle();
  void setStyle(Style style);
  
  Widget getParent();
  void setParent(Widget parent);

}
