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

import static org.semantictools.frame.model.OntologyType.RDF;
import static org.semantictools.frame.model.OntologyType.XSD;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;

import org.semantictools.frame.model.BindVocabulary;
import org.semantictools.frame.model.ContainerRestriction;
import org.semantictools.frame.model.Datatype;
import org.semantictools.frame.model.DublinCoreTerms;
import org.semantictools.frame.model.Field;
import org.semantictools.frame.model.Frame;
import org.semantictools.frame.model.ListType;
import org.semantictools.frame.model.OntologyInfo;
import org.semantictools.frame.model.OntologyType;
import org.semantictools.frame.model.RdfType;
import org.semantictools.frame.model.Uri;
import org.semantictools.frame.model.VannVocabulary;
import org.xml.sax.SAXException;

import com.hp.hpl.jena.datatypes.xsd.impl.XMLLiteralType;
import com.hp.hpl.jena.ontology.AllValuesFromRestriction;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.Ontology;
import com.hp.hpl.jena.ontology.Restriction;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.vocabulary.OWL2;
import com.hp.hpl.jena.vocabulary.RDFS;

public class TypeManager {
  
  private static OntologyInfo XSD_INFO = new OntologyInfo("http://www.w3.org/2001/XMLSchema#", "xs", "http://www.w3.org/2001/XMLSchema#", XSD);
  private static OntologyInfo OWL_INFO = new OntologyInfo("http://www.w3.org/2002/07/owl", "owl", "http://www.w3.org/2002/07/owl#", RDF);
  private static OntologyInfo RDFS_INFO = new OntologyInfo("http://www.w3.org/2000/01/rdf-schema#", "rdfs", "http://www.w3.org/2000/01/rdf-schema#", RDF);
  private static OntologyInfo XDT_INFO = new OntologyInfo("http://www.w3.org/2004/10/xpath-datatypes#", "xdt", "http://www.w3.org/2004/10/xpath-datatypes#", XSD);
  private static OntologyInfo BIND = new OntologyInfo("http://purl.org/semantictools/v1/vocab/bind#", "bind", "http://purl.org/semantictools/v1/vocab/bind#", RDF);
  
  private Map<String, Frame> uri2Frame = new HashMap<String, Frame>();
  private Map<String, Datatype> uri2Datatype = new HashMap<String, Datatype>();
  private Map<String, OntologyInfo> uri2Ontology = new HashMap<String, OntologyInfo>();
  private Map<String, ListType> elemURI2ListType = new HashMap<String, ListType>();
  private Map<String, ListType> listURI2ListType = new HashMap<String, ListType>();
  private Set<String> standard = new HashSet<String>();
  private Set<String> standardDatatype = new HashSet<String>();
  private Set<String> standardLiteralType = new HashSet<String>();
  
  private OntModel ontModel;
  
  public TypeManager() {
    ontModel = ModelFactory.createOntologyModel();
    addStandard(XSD_INFO);
    addStandard(OWL_INFO);
    addStandard(RDFS_INFO);
    addStandard(XDT_INFO);
    addStandard(BIND);
    addDayTimeDuration();
    addOwlThing();
    addResource();
    addStandardLiteralTypes();
  }
  
  
  private void addStandardLiteralTypes() {
    standardLiteralType.add(RDFS.Literal.getURI());
    standardLiteralType.add(RDFS.Datatype.getURI());
    standardLiteralType.add(XMLLiteralType.theXMLLiteralType.getURI());
  }
  
  public boolean isStandardLiteralType(String typeURI) {
    return standardLiteralType.contains(typeURI);
  }

  private void addDayTimeDuration() {
   
    Datatype duration = getDatatypeByUri(com.hp.hpl.jena.vocabulary.XSD.duration.getURI());
    Datatype dayTimeDuration = new Datatype();
    dayTimeDuration.setLocalName("dayTimeDuration");
    dayTimeDuration.setUri(XDT_INFO.getNamespaceUri() + "dayTimeDuration");
    dayTimeDuration.setBase(duration);
    add(dayTimeDuration);
  }
  
  public void analyzeOntologies() {
    markOntologiesWithClasses(ontModel.listStatements(null, com.hp.hpl.jena.vocabulary.RDF.type,  com.hp.hpl.jena.vocabulary.RDFS.Class));
    markOntologiesWithClasses(ontModel.listStatements(null, com.hp.hpl.jena.vocabulary.RDF.type,  com.hp.hpl.jena.vocabulary.OWL.Class));
    
//    List<OntClass> classList = ontModel.listClasses().toList();
//    for (OntClass type : classList) {
//      String namespace = type.getNameSpace();
//      OntologyInfo info = getOntologyByNamespaceUri(namespace);
//      if (info != null) {
//        info.setHasClasses(true);
//      }
//    }
  }
  
