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
package org.semantictools.jsonld.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.semantictools.jsonld.LdAsset;
import org.semantictools.jsonld.LdAssetManager;
import org.semantictools.jsonld.LdClass;
import org.semantictools.jsonld.LdContext;
import org.semantictools.jsonld.LdContextEnhancer;
import org.semantictools.jsonld.LdProperty;
import org.semantictools.jsonld.LdQualifiedRestriction;
import org.semantictools.jsonld.LdRestriction;
import org.semantictools.jsonld.LdTerm;
import org.semantictools.jsonld.io.LdDatatypeReader;

import com.hp.hpl.jena.ontology.ConversionException;
import com.hp.hpl.jena.ontology.MaxCardinalityRestriction;
import com.hp.hpl.jena.ontology.MinCardinalityRestriction;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntProperty;
import com.hp.hpl.jena.ontology.OntResource;
import com.hp.hpl.jena.ontology.Restriction;
import com.hp.hpl.jena.ontology.UnionClass;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFList;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.OWL2;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;
import com.hp.hpl.jena.vocabulary.XSD;

public class LdContextEnhancerImpl implements LdContextEnhancer {

  private LdAssetManager assetManager;

  public LdContextEnhancerImpl(LdAssetManager assetManager) {
    this.assetManager = assetManager;
  }
  
  @Override
  public void enhance(LdContext context) throws LdContextEnhanceException {
    Delegate delegate = new Delegate();
    delegate.enhance(context);
  }
  
  private boolean isStandardNamespace(String namespaceURI) {
    return 
        OWL.NS.equals(namespaceURI) || 
        RDF.getURI().equals(namespaceURI) ||
        XSD.getURI().equals(namespaceURI) ||
        RDFS.getURI().equals(namespaceURI);
  }
  
  /**
   * A delegate that actually performs the work of enhancement.
   * This delegate is not threadsafe. Thus, the parent class
   * creates a new Delegate for each request.
   * 
   * @author Greg McFall
   *
   */
  class Delegate {
  
    private Set<String> namespaceSet;
    private List<LdAsset> datatypeList;
    private Map<String, LdTerm> uri2Term;
    private OntModel ontModel;
    
    private StringBuilder errorBuilder;
    private LdContext context;
    private List<LdProperty> functionalPropertyList;
    
  
    public void enhance(LdContext context) throws LdContextEnhanceException {
     
      try {
        this.context = context;
        errorBuilder = new StringBuilder();
        collectNamespaces(context);
        if (uri2Term == null) return;
        
//        buildOntModel();
        computeTermTypes();
        buildRestrictions();
        buildHierarchy();
        buildDatatypes();
        addFunctionalPropertyRestrictions();
        
//        if (errorBuilder.length()>0) {
//          throw new LdContextEnhanceException(errorBuilder.toString());
//        }
        
      } finally {
        release();
      }
    }
    
    
  
    private void addFunctionalPropertyRestrictions() {
      if (functionalPropertyList != null) {
        for (LdProperty p : functionalPropertyList) {
          addFunctionalPropertyRestrictions(p);
        }
      }
      
    }
  
    private void addFunctionalPropertyRestrictions(LdProperty p) {
      String propertyURI = p.getURI();
      List<String> domainList = p.getDomain();
      if (domainList != null) {
        for (String domainURI : domainList) {
          
          LdClass rdfClass = context.getClass(domainURI);
          if (rdfClass == null) continue;
          LdRestriction r = rdfClass.findRestrictionByPropertyURI(propertyURI);
          
          if (r == null) {
            r = new LdRestriction();
            r.setPropertyURI(propertyURI);
            rdfClass.add(r);
          }
  //        r.setMaxCardinality(1);
        }
      }
      
    }
  
    private void buildDatatypes() {
      if (datatypeList != null) {
        LdDatatypeReader reader = new LdDatatypeReader(context);
        for (LdAsset ns : datatypeList) {
          try {
            reader.read(ns.getReader());
          } catch (Exception e) {
            // TODO: add an error to LdValidationReport.
          }
        }
      }
      
    }
  
    private void computeTermTypes() {
      List<LdTerm> termList = new ArrayList<LdTerm>(context.listTerms());
      for (LdTerm term : termList) {
          computeType(term);
      }
      
    }
  
