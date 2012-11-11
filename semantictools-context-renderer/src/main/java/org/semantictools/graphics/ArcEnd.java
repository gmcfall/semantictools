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

import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;

public class ArcEnd  implements Widget {
  
  public static final Shape DIAMOND = createDiamond();
  public static final Shape TRIANGLE = createTriangle();


  private static Shape createDiamond() {
    int height = 4;
    int width = 22;
    int center = width/2;
    int x[] = new int[] {0, center, width, center};
    int y[] = new int[] {0, -height, 0, height};
    
    return new Polygon(x, y, 4);
  }
  
  private static Shape createTriangle() {
    int x[] = new int[] {0, 20, 20};
    int y[] = new int[] {0, -8, 8};
    return new Polygon(x, y, 3);
  }
  
  private Arc arc;
  private Shape shape;
  private boolean filled;
  private int tipX;
  private int tipY;
  private int arcX;
  private int arcY;
  private Edge edge;
  private String title;
  private String subtitle;
  private int titleX;
  private int titleY;
  private int subtitleX;
  private int subtitleY;
  private Style style;
  private Rect bounds = new BaseRect();
  private Widget attachedWidget;
  
  public ArcEnd(String title, String subtitle, Style style) {
    this.title = title;
    this.subtitle = subtitle;
    this.style = style;
  }
  
  
  /**
   * Returns the shape that represents this end point (e.g. arrow, diamond, etc.)
   */
  public Shape getShape() {
    return shape;
  }


  /**
   * Sets the shape that represents this end point (e.g. arrow, diamond, etc.)
   */
  public void setShape(Shape shape) {
    this.shape = shape;
  }


  /**
   * Returns a value specifying whether the shape (arrow head, diamond, etc.) representing this end point
   * is filled.
   */
  public boolean isFilled() {
    return filled;
  }

  /**
   * Set the value specifying whether the shape (arrow head, diamond, etc.) representing this end point
   * is filled.
   */
  public void setFilled(boolean filled) {
    this.filled = filled;
  }

  /**
   * Returns the x-coordinate of the point where the arc joins this ArcEnd.
   */
  public int getArcX() {
    return arcX;
  }

  /**
   * Returns the y-coordinate of the point where the arc joins this ArcEnd.
   */
  public void setArcX(int arcX) {
    this.arcX = arcX;
  }


  public int getArcY() {
    return arcY;
  }

  public void setArcY(int arcY) {
    this.arcY = arcY;
  }


  public Style getStyle() {
    return style;
  }


  public void setStyle(Style style) {
    this.style = style;
  }


  public int getX() {
    return tipX;
  }

  public void setX(int x) {
    this.tipX = x;
  }

  public int getY() {
    return tipY;
  }

  public void setY(int y) {
    this.tipY = y;
  }

  public String getTitle() {
    return title;
  }

  public String getSubtitle() {
    return subtitle;
  }

  public Arc getArc() {
    return arc;
  }

  public void setArc(Arc arc) {
    this.arc = arc;
  }
  
  /**
   * Returns the amount of space reserved above the arc for text or decorators 
   * (like arrows or diamonds).
   */
  public int getAscent() {
    int result = 0;
    if (title != null) {
      int padTop = style.getPadTop();
      int padBottom = style.getPadBottom();
      int ascent = style.getMetrics().getMaxAscent();
      result += padBottom + ascent + padTop;
    }
    
    return result;
  }
  
  /**
   * Returns the amount of space reserved below the arc for text or decorators
   * (like an arrow or diamond).
   */
  public int getDescent() {
    int result = 0;
    if (subtitle != null) {
      int padTop = style.getPadTop();
      int padBottom = style.getPadBottom();
      int ascent = style.getMetrics().getMaxAscent();
      result += padBottom + ascent + padTop;
    }
    
    return result;
    
  }
  
  
  public void attachAt(int x, int y) {
    this.tipX = x;
    this.tipY = y;
    
    Edge edge = getEdge();
    switch (edge) {
    
    case RIGHT : attachRight(); break;
    case LEFT :  attachLeft(); break;
    case TOP : attachTop(); break;
    case BOTTOM: attachBottom(); break;
      
    
    }
    
    
  }


  public Widget getAttachedWidget() {
    return attachedWidget;
  }




  public void attachTo(Widget attachedWidget) {
    this.attachedWidget = attachedWidget;
  }


  /**
   * Attach this ArcEnd to the left edge of the attachedWidget
   */
  private void attachLeft() {

    FontMetrics metrics = style.getMetrics();
    
    int shapeWidth = 0;
    int width = 0;
    int top = tipY;
    int bottom = tipY;
    
    arcX = tipX;
    arcY = tipY;
    if (shape != null) {
      Rectangle2D bounds = shape.getBounds2D();
      shapeWidth = (int) bounds.getWidth();
      
      int height = (int)(bounds.getHeight()/2);
      top = tipY - height - style.getPadTop();
      bottom = tipY + height + style.getPadBottom();
      
      arcX = tipX - shapeWidth;
    }
    
    if (title != null) {
      int textWidth = metrics.stringWidth(title);
      titleX = arcX - style.getPadRight() - textWidth;
      titleY = arcY - style.getPadBottom();
      width = textWidth;
      top = Math.min(top, titleY - metrics.getMaxAscent() - style.getPadTop());
      
    }
    
    if (subtitle != null) {
      int textWidth = metrics.stringWidth(subtitle);
      subtitleX = arcX - style.getPadRight() - textWidth;
      subtitleY = arcY + style.getPadTop() + metrics.getMaxAscent();
      width = Math.max(width, textWidth);
      bottom = Math.max(bottom, subtitleY + metrics.getMaxDescent() + style.getPadBottom());
      
    }
    
    width = shapeWidth + width + style.getPadLeft() + style.getPadRight();
    int height = bottom - top;
    
    bounds.setTop(top);
    bounds.setLeft(tipX);
    bounds.setHeight(height);
    bounds.setWidth(width);
  }
  
