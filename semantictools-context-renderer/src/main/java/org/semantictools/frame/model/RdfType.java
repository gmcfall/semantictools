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

public interface RdfType {
  
  public String getLocalName();
  public String getUri();
  
  public boolean canAsOntClass();
  public boolean canAsFrame();
  public boolean canAsDatatype();
  public boolean canAsListType();
  public boolean canAsEnumeration();
  
  public OntClass asOntClass();
  public Frame asFrame();
  public Datatype asDatatype();
  public ListType asListType();
  public Enumeration asEnumeration();
  
  public String getNamespace();

}
