package org.semantictools.graphics;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;

public class GraphicsUtil {
  
  public static void paint(Graphics2D g, Widget w) {
    if (w == null) return;
    beginWidget(g, w);
    w.paint(g);
    endWidget(g, w);
  }
  public static void beginWidget(Graphics2D g, Widget w) {
    int x = w.getBounds().getLeft();
    int y = w.getBounds().getTop();
    g.translate(x, y);
  }
  
  public static void endWidget(Graphics2D g, Widget w) {
    int x = -w.getBounds().getLeft();
    int y = -w.getBounds().getTop();
    g.translate(x, y);
  }
  
  public static void setTransform(Graphics2D g, Widget w) {
    AffineTransform t = new AffineTransform();
    while (w != null) {
      int x = w.getBounds().getLeft();
      int y = w.getBounds().getTop();
      t.translate(x, y);
      w = w.getParent();
    }
    g.setTransform(t);
  }

}
