package org.semantictools.uml.model;

import org.semantictools.frame.model.Encapsulation;
import org.semantictools.frame.model.Field;

public class UmlAssociationEnd {

  private UmlClass associationEnd;
  private Encapsulation encapsulation = Encapsulation.NONE;
  private String multiplicity;
  private String name;

  private Field field;
  
  

  public UmlAssociationEnd(UmlClass associationEnd) {
    this.associationEnd = associationEnd;
  }


  public Field getField() {
    return field;
  }


  public void setField(Field field) {
    this.field = field;
  }


  public boolean equals(UmlAssociationEnd other) {
    
    return 
        (associationEnd == other.associationEnd) &&
        equals(name, other.name);
  }
  
  private boolean equals(String a, String b) {
    return (a==null && b==null) || (a!=null && a.equals(b));
  }


  public UmlClass getParticipant() {
    return associationEnd;
  }

  
  public String getLocalName() {
    return name;
  }
  
  public void setLocalName(String name) {
    this.name = name;
  }

//  
//  public String getPropertyURI() {
//    return field.getProperty().getURI();
//  }


  public Encapsulation getEncapsulation() {
    return encapsulation;
  }


  public void setEncapsulation(Encapsulation encapsulation) {
    this.encapsulation = encapsulation;
  }


  public String getMultiplicity() {
    return multiplicity;
  }


  public void setMultiplicity(String multiplicity) {
    this.multiplicity = multiplicity;
  }
  
  
  public String toString() {
    StringBuilder builder = new StringBuilder("End(attachedTo=");
    builder.append(associationEnd.getLocalName());
    String fieldName = getLocalName();
    if (fieldName != null) {
      builder.append(", property=");
      builder.append(fieldName);
    }
    builder.append(")");
    return builder.toString();
  }
  
  

}
