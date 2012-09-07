package org.semantictools.graphics;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;

/**
 * A widget that can have connectors attached to it.
 * @author Greg McFall
 *
 */
public class ConnectableWidget extends BaseRect implements HasConnectors, Widget {

  private Style style;
  private ArcList leftArcs;
  private ArcList rightArcs;
  private ArcList topArcs;
  private ArcList bottomArcs;
  private Widget body;
  private Widget parent;
  
  private int routeSpacing = 10;
  
  
  public ConnectableWidget(Widget body, Style style) {
    this.style = style;
    this.body = body;
    body.setParent(this);
  }
  
  public Widget getBody() {
    return body;
  }



  @Override
  public List<ArcEnd> listLeftArcs() {
    return leftArcs;
  }

  @Override
  public List<ArcEnd> listTopArcs() {
    return topArcs;
  }

  @Override
  public List<ArcEnd> listRightArcs() {
    return rightArcs;
  }

  @Override
  public List<ArcEnd> listBottomArcs() {
    return bottomArcs;
  }

  @Override
  public void addLeftArc(ArcEnd end) {
    if (leftArcs == null) {
      leftArcs = new ArcList();
    }
    leftArcs.add(end);
    end.attachTo(this);
  }

  @Override
  public void addRightArc(ArcEnd end) {
    
    if (rightArcs == null) {
      rightArcs = new ArcList();
    }
    rightArcs.add(end);
    end.attachTo(this);
  }

  @Override
  public void addTopArc(ArcEnd end) {
    if (topArcs == null) {
      topArcs = new ArcList();
    }
    topArcs.add(end);
    end.attachTo(this);
  }

  @Override
  public void addBottomArc(ArcEnd end) {
    if (bottomArcs == null) {
      bottomArcs = new ArcList();
    }
    bottomArcs.add(end);
    end.attachTo(this);
  }

  /**
   * Layout the body contained within this ConnectableWidget, and also place
   * the ArcEnd entities along the edges.
   * If the default size of this widget is not big enough to accommodate the
   * connectors, then the widget will be enlarged.
   */
  @Override
  public void layout() {

    body.layout();
    
    int bodyWidth = body.getBounds().getWidth();
    int bodyHeight = body.getBounds().getHeight();
    
    setWidth(bodyWidth);
    setHeight(bodyHeight);
   
    int height = computeArcHeight(rightArcs, bodyWidth);
    height = Math.max(height, computeArcHeight(leftArcs, 0));
    
    int width = computeArcWidth(topArcs, 0);
    width = Math.max(width, computeArcWidth(bottomArcs, bodyHeight));
    
    
    if (height > bodyHeight) {
      setHeight(height);
    }
    
    attachRight();
    attachLeft();
    attachTop();
    attachBottom();
    
  }
  


  private void attachTop() {
    if (topArcs==null || topArcs.isEmpty()) return;
    attachHorizontal(topArcs, 0);
    
  }

  private void attachBottom() {
    if (bottomArcs==null || bottomArcs.isEmpty()) return;
    attachHorizontal(bottomArcs, getHeight());
    
  }