  private void markOntologiesWithClasses(StmtIterator sequence) {
    while (sequence.hasNext()) {
      String uri = sequence.next().getSubject().getNameSpace();
      if (uri != null) {
        OntologyInfo info = getOntologyByNamespaceUri(uri);
        if (info != null) {
          info.setHasClasses(true);
        }
      }
      
    }
  }

  
  public String getXsdBaseURI(Datatype type) {
    if (com.hp.hpl.jena.vocabulary.RDFS.Literal.getURI().equals(type.getUri())) {
      return com.hp.hpl.jena.vocabulary.XSD.xstring.getURI();
    }
    String xsdURI = com.hp.hpl.jena.vocabulary.XSD.getURI();
    while (type != null) {
      if (type.getUri().startsWith(xsdURI)) {
        return type.getUri();
      }
      type = type.getBase();
    } 
    return null;
  }

  public boolean isStandard(String ontologyURI) {
    return standard.contains(ontologyURI);
  }
  
  public boolean isStandardDatatype(String namespaceURI) {
    return standardDatatype.contains(namespaceURI);
  }
  
  private void addStandard(OntologyInfo info) {
    add(info);
    if (info.getType() == OntologyType.XSD) {
      standardDatatype.add(info.getNamespaceUri());
    }
    standard.add(info.getNamespaceUri());
  }
  
  private void addResource() {
    OntClass resourceClass = ontModel.createClass(com.hp.hpl.jena.vocabulary.RDFS.Resource.getURI());
    Frame resourceFrame = new Frame(this, resourceClass);
    add(resourceFrame);
    
  }

  private void addOwlThing() {
    OntClass thingClass = ontModel.createClass(OWL2.Thing.getURI());
    Frame thingFrame = new Frame(this, thingClass);
    add(thingFrame);
  }

  public OntModel getOntModel() {
    return ontModel;
  }




  public void loadDir(File dir) throws IOException, ParserConfigurationException, SAXException {
   
    FrameBuilder builder = new FrameBuilder(this);
    builder.setOntModel(ontModel);
    builder.loadDir(dir);
  }
  
  public void processOntologies() {
    List<Ontology> list = ontModel.listOntologies().toList();
    for (Ontology ontology : list) {

      String prefix = null;
      
      RDFNode prefixNode = ontology.getPropertyValue(VannVocabulary.preferredNamespacePrefix);
      
      if (prefixNode != null) {
        prefix = prefixNode.asLiteral().getString();
      } else {
        prefixNode = ontology.getPropertyValue(BindVocabulary.suggestedPrefix);
        prefix = (prefixNode==null) ? null : prefixNode.asLiteral().getString();
      }
      
      /**
       * We only manage ontologies that have an explicitly declared prefix.
       */
      if (prefix == null) {
        continue;
      }
      
      RDFNode namespaceNode = ontology.getPropertyValue(VannVocabulary.preferredNamespaceUri);
      String namespaceURI = (namespaceNode == null) ? ontology.getURI() : namespaceNode.asLiteral().getString();
      String label = ontology.getLabel(null);
      String ontologyURI = ontology.getURI();
      
      if (label == null) {
        RDFNode labelNode = ontology.getPropertyValue(DublinCoreTerms.title);
        if (labelNode == null) {
          labelNode = ontology.getPropertyValue(DublinCoreElements.title);
        }
        if (labelNode != null) {
          label = labelNode.asLiteral().getString();
        }
      }
      
      
      OntologyInfo info = new OntologyInfo();
      info.setOntologyURI(ontologyURI);
      info.setPrefix(prefix);
      info.setNamespaceUri(namespaceURI);
      info.setLabel(label);
      add(info);
    }
    
    analyzeContainers();
  }
  
  
  private void analyzeContainers() {
   
    Collection<Frame> frameList = listFrames();
    for (Frame frame : frameList) {
      ContainerRestriction r = frame.getContainerRestriction();
      if (r != null) {
        Uri membershipSubject = r.getMembershipSubject();
        Uri membershipPredicate = r.getMembershipPredicate();
        Frame subject = getFrameByUri(membershipSubject.stringValue());
        if (subject != null) {
          Field field = subject.getFieldByURI(membershipPredicate.stringValue());
          if (field != null) {
            RdfType type = field.getRdfType();
            if (type.canAsFrame()) {
              Frame target = type.asFrame();
              target.addContainer(frame);
            }
          }
        }
      }
    }
    
  }


  public String getQName(String uri) {
    int hash = uri.lastIndexOf('#');
    int slash = uri.lastIndexOf('/');
    int delim = Math.max(hash, slash);
    String namespaceURI = uri.substring(0, delim+1);
    OntologyInfo info = getOntologyByNamespaceUri(namespaceURI);
    if (info == null) return null;
    
    String localName = uri.substring(delim+1);
    return info.getPrefix() + ":" + localName;
  }
  
  public static String getLocalName(String uri) {

    int hash = uri.lastIndexOf('#');
    int slash = uri.lastIndexOf('/');
    int delim = Math.max(hash, slash);
    
    String localName = uri.substring(delim+1);
    return localName;
  }
  
