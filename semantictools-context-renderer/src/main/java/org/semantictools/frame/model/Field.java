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
package org.semantictools.frame.model;

import java.util.List;

import org.semantictools.bind.BindOntology;
import org.semantictools.frame.api.TypeManager;

import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntProperty;
import com.hp.hpl.jena.ontology.OntResource;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.RDF;

public class Field implements Comparable<Field> {
  private Frame frame;
  private OntProperty property;
  private OntResource type;
  private String comment;
  private int minCardinality = 0;
  private int maxCardinality = -1;
  private OntProperty inverseOf;
  private RdfType rdfType;
  private Encapsulation encapsulation = null;
  private InverseProperty inverse;
  
  private NamedIndividual valueRestriction;
  
  public Field(Frame frame, OntProperty property, OntResource type, int minCardinality,
      int maxCardinality) {
    this.frame = frame;
    this.property = property;
    this.type = type;
    this.minCardinality = minCardinality;
    this.maxCardinality = maxCardinality;
  }
  
  public Field(Frame frame, OntProperty property, ListType listType) {
    this(frame, property, listType.getOntClass(), 0, 1);
    rdfType = listType;
  }
  
  public Frame getDeclaringFrame() {
    return frame;
  }

  public OntProperty getInverseOf() {
    return inverseOf;
  }
  
  public Field getInverseField() {
    if (inverseOf != null) {
      Frame otherFrame = frame.getTypeManager().getFrameByUri(type.getURI());
      if (otherFrame == null) return null;
      
      String inverseURI = inverseOf.getURI();
      
      List<Field> otherFields = otherFrame.getDeclaredFields();
      
      for (Field field : otherFields) {
        OntProperty p = field.getProperty();
        if (p == null) continue;
        if (p.getURI().equals(inverseURI)) return field;
      }
      
    }
    return null;
  }

  
  /**
   * Returns an individual named as the owl:hasValue restriction on this
   * field.
   */
  public NamedIndividual getValueRestriction() {
    return valueRestriction;
  }

  public void setValueRestriction(NamedIndividual valueRestriction) {
    this.valueRestriction = valueRestriction;
  }

  public InverseProperty getInverseProperty() {
    if (inverse == null) {
      analyzeAssociationQualifier();
    }
    
    return inverse;
  }

  public void setInverseOf(OntProperty inverseOf) {
    this.inverseOf = inverseOf;
  }

  public Encapsulation getEncapsulation() {
    if (encapsulation == null) {
      analyzeAssociationQualifier();
    }
    return encapsulation;
  }
//
//  private Encapsulation computeEncapsulation() {
//    if (property == null) return Encapsulation.NONE;
//    
//    OntClass ontClass = frame.getType();
//    
//    List<OntClass> list = ontClass.listSuperClasses().toList();
//    for (OntClass superClass : list) {
//      
//      if (!superClass.hasRDFType(BindOntology.AssociationQualifier, true)) {
//        continue;
//      }
//      
//      Resource onProperty = superClass.getPropertyResourceValue(BindOntology.onProperty);
//      if (!property.equals(onProperty)) {
//        continue;
//      }
//      
//      Resource associationType = superClass.getPropertyResourceValue(BindOntology.associationType);
//      if (BindOntology.Aggregation.equals(associationType)) return Encapsulation.AGGREGATION;
//      if (BindOntology.Composition.equals(associationType)) return Encapsulation.COMPOSITION;
//    }
//    
//    
//    return Encapsulation.NONE;
//  }
  
