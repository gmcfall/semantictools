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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.semantictools.context.renderer.model.Container;
import org.semantictools.context.renderer.model.ContextProperties;
import org.semantictools.context.renderer.model.FrameConstraints;
import org.semantictools.context.renderer.model.JsonContext;
import org.semantictools.context.renderer.model.TermInfo;
import org.semantictools.context.renderer.model.TermInfo.TermCategory;
import org.semantictools.context.renderer.model.TermValue;
import org.semantictools.frame.model.BindVocabulary;
import org.semantictools.frame.model.Datatype;
import org.semantictools.frame.model.DublinCoreTerms;
import org.semantictools.frame.model.Enumeration;
import org.semantictools.frame.model.Field;
import org.semantictools.frame.model.Frame;
import org.semantictools.frame.model.NamedIndividual;
import org.semantictools.frame.model.OntologyInfo;
import org.semantictools.frame.model.RdfType;
import org.semantictools.frame.model.RestCategory;
import org.semantictools.frame.model.VannVocabulary;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntProperty;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.OWL2;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;
import com.hp.hpl.jena.vocabulary.XSD;

public class ContextBuilder {
  private OntModel model;
  private TypeManager typeManager;
  
  private OntProperty suggestedPrefix;
  
  private Set<String> history;

  public ContextBuilder(TypeManager typeManager) {
    this.model = typeManager.getOntModel();
    this.typeManager = typeManager;
    
    suggestedPrefix = model.getOntProperty("http://purl.org/semantictools/v1/vocab/bind#suggestedPrefix");
  }



  public JsonContext createContext(ContextProperties properties) {
    history = new HashSet<String>();
    JsonContext context = new JsonContext();
    context.setContextURI(properties.getContextURI());
    context.setMediaType(properties.getMediaType());
    context.setRootType(properties.getRdfTypeURI());
    
    String typeURI = properties.getRdfTypeURI();
    if (typeURI == null) {
      return null;
    }

    addGraphTypes(context, properties);
    Frame frame = typeManager.getFrameByUri(typeURI);
    if (frame == null) {
      throw new FrameNotFoundException(typeURI);
    }
    addType(properties, context, frame, false);
    context.expand();
    context.invertRewriteRules();
    
    updateFrameConstraints(properties, context);
    
    return context;
  }


  private void addGraphTypes(JsonContext context, ContextProperties properties) {
    List<String> graphTypes = properties.getGraphTypes();
    if (!graphTypes.isEmpty()) {
      for (String uri : graphTypes) {
        Frame frame = typeManager.getFrameByUri(uri);
        if (frame == null) {
          throw new FrameNotFoundException(uri);
        }
        addType(properties, context, frame, false);
      }
    }
    
  }



  private void updateFrameConstraints(ContextProperties properties,  JsonContext context) {
    
    List<FrameConstraints> list = properties.listFrameConstraints();
    for (FrameConstraints c : list) {
      String name = c.getClassURI();
      TermInfo term = context.getTermInfoByShortName(name);
      if (term != null) {
        String uri = term.getIri();
        uri = context.rewrite(uri);
        c.setClassURI(uri);
        properties.addFrameConstraints(c);
      }
    }
    
    
  }



  private boolean isStandard(String uri) {
    return uri != null && uri.startsWith("http://www.w3.org/2001/XMLSchema#");
   
//    return uri.startsWith("http://www.w3.org/2001/XMLSchema#") ||
//        uri.startsWith("http://www.w3.org/2002/07/owl#");
  }

  private void addType(ContextProperties properties, JsonContext context, RdfType rdfType, boolean stubbed) {

    String typeURI = rdfType.getUri();
    if (properties.getExcludedTypes().contains(typeURI)) {
      return;
    }
    if (rdfType.canAsListType() && embedListMembers(properties)) {
//      rdfType = rdfType.asListType().getElementType();
      addType(properties, context, rdfType.asListType().getElementType(), stubbed);
    }
    
        
    if (isStandard(typeURI)) return;

    String localName = rdfType.getLocalName();
    
    String prefix = getPrefix(context, rdfType.getNamespace());
    
    
    String iri = (prefix == null) ? typeURI : prefix + ":" + localName;

    if (!context.containsTerm(localName)) {
      context.add(localName, iri).setCategory(TermCategory.TYPE);
    }
    

    if (stubbed || history.contains(typeURI)) return;
    
    Frame frame = typeManager.getFrameByUri(typeURI);
    if (frame == null) {
      // Since the frame was not found, the type is probably a Datatype.
      // Let's confirm that the datatype exists.
      
      Datatype datatype = typeManager.getDatatypeByUri(typeURI);
      if (datatype == null) {
        throw new FrameNotFoundException(typeURI);
      }
      return;
    }
    
    history.add(typeURI);
    
    List<Field> fieldList = frame.listAllFields();
    for (Field field : fieldList) {
      
      addField(properties, context, field, frame);
    }
    
    addSubtypes(properties, context, frame);
    
  }


