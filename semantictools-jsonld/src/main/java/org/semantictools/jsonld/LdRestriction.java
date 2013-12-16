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
import java.util.List;

/**
 * LdRestriction defines restrictions on a property as specified by an owl:Restriction.
 * This is used during validation of a JSON-LD document.
 * 
 * @author Greg McFall
 *
 */
public class LdRestriction implements Serializable {
  private static final long serialVersionUID = 1L;
  
  private LdClass domain;
  private String property;
  private Integer minCardinality;
  private Integer maxCardinality;
  private List<LdQualifiedRestriction> qlist;
  private String allValuesFrom;
  
  public LdRestriction() {}
  
  /**
   * Returns the domain to which this LdRestriction applies.
   * The domain is the type of resource on which the property is defined.
   * Since a property may be defined on more than one type of resource,
   * it is necessary to specify the domain.  
   */
  public LdClass getDomain() {
    return domain;
  }
  
  public LdQualifiedRestriction findQualifiedRestrictionByRangeURI(String rangeURI) {
    if (rangeURI == null) return null;
    if (qlist == null) return null;
    for (LdQualifiedRestriction q : qlist) {
      if (rangeURI.equals(q.getRangeURI())) return q;
    }
    return null;
  }
  
  public String getAllValuesFrom() {
    return allValuesFrom;
  }

  public void setAllValuesFrom(String allValuesFrom) {
    this.allValuesFrom = allValuesFrom;
  }

  /**
   * Sets the domain to which this LdRestriction applies.
   */
  public void setDomain(LdClass domain) {
    this.domain = domain;
  }
  
  
  /**
   * Returns a reference to the property to which this LdRestriction applies.
   * This corresponds to the owl:onProperty attribute of the OWL Restriction.
   */
  public String getPropertyURI() {
    return property;
  }

  /**
   * Sets a reference for the property to which this LdRestriction applies.
   * This corresponds to the owl:onProperty attribute of the OWL Restriction.
   */
  public void setPropertyURI(String property) {
    this.property = property;
  }

  /**
   * Returns the minimum cardinality for the property.
   */
  public Integer getMinCardinality() {
    return minCardinality;
  }
  
  public List<LdQualifiedRestriction> listQualifiedRestrictions() {
    return qlist;
  }
  
  public void add(LdQualifiedRestriction q) {
    if (qlist == null) {
      qlist = new ArrayList<LdQualifiedRestriction>();
    }
    qlist.add(q);
    q.setRestriction(this);
  }
  

  /**
   * Sets the minimum cardinality for the property.
   */
  public void setMinCardinality(Integer minCardinality) {
    this.minCardinality = minCardinality;
  }
  
  /**
   * Returns the maximum cardinality for the property, or null if the
   * property is unbounded.
   */
  public Integer getMaxCardinality() {
    return maxCardinality;
  }

  /**
   * Sets the maximum cardinality for the property.  A null value
   * signifies that the property is unbounded.
   */
  public void setMaxCardinality(Integer maxCardinality) {
    this.maxCardinality = maxCardinality;
  }
  
  public String toString() {
    return "LdRestriction(domainURI=" + domain + ", maxCardinality=" + maxCardinality + ", minCardinality=" + minCardinality +")"; 
  }
  
  /**
   * Returns true if this restriction applies to the specified domain within 
   * the given context.
   */
  public boolean applies(LdContext context, String domainURI) {
    return context.isAssignableFrom(domainURI, domain.getURI());
  }
  
  /**
   * Returns the URI for the qualified range of the property associated with this restriction,
   * or null if the qualified range cannot be inferred.
   * The qualified range can be inferred if there is a single qualified cardinality restriction
   * on the property, and the maximum cardinality of the qualified restriction matches the
   * maximum cardinality of the unqualified restriction.  In this case, the qualified range
   * is specified by the owl:onClass attribute of the qualified restriction.
   */
  public String inferQualifiedRange() {
    if (qlist == null || qlist.size()!=1) return null;
    LdQualifiedRestriction qr = qlist.get(0);
    int max = maxCardinality==null ? 0 : maxCardinality;
    int qmax = qr.getMaxCardinality()==null ? 0 : qr.getMaxCardinality();
    
    return (max == qmax) ? qr.getRangeURI() : null;
  }

}