  private void attachLeft() {

    if (leftArcs == null || leftArcs.isEmpty()) return;
    
    attachVertical(leftArcs, 0);
//
//    ArcList arcList = leftArcs;
//    
//    int x = 0;
//    
//    int middle = getHeight()/2;
//    int halfSpan = arcList.getHalfSpan();
//    
//    int maxEndSpan = arcList.getMaxEndSpan();
//    
//    int y = middle - halfSpan;
//    
//    int dx = 0;
//    int marginBottom = 0;
//    int delta = 0;
//    
//    // The "extraSpace" variable denotes the amount by which
//    // we must expand the width of all endpoints to ensure that there is adequate space for labels.
//    int extraSpace = 0;
//    
//    for (ArcEnd end : arcList) {
//      int marginTop = end.getStyle().getMarginTop();
//      int margin = Math.max(marginBottom, marginTop);
//      int ascent = end.getAscent();
//      if (delta != 0) {
//        y += margin+ascent;
//      }
//     
//      
//      end.attachAt(x, y);
//      y += end.getDescent();
//      
//      
//      // Compute the width of the end.  
//      // This will determine where the arc bends.
//      
//      if (y >= middle && delta>0) {
//        delta = 0;
//      }
//      dx += delta;
//      
//      int endWidth = maxEndSpan - Math.max(dx, 0);
//      int oldBounds = end.getBounds().getWidth();
//      
//      if (endWidth < oldBounds) {
//        extraSpace = Math.max(extraSpace, oldBounds - endWidth);
//      }
//      
//      end.getBounds().setWidth(endWidth);
//      
//      if (y <= middle) {
//        delta = routeSpacing;
//        
//      } else {
//        delta = -routeSpacing;
//      }
//    }
//    
//    if (extraSpace > 0) {
//      applyExtraSpace(extraSpace, arcList);
//    }
    
  }

//  private void applyExtraSpace(int extraSpace, ArcList arcList) {
//
//    for (ArcEnd end : arcList) {
//      int width = end.getBounds().getWidth() + extraSpace;
//      end.getBounds().setWidth(width);
//    }
//    
//  }

  private void attachRight() {
    if (rightArcs == null || rightArcs.isEmpty()) return;
    
    attachVertical(rightArcs, getWidth());
  }
  
  public void setEndWidths(List<ArcEnd> arcList) {
    setEndWidthsFromTop(arcList);
    setEndWidthsFromBottom(arcList);
  }
  
  private void setEndWidthsFromBottom(List<ArcEnd> arcList) {

    if (arcList == null) return;
    
    int index = arcList.size()-1;
    
    // Work top-down until we find an end point that is higher than it's peer.
    
    int w = 0;

    for (; index >= 0; index--) {

      ArcEnd e0 = arcList.get(index);
      ArcEnd e1 = e0.getArc().getOtherEnd(e0);

      Transformer t0 = new Transformer(e0.getAttachedWidget());
      Transformer t1 = new Transformer(e1.getAttachedWidget());

      int ex0 = e0.getArcX();
      int ey0 = e0.getArcY();

      int ex1 = e1.getArcX();
      int ey1 = e1.getArcY();

      int x0 = t0.x(ex0, ey0);
      int y0 = t0.y(ex0, ey0);
      
      int x1 = t1.x(ex1, ey1);
      int y1 = t1.y(ex1, ey1);

      if (x1 < x0) {
        int y = y1;
        y1 = y0;
        y0 = y;
        e0 = e1;
      }

      if (y1 < y0) {
       break;
      }

      w = Math.max(w, e0.getBounds().getWidth());
      if (index != arcList.size()-1) {
        w += routeSpacing;
      }
      e0.getBounds().setWidth(w);
    }
  }
  

  private void setEndWidthsFromTop(List<ArcEnd> arcList) {
    if (arcList == null) return;
    
    int index = 0;
    
    // Work top-down until we find an end point that is higher than it's peer.
    
    int w = 0;

    for (; index < arcList.size(); index++) {

      ArcEnd e0 = arcList.get(index);
      ArcEnd e1 = e0.getArc().getOtherEnd(e0);

      Transformer t0 = new Transformer(e0.getAttachedWidget());
      Transformer t1 = new Transformer(e1.getAttachedWidget());

      int ex0 = e0.getArcX();
      int ey0 = e0.getArcY();

      int ex1 = e1.getArcX();
      int ey1 = e1.getArcY();

      int x0 = t0.x(ex0, ey0);
      int y0 = t0.y(ex0, ey0);
      
      int x1 = t1.x(ex1, ey1);
      int y1 = t1.y(ex1, ey1);
      
      if (x1 < x0) {
        int y = y1;
        y1 = y0;
        y0 = y;
        e0 = e1;
      }

      
      if (y1 >= y0)
        break;

      
      
      w = Math.max(w, e0.getBounds().getWidth());
      if (index > 0) {
        w += routeSpacing;
      }
      e0.getBounds().setWidth(w);
    }
    
    
  }


  
  
