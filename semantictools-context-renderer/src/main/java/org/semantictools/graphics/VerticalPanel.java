package org.semantictools.graphics;

import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;


public class VerticalPanel extends BaseRect implements Widget {
  private List<Widget> children = new ArrayList<Widget>();
  private Style style;
  private Widget parent;
  
  public VerticalPanel() {
  }
  
  public void add(Widget child) {
    children.add(child);
    child.setParent(this);
  }
  
  public Widget getWidget(int index) {
    return children.get(index);
  }

  @Override
  public void paint(Graphics2D graphics) {
    
    for (Widget w : children) {
      GraphicsUtil.paint(graphics, w);
    }

  }

  @Override
  public Rect getBounds() {
    return this;
  }

  @Override
  public void layout() {
    int y = 0;
    
    int marginBottom = 0;
    boolean addMargin = false;
    for (Widget w : children) {
      
      int marginTop = w.getStyle().getMarginTop();
      int margin = Math.max(marginBottom, marginTop);
      if (addMargin) {
        y += margin;
      }
      addMargin = true;
      w.setPosition(0, y);
      w.layout();
      y += w.getBounds().getHeight();

      int width = w.getBounds().getWidth();
      if (width > getWidth()) {
        setWidth(width);
      }
      setHeight(y);
      marginBottom = w.getStyle().getMarginBottom();
    }
    
    if (style != null) {
      int height = getHeight() + style.getPadBottom();
      setHeight(height);
    }
    
  }
  
  public int getWidgetCount() {
    return children.size();
  }
  
  public List<Widget> getChildren() {
    return children;
  }

  @Override
  public void setPosition(int left, int top) {
    setLeft(left);
    setTop(top);
  }

  public Style getStyle() {
    return style;
  }

  public void setStyle(Style style) {
    this.style = style;
  }

  @Override
  public Widget getParent() {
    return parent;
  }

  @Override
  public void setParent(Widget parent) {
    this.parent = parent;
    
  }
  
  

}
