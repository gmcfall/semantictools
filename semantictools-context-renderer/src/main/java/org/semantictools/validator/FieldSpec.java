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
package org.semantictools.validator;

public class FieldSpec {
  private String localName;
  private String propertyURI;
  private int minCardinality;
  private int maxCardinality;
  private String rangeURI;
  
  public String getLocalName() {
    return localName;
  }
  public void setLocalName(String localName) {
    this.localName = localName;
  }
  public String getPropertyURI() {
    return propertyURI;
  }
  public void setPropertyURI(String propertyURI) {
    this.propertyURI = propertyURI;
  }
  public int getMinCardinality() {
    return minCardinality;
  }
  public void setMinCardinality(int minCardinality) {
    this.minCardinality = minCardinality;
  }
  public int getMaxCardinality() {
    return maxCardinality;
  }
  public void setMaxCardinality(int maxCardinality) {
    this.maxCardinality = maxCardinality;
  }
  public String getRangeURI() {
    return rangeURI;
  }
  public void setRangeURI(String rangeURI) {
    this.rangeURI = rangeURI;
  }
  
  

}
