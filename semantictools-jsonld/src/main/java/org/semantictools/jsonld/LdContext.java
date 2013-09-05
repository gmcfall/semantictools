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
package org.semantictools.jsonld;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * LdContext provides an API for accessing terms within a JSON-LD context.
 * @author Greg McFall
 *
 */
public class LdContext implements Serializable {
  private static final long serialVersionUID = 1L;
  private static Random random = new Random(new Date().getTime());
  
  private transient LdContext parentContext;
  private String contextURI;
  private List<LdTerm> termList;
  private Map<String,LdTerm> termMap;
  private List<LdContext> components;
  
  /**
   * Creates a new LdContext which inherits terms from the specified parent context.
   */
  public LdContext(LdContext parentContext) {
    this.parentContext = parentContext;
  }
  
  public LdContext() {}
  
  /**
   * Returns a shallow copy of this LdContext.
   */
  public LdContext copy() {
    LdContext copy = new LdContext();
    copy.parentContext = parentContext;
    copy.termList = termList;
    copy.termMap = termMap;
    copy.components = components;
    copy.contextURI = contextURI;
    
    return copy;
  }
  
  /**
   * Returns the URI for this context, or null if the URI is not known.
   */
  public String getContextURI() {
    return contextURI;
  }

  /**
   * Sets the URI for this JSON-LD context.
   */
  public void setContextURI(String contextURI) {
    this.contextURI = contextURI;
  }

  /**
   * Sets the parent context from which terms are inherited.
   */
  public void setParentContext(LdContext parent) {
    parentContext = parent;
  }

  /**
   * Adds a new term to this context.
   * @throws IllegalStateException If the context is closed.
   */
  public void add(LdTerm term) throws IllegalStateException {
    if (termMap != null) {
      throw new IllegalStateException("Cannot add a new term because this context is closed.");
    }
    if (termList == null) {
      termList = new ArrayList<LdTerm>();
    }
    termList.add(term);
  }
  
  /**
   * Return the list of terms declared locally within this context.
   * Does not return terms inherited from the parent context or
   * terms from components.
   */
  public List<LdTerm> getTermList() {
    if (termList == null) {
      termList = new ArrayList<LdTerm>();
    }
    return termList;
  }
  
  /**
   * Adds a component context.  This corresponds to the case where multiple contexts are
   * declared within the scope of a JSON object.
   * @throws IllegalStateException If the context is closed.
   */
  public void add(LdContext context) throws IllegalStateException {
    if (termMap != null) {
      throw new IllegalStateException("Cannot add a component context because the enapsulating context is closed.");
    }
    if (components == null) {
      components = new ArrayList<LdContext>();
    }
    components.add(context);
  }
  
  /**
   * Close this LdContext to further modification.
   * It is an error to add a new LdTerm or a component LdContext to after this context has
   * been closed.
   * This method builds internal hash maps which allow the context to perform URI expansion
   * rapidly.
   */
  public void close() {

    // Only build the termMap once. 
    // We guard against the case where the close method is accidentally
    // called more than once.  Instead of throwing an exception, we
    // bail out immediately.
    //
    if (termMap != null) return;
    
    if (termList!=null) {
       
      termMap = new HashMap<String, LdTerm>();
      for (LdTerm term : termList) {
        String name = term.getShortName();        
        termMap.put(name, term);
        
        String rawIRI = term.getRawIRI();
        termMap.put(rawIRI, term);
        
        if (isFullyQualified(rawIRI)) {
          term.setIRI(rawIRI);
          termMap.put(rawIRI, term);
        }
        
        String rawTypeIRI = term.getRawTypeIRI();
        if (isFullyQualified(rawTypeIRI)) {
          term.setTypeIRI(rawTypeIRI);
        }
      }
      
    }
    if (components != null) {
      for (LdContext c : components) {
        c.close();
      }
    }
    
    expandTerms();
  }
  
  public List<LdTerm> listTerms() {
    return termList;
  }
  
  public List<LdContext> listComponents() {
    return components;
  }
  
