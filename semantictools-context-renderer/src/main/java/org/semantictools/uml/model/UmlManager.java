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

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.semantictools.frame.api.TypeManager;
import org.semantictools.frame.model.Encapsulation;
import org.semantictools.frame.model.Field;
import org.semantictools.frame.model.Frame;
import org.semantictools.frame.model.InverseProperty;
import org.semantictools.frame.model.RdfType;

public class UmlManager {
  private TypeManager typeManager;
  
  
  private Map<String, UmlClass> uri2Class = new HashMap<String, UmlClass>();

  public UmlManager(TypeManager typeManager) {
    this.typeManager = typeManager;
    init();
  }
  
  public void add(UmlClass umlClass) {
    uri2Class.put(umlClass.getURI(), umlClass);
  }
  
  public TypeManager getTypeManager() {
    return typeManager;
  }
  
  public UmlClass getUmlClassByURI(String uri) {
    return uri2Class.get(uri);
  }
  
  public Collection<UmlClass> listUmlClasses() {
    return uri2Class.values();
  }

  private void init() {
    
    buildClasses();
    buildAssociations();
    sortAssociations();
    
  }

  private void sortAssociations() {
    for (UmlClass umlClass : uri2Class.values()) {
      Comparator<UmlAssociation> comparator = new UmlAssociationComparator(umlClass);
      sort(umlClass.getChildren(), comparator);
    }
    
  }

  private void sort(List<UmlAssociation> list, Comparator<UmlAssociation> comparator) {
    if (list.isEmpty()) return;
    Collections.sort(list, comparator);
    
  }

  private void buildClasses() {
    
    for (Frame frame : typeManager.listFrames() ) {
      buildClass(frame);
    }
    for (Frame frame : typeManager.listListTypes()) {
      buildClass(frame);
    }
    
    
  }

  private void buildClass(Frame frame) {
    
    String uri = frame.getUri();
    
    UmlClass umlClass = new UmlClass(frame, this);
    uri2Class.put(uri, umlClass);
    
    for (Field field : frame.getDeclaredFields()) {
      
      RdfType fieldType = field.getRdfType();
     
      
      if (fieldType.canAsDatatype()) {
        umlClass.add(field);
      }
      
    }
  }
  


  private void buildAssociations() {

    for (Frame frame : typeManager.listFrames() ) {
      buildAssociations(frame);
    }
    
  }

  private void buildAssociations(Frame frame) {
    
    UmlClass umlClass = getUmlClassByURI(frame.getUri());
    
    addChildren(umlClass, frame);
    
    
  }

  private void addChildren(UmlClass umlClass, Frame frame) {
   
    for (Field field : frame.getDeclaredFields()) {
      RdfType fieldType = field.getRdfType();

      
      Field inverseField = field.getInverseField();
      
      UmlClass otherClass = null;
      if (fieldType.canAsFrame()) {
        otherClass = getUmlClassByURI(fieldType.getUri());
      } else if (fieldType.canAsListType()) {
        otherClass = getUmlClassByURI(fieldType.asListType().getElementType().getUri());
      }
      
      if (otherClass != null) {
        
        InverseProperty inverse = field.getInverseProperty();
        
        Encapsulation inverseEncapsulation = inverseField==null ?  inverse.getEncapsulation() :
          inverseField.getEncapsulation();
        
        Encapsulation encapsulation = field.getEncapsulation();
        
        if (encapsulation == Encapsulation.NONE && inverseEncapsulation != Encapsulation.NONE) {
          //
          // The other class is the parent
          

          UmlAssociationEnd end0 = new UmlAssociationEnd(umlClass);
          end0.setMultiplicity(inverse.getMultiplicity());
          defineEnd(end0, inverseField, encapsulation);
          
          UmlAssociationEnd end1 = new UmlAssociationEnd(otherClass);
          defineEnd(end1, field, inverseEncapsulation);
          
          UmlAssociation assoc = new UmlAssociation(end0, end1);
          
          otherClass.addChild(assoc);
          umlClass.addParent(assoc);
          
          
        } else {
        
          
          UmlAssociationEnd end0 = new UmlAssociationEnd(umlClass);
          UmlAssociationEnd end1 = new UmlAssociationEnd(otherClass);
          defineEnd(end0, inverseField, encapsulation);
          defineEnd(end1, field, inverseEncapsulation);
          
          UmlAssociation assoc = new UmlAssociation(end0, end1);
          
          umlClass.addChild(assoc);
          otherClass.addParent(assoc);
        }
        
      } else {
        // TODO: handle other conditions
        
        
      }
      
    }
    
  }
  
  private void defineEnd(UmlAssociationEnd end, Field field, Encapsulation encapsulation) {

    end.setEncapsulation(encapsulation);
    if (field == null) return;
    end.setField(field);
    end.setLocalName(field.getLocalName());
    end.setMultiplicity(field.getMultiplicity());
    
  }

  static class UmlAssociationComparator implements Comparator<UmlAssociation> {
    private UmlClass umlClass;
    

    public UmlAssociationComparator(UmlClass umlClass) {
      this.umlClass = umlClass;
    }


    @Override
    public int compare(UmlAssociation a, UmlAssociation b) {
      String aName = a.getOtherEnd(umlClass).getParticipant().getLocalName();
      String bName = b.getOtherEnd(umlClass).getParticipant().getLocalName();
      
      return aName.compareTo(bName);
    }
    
  }
  

}
