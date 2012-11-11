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
