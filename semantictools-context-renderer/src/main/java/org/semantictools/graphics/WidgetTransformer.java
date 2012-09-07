package org.semantictools.graphics;

public class WidgetTransformer extends Transformer {
  
  Widget widget;
  
  public WidgetTransformer(Widget w) {
    super(w);
  }
  
  public void set(Widget w) {
    super.set(w);
    widget = w;
  }
  
  public int getLeft() {
    return widget.getBounds().getLeft() + getDx();
  }
  
  public int getRight() {
    return widget.getBounds().getRight() + getDx();
  }
  
  public int getTop() {
    return widget.getBounds().getTop() + getDy();
  }
  
  public int getBottom() {
    return widget.getBounds().getBottom() + getDy();
  }
  
  public int getWidth() {
    return widget.getBounds().getWidth();
  }
  
  public int getHeight() {
    return widget.getBounds().getHeight();
  }

}
