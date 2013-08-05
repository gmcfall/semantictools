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
package org.semantictools.frame.api;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import org.semantictools.frame.api.impl.DatatypeReader;
import org.semantictools.frame.model.BindVocabulary;
import org.semantictools.frame.model.Datatype;
import org.semantictools.frame.model.Enumeration;
import org.semantictools.frame.model.Field;
import org.semantictools.frame.model.Frame;
import org.semantictools.frame.model.ListType;
import org.semantictools.frame.model.NamedIndividual;
import org.semantictools.frame.model.RdfType;
import org.semantictools.frame.model.RestCategory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntProperty;
import com.hp.hpl.jena.ontology.OntResource;
import com.hp.hpl.jena.ontology.impl.OntResourceImpl;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFList;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.OWL2;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

public class FrameBuilder {
  private static Logger logger = LoggerFactory.getLogger(FrameBuilder.class);
  
  private static final OntModel owlModel = ModelFactory.createOntologyModel();
  private static final OntClass THING = owlModel.createClass("http://www.w3.org/2002/07/owl#Thing");
  private static final String[] STANDARD_URI = {
    "http://www.w3.org/2001/XMLSchema#",
    "http://www.w3.org/2002/07/owl#",
    "http://www.w3.org/1999/02/22-rdf-syntax-ns#",
    "http://www.w3.org/2000/01/rdf-schema#",
    "http://purl.org/semantictools/v1/vocab/bind#"
  };
  
  private TypeManager manager;
  private OntModel model;
  private DatatypeReader datatypeReader;

  public FrameBuilder(TypeManager manager) {
    this.manager = manager;
    datatypeReader = new DatatypeReader(manager);
  }
  
  public OntModel getOntModel() {
    return model;
  }
  
  public void setOntModel(OntModel model) {
    this.model = model;
  }
  
  private static boolean isStandard(String uri) {
    for (int i=0; i<STANDARD_URI.length; i++) {
      if (uri.startsWith(STANDARD_URI[i])) return true;
    }
    return false;
  }
  
  public void buildFrames(OntModel model) {
    this.model = model;
    applyInverseOfReasoning();
    createFrames();
    addSupertypesAndRestrictions();
    addFields();
    addFieldsFromRestrictions();
  }



  /**
   * For all inverse relations, ensure that the relationship
   * is defined in both directions.
   */
  private void applyInverseOfReasoning() {
    
    List<Statement> list = model.listStatements(null, OWL.inverseOf, (RDFNode) null).toList();
    for (Statement s : list) {
      Resource subject = s.getSubject();
      Resource object = s.getObject().asResource();
      Statement newStatement = model.createStatement(object, OWL.inverseOf, subject);
      model.add(newStatement);
    }
    
  }

  private void addFieldsFromRestrictions() {
    
    for (Frame frame : manager.listFrames()) {
      addFieldsFromRestrictions(frame);
    }
    
  }

  private void addFieldsFromRestrictions(Frame frame) {
    Map<String, Field> fieldMap = createFieldMap(frame);
    for (OntClass restriction : frame.listRestrictions()) {
      addFieldFromRestriction(fieldMap, frame, restriction);
    }
    
  }