    private void computeType(LdTerm term) {
      String propertyURI = term.getIRI();
      OntResource resource = ontModel.getOntResource(propertyURI);
      
      if (resource != null && resource.isProperty()) {
  
        OntProperty property = resource.asProperty();
        
        if (term.getTypeIRI() == null) {
          String rangeURI = null;
          OntResource range = property.getRange();
          if (range == null) {
            rangeURI = OWL.Thing.getURI();
          } else {
            rangeURI = range.getURI();
          }
          
          term.setTypeIRI(rangeURI);
        }
        String typeIRI = term.getTypeIRI();
        if (typeIRI != null) {
          // Ensure that a term exists for this datatype.
          // This term will be populated when the XSD schemas are parsed.
          
          context.ensureTerm(typeIRI);
        }
        
        buildDomain(term, property);
      }
      
    }
  
    private void buildDomain(LdTerm term, OntProperty property) {
      List<? extends OntResource> domainList = property.listDomain().toList();
      for (OntResource resource : domainList) {
        if (resource.isClass()) {
          addDomain(property, term, resource.asClass());
        }
      }
      
    }
    
    private void addDomain(OntProperty ontProperty, LdTerm term, OntClass type) {
      String uri = type.getURI();
      if (uri != null) {
        LdProperty property = term.ensureProperty();
        property.addDomain(uri);
        if (ontProperty.isFunctionalProperty()) {
          addFunctionalProperty(property);
        }
        
        
      } 
      if (type.isUnionClass()) {
       try {

//         List<? extends OntClass> unionList = type.asUnionClass().listOperands().toList();
         List<? extends OntClass> unionList = listUnion(type);
          for (OntClass op : unionList) {
            addDomain(ontProperty, term, op);
          }
       } catch (Throwable oops) {
         System.out.println("Property: " + ontProperty.getURI());
       }
      }
    }
    
    private List<OntClass> listUnion(OntClass type) {
      List<OntClass> list = new ArrayList<OntClass>();
      RDFNode node = type.getPropertyValue(OWL.unionOf);
      if (node !=null && node.canAs(RDFList.class)) {
        RDFList rdfList = node.as(RDFList.class);
        List<RDFNode> nodeList = rdfList.asJavaList();
        for (RDFNode element : nodeList) {
          if (element.canAs(OntClass.class)) {
            list.add(element.as(OntClass.class));
          } else {
            if (element.isURIResource()) {
              ontModel.add(element.asResource(), RDF.type, OWL.Class);
              list.add(element.as(OntClass.class));
            }
          }
        }
      }
      
      return list;
    }
  
    private void buildHierarchy() {
      HashSet<String> memory = new HashSet<String>();
      
      List<LdClass> drList = context.listClasses();
      for (LdClass dr : drList) {
        OntClass ontClass = ontModel.getOntClass(dr.getURI());
        
        buildHierarchy(memory, ontClass, dr);
      }
      
    }
  
    private void buildHierarchy(Set<String> memory, OntClass ontClass,  LdClass ldClass) {
      
      String uri = ldClass.getURI();
      if (memory.contains(uri)) return;
      memory.add(uri);
      
      buildSubclasses(memory, ontClass, ldClass);
      buildSuperclasses(memory, ontClass, ldClass);
      
      
      
    }
  
    private void buildSuperclasses(Set<String> memory, OntClass ontClass, LdClass subClass) {
  
      List<OntClass> superList = ontClass.listSuperClasses(true).toList();
      for (OntClass ontSuperClass : superList) {
        if (ontSuperClass.equals(RDFS.Resource)) continue;
        addSuperclass(memory, subClass, ontSuperClass);
      }
      
    }
  
    private void addSuperclass(Set<String> memory, LdClass subClass, OntClass ontSuperClass) {
  
      String superURI = ontSuperClass.getURI();
      if (superURI == null) return;
      
      LdTerm term = context.ensureTerm(superURI);
          
      LdClass superClass = term.getRdfClass();
      if (superClass == null) {
        superClass = new LdClass(superURI);
        term.setRdfClass(superClass);
      }
      subClass.addSupertype(superClass);
      buildHierarchy(memory, ontModel.getOntClass(superURI), superClass);
      
    }
  
    private void buildSubclasses(Set<String> memory, OntClass ontClass, LdClass superClass) {
      
      
      List<OntClass> sublist = ontClass.listSubClasses(true).toList();
      for (OntClass ontSubdomain : sublist) {
        addSubclass(memory, superClass, ontSubdomain);
      }
      
    }
  
