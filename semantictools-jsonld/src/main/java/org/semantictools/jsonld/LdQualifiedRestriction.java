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

/**
 * Returns a qualified restriction that defines cardinality constraints applied
 * to the specific type of the object in a relation.
 * @author Greg McFall
 *
 */
public class LdQualifiedRestriction {

  private LdRestriction restriction;
  private String range;
  private Integer minCardinality;
  private Integer maxCardinality;
  
  
  /**
   * Returns the LdRestriction within which this LdQualifiedRestriction
   * is defined.
   */
  public LdRestriction getRestriction() {
    return restriction;
  }

  /**
   * Sets the LdRestriction within which this LdQualifiedRestriction
   * is defined.
   */
  public void setRestriction(LdRestriction restriction) {
    this.restriction = restriction;
  }

  /**
   * Returns the owl:onClass value from the RDF restriction.
   */
  public String getRangeURI() {
    return range;
  }

  /**
   * Sets the owl:onClass value from the RDF restriction.
   */
  public void setRangeURI(String rangeURI) {
    this.range = rangeURI;
  }

  /**
   * Returns the minimum cardinality expressed by this qualified restriction.
   * This value corresponds to own:minQualifiedCardinality.
   */
  public Integer getMinCardinality() {
    return minCardinality;
  }

  /**
   * Sets the minimum cardinality expressed by this qualified restriction.
   * This value corresponds to own:minQualifiedCardinality.
   */
  public void setMinCardinality(Integer minCardinality) {
    this.minCardinality = minCardinality;
  }


  /**
   * Returns the maximum cardinality expressed by this qualified restriction.
   * This value corresponds to own:maxQualifiedCardinality.
   * A null value signifies that the cardinality is unbounded.
   */
  public Integer getMaxCardinality() {
    return maxCardinality;
  }


  /**
   * Sets the maximum cardinality expressed by this qualified restriction.
   * This value corresponds to own:maxQualifiedCardinality.
   * A null value signifies that the cardinality is unbounded.
   */
  public void setMaxCardinality(Integer maxCardinality) {
    this.maxCardinality = maxCardinality;
  }
  
  
  
}