  private void addFieldFromRestriction(Map<String, Field> fieldMap, Frame frame, OntClass restriction) {
      int minCardinality = 0;
      int maxCardinality = -1;
      OntResource range = null;
      
      Resource resource = restriction.getPropertyResourceValue(OWL2.onProperty);
      String uri = resource.getURI();
      Field priorField = fieldMap.get(uri);
      
      if (priorField != null) {
        maxCardinality = priorField.getMaxCardinality();
      }
      
      
      OntProperty property = null;
      
      if (restriction.getPropertyResourceValue(OWL2.onProperty).canAs(OntProperty.class)) {
        property = resource.as(OntProperty.class);
      } else {
        property = model.createOntProperty(resource.getURI());
      }
      
      
      if (restriction.hasProperty(OWL.minCardinality)) {
        minCardinality = restriction.getProperty(OWL.minCardinality).getInt();
      }
      if (restriction.hasProperty(OWL.maxCardinality)) {
        maxCardinality = restriction.getProperty(OWL.maxCardinality).getInt();
      }
      Resource valueType = restriction.getPropertyResourceValue(OWL.allValuesFrom);
      if (valueType != null) {
        range = valueType.as(OntResource.class);
      }
      
    
//      Resource onClass = restriction.getPropertyResourceValue(OWL2.onClass);
//      if (onClass != null) {
//        range = onClass.as(OntResource.class);
//        if (restriction.hasProperty(OWL2.minQualifiedCardinality)) {
//          minCardinality = restriction.getProperty(OWL2.minQualifiedCardinality).getInt();
//        }
//        if (restriction.hasProperty(OWL2.maxQualifiedCardinality)) {
//          maxCardinality = restriction.getProperty(OWL2.maxQualifiedCardinality).getInt();
//        }
//        
//      } else {
//        if (restriction.hasProperty(OWL.minCardinality)) {
//          minCardinality = restriction.getProperty(OWL.minCardinality).getInt();
//        }
//        if (restriction.hasProperty(OWL.maxCardinality)) {
//          maxCardinality = restriction.getProperty(OWL.maxCardinality).getInt();
//        }
//      }
      
      if (range == null) {
        Resource value = property.getPropertyResourceValue(RDFS.range);
        if (value == null) {
          logger.warn("Ignoring field " + resource.getLocalName() + " on class " + frame.getLocalName() + ": the range is not defined.");
          return;
        }
        range = property.getPropertyResourceValue(RDFS.range).as(OntResource.class);
      }
      
      String comment = restriction.getComment(null);
      if (priorField != null && priorField.getDeclaringFrame()==frame) {
        
       
        priorField.setComment(comment);
        priorField.setMinCardinality(minCardinality);
        priorField.setMaxCardinality(maxCardinality);
        return;
      }
     Field field = new Field(frame, property, range, minCardinality, maxCardinality);
     field.setComment(comment);
     fieldMap.put(uri, field);
     
     frame.getDeclaredFields().add(field);
  }

  private Map<String, Field> createFieldMap(Frame frame) {
    Map<String, Field> map = new HashMap<String, Field>();
    for (Field field : frame.listAllFields()) {
      map.put(field.getURI(), field);
    }
    return map;
  }

  private void addFields() {
    List<OntProperty> list = listProperties();
    for (OntProperty p : list) {
      if (isStandard(p.getURI())) continue;
      addFields(p, p);
    }
    
  }



  private void addFields(OntProperty p, OntProperty ancestor) {
    OntResource domainResource = ancestor.getDomain();
    if (domainResource == null) {
      handleNullDomain(p, ancestor);
      return;
    }
    
    OntClass domain = domainResource.as(OntClass.class);
    List<OntResource> domainList = listUnionMembers(p, domain);
    if (domainList.isEmpty()) {
      domainList.add(domain);
    }
    
    for (OntResource type : domainList) {
      addField(type, p, ancestor);
    }
  }

  private void handleNullDomain(OntProperty p, OntProperty ancestor) {
    handlePropertySubclassOf(p);
    handleSubpropertyOf(p, ancestor);
  }
  
  private void handleSubpropertyOf(OntProperty p, OntProperty ancestor) {
    
    List<RDFNode> list = ancestor.listPropertyValues(RDFS.subPropertyOf).toList();
    for (RDFNode node : list) {
      if (!node.canAs(OntProperty.class)) continue;
      
      OntProperty superProperty = node.as(OntProperty.class);
      if (superProperty.equals(ancestor)) continue;
      addFields(p, superProperty);
      handleSubpropertyOf(p, superProperty);
      
      
    }
  }

