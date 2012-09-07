package org.semantictools.context.renderer.model;

import java.util.ArrayList;
import java.util.List;


public class Node {
  
  private String nameText;
  private String typeText;
  private String nameQualifier;
 
  private Rect outline = new Rect();
  private Rect nameRect = new Rect();
  private Rect typeRect = new Rect();
  private Rect boundary = outline;
  
  
  private Modifier modifier = Modifier.NONE;
  private BranchStyle branchStyle = BranchStyle.RECTILINEAR;
  
  
  private List<Node> children;
  private Node parent;

  private int row;
  private int column;
  
  public Node() {
  }
  



  public Node(String nameText, String typeText) {
    this.nameText = nameText;
    this.typeText = typeText;
  }
  
  public Node(String nameText, String typeText, Modifier modifier) {
    this(nameText, typeText);
    setModifier(modifier);
  }
  public Modifier getModifier() {
    return modifier;
  }
  
  /**
   * Returns the text that indicates the qualifer that appears before the name text.
   * Valid qualifiers include @id, @nm.
   * @return
   */
  public String getNameQualifier() {
    return nameQualifier;
  }

  public void setNameQualifier(String nameQualifier) {
    this.nameQualifier = nameQualifier;
  }

  public boolean isIriRef() {
    return nameText.startsWith("#uri");
  }
  
  public void applyIriRef() {
    nameText = "#uri  " + nameText;
    nameQualifier = "#uri";
  }
  
  public void applyNameRef() {
    nameText = "#sn  " + nameText;
    nameQualifier = "#sn";
  }
  
  public void applyExpandedValue() {
    nameText = "#ev  " + nameText;
    nameQualifier = "#ev";
  }


  public void setModifier(Modifier modifier) {
    this.modifier = modifier;
    if (modifier == Modifier.NONE) {
      boundary = outline;
    } else if (boundary == outline) {
      boundary = new Rect();
    }
  }
  
  public void setGridCoordinates(int row, int column) {
    this.row = row;
    this.column = column;
  }
  
  public int getRow() {
    return row;
  }
  
  public int getColumn() {
    return column;
  }
  
  public void setLeft(int left, int modifierWidth) {
    boundary.setX(left);
    if (modifier != Modifier.NONE) {
      left += modifierWidth;
      outline.setX(left);
    }
    nameRect.setX(left);
    typeRect.setX(left);
  }
  
  public Node getParent() {
    return parent;
  }

  public void setParent(Node parent) {
    this.parent = parent;
  }

  public Rect getBoundary() {
    return boundary;
  }
  



  public BranchStyle getBranchStyle() {
    return branchStyle;
  }




  public void setBranchStyle(BranchStyle branchStyle) {
    this.branchStyle = branchStyle;
  }




  public void alignWidth(int modifierWidth) {
    int width = Math.max(nameRect.getWidth(), typeRect.getWidth());
    outline.setWidth(width);
    nameRect.setWidth(width);
    typeRect.setWidth(width);
    if (boundary != outline) {
      boundary.setWidth(width + modifierWidth);
    }
  }
  
  public void add(Node node) {
    if (children == null) {
      children = new ArrayList<Node>();
    }
    children.add(node);
    node.setParent(this);
  }
  
  
  public List<Node> getChildren() {
    return children;
  }


  public String getNameText() {
    return nameText;
  }

  public void setNameText(String nameText) {
    this.nameText = nameText;
  }

  public String getTypeText() {
    return typeText;
  }

  public void setTypeText(String typeText) {
    this.typeText = typeText;
  }

  public void align() {
    int height = nameRect.getHeight() + typeRect.getHeight();
    int width = Math.max(nameRect.getWidth(), typeRect.getWidth());

    
    nameRect.setWidth(width);
    typeRect.setWidth(width);
    typeRect.setPosition(nameRect.getX(), nameRect.getY() + nameRect.getHeight());
    
    outline.setPosition(nameRect.getX(), nameRect.getY());
    outline.setHeight(height);
    outline.setWidth(width);
  }
  
  public Rect getOutline() {
    return outline;
  }

  public Rect getNameRect() {
    return nameRect;
  }
  
  public Rect getTypeRect() {
    return typeRect;
  }



  public void setTop(int top) {
    boundary.setY(top);
    outline.setY(top);
    nameRect.setY(top);
    
    int typeTop = top + nameRect.getHeight();
    typeRect.setY(typeTop);
    
  }

  public void computeHeight() {
    int height = nameRect.getHeight() + typeRect.getHeight();
    
    outline.setHeight(height);
    if (boundary != outline) {
      boundary.setHeight(height);
    }
    
  }

  public int getRight() {
    return boundary.getX() + boundary.getWidth();
  }
  
  public int getBottom() {
    return boundary.getY() + boundary.getHeight();
  }

  public int getTop() {
    return boundary.getY();
  }

  public int getHeight() {
    return boundary.getHeight();
  }

  public Node getFirstChild() {
    return (children==null || children.isEmpty()) ? null : children.get(0);
  }
  
  public Node getLastChild() {
    return (children == null || children.isEmpty()) ? null : children.get(children.size()-1);
  }

  public int getLeft() {
    return boundary.getX();
  }

  public int getWidth() {
    return boundary.getWidth();
  }
  
  public String toString() {
    return nameText + ":" + typeText;
  }

}
