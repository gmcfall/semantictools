package org.semantictools.graphics;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;

public class Arc {
  
  private ArcEnd[] ends;
  
  public Arc(ArcEnd a, ArcEnd b) {
    ends = new ArcEnd[]{a, b};
    a.setArc(this);
    b.setArc(this);
  }

  public ArcEnd[] getEnds() {
    return ends;
  }
  
  public ArcEnd getOtherEnd(ArcEnd end) {
    return 
        end == ends[0] ? ends[1] :
        end == ends[1] ? ends[0] :
        null;
  }
  
  public void paint(Graphics2D g) {
    
    
    
    switch (ends[0].getEdge()) {
    case RIGHT : paintFromRightEdge(g); break;
    case TOP: paintFromTopEdge(g); break;
    case LEFT: paintFromLeftEdge(g); break;
    case BOTTOM: paintFromBottomEdge(g); break;
      
    }
    
  }

  private void paintFromBottomEdge(Graphics2D g) {
    switch (ends[1].getEdge()) {
    case TOP: paintFromBottomToTop(g); break;
    }
    
  }

  private void paintFromTopEdge(Graphics2D g) {
    
    switch(ends[1].getEdge()) {
    case BOTTOM: paintFromTopToBottom(g); break;
    }
    
  }

  private void paintFromRightEdge(Graphics2D g) {
    
    switch (ends[1].getEdge()) {
    case LEFT : paintFromRightToLeft(g);
    }
    
  }

  private void paintFromLeftEdge(Graphics2D g) {
    
    switch (ends[1].getEdge()) {
    case RIGHT : paintFromLeftToRight(g);
    }
    
  }
  


  private void paintFromBottomToTop(Graphics2D g) {

    g.setTransform(new AffineTransform());
    Transformer t0 = new Transformer(ends[0].getParent());
    Transformer t1 = new Transformer(ends[1].getParent());
    
    int xe0 = ends[0].getArcX();
    int ye0 = ends[0].getArcY();
    
    
    
    int xe1 = ends[1].getArcX();
    int ye1 = ends[1].getArcY();
    
    int x1 = t0.x(xe0, ye0);
    int y1 = t0.y(xe0, ye0);
    
    int x4 = t1.x(xe1, ye1);
    int y4 = t1.y(xe1, ye1);
    
    int x2 = x1;
    int y2 = (y1+y4)/2;
    
    int x3 = x4;
    int y3 = y2;
    
    
    Style style = ends[0].getStyle();
    
    g.setColor(style.getBorderColor());
    g.drawLine(x1, y1, x2, y2);
    g.drawLine(x2, y2, x3, y3);
    g.drawLine(x3, y3, x4, y4);
    
    paintEnd(g, ends[0]);
    paintEnd(g, ends[1]);
    
  }
  
  private void paintFromTopToBottom(Graphics2D g) {

    
//    Transformer t0 = new Transformer(ends[0].getParent());
//    Transformer t1 = new Transformer(ends[1].getParent());
//    
//    int xe0 = ends[0].getArcX();
//    int ye0 = ends[0].getArcY();
//    
//    
//    
//    int xe1 = ends[1].getArcX();
//    int ye1 = ends[1].getArcY();
//    
//    int x1 = t0.x(xe0, ye0);
//    int y1 = t0.y(xe0, ye0);
//    
//    int x2 = t1.x(xe1, ye1);
//    int y2 = t1.y(xe1, ye1);
//    
//    
//    Style style = ends[0].getStyle();
//    
//    g.setColor(style.getBorderColor());
//    g.drawLine(x1, y1, x2, y2);
//    
//    paintEnd(g, ends[0]);
//    paintEnd(g, ends[1]);
    

    g.setTransform(new AffineTransform());
    Transformer t0 = new Transformer(ends[0].getParent());
    Transformer t1 = new Transformer(ends[1].getParent());
    
    int xe0 = ends[0].getArcX();
    int ye0 = ends[0].getArcY();
    
    
    
    int xe1 = ends[1].getArcX();
    int ye1 = ends[1].getArcY();
    
    int x1 = t0.x(xe0, ye0);
    int y1 = t0.y(xe0, ye0);
    
    int x4 = t1.x(xe1, ye1);
    int y4 = t1.y(xe1, ye1);
    
    int x2 = x1;
    int y2 = (y1+y4)/2;
    
    int x3 = x4;
    int y3 = y2;
    
    
    Style style = ends[0].getStyle();
    
    g.setColor(style.getBorderColor());
    g.drawLine(x1, y1, x2, y2);
    g.drawLine(x2, y2, x3, y3);
    g.drawLine(x3, y3, x4, y4);
    
    paintEnd(g, ends[0]);
    paintEnd(g, ends[1]);
    
    
  }