  private void handlePropertySubclassOf(OntProperty p) {
 
    List<RDFNode> list = p.listPropertyValues(RDFS.subClassOf).toList();
    for (RDFNode node : list) {
      if (!node.canAs(Resource.class)) continue;
      Resource resource = node.asResource();
      Resource onPropertyValue = resource.getPropertyResourceValue(OWL.onProperty);
      if (!RDFS.domain.equals(onPropertyValue)) continue;
      Resource someValuesFrom = resource.getPropertyResourceValue(OWL.someValuesFrom);
      if (someValuesFrom == null) continue;
      String uri = someValuesFrom.getURI();
      if (uri != null) {
        OntResource type = someValuesFrom.as(OntResource.class);
        addField(type, p, null);
      } else {
        Resource unionList = someValuesFrom.getPropertyResourceValue(OWL.unionOf);
        while (unionList != null) {
          Resource first = unionList.getPropertyResourceValue(RDF.first);
          if (first != null) {
            String typeURI = first.getURI();
            if (typeURI == null) {
              logger.warn("Cannot handle union that contains an anonymous class in the domain of " + p.getURI());
            } else {
              OntResource type = first.as(OntResource.class);
              addField(type, p, null);
            }
          } 
          unionList = unionList.getPropertyResourceValue(RDF.rest);
          if (RDF.nil.equals(unionList)) {
            break;
          }
        }
      }
    }
  }

  private void addField(OntResource type, OntProperty p, OntProperty ancestor) {
    int minCardinality = 0;
    int maxCardinality = -1;
    OntResource range = null;
    
    String typeURI = type.getURI();
    if (typeURI == null) {
      // We only add fields to named types.
      return;
    }
    
    // Do not add abstract properties.
    if (isAbstract(p)) return;
    
    Frame frame = manager.getFrameByUri(typeURI);
    if (frame == null) {
      if (isStandard(typeURI)) return;
      logger.warn("Ignoring property " + p.getLocalName() + " on class " + type.getLocalName() + ": frame not found");
      return;
    }
    
    if (frame.getDeclaredFieldByPropertyURI(p.getURI()) != null) return;

    if (p.hasRDFType(OWL.FunctionalProperty)) {
      maxCardinality = 1;
    }
    
    OntClass restriction = frame.getRestriction(p.getURI());
    range = p.getRange();
    if (range == null && ancestor!=null) {
      range = ancestor.getRange();
    }
    if (range == null) {
//      logger.warn("Ignoring property " + p.getLocalName() + " on class " + type.getLocalName() + ": range not defined");
//      return;
      range = THING;
    }
    if (restriction != null) {
      Resource onClass = restriction.getPropertyResourceValue(OWL2.allValuesFrom);
      if (onClass != null) {
        range = onClass.as(OntResource.class);
        if (restriction.hasProperty(OWL2.minQualifiedCardinality)) {
          minCardinality = restriction.getProperty(OWL2.minQualifiedCardinality).getInt();
        }
        if (restriction.hasProperty(OWL2.maxQualifiedCardinality)) {
          maxCardinality = restriction.getProperty(OWL2.maxQualifiedCardinality).getInt();
        }
        
      } else {
        if (restriction.hasProperty(OWL.minCardinality)) {
          minCardinality = restriction.getProperty(OWL.minCardinality).getInt();
        }
        if (restriction.hasProperty(OWL.maxCardinality)) {
          maxCardinality = restriction.getProperty(OWL.maxCardinality).getInt();
        }
      }
    }

    Field field = null;
    
    String rangeURI = range.getURI();
    if (rangeURI == null) {
      
      field = createListField(frame, p, range);
      
      if (field == null) {
        logger.warn("Ignoring property " + p.getLocalName() + " on class " + type.getLocalName() + ": range has no URI");
        return;
      }
    } else {
    
      field = new Field(frame, p, range, minCardinality, maxCardinality);
      
      if (field.getRdfType() == null) {
        logger.warn("Failed to create RdfType for field " + field.getLocalName() + " of type " + field.getType().getURI());
       
      }
      
    }

    Resource rawInverse = p.getPropertyResourceValue(OWL.inverseOf);
    if (rawInverse != null && rawInverse.canAs(OntProperty.class)) {
      field.setInverseOf(rawInverse.as(OntProperty.class));
    }
    frame.getDeclaredFields().add(field);
    
  }
  