  /**
   * Returns true if rdf:member is not represented as an idref.
   */
  private boolean embedListMembers(ContextProperties properties) {
    properties.isIdRef("http://www.w3.org/2000/01/rdf-schema#member");
    return false;
  }



  private void addSubtypes(ContextProperties properties, JsonContext context, Frame frame) {
    
    FrameConstraints c = properties.getFrameConstraints(frame.getLocalName());
    
    List<Frame> list = frame.listAllSubtypes();
    for (Frame sub : list) {
      if (c != null) {
        // If the supertype has constraints, then they must bubble down to 
        // subtypes.  This means that the constraints cannot be null on the subtypes...
        FrameConstraints cc = properties.getFrameConstraints(sub.getLocalName());
        if (cc == null) {
          cc = new FrameConstraints(sub.getLocalName());
          properties.addFrameConstraints(cc);
        }
        cc.copyAll(c);
      } 
      
      addType(properties, context, sub, false);
    }
    
  }



  private void addField(ContextProperties properties, JsonContext context, Field field, Frame declaringFrame) {
    OntProperty property = field.getProperty();
    
    if (!isIncluded(field, properties, declaringFrame)) return;
    
    
    String localName = property.getLocalName();
    String propertyURI = property.getURI();
    String iriValue = iriRef(context, property.getNameSpace(), property.getLocalName(), propertyURI);
    TermInfo info = new TermInfo(localName);
    info.setCategory(TermCategory.PROPERTY);
    
   
    RdfType rdfType = field.getRdfType();
    TermValue value = null;
    if (rdfType == null) {
      throw new RuntimeException("RDF type not defined for property: " + field.getLocalName());
    }
    if (rdfType.canAsListType()) {
      value = new TermValue();
      value.setContainer(Container.LIST);
      
    } 
//    if (rdfType.canAsListType()) {
//      rdfType = rdfType.asListType().getElementType();
//    }
    boolean uriRef = properties.isIdRef(property.getURI());
    
    boolean enumerable = 
        rdfType != null && 
        rdfType.canAsFrame() && 
        rdfType.asFrame().getCategory() == RestCategory.ENUMERABLE;
    
    boolean stubbed = false;
    // stubbed=true means that the object is stubbed out because it is coerced as an IRI reference only,
    //              or it is an enumerable type.
    

    if (enumerable || uriRef ) {
      if (value == null) {
        value = new TermValue();
      }
      value.setType("@id");
      stubbed = true;
      
    } 
    if (properties.getOptionalProperties().contains(property.getURI())) {
      if (value == null) {
        value = new TermValue();
      }
      value.setMinCardinality(0);
    }
    
    if (enumerable) {
      addIndividuals(context, rdfType.asFrame());
    }
    
    if (value != null) {
      value.setId(iriValue);
      info.setObjectValue(value);
    } else {
      info.setIriValue(iriValue);
    }
    
    addNamespace(context, field, property);
    
   

    if (!context.containsTerm(localName)) {
      context.add(info);
    }
    
    
    if (value != null && value.getContainer() == Container.LIST) {
      addType(properties, context, rdfType, stubbed);
      
    } else if (rdfType.canAsFrame()) {
      addType(properties, context, rdfType, stubbed);
      
    } else {
      
      addNamespace(context, rdfType);
      
      if (!properties.getExpandedValues().contains(propertyURI)) {
        String propertyTypeURI = rdfType.getUri();
        
        String typeIRI = iriRef(context, rdfType.getNamespace(), rdfType.getLocalName(), propertyTypeURI);
        if (value == null) {
          value = new TermValue();
        }
        value.setId(iriValue);
        value.setType(typeIRI);
        info.setObjectValue(value);
        addNamespace(context, rdfType);
      } else {
        System.out.println("expanded value: " + info.getTermName());
      }
      
      
    }
    
    coerceType(info, properties);
    
  }
  
  
  private void coerceType(TermInfo info, ContextProperties properties) {
    
    String key = info.getTermName() + ".@type";
    String typeURI = properties.getProperty(key);
    
    if (typeURI == null) return;
    
    
    
  }



  private void addNamespace(JsonContext context, RdfType rdfType) {
    
    String namespace = rdfType.getNamespace();
    TermInfo term = context.getTermInfoByURI(namespace);
    if (term != null) return;
    
    OntologyInfo info = typeManager.getOntologyByNamespaceUri(namespace);
   
    
    String prefix = info==null ?
        TypeManager.getDefaultNamespacePrefix(namespace) :
        info.getPrefix();
    
    term = new TermInfo(prefix);
    term.setIriValue(namespace);
    term.setCategory(TermCategory.NAMESPACE);
    context.add(term);
    
    
    
  }



  private boolean isIncluded(Field field, ContextProperties properties, Frame declaringFrame) {
    
    String fieldType = field.getRdfType().getUri();
    if (properties.getExcludedTypes().contains(fieldType)) return false;
    
    FrameConstraints constraints = properties.getFrameConstraints(declaringFrame.getLocalName());
   
    return (constraints == null) || constraints.isIncludedProperty(field.getURI());
  }



