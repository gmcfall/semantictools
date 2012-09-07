package org.semantictools.context.renderer;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import org.semantictools.context.renderer.model.DiagramSpec;
import org.semantictools.context.renderer.model.Modifier;
import org.semantictools.context.renderer.model.Node;
import org.semantictools.context.renderer.model.Rect;
import org.semantictools.graphics.Padding;

public class ContextRenderer {
  
  private Style style;
  private FontMetrics nameMetrics;
  private FontMetrics typeMetrics;
  private StreamFactory streamFactory;

  public ContextRenderer(StreamFactory factory) {
    this.streamFactory = factory;
    style = new Style(true);
    
  }
  
  
  public void renderGraphicalNotationFigure(DiagramSpec spec) throws IOException {
    computeLayout(spec);
    NotationPainter painter = new NotationPainter(spec);
    painter.paintImage();
  }

  public void render(DiagramSpec spec) throws IOException {
    
    computeLayout(spec);
    paint(spec);

    
  }

  private void paint(DiagramSpec spec) throws IOException {
    
    paintRootNode(spec);
    
  }

  private void paintRootNode(DiagramSpec spec) throws IOException {
    Painter painter = new Painter(spec);
    painter.paintImage();
    
  }


  private void computeLayout(DiagramSpec spec) {
   
    LayoutEngine engine = new LayoutEngine(spec);
    engine.computeLayout();
    
    
  }

  
  class NotationPainter extends Painter {

    private static final int ARROW_LENGTH = 50;
    private static final int ARROW_HEAD_LENGTH = 4;
    private static final int ARROW_SPACING = 5;
    private static final String PROPERTY_NAME = "property name";
    private static final String PROPERTY_TYPE = "property type";
    
    private Rectangle2D propertyNameBounds;
    private Rectangle2D propertyTypeBounds;
    private int ascent;
    
    NotationPainter(DiagramSpec spec) {
      super();
      init(spec);
    }
    
    protected void computeImageDimensions() {
      
      super.computeImageDimensions();
      
      BufferedImage image = new BufferedImage(10, 10, BufferedImage.TYPE_INT_ARGB);
      Graphics2D g = image.createGraphics();
      
      FontMetrics metrics = g.getFontMetrics( style.getLabelFont() );
      ascent = metrics.getAscent()/2 - metrics.getDescent();
      
      propertyNameBounds = metrics.getStringBounds(PROPERTY_NAME, g);
      propertyTypeBounds = metrics.getStringBounds(PROPERTY_TYPE, g);
      
      int textWidth = (int)Math.max(propertyNameBounds.getWidth(), propertyTypeBounds.getWidth());
      imageWidth += ARROW_LENGTH + 2*ARROW_HEAD_LENGTH + 3*ARROW_SPACING + textWidth;
      
    }
    


    public void paintImage() throws IOException {
      paintLabeledArrow(spec.getRoot().getNameRect(), propertyNameBounds, PROPERTY_NAME);
      paintLabeledArrow(spec.getRoot().getTypeRect(), propertyTypeBounds, PROPERTY_TYPE);
      super.paintImage();
    }

    private void paintLabeledArrow(Rect targetRect, Rectangle2D labelBounds, String labelText) {
      
      int x0 = targetRect.getWidth() + ARROW_SPACING;
      int y0 = targetRect.getY() + targetRect.getHeight()/2;
      
      int x1 = x0 + ARROW_HEAD_LENGTH*2;
      int y1 = y0 + ARROW_HEAD_LENGTH;
      
      int x2 = x1;
      int y2 = y0 - ARROW_HEAD_LENGTH;
      
      int x[] = new int[] {x0, x1, x2, x0};
      int y[] = new int[] {y0, y1, y2, y0};
      
      Stroke width1 = new BasicStroke(1);
      Stroke width2 = new BasicStroke(2);
      
      graphics.setColor(Color.red);
      graphics.setPaint(Color.red);
      graphics.setStroke(width1);
      graphics.fillPolygon(x, y, 3);
      graphics.drawPolygon(x, y, 3);
      
      Stroke stroke = graphics.getStroke();
      graphics.setStroke(width2);
      
      x0 = x1;
      x1 = x0 + ARROW_LENGTH;
      y1 = y0;
      
      graphics.drawLine(x0, y0, x1, y1);
      
      graphics.setStroke(stroke);
      
      graphics.setColor(Color.black);
      graphics.setFont(style.getLabelFont());
      
      x0 = x1 + 2*ARROW_SPACING;
      
      y0 = y0 + ascent;
      
      graphics.drawString(labelText, x0, y0);
      
    }
  }
  
