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