  private void paintFromLeftToRight(Graphics2D g) {

    AffineTransform save = new AffineTransform(g.getTransform());
    g.setTransform(new AffineTransform());
    
    ArcEnd e0 = ends[0];
    ArcEnd e1 = ends[1];
    
    Transformer t0 = new Transformer(e0.getParent());
    Transformer t1 = new Transformer(e1.getParent());
    
    int xe0 = ends[0].getArcX();
    int ye0 = ends[0].getArcY();
    
    int xe1 = ends[1].getArcX();
    int ye1 = ends[1].getArcY();
    

    int x1 = t0.x(xe0, ye0);
    int y1 = t0.y(xe0, ye0);
    
    int x4 = t1.x(xe1, ye1);
    int y4 = t1.y(xe1, ye1);
    
    if (x4 < x1) {
      int temp = x4;
      x4 = x1;
      x1 = temp;
      
      temp = y4;
      y4 = y1;
      y1 = temp;
      
      ArcEnd e = e1;
      e1 = e0;
      e0 = e;
      
      t0 = t1;
    }
    
    int x2e0 = e0.getX() +  e0.getBounds().getWidth();
    int y2e0 = e0.getY();
    
    int x2 = t0.x(x2e0, y2e0);
    int y2 = t0.y(x2e0, y2e0);
    
    int x3 = x2;
    int y3 = y4;
    
    
    Style style = ends[0].getStyle();
    
    g.setColor(style.getBorderColor());
    g.drawLine(x1, y1, x2, y2);
    g.drawLine(x2, y2, x3, y3);
    g.drawLine(x3, y3, x4, y4);
    
    paintEnd(g, ends[0]);
    paintEnd(g, ends[1]);
    
    g.setTransform(save);
    
  }
  
//  private void paintFromLeftToRight(Graphics2D g) {
//
//    AffineTransform save = new AffineTransform(g.getTransform());
//    g.setTransform(new AffineTransform());
//    
//    Transformer t0 = new Transformer(ends[0].getParent());
//    Transformer t1 = new Transformer(ends[1].getParent());
//    
//    int xe0 = ends[0].getArcX();
//    int ye0 = ends[0].getArcY();
//    
//    int xe1 = ends[1].getArcX();
//    int ye1 = ends[1].getArcY();
//    
//    int tx0 = t0.x(xe0, ye0);
//    int tx1 = t1.x(xe1, ye1);
//
//    int dx = (int)Math.signum(tx1 - tx0);
//    
//    int x2e0 = ends[0].getX() + dx * ends[0].getBounds().getWidth();
//    int y2e0 = ends[0].getY();
//    
//    
//    
//    int x1 = tx0;
//    int y1 = t0.y(xe0, ye0);
//    
//    int x2 = t0.x(x2e0, y2e0);
//    int y2 = t0.y(x2e0, y2e0);
//    
//    
//    
//    int x3 = x2;
//    int y3 = t1.y(xe1, ye1);
//    
//    int x4 = tx1;
//    int y4 = y3;
//    
//    
//    Style style = ends[0].getStyle();
//    
//    g.setColor(style.getBorderColor());
//    g.drawLine(x1, y1, x2, y2);
//    g.drawLine(x2, y2, x3, y3);
//    g.drawLine(x3, y3, x4, y4);
//    
//    paintEnd(g, ends[0]);
//    paintEnd(g, ends[1]);
//    
//    g.setTransform(save);
//    
//  }

  private void paintFromRightToLeft(Graphics2D g) {

    AffineTransform save = new AffineTransform(g.getTransform());
    
    Transformer t0 = new Transformer(ends[0].getParent());
    Transformer t1 = new Transformer(ends[1].getParent());
    
    int xe0 = ends[0].getArcX();
    int ye0 = ends[0].getArcY();
    
    int x2e0 = ends[0].getX() + ends[0].getBounds().getWidth();
    int y2e0 = ends[0].getY();
    
    
    
    int x1 = t0.x(xe0, ye0);
    int y1 = t0.y(xe0, ye0);
    
    int x2 = t0.x(x2e0, y2e0);
    int y2 = t0.y(x2e0, y2e0);
    
    
    int xe1 = ends[1].getArcX();
    int ye1 = ends[1].getArcY();
    
    int x3 = x2;
    int y3 = t1.y(xe1, ye1);
    
    int x4 = t1.x(xe1, ye1);
    int y4 = y3;
    
    
    Style style = ends[0].getStyle();
    
    g.setColor(style.getBorderColor());
    g.drawLine(x1, y1, x2, y2);
    g.drawLine(x2, y2, x3, y3);
    g.drawLine(x3, y3, x4, y4);
    
    paintEnd(g, ends[0]);
    paintEnd(g, ends[1]);
    
    g.setTransform(save);
    
  }
  
  private void paintEnd(Graphics2D g, ArcEnd end) {
    GraphicsUtil.setTransform(g, end.getParent());
    end.paint(g);
  }
  
  
  
  
  

}
