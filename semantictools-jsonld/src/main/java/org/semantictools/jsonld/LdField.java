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

public class LdField {
  private LdObject owner;
  private String localName;
  private String propertyURI;
  private LdNode value;
  
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
  public LdNode getValue() {
    return value;
  }
  public void setValue(LdNode value) {
    this.value = value;
  }
  
  /**
   * Returns the LdObject instance that owns this field.
   */
  public LdObject getOwner() {
    return owner;
  }
  
  /**
   * Sets the LdObject instance that owns this field.
   */
  public void setOwner(LdObject owner) {
    this.owner = owner;
  }
  
  public String toString() {
    return "LdField(" + getLocalName() + ")";
  }
  
  

}