    private void addSubclass(Set<String> memory, LdClass superDomain, OntClass ontSubdomain) {
      
      String subdomainURI = ontSubdomain.getURI();
      if (subdomainURI == null) return;
      
      LdTerm term = context.ensureTerm(subdomainURI);
          
      LdClass subDomain = term.getRdfClass();
      if (subDomain == null) {
        subDomain = new LdClass(subdomainURI);
        term.setRdfClass(subDomain);
        
      } 
      subDomain.addSupertype(superDomain);
      buildHierarchy(memory, ontModel.getOntClass(subdomainURI), subDomain);
      
    }
  
    /**
     * Release internal data structures that were used during enhancement so that
     * they may be reclaimed by the garbage collector.
     */
    private void release() {
      ontModel = null;
      namespaceSet = null;
      uri2Term = null;
      errorBuilder = null;
      context = null;
      functionalPropertyList = null;
    }
  
    private void buildRestrictions() {
      List<OntClass> classList = ontModel.listClasses().toList();
      for (OntClass domain : classList) {
        String uri = domain.getURI();
        LdTerm term = context.getTerm(uri);
        if (term == null) continue;
        buildRestrictionsForDomain(domain);
      }
      
    }
    
    private void addFunctionalProperty(LdProperty property) {
      if (functionalPropertyList == null) {
        functionalPropertyList = new ArrayList<LdProperty>();
      }
      functionalPropertyList.add(property);
    }
  
  
  
    private void buildRestrictionsForDomain(OntClass domain) {
      String uri = domain.getURI();
      if (uri == null) return;
      LdClass rdfClass = context.ensureClass(uri);
      List<OntClass> superList = domain.listSuperClasses(true).toList();
      for (OntClass superType : superList) {
        if (superType.canAs(Restriction.class)) {
          buildRestriction(rdfClass, superType.asRestriction());
        }
      }
      
    }
  
    private void buildRestriction(LdClass rdfClass, Restriction restriction) {
      try {
//        String propertyURI = restriction.getOnProperty().getURI();
        String propertyURI = getOnPropertyURI(restriction);
        
        // We are only interested in restrictions on properties that
        // are declared in the JSON-LD context.  Bail out if a term
        // for the specified property is not found.
        //
        LdTerm propertyTerm = getTerm(propertyURI);
        if (propertyTerm == null) return;
        
        
        Integer minCardinality = minCardinality(restriction);
        Integer maxCardinality = maxCardinality(restriction);
        String rangeURI = onClass(restriction);
        
        LdRestriction r = new LdRestriction();
        r.setDomain(rdfClass);
        r.setPropertyURI(propertyURI);
        r.setMaxCardinality(maxCardinality);
        r.setMinCardinality(minCardinality);
        if (rangeURI != null) {
          addQualifiedRestriction(restriction, r, rangeURI);
        }
        
        rdfClass.add(r);
      } catch (RuntimeException oops) {
        throw oops;
      }
    }
    
    private String getOnPropertyURI(Restriction restriction) {
      RDFNode node = restriction.getPropertyValue(OWL.onProperty);
      return node.isResource() ? node.asResource().getURI() : null;
    }
  
    private void addQualifiedRestriction(Restriction restriction, LdRestriction r, String rangeURI) {
      
      LdQualifiedRestriction q = new LdQualifiedRestriction();
      q.setRangeURI(rangeURI);
      q.setMinCardinality(minQualifiedCardinality(restriction));
      q.setMaxCardinality(maxQualifiedCardinality(restriction));
      
      r.add(q);
      
    }
  
    private Integer maxQualifiedCardinality(Restriction restriction) {
      
      RDFNode node = restriction.getPropertyValue(OWL2.minQualifiedCardinality);
      if (node==null || !node.canAs(Literal.class)) return null;    
      return node.asLiteral().getInt();
    }
  
    private Integer minQualifiedCardinality(Restriction restriction) {
      RDFNode node = restriction.getPropertyValue(OWL2.minQualifiedCardinality);
      if (node==null || !node.canAs(Literal.class)) return null;    
      return node.asLiteral().getInt();
    }
  
    private LdTerm getTerm(String propertyURI) {
      return uri2Term.get(propertyURI);
    }
  
    private String onClass(Restriction restriction) {
      Resource onClass = restriction.getPropertyResourceValue(OWL2.onClass);
      return onClass==null ? null : onClass.getURI();
    }
  
