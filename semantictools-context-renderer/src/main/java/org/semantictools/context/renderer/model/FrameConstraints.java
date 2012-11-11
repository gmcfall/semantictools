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
package org.semantictools.context.renderer.model;

import java.util.HashSet;
import java.util.Set;

public class FrameConstraints {
  private String classURI;
  private Set<String> includedProperties = new HashSet<String>();
  private Set<String> excludedProperties = new HashSet<String>();
  private Set<String> excludeSubtypes = new HashSet<String>();
  
  public FrameConstraints(String classURI) {
    this.classURI = classURI;
  }
  
  /**
   * Returns the URI for the RDF class to which this set of constraints applies.
   */
  public String getClassURI() {
    return classURI;
  }
  
  public void setClassURI(String classURI) {
    this.classURI = classURI;
  }

  /**
   * Adds the specified property to the set of properties that are included
   * in the representation.
   * @param propertyURI
   */
  public void addIncludedProperty(String propertyURI) {
    includedProperties.add(propertyURI);
  }

  /**
   * Adds the specified property to the set of properties that are excluded from
   * the representation.
   * @param propertyURI
   */
  public void addExcludedProperty(String propertyURI) {
    excludedProperties.add(propertyURI);
  }
  
  /**
   * Declare that the specified property should not include subtypes in the JSON-LD context.
   */
  public void addExcludesSubtype(String propertyURI) {
    excludeSubtypes.add(propertyURI);
  }
  
  /**
   * Returns true if the specified property should not include subtypes in the JSON-LD context.
   * @return
   */
  public boolean isExcludesSubtypes(String propertyURI) {
    return excludeSubtypes.contains(propertyURI);
  }
  
  /**
   * Returns true if the specified property is included in the representation
   * of the RDF class associated with this FrameConstraints object.
   */
  public boolean isIncludedProperty(String propertyURI) {
    return !excludedProperties.contains(propertyURI) && 
        (includedProperties.isEmpty() || includedProperties.contains(propertyURI));
  }
  
  
  

}
