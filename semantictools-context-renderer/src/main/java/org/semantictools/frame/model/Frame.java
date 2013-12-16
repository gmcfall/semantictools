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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.semantictools.bind.LDP;
import org.semantictools.frame.api.TypeManager;

import com.hp.hpl.jena.ontology.AllValuesFromRestriction;
import com.hp.hpl.jena.ontology.HasValueRestriction;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntProperty;
import com.hp.hpl.jena.ontology.OntResource;
import com.hp.hpl.jena.ontology.Restriction;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.OWL;

/**
 * @author Greg McFall
 *
 */
public class Frame implements Comparable<Frame>, RdfType {
  
  private TypeManager typeManager;
  protected OntClass type;
  private RestCategory category = RestCategory.UNKNOWN;
  private List<Frame> supertypeList = new ArrayList<Frame>();
  private List<Frame> subtypeList = new ArrayList<Frame>();
  private List<Field> declaredFields = new ArrayList<Field>();
  private List<Datatype> subdatatypeList = new ArrayList<Datatype>();
  private List<Frame> containerList;
  private boolean isAbstract = false;
  private Map<String, OntClass> propertyUri2Restriction = new HashMap<String, OntClass>();
  
  private ContainerRestriction containerRestriction;
  
  public Frame(TypeManager typeManager, OntClass type) {
    this.typeManager = typeManager;
    this.type = type;
    setContainerRestriction();
  }
  
  private void setContainerRestriction() {
    
    Uri membershipSubject = null;
    Uri membershipPredicate = null;
    
    List<OntClass> superList = type.listSuperClasses().toList();
    for (OntClass superType : superList) {
      if (superType.canAs(Restriction.class)) {
        Restriction restriction = superType.as(Restriction.class);
        OntProperty property = restriction.getOnProperty();
        if (LDP.membershipPredicate.equals(property.getURI())) {
          if (restriction.canAs(HasValueRestriction.class)) {
            HasValueRestriction hasValue = restriction.as(HasValueRestriction.class);
            String value = hasValue.getHasValue().asResource().getURI();
            membershipPredicate = new Uri(value);
          }
        } else if (
           LDP.membershipSubject.equals(property.getURI()) &&
           restriction.canAs(AllValuesFromRestriction.class)
        ) {
          AllValuesFromRestriction r = restriction.as(AllValuesFromRestriction.class);
          String value = r.getAllValuesFrom().asResource().getURI();
          membershipSubject = new Uri(value);
        }
      }
    }
    
    if (membershipSubject != null || membershipPredicate != null) {
      containerRestriction = new ContainerRestriction(this, membershipSubject, membershipPredicate);
    }
    
  }

  public TypeManager getTypeManager() {
    return typeManager;
  }

  public boolean isAbstract() {
    return isAbstract;
  }

  public void setAbstract(boolean isAbstract) {
    this.isAbstract = isAbstract;
  }

  public RestCategory getCategory() {
    return category;
  }

  public void setCategory(RestCategory category) {
    this.category = category;
  }
  
  public boolean hasInstances() {
    Iterator<? extends OntResource> sequence = type.listInstances(false);
    return sequence.hasNext();
  }
  
  public List<NamedIndividual> listInstances(boolean direct) {
    List<NamedIndividual> list = new ArrayList<NamedIndividual>();
    Iterator<? extends OntResource> sequence = type.listInstances(direct);
    while (sequence.hasNext()) {
      list.add(new NamedIndividual(sequence.next()));
    }
    return list;
  }

  public String getComment() {
    String result = FrameUtil.getClassDescription(type);
    if (result == null) {
      result = "";
    }
    return result;
  }

  public String getUri() {
    return type.getURI();
  }
  
  public void addRestriction(OntClass restriction) {
    Resource property = restriction.getPropertyResourceValue(OWL.onProperty);
    propertyUri2Restriction.put(property.getURI(), restriction);
  }
  
  public OntClass getRestriction(String propertyURI) {
    return propertyUri2Restriction.get(propertyURI);
  }
  
  public List<OntClass> listRestrictions() {
    return new ArrayList<OntClass>(propertyUri2Restriction.values());
  }
  
  public String getLocalName() {
    return type.getLocalName();
  }
  
  public OntClass getType() {
    return type;
  }

  public void setType(OntClass type) {
    this.type = type;
  }
  
  public List<Frame> getSubtypeList() {
    return subtypeList;
  }
  
  public List<Frame> listAllSubtypes() {
    List<Frame> list = new ArrayList<Frame>();
    
    addSubtypes(list, this);
    
    return list;
  }

  private void addSubtypes(List<Frame> list, Frame frame) {
    
    for (Frame sub : frame.getSubtypeList()) {
      if (!list.contains(sub)) {
        list.add(sub);
        addSubtypes(list, sub);
      }
    }
    
  }
  