  /**
   * Returns true if the given URI is fully qualified.
   */
  private boolean isFullyQualified(String anyURI) {
    if (anyURI==null) return false;
    return 
        anyURI.startsWith("http://") ||
        anyURI.startsWith("urn:") ||
        anyURI.contains("://") || 
        anyURI.startsWith("cid:") ||
        anyURI.startsWith("data:") ||
        anyURI.startsWith("dav:") ||
        anyURI.startsWith("dns:") ||
        anyURI.startsWith("geo:") ||
        anyURI.startsWith("go:") ||
        anyURI.startsWith("gopher:") ||
        anyURI.startsWith("h323:") ||
        anyURI.startsWith("iax:") ||
        anyURI.startsWith("im:") ||
        anyURI.startsWith("mid:") ||
        anyURI.startsWith("news:") ||
        anyURI.startsWith("pres:") ||
        anyURI.startsWith("sip:") ||
        anyURI.startsWith("sms:") ||
        anyURI.startsWith("snmp:") ||
        anyURI.startsWith("tag:") ||
        anyURI.startsWith("tel:") ||
        anyURI.startsWith("uuid:") ||
        anyURI.startsWith("ws:") ||
        anyURI.startsWith("xmpp:");
  }
  
  /**
   * Compute the fully qualified URI for local terms in this context.
   */
  private void expandTerms() {
    if (termList == null || termMap==null) return;
    
    for (LdTerm term : termList) {
      String iri = term.getIRI();
      if (iri == null) {
        String rawIRI = term.getRawIRI();
        iri = expand(rawIRI);
        if (!iri.equals(rawIRI)) {
          term.setIRI(iri);
          termMap.put(iri, term);
        }
      }
      
      String typeIRI = term.getTypeIRI();
      if (typeIRI == null) {
        String rawTypeIRI = term.getRawTypeIRI();
        if (rawTypeIRI != null) {
          typeIRI = expand(rawTypeIRI);
          if (!typeIRI.equals(rawTypeIRI)) {
            term.setTypeIRI(typeIRI);
          }
        }
      }
    }
    
  }

  /**
   * Returns the fully qualified IRI for the specified key, or
   * returns the given key if it cannot be expanded. 
   * @param key  The string that should be expanded to a fully qualified IRI. 
   * This value may be a compact IRI (with a colon separating a namespace prefix from a local name)
   * or it may be a simple name declared in the JSON-LD context.
   */
  public String expand(String key) {
    if (key == null) return null;
    
    int colon = key.indexOf(':');
    
    if (colon<0) {
      LdTerm term = getTerm(key);
      return term==null ? key : term.getIRI();
    }
    
    String prefix = key.substring(0, colon);
    String namespaceIRI = null;
    
    LdTerm term = getTerm(prefix);
    if (term == null) {
      return key;
    }
    namespaceIRI = term.getIRI();
    if (namespaceIRI == null) {
      // The namespace IRI needs to be expanded.
      namespaceIRI = term.getRawIRI();
      String expandedIRI = expand(namespaceIRI);
      if (!namespaceIRI.equals(expandedIRI)) {
        term.setIRI(expandedIRI);
        namespaceIRI = expandedIRI;
      }
    }
    if (namespaceIRI == null) {
      return key;
    }
    String suffix = key.substring(colon+1);
    
    return namespaceIRI + suffix;
  }

  /**
   * Returns the specified term as defined within this JSON-LD context.
   * The return value may come from this context, or it may be inherited from
   * the parent context.
   * 
   * @param key  Either the short name for the term, or a compact IRI, or a fully qualified IRI.
   */
  public LdTerm getTerm(String key) {
    if (key==null) return null;
    
    LdTerm term = (termMap != null) ? termMap.get(key) : null;
    if (term != null) return term;
    
    if (components != null) {
      for (int i=components.size()-1; i>=0; i--) {
        LdContext c = components.get(i);
        term = c.getTerm(key);
        if (term != null) return term;
      }
    }
    if (parentContext != null) {
      term = parentContext.getTerm(key);
    }
    return term;
  }
  