  private boolean isAbstract(OntProperty p) {
    List<RDFNode> list = p.listPropertyValues(RDF.type).toList();
    for (RDFNode node : list) {
      if (node.canAs(Resource.class)) {
        Resource r = node.asResource();
        if (BindVocabulary.AbstractProperty.getURI().equals(r.getURI())) return true;
      }
    }
    return false;
  }

  private Field createListField(Frame frame, OntProperty p, OntResource range) {
    
    Resource intersection = range.getPropertyResourceValue(OWL.intersectionOf);
    if (intersection == null) return null;
    
    if (intersection.canAs(RDFList.class)) {
      List<RDFNode> intersectionList = intersection.as(RDFList.class).asJavaList();
      for (RDFNode node : intersectionList) {
        if (node.canAs(OntClass.class)) {
         OntClass intersectionMember = node.as(OntClass.class);
        
         if (
             RDF.first.equals(intersectionMember.getPropertyResourceValue(OWL.onProperty))
         ) {
           // The intersectionMember has an owl:onProperty property whose value is rdf:first
           
           Resource elementRdfType = intersectionMember.getPropertyResourceValue(OWL.allValuesFrom);
           
           if (elementRdfType != null) {
             
             String elementTypeURI = elementRdfType.getURI();
             if (elementTypeURI != null) {
               RdfType elementType = manager.getTypeByURI(elementTypeURI);
               if (elementType != null) {
                 
                 
                 ListType listType = manager.getListTypeByElementUri(elementTypeURI);
                 if (listType == null) {
                   listType = new ListType(manager, intersectionMember, elementType);
                   manager.add(listType);
                 }
                 
                 return new Field(frame, p, listType);
                 
               }
             }
           }
           
         }
        }
        
      }
    }
    
    
    
    
    return null;
  }
  

  private List<OntClass> listRestrictions(OntClass type) {
    List<OntClass> list = new ArrayList<OntClass>();
    Iterator<OntClass> sequence = type.listSuperClasses(true);
    while (sequence.hasNext()) {
      OntClass supertype = sequence.next();
      if (supertype.hasRDFType(OWL.Restriction, false)) {
        list.add(supertype);
      }
    }
    return list;
  }

  private List<OntResource> listUnionMembers(OntProperty p, OntResource domain) {
    List<OntResource> list = new ArrayList<OntResource>();
    Resource union = domain.getPropertyResourceValue(OWL.unionOf);
    if (union != null && union.canAs(RDFList.class)) {
      
      RDFList rdfList = union.as(RDFList.class);
      Iterator<RDFNode> sequence = rdfList.iterator();
      while (sequence.hasNext()) {
        list.add(sequence.next().as(OntResource.class));
      }
      
    }
    return list;
  }

//
//  private void debugUnion(OntProperty p, List<OntResource> list) {
//   System.out.println(p.getLocalName() + " unionOf");
//   for (int i=0; i<list.size(); i++) {
//     OntResource r = list.get(i);
//     System.out.println("  " +(i+1) + ". " + r.getURI());
//     Iterator<Statement> sequence = r.listProperties();
//     while (sequence.hasNext()) {
//       System.out.println("    " + sequence.next().getPredicate().getLocalName());
//     }
//   }
//    
//  }

  private List<OntProperty> listProperties() {
    List<OntProperty> list = new ArrayList<OntProperty>();
    Iterator<OntProperty> sequence = model.listAllOntProperties();
    while (sequence.hasNext()) {
      list.add(sequence.next());
    }
    return list;
  }

  private void addSupertypesAndRestrictions() {
    Collection<Frame> frameList = manager.listFrames();
    
    for (Frame frame : frameList) {
      addSupertypes(frame);
      addRestrictions(frame);
      addSubtypes(frame);
    }
    
  }