  private void addIndividuals(JsonContext context, Frame frame) {
    
    if (frame.canAsEnumeration()) {
      addIndividuals(context, frame.asEnumeration());
      
    } else {
      List<NamedIndividual> list = frame.listInstances(false);
      for (NamedIndividual n : list) {
        String name = n.getLocalName();
        String uri = n.getUri();
        TermInfo term = context.getTermInfoByURI(uri);
        if (term == null) {
          term = new TermInfo(name);
          term.setIriValue(uri);
          context.add(term);
        }
      }
    }
    
  }
  
  private void addIndividuals(JsonContext context, Enumeration e) {
    List<NamedIndividual> list = e.getIndividualList();
    for (NamedIndividual n : list) {
      String name = n.getLocalName();
      TermInfo term = new TermInfo(name);
      term.setIriValue(n.getUri());
      context.add(term);
    }
  }



  private void addNamespace(JsonContext context, Field field, OntProperty property) {

    String namespace = field.getProperty().getNameSpace();
    String prefix = getPrefix(context, namespace);
    
    TermInfo term = context.getTermInfoByShortName(prefix);
    if (term != null) return;
    term = new TermInfo(prefix);
    term.setCategory(TermCategory.NAMESPACE);
    term.setIriValue(namespace);
    
    context.add(term);
  }



  private String iriRef(JsonContext context, String namespaceURI, String localName, String uri) {
    if (typeManager.isStandardLiteralType(uri)) {
      namespaceURI = XSD.getURI();
      localName = XSD.xstring.getLocalName();
    }
    String prefix = getPrefix(context, namespaceURI);
    if (prefix == null) {
      return uri;
    }
    return prefix + ":" + localName;
  }

  private OntologyInfo createOntologyInfo(String namespaceURI) {
    Model tmp = ModelFactory.createDefaultModel();
    Resource namespaceResource = tmp.createResource(namespaceURI);
    String ontologyURI = null;
    String prefix = null;
    String label = null;
    
    // Check to see if the namespaceURI is listed as the subject in any statements
    StmtIterator sequence = model.listStatements(namespaceResource, null, (RDFNode) null);
    while (sequence.hasNext()) {
      Statement s = sequence.next();
      if (s.getPredicate().equals(RDF.type) && s.getObject().equals(OWL.Ontology)) {
        ontologyURI = namespaceURI;
      }
      if (s.getPredicate().equals(RDFS.label) ||
          s.getPredicate().equals(DublinCoreTerms.title)) {
        label = s.getObject().asLiteral().getString();
      }
      if (s.getPredicate().equals(BindVocabulary.suggestedPrefix) || 
          s.getPredicate().equals(VannVocabulary.preferredNamespacePrefix)) {
        prefix = s.getObject().asLiteral().getString();
      }
      if ((ontologyURI != null) && (prefix!=null) ) {
        break;
      }
    }
    
    if (ontologyURI == null) {
      // Check to see if the namespaceURI is referenced via the preferredNamespacePrefix
      Literal object = tmp.createLiteral(namespaceURI);
      sequence = model.listStatements(null, null, object);
      if (sequence.hasNext()) {
        Statement s = sequence.next();
        Resource subject = s.getSubject();
        ontologyURI = subject.getURI();
       
        s = subject.getProperty(VannVocabulary.preferredNamespacePrefix);
        if (s != null) {
          prefix = s.getObject().asLiteral().getString();
        } else {
          s = subject.getProperty(BindVocabulary.suggestedPrefix);
          if (s != null) {
            prefix =  s.getObject().asLiteral().getString();
          }
        }
        
        if (prefix != null) {
          s = subject.getProperty(DublinCoreTerms.title);
          if (s == null) {
            s = subject.getProperty(RDFS.label);
          }
          if (s == null) {
            s = subject.getProperty(DublinCoreElements.title);
          }
          if (s != null) {
            label = s.getObject().asLiteral().getString();
          }
          
        }
        
      }
    }
    
   OntologyInfo info = new OntologyInfo();
   
   info.setNamespaceUri(namespaceURI);
   info.setOntologyURI(ontologyURI);
   info.setPrefix(prefix);
   info.setLabel(label);
    
    return info;
  }

  private String getPrefix(JsonContext context, String namespaceURI) {

    String prefix = null;
    
    // Special handling for OWL namespace.
    if (OWL2.NS.equals(namespaceURI)) {
      OntologyInfo info = typeManager.getOntologyByNamespaceUri(namespaceURI);
      prefix = info.getPrefix();
                
    } else {
      
      OntologyInfo info = typeManager.getOntologyByNamespaceUri(namespaceURI);
      if (info == null) {
      
        info = createOntologyInfo(namespaceURI);
        typeManager.add(info);
      } else {
        prefix = info.getPrefix();
      }
    }
    if (prefix == null && namespaceURI.equals("http://www.w3.org/1999/02/22-rdf-syntax-ns#")) {
      prefix = "rdf";
    }

    if (prefix !=null && !context.containsTerm(prefix)) {
      String uri = namespaceURI;
      context.add(prefix, uri).setCategory(TermCategory.NAMESPACE);
    }
    
    
    return prefix;
  }

}