  private void attachVertical(ArcList arcList, int x) {
    
    int middle = getHeight()/2;
    int halfSpan = arcList.getHalfSpan();
    
//    int maxEndSpan = arcList.getMaxEndSpan();
    
    int y = middle - halfSpan;
    
//    int dx = 0;
    int marginBottom = 0;
    int delta = 0;
    for (ArcEnd end : arcList) {
      int marginTop = end.getStyle().getMarginTop();
      int margin = Math.max(marginBottom, marginTop);
      int ascent = end.getAscent();
      if (delta != 0) {
        y += margin+ascent;
      }
     
      
      end.attachAt(x, y);
      y += end.getDescent();
      
      
      // Compute the width of the end.  
      // This will determine where the arc bends.
      
      if (y >= middle && delta>0) {
        delta = 0;
      }
//      dx += delta;
      
//      int endWidth = maxEndSpan + Math.max(dx, 0);
//      end.getBounds().setWidth(endWidth);
      
      if (y <= middle) {
        delta = routeSpacing;
        
      } else {
        delta -= routeSpacing;
      }
    }
    
  }


  private void attachHorizontal(ArcList arcList, int yEdge) {

    int center = getWidth()/2;
    int halfSpan = arcList.getHalfSpan();
    
    
    int x = center - halfSpan;
    
    int dy = 0;
    int marginRight = 0;
    int delta = 0;
    for (ArcEnd end : arcList) {
      int marginLeft = end.getStyle().getMarginLeft();
      int margin = Math.max(marginRight, marginLeft);
      
      int dx = end.getX() - end.getBounds().getLeft();
      
      if (delta != 0) {
        x += margin+dx;
      }
      
      end.attachAt(x, yEdge);
      end.layout();
      
      // Adjust the height of the end, which determines
      // where the arc bends.
      
      if (x >= center && delta>0) {
        delta = 0;
      }
      dy += delta;
      
      int endHeight = end.getBounds().getHeight() + dy;
      end.getBounds().setHeight(endHeight);
      
      if (x <= center) {
        delta = routeSpacing;
        
      } else {
        delta -= routeSpacing;
      }
    }
   
    
  }

  private int computeArcWidth(ArcList arcList, int yEdge) {
    if (arcList == null || arcList.isEmpty()) return 0;
    int marginRight = 0;
    
    int center = getWidth()/2;
    
    
    int x = 0;  // The marker for our current position along the edge.
    int x1 = 0; // The x-coordinate where the first arc attaches.
    int x2 = 0; // The x-coordinate where the last arc attaches
    
    for (ArcEnd end : arcList) {
      int marginLeft = end.getStyle().getMarginLeft();
      int margin = Math.max(marginLeft, marginRight);
      
      // Temporarily attach to the center top of this widget so we can
      // compute the bounds of the ArcEnd.
      //
      end.attachAt(center, yEdge);
      end.layout();
      int endLeft = end.getBounds().getLeft(); 
      int endRight = end.getBounds().getRight();
      int endX = end.getX(); // The point where the arc attaches.
      
      // Compute the distance from the left edge of the ArcEnd's bounding box
      // to the point where the arc attaches.
      int dx = endX - endLeft;
      
      // This is actually where we want to actually attach the edge.
      x2 = x + margin + dx;
      if (x1 == 0) {
        x1 = x2;
      }
      
      x = x2 + endRight - endX;
      marginRight = end.getStyle().getMarginRight();
    }
    int width = x2 - x1;
    arcList.setSpan(width);
    setHorizontalHalfSpan(arcList);
    
    width += 2*routeSpacing;
    return width;
  }
  
  
  private void setHorizontalHalfSpan(ArcList arcList) {

    int size = arcList.size();
    int span = arcList.getSpan();
    int halfSpan = span/2;
    boolean even = size%2 == 0;

    int x0 = arcList.get(0).getX();
    int middle = x0 + halfSpan;
    
    for (int i=0; i<size; i++) {
      ArcEnd end = arcList.get(i);
      int x = end.getX();
      if (x == middle) {
        halfSpan = x - x0;
        break;
      }
      if (x > middle) {
        if (even) {
          halfSpan = (x + arcList.get(i-1).getX())/2 - x0;
          
        } else {
          halfSpan = x - x0;
        }
        
        break;
      }
    }
    
    
    arcList.setHalfSpan(halfSpan);
    
  }