  /**
   * Attach this ArcEnd to the top edge of the parent widget
   */
  private void attachTop() {
    
    arcX = tipX;
    arcY = tipY;
    bounds.setPosition(tipX, tipY);
    bounds.setWidth(0);
    bounds.setHeight(0);
    
    
  }
  

  /**
   * Attach this ArcEnd to the top edge of the parent widget
   */
  private void attachBottom() {
    
    arcX = tipX;
    arcY = tipY;
    bounds.setPosition(tipX, tipY);
    bounds.setWidth(0);
    bounds.setHeight(0);
    if (shape != null) {
      int width = (int) shape.getBounds2D().getHeight();
      int height = (int) shape.getBounds2D().getWidth();
      arcY += height;
      bounds.setHeight(height);
      bounds.setWidth(width);
    }
    
    
  }
  
  public void translate(int dx, int dy) {
    arcX += dx;
    arcY += dy;
    bounds.setPosition(bounds.getLeft()+dx, bounds.getTop()+dy);
    subtitleX += dx;
    subtitleY += dy;
    tipX += dx;
    tipY += dy;
    titleX += dx;
    titleY += dy;
  }


  /**
   * Attach this ArcEnd to the right edge of the attachedWidget
   */
  private void attachRight() {
    
    FontMetrics metrics = style.getMetrics();
    
    int shapeWidth=0;
    int width = 0;
    int top = tipY;
    int bottom = tipY;
    
    arcX = tipX;
    arcY = tipY;
    
    if (shape != null) {
      Rectangle2D bounds = shape.getBounds2D();
      shapeWidth = (int) bounds.getWidth();
      int height = (int)(bounds.getHeight()/2);
      
      arcX = tipX + shapeWidth;
      top = tipY - height - style.getPadTop();
      bottom = tipY + height + style.getPadBottom();
    }
    
    if (title != null) {
      titleX = arcX + style.getPadLeft();
      titleY = arcY - style.getPadBottom();
      width = metrics.stringWidth(title);
      top = Math.min(top, arcY - metrics.getMaxAscent() - style.getPadTop());
      
    }
    
    if (subtitle != null) {
      subtitleX = arcX + style.getPadLeft();
      subtitleY = arcY + style.getPadTop() + metrics.getMaxAscent();
      width = Math.max(width, metrics.stringWidth(subtitle));
      bottom = Math.max(bottom, subtitleY + metrics.getMaxDescent() + style.getPadBottom());
      
    }
    
    int height = bottom - top;
   
    width += shapeWidth + style.getPadLeft() + style.getPadRight();
    
    
    bounds.setTop(top);
    bounds.setLeft(tipX);
    bounds.setHeight(height);
    bounds.setWidth(width);
    
    
  }




  public Edge getEdge() {
    if (attachedWidget == null) {
      throw new RuntimeException("ArcEnd is not attached to any widget");
    }
    if (edge == null) {
    Rect bounds = attachedWidget.getBounds();
    edge =
        (tipX==0) ? Edge.LEFT :
        (tipX==bounds.getWidth()) ? Edge.RIGHT :
        (tipY==0) ? Edge.TOP :
        Edge.BOTTOM;
    }
    return edge;
  }





  @Override
  public void paint(Graphics2D g) {
    if (title != null) {
      
      g.setColor(style.getColor());
      g.setFont(style.getFont());
      g.drawString(title, titleX, titleY);
      
    } 
    if (subtitle != null) {
      g.setColor(style.getColor());
      g.setFont(style.getFont());
      g.drawString(subtitle, subtitleX, subtitleY);
    }
    if (shape != null) {
      paintShape(g);
    }
    
  }


  private void paintShape(Graphics2D g) {
    g.setColor(style.getBorderColor());
    
    AffineTransform tx = new AffineTransform(g.getTransform());
    
    g.translate(tipX, tipY);
    switch (edge) {
    case BOTTOM :
      g.rotate(Math.PI/2);
      break;
      
    case TOP :
      g.rotate(-Math.PI/2);
      break;
      
    }
    g.draw(shape);
    if (filled) g.fill(shape);
    
    g.setTransform(tx);
    
  }


  @Override
  public Rect getBounds() {
    return bounds;
  }




  @Override
  public void layout() {
    attachAt(tipX, tipY);
  }




  @Override
  public void setPosition(int left, int top) {
    throw new UnsupportedOperationException();    
  }

  @Override
  public Widget getParent() {
    return attachedWidget;
  }


  @Override
  public void setParent(Widget parent) {
    attachedWidget = parent;
  }
  
  public String toString() {
    return "ArcEnd(x=" + tipX + ", y=" + tipY + ")";
  }
  

}