  private void analyzeAssociationQualifier() {

    encapsulation = Encapsulation.NONE;
    inverse = new InverseProperty();
    
    if (property == null) return;
    
    OntClass ontClass = frame.getType();
    
    List<OntClass> list = ontClass.listSuperClasses().toList();
    for (OntClass superClass : list) {
      
      if (!superClass.hasRDFType(BindOntology.AssociationQualifier, true)) {
        continue;
      }
      
      Resource onProperty = superClass.getPropertyResourceValue(BindOntology.onProperty);
      if (!property.equals(onProperty)) {
        continue;
      }
      
      Resource associationType = superClass.getPropertyResourceValue(BindOntology.associationType);
      if (BindOntology.Aggregation.equals(associationType)) encapsulation = Encapsulation.AGGREGATION;
      if (BindOntology.Composition.equals(associationType)) encapsulation = Encapsulation.COMPOSITION;
      
      Resource inverseType = superClass.getPropertyResourceValue(BindOntology.inverseAssociationType);
      if (BindOntology.Aggregation.equals(inverseType)) inverse.setEncapsulation( Encapsulation.AGGREGATION );
      if (BindOntology.Composition.equals(inverseType)) inverse.setEncapsulation( Encapsulation.COMPOSITION );
      
      RDFNode min = superClass.getPropertyValue(BindOntology.inverseMinCardinality);
      if (min != null) {
        inverse.setMinCardinality(min.asLiteral().getInt());
      }
      RDFNode max = superClass.getPropertyValue(BindOntology.inverseMinCardinality);
      if (max != null) {
        inverse.setMaxCardinality(max.asLiteral().getInt());
      }
      RDFNode unbounded =  superClass.getPropertyValue(BindOntology.inverseUnboundedCardinality);
      if (unbounded != null) {
        inverse.setUnboundedCardinality(unbounded.asLiteral().getBoolean());
        
      }
    }
  }

  public void setEncapsulation(Encapsulation encapsulation) {
    this.encapsulation = encapsulation;
  }

  public String getLocalName() {
    return property.getLocalName();
  }
  
  public String getComment() {
    if (comment == null) {
      comment = FrameUtil.getPropertyDescription(property);
      if (comment == null) {
        comment = "";
      }
    }
    return comment;
  }
  
  public void setComment(String comment) {
    this.comment = comment;
  }
  
  public String getURI() {
    return property.getURI();
  }
  public OntProperty getProperty() {
    return property;
  }
  
  public RdfType getRdfType() {
    if (rdfType == null) {
      TypeManager manager = frame.getTypeManager();
      rdfType = manager.getFrameByUri(type.getURI());
      
      if (rdfType == null) {
        rdfType = manager.getDatatypeByUri(type.getURI());
      }
      
      if (rdfType == null) {
        rdfType = manager.getListTypeByListUri(type.getURI());
      }
      
      if (rdfType==null && type.getURI().startsWith(RDF.getURI())) {
        
        Frame f = new Frame(frame.getTypeManager(), type.as(OntClass.class));
        frame.getTypeManager().add(f);
        rdfType = f;
      }
      
      if (rdfType==null && type.canAs(OntProperty.class)) {
        Frame f = new Frame(manager, type.asClass());
        manager.add(f);
        rdfType = f;
      }
     
    }
    return rdfType;
  }
  
  public OntResource getType() {
    return type;
  }
  public int getMinCardinality() {
    return minCardinality;
  }
  public int getMaxCardinality() {
    return maxCardinality;
  }
  
  public void setMinCardinality(int value) {
    minCardinality = value;
  }
  
  public void setMaxCardinality(int value) {
    maxCardinality = value;
  }
  
  public String getMultiplicity() {
    return
        minCardinality==0 && maxCardinality<0 ? "*" :
        minCardinality==maxCardinality ? Integer.toString(minCardinality) :
        maxCardinality<0 ? minCardinality + "..*" :
        minCardinality + ".." + maxCardinality;
  }
  
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append(getLocalName());
    builder.append(":");
    builder.append(type.getLocalName());
    builder.append("[");
    builder.append(getMultiplicity());
    builder.append("]");
    
    return builder.toString();
  }

  @Override
  public int compareTo(Field o) {
    return getLocalName().compareTo(o.getLocalName());
  }

}