  public List<Frame> listAllSupertypes() {
    Set<Frame> set = new HashSet<Frame>();
    addSupertypes(this, set);
    List<Frame> list = new ArrayList<Frame>(set);
    return list;
  }

  private void addSupertypes(Frame frame, Set<Frame> set) {
    for (Frame type : frame.getSupertypeList()) {
      if (set.contains(type)) continue;
      set.add(type);
      addSupertypes(type, set);
    }
    
  }
  
  public boolean isSubclassOf(String uri) {
    if (getUri().equals(uri)) return true;
    for (Frame supertype : supertypeList) {
      if (supertype.isSubclassOf(uri)) return true;
    }
    
    return false;
  }

  public List<Frame> getSupertypeList() {
    return supertypeList;
  }
  public List<Field> getDeclaredFields() {
    return declaredFields;
  }
  
  public Field getDeclaredFieldByPropertyURI(String uri) {
    for (Field field : declaredFields) {
      if (uri.equals(field.getURI())) return field;
    }
    return null;
  }
  
  /**
   * Returns the list of all fields defined on this type, including
   * fields declared on supertypes.
   */
  public List<Field> listAllFields() {
    List<Field> list = new ArrayList<Field>();
    Map<String, Field> fieldMap = new HashMap<String, Field>();
    addFields(declaredFields, fieldMap, list);
    
    addSuperFields(fieldMap, list, this);
    
    return list;
  }
  
  /**
   * Returns true if this frame has any fields either declared directly or
   * declared by a supertype.
   */
  public boolean hasFields() {
    if (!declaredFields.isEmpty()) return true;
    for (Frame superType : supertypeList) {
      if (superType.hasFields()) return true;
    }
    return false;
  }
  
  

  private void addSuperFields(Map<String, Field> fieldMap, List<Field> list,  Frame frame) {
    for (Frame parent : frame.supertypeList) {
      addFields(parent.declaredFields, fieldMap, list);
      addSuperFields(fieldMap, list, parent);
    }
    
  }

  private void addFields(List<Field> source, Map<String, Field> fieldMap, List<Field> sink) {
    
    for (Field field : source) {
      if (!fieldMap.containsKey(field.getURI())) {
        fieldMap.put(field.getURI(), field);
        sink.add(field);
      }
    }
    
  }

  public String toMultilineString() {
    return toString(true);
  }
  
  public String toString() {
    return toString(false);
  }
  
  private String toString(boolean multiline) {
    StringBuilder builder = new StringBuilder();
    builder.append(getLocalName());
    builder.append("{");
    String comma = multiline ? "\n  " : "";
    List<Field> list = listAllFields();
    for (Field field : list) {
      builder.append(comma);
      builder.append(field.toString());
      comma = multiline ? ",\n  " : ", ";
    }
    if (multiline) {
      builder.append("\n");
    }
    builder.append("}");
    
    return builder.toString();
  }

  @Override
  public int compareTo(Frame peer) {
    String myName = getLocalName();
    String yourName = peer.getLocalName();
    int result = myName.compareTo(yourName);
    if (result == 0) {
      String myURI = getUri();
      String yourURI = peer.getUri();
      result = myURI.compareTo(yourURI);
    }
    return result;
  }
  
  @Override
  public boolean canAsOntClass() {
    return true;
  }

  @Override
  public boolean canAsFrame() {
    return true;
  }

  @Override
  public boolean canAsDatatype() {
    return false;
  }
  
  @Override
  public OntClass asOntClass() {
    return type;
  }

  @Override
  public Frame asFrame() {
    return this;
  }

  @Override
  public Datatype asDatatype() {
    return null;
  }

  @Override
  public boolean canAsListType() {
    return false;
  }

  @Override
  public ListType asListType() {
    return null;
  }

  @Override
  public String getNamespace() {
    return type.getNameSpace();
  }
  
  public void addSubdatatype(Datatype value) {
    subdatatypeList.add(value);
  }

  public List<Datatype> getSubdatatypeList() {
    return subdatatypeList;
  }

  @Override
  public boolean canAsEnumeration() {
    return false;
  }

  @Override
  public Enumeration asEnumeration() {
    return null;
  }
  
  public ContainerRestriction getContainerRestriction() {
    return containerRestriction;
  }

  public void addContainer(Frame frame) {
    if (containerList == null) {
      containerList = new ArrayList<Frame>();
    }
    containerList.add(frame);
  }
  
  public List<Frame> getContainerList() {
    return containerList;
  }

  public Field getFieldByURI(String predicateURI) {
    for (Field field : declaredFields) {
      if (predicateURI.equals(field.getURI())) {
        return field;
      }
    }
    for (Frame type : supertypeList) {
      Field field = type.getFieldByURI(predicateURI);
      if (field != null) {
        return field;
      }
    }
    return null;
  }

}
