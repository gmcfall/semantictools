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

import com.hp.hpl.jena.ontology.OntClass;

public class ListType implements RdfType {
  
  private OntClass ontClass;
  private RdfType elementType;
  

  public ListType(OntClass ontClass, RdfType elementType) {
    this.ontClass = ontClass;
    this.elementType = elementType;
  }

  /**
   * Returns the class definition for this ListType
   */
  public OntClass getOntClass() {
    return ontClass;
  }

  /**
   * Returns the RdfType of the elements contained within this list
   */
  public RdfType getElementType() {
    return elementType;
  }

  @Override
  public String getLocalName() {
    return null;
  }

  @Override
  public String getUri() {
    return ontClass.getURI();
  }

  @Override
  public boolean canAsOntClass() {
    return false;
  }

  @Override
  public boolean canAsFrame() {
    return false;
  }

  @Override
  public boolean canAsDatatype() {
    return false;
  }

  @Override
  public OntClass asOntClass() {
    return null;
  }

  @Override
  public Frame asFrame() {
    return null;
  }

  @Override
  public Datatype asDatatype() {
    return null;
  }

  @Override
  public boolean canAsListType() {
    return true;
  }

  @Override
  public ListType asListType() {
    return this;
  }

  @Override
  public String getNamespace() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public boolean canAsEnumeration() {
    return false;
  }

  @Override
  public Enumeration asEnumeration() {
    return null;
  }

}