  private class Painter {
    BufferedImage image;
    Graphics2D graphics;
    DiagramSpec spec;
    
    protected int imageWidth;
    protected int imageHeight;
    
    
    protected Painter() {
      
    }
    
    Painter(DiagramSpec spec) {
      init(spec);
    }
    
    protected void init(DiagramSpec spec) {

      this.spec = spec;
      computeImageDimensions();
      
      image = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_ARGB);
      graphics = image.createGraphics();
      graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
      graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    }
    
    

    protected void computeImageDimensions() {
      updateDimensions(spec.getRoot());
      
    }



    private void updateDimensions(Node node) {
      imageWidth = Math.max(imageWidth, node.getRight()+1);
      imageHeight = Math.max(imageHeight, node.getBottom()+1);
      if (node.getChildren() != null) {
        for (Node n : node.getChildren()) {
          updateDimensions(n);
        }
      }
    }



    public void paintImage() throws IOException {
      paintNode(spec.getRoot());
      writeFile();
      
    }

    private void writeFile() throws IOException {

      OutputStream stream = streamFactory.createOutputStream(spec.getImagePath());
      try {
        ImageIO.write(image, "png", stream);
      } finally {
        stream.close();
      }
      
    }


    private void paintNode(Node node) {
     
      graphics.setColor( style.getBoxBorderColor() );
      drawText( 
        node.getTypeText(), 
        node.getTypeRect(), 
        style.getTypePadding(),
        style.getTypeFont(), 
        style.getTypeTextColor(), 
        style.getTypeBgColor());
      
      drawText(
          node.getNameText(),
          node.getNameRect(),
          style.getNamePadding(),
          style.getNameFont(),
          style.getNameTextColor(),
          style.getNameBgColor());
      
      String q = node.getNameQualifier();
      if (q!=null) {
        drawText(
            q,
            node.getNameRect(),
            style.getNamePadding(),
            style.getNameFont(),
            style.getArcColor(),
            null
        );
      }

      drawRect(node.getOutline(), style.getBoxBorderColor());
      drawModifier(node);
      
      if (node.getChildren() != null) {
        for (Node n : node.getChildren()) {
          paintNode(n);
        }
        paintArc(node);
      }
      
     
      
      
    }

    private void drawModifier(Node node) {
      
      if (node.getModifier() == Modifier.NONE) return;
      
      int diameter = style.getModifierDiameter();
      
      int middle = (node.getTop() + node.getBottom()) / 2;
      
      int x = node.getLeft();
      int y = middle - diameter/2;
      
      Ellipse2D.Float circle = new Ellipse2D.Float(x, y, diameter, diameter);
      
      graphics.setPaint(style.getTypeBgColor());
      graphics.fill(circle);
      graphics.setPaint(style.getBoxBorderColor());
      graphics.draw(circle);
      
      String symbol = node.getModifier().getSymbol();
      Rectangle2D bounds = nameMetrics.getStringBounds(symbol, graphics);
      int height = (int) bounds.getHeight();
      int width = (int) bounds.getWidth();
      
      // Compute the coordinates of the symbol so that it is centered in the circle
      x = x + (diameter - width)/2 + nameMetrics.getLeading();
      y = y + (diameter - height)/2 + nameMetrics.getAscent() + nameMetrics.getLeading();
      
      graphics.setFont(style.getNameFont());
      graphics.setColor(style.getArcColor());
      graphics.drawString(symbol, x, y);
      
    }



    private void paintArc(Node node) {
     switch (node.getBranchStyle()) {
     case RECTILINEAR : paintRectilinearArc(node); break;
     case OBLIQUE: paintObliqueArc(node); break;
     }
      
    }
    