  private void addRestrictions(Frame frame) {
    
    List<OntClass> restrictionList = listRestrictions(frame.asOntClass());
    for (OntClass restriction : restrictionList) {
      frame.addRestriction(restriction);
    }
    
  }

  private void addSubtypes(Frame frame) {

    Iterator<OntClass> sequence = frame.asOntClass().listSubClasses(true);
    while (sequence.hasNext()) {
      OntClass type = sequence.next();
      String subURI = type.getURI();
      if (subURI == null) continue;
      Frame subFrame = manager.getFrameByUri(subURI);
      if (subFrame == null) {
        subFrame = manager.getListTypeByListUri(subURI);
      }
      if (subFrame != null) {
        frame.getSubtypeList().add(subFrame);
      } else {
        
        Datatype datatype = manager.getDatatypeByUri(subURI);
        if (datatype != null) {
          frame.addSubdatatype(datatype);
          continue;
        }
        
        
        
        if (isStandard(subURI)) continue;
        logger.warn("Ignoring supertype of " + type.getLocalName() + " because frame not found: " + subURI);
      }
    }
    
  }

  private void addSupertypes(Frame frame) {
    
    Iterator<OntClass> sequence = frame.asOntClass().listSuperClasses(true);
    while (sequence.hasNext()) {
      OntClass type = sequence.next();
      String superURI = type.getURI();
      if (superURI == null) continue;
      Frame superframe = manager.getFrameByUri(superURI);
      if (superframe != null) {
        frame.getSupertypeList().add(superframe);
      } else {
        if (isStandard(superURI)) continue;
        logger.warn("Ignoring supertype of " + type.getLocalName() + " because frame not found: " + superURI);
      }
    }
    
  }

  private void createFrames() {
    
    List<OntClass> list = listNamedClasses();
    
    for (OntClass type : list) {
      if (isProperty(type)) {
        continue;
      }
      Frame frame = createFrame(type);
      if (frame == null) continue;
      setRestCategory(frame);
      setAbstract(frame);
    }
    
    addStandardFrames();
    
  }
  
  private boolean isProperty(OntClass type) {
    return type.canAs(OntProperty.class);
  }

  private List<OntClass> listNamedClasses() {
    List<OntClass> result = new ArrayList<OntClass>();
    
    StmtIterator classes = model.listStatements(null, RDF.type, OWL.Class);
    while (classes.hasNext()) {
      Resource type = classes.next().getSubject();
      String uri = type.getURI();
      if (uri == null) continue;
      if (!isStandard(uri)) {
        result.add(type.as(OntClass.class));
      }
    }
    classes = model.listStatements(null, RDF.type, RDFS.Class);
    while (classes.hasNext()) {
      Resource type = classes.next().getSubject();
      String uri = type.getURI();
      if (uri == null) continue;
      if (!isStandard(uri)) {
        result.add(type.as(OntClass.class));
      }
    }
    
    

    List<Resource> rdfsClassList = model.listResourcesWithProperty(RDF.type, RDFS.Class).toList();
    for (Resource r : rdfsClassList) {
      if (
          r.canAs(OntClass.class) && 
          r.getURI()!=null &&
          !isStandard(r.getURI()) &&
          manager.getDatatypeByUri(r.getURI())==null
      ) {
        result.add(r.as(OntClass.class));
      }
    }
    return result;
  }