  /**
   * Returns true if this context has been enhanced with LdProperty
   * or LdClass data which is useful for validation.
   */
  public boolean isEnhanced() {
    if (termList != null) {
      for (LdTerm term : termList) {
//        if (term.getProperty() != null) return true;
        if (
            // TODO: The special case handling of owl:Thing is forced by a hack that added this RDF class by brute force.
            // Eliminate this hack.
            (term.getRdfClass() != null && !"http://www.w3.org/2002/07/owl#Thing".equals(term.getRdfClass().getURI())) ||
            (term.getDatatype()!=null) ||
            (term.getProperty()!=null)
            
        ) return true;
      }
    }
    return (parentContext==null) ? false : parentContext.isEnhanced();
  }
  
  
  /**
   * Returns a representation of the specified RDF class.
   */
  public LdClass getClass(String classURI) {
    LdTerm term = getTerm(classURI);    
    LdClass result = (term==null) ? null : term.getRdfClass();
    return (result==null && parentContext!=null) ? parentContext.getClass(classURI) : result;
  }
  
  public List<LdClass> listClasses() {
   List<LdClass> list = new ArrayList<LdClass>();
   addClasses(list);
   return list;
  }
  
  private void addClasses(List<LdClass> list) {
    for (LdTerm term : termList) {
      LdClass c = term.getRdfClass();
      if (c != null) {
        list.add(c);
      }
    }
    if (parentContext != null) {
      parentContext.addClasses(list);
    }
    
  }

  /**
   * Returns true if the subURI is the same as superURI, or if the 
   * subURI is the URI for a class that is known to be a subclass of the 
   * class referenced by superURI.
   */
  public boolean isAssignableFrom(String subURI, String superURI) {
    if (subURI.equals(superURI)) return true;
    LdClass subDomain = getClass(subURI);
    return (subDomain == null) ? null : subDomain.hasSuperType(superURI);
  }
  
  public LdDatatype findDatatypeByURI(String datatypeURI) {
    if (datatypeURI==null) {
      return null;
    }
    if (datatypeURI.startsWith(XsdType.URI) || datatypeURI.startsWith(LdDatatypeManager.XPATH_DATATYPES_URI)) {
      return LdDatatypeManager.getXsdTypeByURI(datatypeURI);
    }
    LdTerm term = getTerm(datatypeURI);
    return term == null ? null : term.getDatatype();
  }
  
  /**
   * Return the term with the specified uri, and create it if 
   * it does not already exist.
   */
  public LdTerm ensureTerm(String uri) {
    LdTerm term = getTerm(uri);
    if (term == null) {
      term = new LdTerm();
      term.setRawIRI(uri);
      
      String shortName = makeUnique(localName(uri));
      term.setShortName(shortName);
      termList.add(term);
      if (termMap != null) {
        termMap.put(shortName, term);
        termMap.put(uri, term);
      }
    }
    return term;
  }
  
  /**
   * Ensure that the specified class is defined in this context, and return
   * the requested LdClass instance.  If the specified LdClass is not found in the context,
   * it will be created.  Likewise an LdTerm for the class will be created as
   * a side-effect if necessary.
   */
  public LdClass ensureClass(String classURI) {
    LdTerm term = ensureTerm(classURI);
    LdClass result = term.getRdfClass();
    if (result == null) {
      result = new LdClass(classURI);
      term.setRdfClass(result);
    }
    return result;
  }
  
  
  
  private String makeUnique(String name) {
    int max = 100;
    String result = name;
    int i = 1;
    for (; i<max && getTerm(result)!=null; i++) {
      result = name + i;
    }
    if (i == max) {
      result = "x" + (max + random.nextLong());
    }
    return result;
  }

  private String localName(String uri) {
    int mark = uri.lastIndexOf('#');
    if (mark < 0) {
      mark = uri.lastIndexOf('/');
    }
    if (mark < 0) {
      return "x";
    }
    return uri.substring(mark+1);
  }
  
}