    private void paintObliqueArc(Node node) {

      int top = node.getTop();
      int bottom = node.getBottom();
      
      int x0 = node.getRight();
      int y0 = (top+bottom)/2;
      
      Node firstChild = node.getFirstChild();
      
      int childLeft = firstChild.getLeft();
      
      int x2 = childLeft;
      int y2 = firstChild.getTop() + firstChild.getHeight()/2;
      
      paintArc(x0, y0, x2, y2);

      Node lastChild = node.getLastChild();
      
      if (lastChild == firstChild) return;
      
      y2 = lastChild.getTop() + lastChild.getHeight()/2;
      
      paintArc(x0, y0, x2, y2);
      
    }

    private void paintRectilinearArc(Node node) {
      int top = node.getTop();
      int bottom = node.getBottom();
      
      int x0 = node.getRight();
      int y0 = (top+bottom)/2;
      
      Node firstChild = node.getFirstChild();
      
      int childLeft = firstChild.getLeft();
      
      int hspace = style.getHorizontalSpacing()/2;
      
      int x1 = childLeft - hspace;
      int y1 = y0;
      
      paintArc(x0, y0, x1, y1);
      
      int x2 = childLeft;
      int y2 = firstChild.getTop() + firstChild.getHeight()/2;
      
      paintArc(x1, y1, x1, y2);
      paintArc(x1, y2, x2, y2);

      Node lastChild = node.getLastChild();
      
      if (lastChild == firstChild) return;
      
      y2 = lastChild.getTop() + lastChild.getHeight()/2;
      
      paintArc(x1, y1, x1, y2);
      paintArc(x1, y2, x2, y2);
      
    }
    




    private void paintArc(int x0, int y0, int x1, int y1) {
      graphics.setPaint(style.getArcColor());
      graphics.drawLine(x0, y0, x1, y1);
      
    }

    private void fill(Rect box, Color bgColor) {
      Rectangle2D.Float shape = new Rectangle2D.Float();
      shape.setRect(box.getX(), box.getY(), box.getWidth(), box.getHeight());
     
      graphics.setPaint(bgColor);
      graphics.fill(shape);
      
    }

    private void drawText(String text, Rect box, Padding padding, Font font, Color color, Color bgColor) {
     
      int x = box.getX() + padding.getPadLeft();
      int y = box.getY() + box.getHeight() - padding.getPadBottom() - padding.getMaxDescent();


      if (bgColor != null) {
        fill(box, bgColor);
        graphics.setBackground(bgColor);
      }
      
      graphics.setFont(font);
      graphics.setColor(color);
      graphics.drawString(text, x, y);
      
      
    }