    private Integer maxCardinality(Restriction restriction) {
      OntProperty p = restriction.getOnProperty();
      if (p.hasRDFType(OWL.FunctionalProperty)) {
        return 1;
      }
      if (!restriction.canAs(MaxCardinalityRestriction.class)) return null;
      return restriction.asMaxCardinalityRestriction().getMaxCardinality();
    }
  
    private Integer minCardinality(Restriction restriction) {
      
      if (!restriction.canAs(MinCardinalityRestriction.class)) return null;
      return restriction.asMinCardinalityRestriction().getMinCardinality();
    }
//  
//    private void buildOntModel() throws LdContextEnhanceException {
//      if (ontModel == null) {
//        ontModel = ModelFactory.createOntologyModel();
//      }
//      for (String namespaceURI : namespaceSet) {
//        LdAsset ns = assetManager.findAsset(namespaceURI);
//        if (ns == null) {
//          addError("Namespace not found: " + namespaceURI);
//          continue;
//        }
//        loadNamespace(ns);
//      }
//      
//      
//    }
  
    private void loadNamespace(LdAsset ns) throws LdContextEnhanceException {
  
      switch (ns.getFormat()) {
      case TURTLE:
        try {
          ontModel.read(ns.getReader(), null, "TTL");
        } catch (IOException e) {
          throw new LdContextEnhanceException(e);
        }
        break;
        
      case XSD :
        addDatatype(ns);
        break;
      
      }
      
    }
  
    private void addDatatype(LdAsset ns) {
      if (datatypeList == null) {
        datatypeList = new ArrayList<LdAsset>();
      }
      datatypeList.add(ns);
      
    }
  
    private void addError(String message) {
      errorBuilder.append(message);
      errorBuilder.append('\n');
      
    }
  
    /**
     * Scan through the given context and collect the set of all namespaces
     * that are used in that context.
     * @throws LdContextEnhanceException 
     */
    private void collectNamespaces(LdContext context) throws LdContextEnhanceException {

      if (ontModel == null) {
        ontModel = ModelFactory.createOntologyModel();
      }
      if (context == null) return;
      
      if (context.isEnhanced()) return;
      
      List<LdContext> components = context.listComponents();
      if (components != null) {
        for (LdContext c : components) {
          collectNamespaces(c);
        }
      }
      
      List<LdTerm> termList = context.getTermList();
      
      if (termList != null) {
        for (LdTerm term : termList) {
          identifyNamespace(term);
        }
      }
      
      
    }
  
    private void identifyNamespace(LdTerm term) throws LdContextEnhanceException {
     
      String iri = term.getIRI();
      if (isPropertyURI(iri)) {
        putTerm(iri, term);
      }
      
      String propertyNamespace = getNamespace(iri);
      String valueNamespace = getNamespace(term.getTypeIRI());
      
      addNamespace(propertyNamespace);
      addNamespace(valueNamespace);
      
    }
  
    
  
    /**
     * Put the given term into the local hash map of terms that require
     * enhancement.
     */
    private void putTerm(String iri, LdTerm term) {
      if (uri2Term == null) {
        uri2Term = new HashMap<String, LdTerm>();
      }
      uri2Term.put(iri, term);
      
    }
  
    /**
     * Returns true if the given URI is the URI for a property (as opposed to the URI for a namespace).
     */
    private boolean isPropertyURI(String uri) {
  
      return (uri!=null && !uri.endsWith("#") && !uri.endsWith("/"));
    }
  
    private void addNamespace(String namespace) throws LdContextEnhanceException {
      if (namespace == null || isStandardNamespace(namespace)) return;
      
      if (namespaceSet == null) {
        namespaceSet = new HashSet<String>();
      }
      if (!namespaceSet.contains(namespace)) {
        namespaceSet.add(namespace);

        LdAsset ns = assetManager.findAsset(namespace);
        if (ns == null) {
          addError("Namespace not found: " + namespace);
        } else {
          loadNamespace(ns);
        }
      }
      
    }
  
    private String getNamespace(String uri) throws LdContextEnhanceException {
      if (uri == null) return null;
      
      if (uri.endsWith("#") || uri.endsWith("/")) {
        return uri;
      }
      
      int end = uri.lastIndexOf('#');
      if (end < 0) {
        end = uri.lastIndexOf('/');
      }
      if (end<0) {
        throw new LdContextEnhanceException("Namespace not found for uri: " + uri);
      }
      
      
      return uri.substring(0, end+1);
    }
    }
}
