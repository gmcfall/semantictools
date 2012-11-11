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

import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;


public class HorizontalPanel extends BaseRect implements Widget {
  private List<Widget> children = new ArrayList<Widget>();
  private Style style;
  private Widget parent;
  
  public HorizontalPanel() {
  }
  
  public void add(Widget child) {
    children.add(child);
    child.setParent(this);
  }
  
  public Widget getWidget(int index) {
    return children.get(index);
  }

  @Override
  public void paint(Graphics2D g) {
    for (Widget w : children) {
      GraphicsUtil.paint(g, w);
    }

  }

  @Override
  public Rect getBounds() {
    return this;
  }

  @Override
  public void layout() {
    int top = 0;
    int x = 0;
    int height = 0;
    
    if (children.isEmpty()) return;
    
    int marginRight = 0;
    
    for (Widget w : children) {
      int marginLeft = w.getStyle().getMarginLeft();
      int margin = Math.max(marginLeft, marginRight);
      if (x==0) {
        margin = 0;
      }
      x += margin;
      w.setPosition(x, top);
      w.layout();
      marginRight = w.getStyle().getMarginRight();
      
      height = w.getBounds().getHeight();
      if (height > getHeight()) {
        setHeight(height);
      }
      x += w.getBounds().getWidth();
      if (x > getWidth()) {
        setWidth(x);
      }      
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