    public void drawRect(Rect box, Color color) {
      int x = box.getX();
      int y = box.getY();
      int width = box.getWidth();
      int height = box.getHeight();
      graphics.setColor(color);
      graphics.drawRect(x, y, width, height);
      
    }
    
  }



  private class LayoutEngine {
    private DiagramSpec spec;
    private int nameHeight;
    private int typeHeight;
    private Grid grid = new Grid();
    
    
    
    LayoutEngine(DiagramSpec spec) {
      this.spec = spec;
      BufferedImage image = new BufferedImage(10, 10, BufferedImage.TYPE_INT_ARGB);
      Graphics2D g = image.createGraphics();
      
      nameMetrics = g.getFontMetrics( style.getNameFont() );
      typeMetrics = g.getFontMetrics( style.getTypeFont() );
      
      Padding namePad = style.getNamePadding();
      Padding typePad = style.getTypePadding();
      
      nameHeight = (int)(nameMetrics.getHeight() + namePad.getPadTop() + namePad.getPadBottom());
      typeHeight = (int)(typeMetrics.getHeight() + typePad.getPadTop() + typePad.getPadBottom());
      
      style.setModifierDiameter(typeHeight);
      
      namePad.setMaxDescent(nameMetrics.getMaxDescent());
      typePad.setMaxDescent(typeMetrics.getMaxDescent());
      
    }


    
    public void computeLayout() {
      Node root = spec.getRoot();
      grid.inject(root);
      computeDimensions(root);
      root.setLeft(0, style.getModifierDiameter());
      
      grid.computeHorizontalSpacing(style.getHorizontalSpacing());
      grid.computeVerticalSpacing(style.getVerticalSpacing());
      
      grid.refinePlacement(style.getVerticalSpacing());
      
      
    }
    



    public void computeDimensions(Node node) {
      String localName = node.getNameText();
      String typeName = node.getTypeText();
      
      setDimensions(node.getNameRect(), localName, nameHeight, nameMetrics, style.getNamePadding());
      setDimensions(node.getTypeRect(), typeName, typeHeight, typeMetrics, style.getTypePadding());
      node.alignWidth(style.getModifierDiameter());
      node.computeHeight();
      
      List<Node> kids = node.getChildren();
      if (kids != null) {
        for (Node n : kids) {
          computeDimensions(n);
        }
      }
      
    }
    
    
    private void setDimensions(Rect rect, String text, int height, FontMetrics metrics, Padding padding) {
      rect.setHeight(height);
      rect.setWidth((int)(metrics.stringWidth(text) + padding.getPadLeft() + padding.getPadRight()));
    }

    
    
  }


  class Column {
    List<Node> nodeList = new ArrayList<Node>();
    Rect boundary = new Rect();
    
    public List<Node> getNodeList() {
      return nodeList;
    }
    public Rect getBoundary() {
      return boundary;
    }
    
    public void add(Node node) {
      nodeList.add(node);
    }
    public void setLeft(int left) {
      boundary.setX(left);
      for (Node n : nodeList) {
        n.setLeft(left, style.getModifierDiameter());
      }
      
    }
    
  }
  
  class Grid {
    List<Column> columnList = new ArrayList<Column>();
    
    
    public Node getNode(int column, int index) {
      return columnList.get(column).getNodeList().get(index);
    }
    public void refinePlacement(int spacing) {
      Node root = getNode(0, 0);
      refinePlacement(root, spacing);
      
    }
    
    public Column getColumn(int col) {
      return columnList.get(col);
    }
    
    private void refinePlacement(Node node, int spacing) {
      int dy = getDeltaY(node, spacing);
      
      if (dy > 0) {
        shiftColumn(node, dy, spacing);
      }
      
      List<Node> kids = node.getChildren();
      if (kids != null) {
        for (Node n : kids){
          refinePlacement(n, spacing);
        }
      }
      
    }
    
    private int getDeltaY(Node node, int spacing) {
      int row = node.getRow();
      int column = node.getColumn();
      
      int top = node.getBoundary().getY();
      
      int prevTop = 0;
      if (row > 0) {
        Node prev = getNode(column, row-1);
        prevTop = prev.getBottom() + spacing;
      }
      
      int dy = prevTop - top;
     
      return dy;
    }
    
    private void shiftColumn(Node node, int dy, int spacing) {

      int row = node.getRow();
      int column = node.getColumn();
      
      List<Node> list = getColumn(column).getNodeList();
      for (int i=row; i<list.size(); i++) {
        Node n = list.get(i);
        shiftNode(n, dy);
      }

      Node parent = node.getParent();
      shiftParent(parent, spacing);
    
      
    }
    
    private void shiftParent(Node parent, int spacing) {
      if (parent == null) return;

      int row = parent.getRow();
      int column = parent.getColumn();

      List<Node> list = getColumn(column).getNodeList();
      for (int i=row; i<list.size(); i++) {
        Node n = list.get(i);
        placeParent(n, spacing);
      }
      
      shiftParent(parent.getParent(), spacing);
      
    }
    
    private void placeParent(Node n, int spacing) {
//      System.out.println("placeParent " + n.getTypeText());
      
      List<Node> kids = n.getChildren();
      if (kids == null) {
        int dy = getDeltaY(n, spacing);
        if (dy > 0) {
          int top = n.getTop() + dy;
          n.setTop(top);
        }
        return;
      }
      
      Node firstChild = kids.get(0);
      Node lastChild = kids.get(kids.size()-1);
      
      int kidsTop = firstChild.getTop();
      int kidsBottom = lastChild.getBottom();
      
      int middle = (kidsTop + kidsBottom)/2;
      
      int dy = n.getHeight()/2;
      
      int top = middle - dy;
      n.setTop(top);
    }
    /**
     * Shift the given node, and all of its children recursively by the given vertical increment.
     */
    private void shiftNode(Node node, int dy) {
     
      int top = node.getBoundary().getY() + dy;
      node.setTop(top);
      
      if (node.getChildren() != null) {
        for (Node c : node.getChildren()) {
          shiftNode(c, dy);
        }
      }
      
    }
    public void add(Node node, int column) {
      while (columnList.size() < column+1) {
        columnList.add(new Column());
      }
      Column c = columnList.get(column);
      int row = c.getNodeList().size();
      c.add(node);
      node.setGridCoordinates(row, column);
    }

    public void computeVerticalSpacing(int verticalSpacing) {
      computeDefaultVerticalSpacing(verticalSpacing);
      
    }

    private void computeDefaultVerticalSpacing(int spacing) {
      doBaselineVerticalSpacing(spacing);
      doRootVerticalSpacing(spacing);
      
      
    }

    /**
     * Aligns the root node vertically so that it is centered in the middle of its child nodes.
     */
    private void doRootVerticalSpacing(int spacing) {
      if (columnList.size()<2) return;
      Node root = getNode(0, 0);
      int height = columnList.get(1).getBoundary().getHeight();
      
      int middle = height/2;
      int dy = root.getBoundary().getHeight()/2;
      
      int top = middle - dy;
      
      root.setTop(top);
    }


    private void doBaselineVerticalSpacing(int spacing) {
      if (columnList.size()==1) {
        columnList.get(0).getNodeList().get(0).setTop(0);
        return;
      }
      Column col = columnList.get(1);
      int mark = -spacing;
      for (Node n : col.getNodeList()) {
        int top = mark + spacing;
        n.setTop(top);
        Rect boundary = n.getBoundary();
        mark = top + boundary.getHeight();
        
        doChildrenBaseline(n, spacing);
      }
      col.getBoundary().setHeight(mark);
      
    }

    /**
     * Aligns child nodes so that they are evenly spaced and centered on the middle of the the parent.
     */
    private void doChildrenBaseline(Node parent, int spacing) {

      List<Node> kids = parent.getChildren();
      if (kids == null) return;
          
      Rect parentBoundary = parent.getBoundary();
      int boxHeight = parentBoundary.getHeight();
      
      int kidCount = kids.size();
      int totalKidHeight = kidCount*boxHeight + (kidCount-1) * spacing;
      
      int parentMiddle = parentBoundary.getY() + boxHeight/2;
      
      int top = parentMiddle - totalKidHeight/2;
      
      for (Node child : kids) {
        child.setTop(top);
        doChildrenBaseline(child, spacing);
        top += spacing + boxHeight;
      }
      
      
      
    }
    public void computeHorizontalSpacing(int horizontalSpacing) {
      computeColumnWidths();
      setColumnLeftEdges(horizontalSpacing);
      
      
    }

    private void setColumnLeftEdges(int spacing) {
      
      for (int i=1; i<columnList.size(); i++) {
        Column col = columnList.get(i);
        Column prev = columnList.get(i-1);
        Rect prevBoundary = prev.getBoundary();
        int left = prevBoundary.getX() + prevBoundary.getWidth() + spacing;
        
        col.setLeft(left);
        
      }
      
    }

    private void computeColumnWidths() {
      for (Column col : columnList) {
        computeWidth(col);
      }
      
    }

    private void computeWidth(Column col) {
      List<Node> nodeList = col.getNodeList();
      int maxWidth = 0;
      
      for (Node n : nodeList) {
        Rect boundary = n.getBoundary();
        if (boundary.getWidth() > maxWidth) {
          maxWidth = boundary.getWidth();
        }
      }
      
      col.getBoundary().setWidth(maxWidth);
      
    }

    void inject(Node root) {
      inject(0, root);
    }

    private void inject(int column, Node node) {
      add(node, column);
      List<Node> kids = node.getChildren();
      if (kids != null) {
        column++;
        for (Node n : kids) {
          inject(column, n);
        }
      }
    }
  }
  

  
}
