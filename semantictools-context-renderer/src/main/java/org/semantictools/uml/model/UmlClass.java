package org.semantictools.uml.model;

import java.util.ArrayList;
import java.util.List;

import org.semantictools.frame.model.Datatype;
import org.semantictools.frame.model.Field;
import org.semantictools.frame.model.Frame;
import org.semantictools.frame.model.NamedIndividual;
import org.semantictools.frame.model.RdfType;

import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

public class UmlClass {
  
  private RdfType type;
  private String stereotype;
  private UmlManager manager;
  
  private List<Field> fieldList = new ArrayList<Field>();
  private List<UmlClass> supertypeList = null;
  private List<UmlClass> subtypeList = null;
  private List<UmlAssociation> children = new ArrayList<UmlAssociation>();
  private List<UmlAssociation> parentList = new ArrayList<UmlAssociation>();
  
  
  public UmlClass(RdfType type, UmlManager manager) {
    this.type = type;
    this.manager = manager;
  }
  

  public String getDescription() {
    String comment = 
        type.canAsFrame() ? type.asFrame().getComment() :
        null;
        
    return comment;
        
  }
  
  public List<NamedIndividual> listInstances(boolean direct) {
    return type.asFrame().listInstances(direct);
  }
  
  public void add(Field field) {
    fieldList.add(field);
  }


  public String getURI() {
    return type.getUri();
  }

  public String getLocalName() {
    return type.getLocalName();
  }

  public RdfType getType() {
    return type;
  }
  

  public String getStereotype() {
    return stereotype;
  }


  public void setStereotype(String stereotype) {
    this.stereotype = stereotype;
  }


  public List<Field> getFieldList() {
    return fieldList;
  }


  public List<UmlClass> getSupertypeList() {
    if (supertypeList == null) {
      supertypeList = new ArrayList<UmlClass>();
      if (type.canAsFrame()) {
        List<Frame> list = type.asFrame().getSupertypeList();
        for (Frame frame : list) {
          String uri = frame.getUri();
          if (uri.startsWith(RDF.getURI())) continue;
          if (uri.startsWith(RDFS.getURI())) continue;
          
          UmlClass superClass = manager.getUmlClassByURI(frame.getUri());
          if (superClass == null) {
            superClass = new UmlClass(frame, manager);
          }
          supertypeList.add(superClass);
        }
      }
    }
    
    return supertypeList;
  }


  public List<UmlClass> getSubtypeList() {
    if (subtypeList == null) {

      subtypeList = new ArrayList<UmlClass>();
      if (type.canAsFrame()) {
        List<Frame> list = type.asFrame().getSubtypeList();
        for (Frame frame : list) {
          String uri = frame.getUri();
          if (uri.startsWith(RDF.getURI())) continue;
          if (uri.startsWith(RDFS.getURI())) continue;
          
          UmlClass subclass = manager.getUmlClassByURI(frame.getUri());
          if (subclass == null) {
            throw new RuntimeException("UmlClass not found: " + frame.getUri());
          }
          subtypeList.add(subclass);
        }
        List<Datatype> datatypeList = type.asFrame().getSubdatatypeList();
        for (Datatype type : datatypeList) {
          String uri = type.getUri();
          UmlClass subclass = manager.getUmlClassByURI(uri);
          if (subclass == null) {
            subclass = new UmlClass(type, manager);
            manager.add(subclass);
          }
          subtypeList.add(subclass);
        }
      }
    }
    return subtypeList;
  }


  public void setSubtypeList(List<UmlClass> subtypeList) {
    this.subtypeList = subtypeList;
  }


  /**
   * Associations where this UmlClass is the subject.
   */
  public List<UmlAssociation> getChildren() {
    return children;
  }
  
  public void addChild(UmlAssociation child) {
    addUnique(children, child);
  }
  
  public void addParent(UmlAssociation parent) {
    addUnique(parentList, parent);
  }
  
  private void addUnique(List<UmlAssociation> list, UmlAssociation element) {
    for (UmlAssociation a : list) {
      if (a.equals(element)) return;
    }
    list.add(element);
  }


  /**
   * Associations where this UmlClass is the object.
   */
  public List<UmlAssociation> getParentList() {
    return parentList;
  }
//  
//  /**
//   * Returns the child association (where this UmlClass is the subject)
//   * for the specified property, or null if no such association is found.
//   * @param propertyURI
//   * @return
//   */
//  public UmlAssociation getChildByURI(String propertyURI) {
//    
//    for (UmlAssociation assoc : children) {
//      
//      UmlAssociationEnd end = assoc.getOtherEnd(this);
//      if (end.getPropertyURI().equals(propertyURI)) return assoc;
//      
//    }
//    
//    return null;
//  }
  
  public List<UmlProperty> listAllDeclaredProperties() {
    List<UmlProperty> list = new ArrayList<UmlProperty>();
    for (Field field : fieldList) {
      list.add(createUmlProperty(field));
    }
    for (UmlAssociation a : children) {
      UmlAssociationEnd end = a.getOtherEnd(this);
      Field field = end.getField();
      if (field == null) continue;
      
      list.add(createUmlProperty(field));
    }
    
    return list;
  }


  private UmlProperty createUmlProperty(Field field) {
    UmlProperty p = new UmlProperty();
    p.setLocalName(field.getLocalName());
    p.setDescription(field.getComment());
    p.setMultiplicity(field.getMultiplicity());
    p.setType(field.getType().getLocalName());
    p.setField(field);

    return p;
  }
  
}
