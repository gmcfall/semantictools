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
package org.semantictools.frame.model;

/**
 * A container that holds information from the UML about an unnamed inverse property.
 *
 */
public class InverseProperty {
  private Integer minCardinality;
  private Integer maxCardinality;
  private Encapsulation encapsulation = Encapsulation.NONE;
  private Boolean unboundedCardinality;
  
  public Integer getMinCardinality() {
    return minCardinality;
  }
  public void setMinCardinality(Integer minCardinality) {
    this.minCardinality = minCardinality;
  }
  public Integer getMaxCardinality() {
    return maxCardinality;
  }
  public void setMaxCardinality(Integer maxCardinality) {
    this.maxCardinality = maxCardinality;
  }
  public Encapsulation getEncapsulation() {
    return encapsulation;
  }
  public void setEncapsulation(Encapsulation encapsulation) {
    this.encapsulation = encapsulation;
  }
  public Boolean isUnboundedCardinality() {
    return unboundedCardinality;
  }
  public void setUnboundedCardinality(Boolean unboundedCardinality) {
    this.unboundedCardinality = unboundedCardinality;
  }
  
  public String getMultiplicity() {
    String result = minCardinalityString() + maxCardinalityString();
    
    return result.length() == 0 ? null : result;
    
  }
  private String minCardinalityString() {
    return minCardinality == null ? "" : minCardinality + ":";
  }
  
  private String maxCardinalityString() {
    return 
      maxCardinality != null ?  maxCardinality.toString() :
      (unboundedCardinality==null) ? "" : 
      unboundedCardinality ? "*" : 
      "";
  }
  
  

}
