package org.semantictools.context.renderer.model;

public class TermValue {
  private String id;
  private String type;
  private Container container = Container.NONE;
  
  public String getId() {
    return id;
  }
  public void setId(String id) {
    this.id = id;
  }
  public String getType() {
    return type;
  }
  public void setType(String type) {
    this.type = type;
  }
  public Container getContainer() {
    return container;
  }
  public void setContainer(Container container) {
    this.container = container;
  }
  
  
  

}
