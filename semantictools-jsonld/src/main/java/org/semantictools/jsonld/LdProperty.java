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

import java.util.ArrayList;
import java.util.List;

/**
 * LdProperty adds additional information to an LdTerm that represents and RDF property.
 * This extra information is useful during validation.
 * @author Greg McFall
 *
 */
public class LdProperty {

  private LdTerm term;
  private List<String> domain;
  
  /**
   * Returns the term associated with this property.
   */
  public LdTerm getTerm() {
    return term;
  }

  /**
   * Sets the term associated with this property
   */
  public void setTerm(LdTerm term) {
    this.term = term;
  }

  /**
   * Return the list of URI values for classes in the domain of this property,
   * or null if the domain is not known.
   */
  public List<String> getDomain() {
    return domain;
  }
  
  /**
   * Add the URI for a class within the domain of this property.
   */
  public void addDomain(String domainURI) {
    if (domain == null) {
      domain = new ArrayList<String>();
    }
    domain.add(domainURI);
  }
  
  /**
   * Return the URI for this property, as defined by the 
   * associated term.
   */
  public String getURI() {
    return term.getIRI();
  }
}
