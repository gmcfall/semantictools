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

import java.util.ArrayList;
import java.util.List;

import org.semantictools.frame.api.TypeManager;

import com.hp.hpl.jena.ontology.OntClass;

public class Enumeration extends Frame {
  
  private List<NamedIndividual> individualList = new ArrayList<NamedIndividual>();

  public Enumeration(TypeManager typeManager, OntClass type) {
    super(typeManager, type);
    setCategory(RestCategory.ENUMERABLE);
  }
  
  public void add(NamedIndividual individual) {
    individualList.add(individual);
  }

  public List<NamedIndividual> getIndividualList() {
    return individualList;
  }


  @Override
  public boolean canAsEnumeration() {
    return true;
  }

  @Override
  public Enumeration asEnumeration() {
    return this;
  }
  

  public List<NamedIndividual> listInstances(boolean direct) {
    return individualList;
  }
  
}