  private Frame createFrame(OntClass type) {
    if (manager.isStandardDatatype(type.getNameSpace())) {
      manager.getDatatypeByUri(type.getURI());
      return null;
    }
    if (manager.isStandard(type.getNameSpace())) {
      return null;
    }
    String uri = type.getURI();
    if (uri == null) {
      throw new RuntimeException("URI of type is not defined");
    }
    Frame frame = manager.getFrameByUri(uri);
    if (frame != null) {
      return frame;
    }
    
    
    OntClass elemType = manager.getElementType(type);
    if (elemType != null) {
      String elemURI = elemType.getURI();
      ListType listType = manager.getListTypeByElementUri(elemURI);
      if (listType == null) {
        RdfType elemRdfType = manager.getTypeByURI(elemURI);
        if (elemRdfType == null) {
          elemRdfType = createFrame(elemType);
        }
        listType = new ListType(manager, type, elemRdfType);
        manager.add(listType);
      }
      return null;
    }
    
    List<OntResource> individuals = getEnumeratedIndividuals(type);
    
    if (individuals == null) {
      frame = new Frame(manager, type);
    } else {
    
      Enumeration enumFrame = new Enumeration(manager, type);
      frame = enumFrame;
      for (OntResource item : individuals) {
        NamedIndividual value = new NamedIndividual(item);
        enumFrame.add(value);
      }
    }

    manager.add(frame);
    return frame;
  }

  private List<OntResource> getEnumeratedIndividuals(OntClass type) {
    Resource equivalentClass = type.getPropertyResourceValue(OWL.equivalentClass);
    if (equivalentClass == null) {
      return null;
    }
    Resource oneOf = equivalentClass.getPropertyResourceValue(OWL.oneOf);
    if (oneOf == null) return null;
    
   
    List<RDFNode> nodeList = oneOf.as(RDFList.class).asJavaList();
    
    List<OntResource> result = new ArrayList<OntResource>();
    
    for (RDFNode node : nodeList) {
      result.add(node.as(OntResource.class));
    }
    
    return result;
  }

  private void setAbstract(Frame frame) {
    
    if (frame.getType().hasRDFType(BindVocabulary.AbstractClass, true)) {
      frame.setAbstract(true);
    }
    
  }

  private void addStandardFrames() {
    
    addOwlClass();
    
  }

  private void addOwlClass() {
    String typeURI = OWL2.Class.getURI();
    OntClass type = manager.getOntModel().getOntClass(typeURI);
    if (type == null) {
      OntModel model = ModelFactory.createOntologyModel();
      type = model.createClass(typeURI);
    }
    Frame frame = new Frame(manager, type);
    frame.setCategory(RestCategory.ADDRESSABLE);
   
    
    manager.add(frame);
    
  }

  private void setRestCategory(Frame frame) {
    OntClass type = frame.asOntClass();
    Iterator<Resource> sequence = type.listRDFTypes(false);
    while (sequence.hasNext()) {
      Resource  resource = sequence.next();
      if (BindVocabulary.Addressable.equals(resource)) {
        frame.setCategory(RestCategory.ADDRESSABLE);
      } else if (BindVocabulary.Enum.equals(resource)) {
        frame.setCategory(RestCategory.ENUMERABLE);
      } else if (BindVocabulary.EmbeddableClass.equals(resource)) {
        frame.setCategory(RestCategory.EMBEDDABLE);
      }
    }
    
  }

  public void loadDir(File dir) throws IOException, ParserConfigurationException, SAXException {
    File[] list = dir.listFiles();
    if (list == null) {
      logger.warn("no files found in directory: " + dir.getPath());
      return;
    }
    for (int i=0; i<list.length; i++) {
      loadFile(list[i]);
    }
    buildFrames(model);
  }


  private void loadFile(File file) throws IOException, ParserConfigurationException, SAXException {
    String name = file.getName();
    
    if (name.endsWith(".ttl")) {
      readOntology(file, "TURTLE");
      
    } if (name.endsWith(".xsd")) {
      readXmlSchema(file);
    }
  }


  private void readXmlSchema(File file) throws IOException, ParserConfigurationException, SAXException {
    FileInputStream input = new FileInputStream(file);
    try {
      datatypeReader.read(input);
    } finally {
      input.close();
    }
    
  }

  private void readOntology(File file, String format) throws IOException {
    
    FileInputStream input = new FileInputStream(file);
    try {
      if (model==null) {
        model = ModelFactory.createOntologyModel();
      }
      model.read(input, null, format);
    } finally {
      input.close();
    }
    
  }

}
