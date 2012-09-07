package org.semantictools.graphics;

public class Transformer {
  
  private int dx;
  private int dy;
  
  public Transformer(int dx, int dy) {
    this.dx = dx;
    this.dy = dy;
  }
  
  public Transformer(Widget w) {
    set(w);
  }
  
  /**
   * Returns the transformed x-coordinate for the point (x0, y0)
   */
  public int x(int x0, int y0) {
    return x0 + dx;
  }
  
  /**
   * Returns the transformed y-coordinate for the point (x0, y0)
   */
  public int y(int x0, int y0) {
    return y0 + dy;
  }

  /**
   * Returns the amount of translation along the x-axis
   */
  public int getDx() {
    return dx;
  }

  /**
   * Sets the amount of translation along the x-axis
   */
  public void setDx(int dx) {
    this.dx = dx;
  }

  /**
   * Returns the amount of translation along the y-axis
   */
  public int getDy() {
    return dy;
  }

  /**
   * Sets the amount of translation along the y-axis
   */
  public void setDy(int dy) {
    this.dy = dy;
  }
  
  /**
   * Increases the translation by dx along the x-axis and dy along the y-axis.
   */
  public void translate(int dx, int dy) {
    this.dx += dx;
    this.dy += dy;
  }
  
  public void push(Widget widget) {
    int dx = widget.getBounds().getLeft();
    int dy = widget.getBounds().getTop();
    translate(dx, dy);
  }
  
  public void pop(Widget widget) {

    int dx = -widget.getBounds().getLeft();
    int dy = -widget.getBounds().getTop();
    translate(dx, dy);
  }
  
  public void setTranslation(int dx, int dy) {
    this.dx = dx;
    this.dy = dy;
  }
  
  public void set(Widget widget) {
    while (widget != null) {
      push(widget);
      widget = widget.getParent();
    }
  }
  
  
  public String toString() {
    return "Transformer(dx=" + dx + ", dy=" + dy + ")";
  }
}