  public static String getDefaultNamespacePrefix(String uri) {
    int end = uri.length()-1;
    int start = uri.lastIndexOf('/', end);
    return uri.substring(start+1, end);
  }
  
  public static String getNamespace(String uri) {
    int hash = uri.lastIndexOf('#');
    int slash = uri.lastIndexOf('/');
    int delim = Math.max(hash, slash);
    String namespaceURI = uri.substring(0, delim+1);
    return namespaceURI;
  }
  
  /**
   * Returns the OntClass that specifies the type of elements contained within a 
   * List of type listType.  If the given listType is not a subclass of rdf:List,
   * then this method returns null.
   * @param listType  The owl:Class that is to be analyzed as a List.
   * @return  The OntClass for the elements within the specified type of list, or null
   * if the listType is not a subclass of rdf:List.
   */
  public OntClass getElementType(OntClass listType) {
    List<OntClass> superList = listType.listSuperClasses(true).toList();
    for (OntClass superType : superList) {
      if (superType.isRestriction()) {
        Restriction restriction = superType.asRestriction();
        if (restriction.isAllValuesFromRestriction() && restriction.onProperty(com.hp.hpl.jena.vocabulary.RDF.first)) {
          AllValuesFromRestriction allValues = restriction.asAllValuesFromRestriction();
          return allValues.getAllValuesFrom().as(OntClass.class);
        }
      }
    }
    return null;
  }
  

  public void add(ListType listType) {
    add((Frame) listType);
    listURI2ListType.put(listType.getUri(), listType);
    elemURI2ListType.put(listType.getElementType().getUri(), listType);
  }
  
  public ListType getListTypeByElementUri(String uri) {
    return elemURI2ListType.get(uri);
  }
  
  public ListType getListTypeByListUri(String uri) {
    return listURI2ListType.get(uri);
  }
  
  public void add(OntologyInfo info) {
    // XSD Ontology Type takes precedence 
    OntologyInfo oldInfo = uri2Ontology.get(info.getNamespaceUri());
    if (oldInfo != null) {
      OntologyType type = 
          (oldInfo.getType() == OntologyType.XSD || info.getType()==OntologyType.XSD) ? OntologyType.XSD :
          OntologyType.RDF;
      
      info.setType(type);
      oldInfo.setType(type);
      
    } else {
      uri2Ontology.put(info.getNamespaceUri(), info);
    }
  }
  
  public OntologyInfo getOntologyByNamespaceUri(String uri) {
    return uri2Ontology.get(uri);
  }
  
  public Collection<OntologyInfo> listOntologies() {
    return uri2Ontology.values();
  }
  
  public void add(Frame frame) {
    uri2Frame.put(frame.getUri(), frame);
  }
  
  public void add(Datatype datatype) {
    uri2Datatype.put(datatype.getUri(), datatype);
  }
  
  
  public Datatype getDatatypeByUri(String uri) {
    Datatype type = uri2Datatype.get(uri);
    if (type == null) {
      String namespace = getNamespace(uri);
      if (type == null && (  isStandardDatatype(namespace) || uri.startsWith(RDFS_INFO.getNamespaceUri())  )) {
        type = new Datatype();
        type.setUri(uri);
        type.setLocalName(getLocalName(uri));
        if (!namespace.equals(XSD_INFO) && !namespace.equals(RDF)) {
          OntClass ontClass = ontModel.getOntClass(uri);
          if (ontClass != null) {
            List<OntClass> superList = ontClass.listSuperClasses().toList();
            for (OntClass superClass : superList) {
              String superNamespace = superClass.getNameSpace();
              if (isStandard(superNamespace) && !isStandardDatatype(superNamespace)) {
                continue;
              }
              if (superClass != null) {
                Datatype base = getDatatypeByUri(superClass.getURI());
                if (base != null) {
                  type.setBase(base);
                }
              }
            }
          }
        }
       
        
        add(type);
      }
    }
    
    return type;
  }
  
  public Collection<Datatype> listDatatypes() {
    return uri2Datatype.values();
  }
  
  public Frame getFrameByUri(String uri) {
    return uri2Frame.get(uri);
  }
  
  public RdfType getTypeByURI(String uri) {
    RdfType type = getFrameByUri(uri);
    if (type == null) {
      type = getDatatypeByUri(uri);
    }
    return type;
  }
  
  public Collection<Frame> listFrames() {
    return uri2Frame.values();
  }
  
  public Collection<ListType> listListTypes() {
    return listURI2ListType.values();
  }
  
  public List<Frame> listFramesInOntology(String ontologyURI) {
    List<Frame> result = new ArrayList<Frame>();
    Iterator<Frame> sequence = listFrames().iterator();
    while (sequence.hasNext()) {
      Frame frame = sequence.next();
      if (frame.getUri().startsWith(ontologyURI)) {
        result.add(frame);
      }
    }
    return result;
  }

}