  private int computeArcHeight(ArcList arcList, int x) {
    if (arcList == null || arcList.isEmpty()) return 0;
    int marginBottom = 0;
    
    int y = 0;
    int y1 = 0;
    int y2 = 0;
    
    int tempY = getTop() + 1;
    int maxEndSpan = 0;
    
    for (ArcEnd end : arcList) {
      int marginTop = end.getStyle().getMarginTop();
      int margin = Math.max(marginBottom, marginTop);
      
      // y2 is the position where the arc end terminates
      
      y2 = y + margin + end.getAscent();
      if (y1 == 0) {
        y1 = y2;
      }
      // Make a temporary attachment so that we can compute the width
      end.attachAt(x, tempY);
      maxEndSpan = Math.max(maxEndSpan, end.getBounds().getWidth());
      end.setY(y2);
      
      y = y2 + end.getDescent();
      
      marginBottom = end.getStyle().getMarginBottom();
      
    }

    int height = y2 - y1;
    arcList.setMaxEndSpan(maxEndSpan);
    arcList.setSpan(height);
    setVerticalHalfSpan(arcList);
    
    height += 2*routeSpacing;
    
    return height;
  }

  private void setVerticalHalfSpan(ArcList arcList) {
    
    int size = arcList.size();
    int span = arcList.getSpan();
    int halfSpan = span/2;
    boolean even = size%2 == 0;
    

    int y0 = arcList.get(0).getY();
    int middle = y0 + halfSpan;
    int titleCount = 0;
    for (int i=0; i<size; i++) {
      ArcEnd end = arcList.get(i);
      if (end.getTitle() != null) {
        titleCount++;
      }
      int y = end.getY();
      if (y == middle) {
        halfSpan = y - y0;
        break;
      }
      if (y > middle) {
        if (even) {
          halfSpan = (y + arcList.get(i-1).getY())/2 - y0;
          
        } else {
          halfSpan = y - y0;
        }
        
        break;
      }
    }
    if (titleCount == 0 || titleCount==size) {
      arcList.setHalfSpan(halfSpan);
    } else {
      arcList.setHalfSpan(span/2);
    }
    
  }

  @Override
  public void setPosition(int left, int top) {
    setLeft(left);
    setTop(top);
  }
  


  /**
   * A convenience method for painting a list of arcs attached to this widget
   * (or any other widget).
   * @param list
   */
  public void paintArcs(Graphics2D g, List<ArcEnd> list) {
    if (list == null) return;
    for (ArcEnd end : list) {
      end.getArc().paint(g);
    }
  }

  @Override
  public void paint(Graphics2D g) {
   
    int width = getWidth();
    int height = getHeight();
    
    Color bgColor = style.getBgColor();
    if (bgColor != null) {
      g.setColor(bgColor);
      g.fillRect(0, 0, width, height);
    }
    GraphicsUtil.paint(g, body);
    
    Color borderColor = style.getBorderColor();
    if (borderColor != null) {
      g.setColor(borderColor);
      g.drawRect(0, 0, width, height);
    }
    
  }

  @Override
  public Rect getBounds() {
    return this;
  }

  @Override
  public Style getStyle() {
    return style;
  }
  
  public void setStyle(Style style) {
    this.style = style;
  }

  
  @SuppressWarnings("serial")
  static class ArcList extends ArrayList<ArcEnd> {
    private int span;
    private int halfSpan;
    private int maxEndSpan;

    public int getSpan() {
      return span;
    }

    public void setSpan(int span) {
      this.span = span;
    }

    public void setHalfSpan(int s) {
      halfSpan = s;
    }

    public int getHalfSpan() {
      return halfSpan;
    }

    public int getMaxEndSpan() {
      return maxEndSpan;
    }

    public void setMaxEndSpan(int maxEndSpan) {
      this.maxEndSpan = maxEndSpan;
    }


    
   
    
    
    
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
