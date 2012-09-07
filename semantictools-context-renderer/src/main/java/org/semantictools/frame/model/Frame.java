package org.semantictools.frame.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.semantictools.frame.api.TypeManager;

import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntResource;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.OWL;

/**
 * @author Greg McFall
 *
 */
public class Frame implements Comparable<Frame>, RdfType {
  
  private TypeManager typeManager;
  private OntClass type;
  private RestCategory category = RestCategory.UNKNOWN;
  private List<Frame> supertypeList = new ArrayList<Frame>();
  private List<Frame> subtypeList = new ArrayList<Frame>();
  private List<Field> declaredFields = new ArrayList<Field>();
  private List<Datatype> subdatatypeList = new ArrayList<Datatype>();
  private boolean isAbstract = false;
  private Map<String, OntClass> propertyUri2Restriction = new HashMap<String, OntClass>();
  
  public Frame(TypeManager typeManager, OntClass type) {
    this.typeManager = typeManager;
    this.type = type;
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
  
  public List<NamedIndividual> listInstances(boolean direct) {
    List<NamedIndividual> list = new ArrayList<NamedIndividual>();
    Iterator<? extends OntResource> sequence = type.listInstances(direct);
    while (sequence.hasNext()) {
      list.add(new NamedIndividual(sequence.next()));
    }
    return list;
  }

  public String getComment() {
    String result = type.getComment(null);
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
    
    for (Frame sub : subtypeList) {
      if (!list.contains(sub)) {
        list.add(sub);
        addSubtypes(list, sub);
      }
    }
    
  }

  public List<Frame> getSupertypeList() {
    return supertypeList;
  }
  public List<Field> getDeclaredFields() {
    return declaredFields;
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
  

}
