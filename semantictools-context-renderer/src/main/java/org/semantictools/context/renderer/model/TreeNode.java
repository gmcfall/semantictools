package org.semantictools.context.renderer.model;

import java.util.ArrayList;
import java.util.List;

public class TreeNode {
  
  public static enum Kind {
    FRAME,
    PROPERTY
  }
 
  private String localName="";
  private String typeName;
  private String typeHref;
  private String typeURI;
  
  private int minCardinality;
  private int maxCardinality;
  private String description="";
  private ObjectPresentation objectPresentation = ObjectPresentation.NONE;
  private Kind kind = Kind.PROPERTY;
  private BranchStyle branchStyle = BranchStyle.RECTILINEAR;
  private TreeNode parent;
  private boolean sequential;
  private List<TreeNode> children;
  
  public TreeNode() {}
  
  public String toString() {
    return localName + ":" + typeName;
  }
  
  public boolean isSequential() {
    return sequential;
  }

  public void setSequential(boolean sequential) {
    this.sequential = sequential;
  }

  public String getLocalName() {
    return localName;
  }
  public void setLocalName(String localName) {
    this.localName = localName;
  }
  public String getTypeName() {
    return typeName;
  }
  public void setTypeName(String typeName) {
    this.typeName = typeName;
  }
  
  public String getTypeURI() {
    return typeURI;
  }

  public void setTypeURI(String typeURI) {
    this.typeURI = typeURI;
  }

  public String getTypeHref() {
    return typeHref;
  }
  public void setTypeHref(String typeHref) {
    this.typeHref = typeHref;
  }
  public int getMinCardinality() {
    return minCardinality;
  }
  public void setMinCardinality(int minCardinality) {
    this.minCardinality = minCardinality;
  }
  public int getMaxCardinality() {
    return maxCardinality;
  }
  public void setMaxCardinality(int maxCardinality) {
    this.maxCardinality = maxCardinality;
  }
  public String getDescription() {
    return description;
  }
  public void setDescription(String description) {
    this.description = description;
  }
  public ObjectPresentation getObjectPresentation() {
    return objectPresentation;
  }
  public void setObjectPresentation(ObjectPresentation objectPresentation) {
    this.objectPresentation = objectPresentation;
  }
  public Kind getKind() {
    return kind;
  }
  public void setKind(Kind kind) {
    this.kind = kind;
  }
  public TreeNode getParent() {
    return parent;
  }
  public void add(TreeNode child) {
    if (children == null) {
      children = new ArrayList<TreeNode>();
    }
    children.add(child);
    child.parent = this;
  }
  public List<TreeNode> getChildren() {
    return children;
  }


  public BranchStyle getBranchStyle() {
    return branchStyle;
  }


  public void setBranchStyle(BranchStyle branchStyle) {
    this.branchStyle = branchStyle;
  }
  
  
  
  
  

}
