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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * LdClass represents an RDF class.
 * It aggregates all of the restrictions that apply to the RDF class.
 * It is useful during validation.
 * 
 * @author Greg McFall
 *
 */
public class LdClass implements Serializable {
  private static final long serialVersionUID = 1L;
  
  private String uri;
  private Map<String, LdRestriction> restrictions;
  private List<LdClass> superClassList;
  
  private LdTerm term;
  
  public LdClass(String classURI) {
    this.uri = classURI;
  }
  
  public void setURI(String uri) {
    this.uri = uri;
  }

  /**
   * Return a reference to the RDF class to which the restrictions apply.
   */
  public String getURI() {
    return uri;
  }
  
  /**
   * Returns the list of supertypes of this LdClass, or null if there are no
   * known supertypes.
   */
  public List<LdClass> listSupertypes() {
    return superClassList;
  }
  
  public void setSupertypes(List<LdClass> superList) {
    superClassList = superList;
  }

  /**
   * Adds an element to the list of supertypes of this LdClass.
   */
  public void addSupertype(LdClass dr) {
    if (superClassList == null) {
      superClassList = new ArrayList<LdClass>();
    }
    if (!superClassList.contains(dr)) {
      superClassList.add(dr);
    }
  }

  /**
   * Returns the list of restrictions that apply to this LdClass.
   */
  public List<LdRestriction> listRestrictions() {
    return restrictions==null ? null : new ArrayList<LdRestriction>(restrictions.values());
  }
  
  /**
   * Return the restriction on the specified property.
   */
  public LdRestriction findRestrictionByPropertyURI(String propertyURI) {
    return (restrictions == null) ? null : restrictions.get(propertyURI);
  }
  
  /**
   * Add a new restriction that applies to this LdClass.
   */
  public void add(LdRestriction restriction) {
    if (restrictions == null) {
      restrictions = new HashMap<String, LdRestriction>();
    }
    restrictions.put(restriction.getPropertyURI(), restriction);
  }
  
  /**
   * Returns true if the class represented by this domain restriction
   * has a superclass with the specified URI.
   */
  public boolean hasSuperType(String superURI) {
    if (superClassList != null) {
      for (LdClass sr : superClassList) {
        if (sr.getURI().equals(superURI) || sr.hasSuperType(superURI)) {
          return true;
        }
      }
    }
    
    return false;
    
  }

  /**
   * Returns the term associated with this RDF class.
   */
  public LdTerm getTerm() {
    return term;
  }

  /**
   * Sets the term associated with this RDF class
   */
  public void setTerm(LdTerm term) {
    this.term = term;
  }
  
  public String inferQualifiedPropertyType(String propertyURI) throws AmbiguousRestrictionException {
    String result = inferQualifiedPropertyType(this, propertyURI);

    if (result != null) return result;
    
    List<LdQualifiedRestriction> list = new ArrayList<LdQualifiedRestriction>();
    findQualifiedRestrictions(list, propertyURI);
    if (list.size()>1) {
      throw new AmbiguousRestrictionException(list);
    }
    
    return list.isEmpty() ? null : list.get(0).getRangeURI();
  }
  
  /**
   * Traverse the super classes looking for a qualified restriction on the specified property.
   * 
   * @param list  The list into which all matching restrictions will be added.
   * @param propertyURI
   */
  private void findQualifiedRestrictions(List<LdQualifiedRestriction> list,  String propertyURI) {
    
    if (superClassList == null) return;
    
    for (LdClass superclass : superClassList) {
      LdRestriction r = superclass.findRestrictionByPropertyURI(propertyURI);
      if (r == null) continue;
      
      List<LdQualifiedRestriction> qlist = r.listQualifiedRestrictions();
      
      if (qlist == null || qlist.size()!=1) return;
      
      Integer maxCardinality = r.getMaxCardinality();
      LdQualifiedRestriction qr = qlist.get(0);
      int max = maxCardinality==null ? 0 : maxCardinality;
      int qmax = qr.getMaxCardinality()==null ? 0 : qr.getMaxCardinality();
      if (max == qmax) {
        conditionalAdd(list, qr);
      }
    }
    
    
  }
  
  /**
   * Returns true if this RDF class is a subclass of the given otherClass.
   */
  public boolean isSubClassOf(LdClass otherClass) {
    return hasSuper(this, otherClass.getURI());
  }
  
  /**
   * Returns true if the given subClass has an ancestor whose URI is superURI.
   */
  private boolean hasSuper(LdClass subClass, String superURI) {
      
    List<LdClass> superList = subClass.listSupertypes();
    if (superList != null) {
      for (LdClass superClass : superList) {
        if (superClass.getURI().equals(superURI) || hasSuper(superClass, superURI)) return true;
      }
    }
    
    return false;
    
  }

  /**
   * Add the given qualified restriction qr to the list, but only if the list does not contain 
   * any restrictions from a domain that is a subtype of the domain associated with qr.
   */
  private void conditionalAdd(List<LdQualifiedRestriction> list, LdQualifiedRestriction qr) {
    LdClass domain = qr.getRestriction().getDomain();
    
    Iterator<LdQualifiedRestriction> sequence = list.iterator();
    while (sequence.hasNext()) {
      LdQualifiedRestriction other = sequence.next();
      LdClass otherDomain = other.getRestriction().getDomain();
      
      if (domain.isSubClassOf(otherDomain)) {
        // qr is more specific than the other restriction, so
        // remove the other restriction from the list.
        sequence.remove();
      }
      
      if (otherDomain.isSubClassOf(domain)) {
        // The other restriction is more specific than qr, so ignore qr.
        return;
      }
      
    }
    list.add(qr);
    
  }

  private String inferQualifiedPropertyType(LdClass rdfClass, String propertyURI) {
    if (rdfClass==null) return null;
    LdRestriction r = findRestrictionByPropertyURI(propertyURI);
    return (r == null) ? null : r.inferQualifiedRange();
    
    
    
  }
  
  public String toString() {
    return "LdClass(" + uri + ")";
  }

}
