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

public class TermValue {
  private String id;
  private String type;
  private Container container = Container.NONE;
  
  private Integer minCardinality;
  
  public String getId() {
    return id;
  }
  public void setId(String id) {
    this.id = id;
  }
  public String getType() {
    return type;
  }
  public void setType(String type) {
    this.type = type;
  }
  public Container getContainer() {
    return container;
  }
  public void setContainer(Container container) {
    this.container = container;
  }
  
  /**
   * Returns an override for the minimumCardinality of the associated property
   * within the local JSON-LD context.  This is an extension of the JSON-LD specification.
   */
  public Integer getMinCardinality() {
    return minCardinality;
  }
  
  /**
   * Sets an over-ride for the minimumCardinality of the associated property
   * within the local JSON-LD context.  This is an extension of the JSON-LD specification.
   */
  public void setMinCardinality(Integer value) {
    minCardinality = value;
  }
  
  
  

}
