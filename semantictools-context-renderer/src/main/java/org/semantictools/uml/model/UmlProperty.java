package org.semantictools.uml.model;

import org.semantictools.frame.model.Field;

public class UmlProperty {
  private String localName;
  private String description;
  private String multiplicity;
  private String type;
  private Field field;
  
  public UmlProperty() {}
  
  public String getLocalName() {
    return localName;
  }
  public void setLocalName(String localName) {
    this.localName = localName;
  }
  public String getDescription() {
    return description;
  }
  public void setDescription(String description) {
    this.description = description;
  }
  public String getMultiplicity() {
    return multiplicity;
  }
  public void setMultiplicity(String multiplicity) {
    this.multiplicity = multiplicity;
  }
  public String getType() {
    return type;
  }
  public void setType(String type) {
    this.type = type;
  }

  public Field getField() {
    return field;
  }

  public void setField(Field field) {
    this.field = field;
  }
  
  

}
