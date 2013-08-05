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

import com.hp.hpl.jena.ontology.OntResource;

public class NamedIndividual {
  
  private OntResource ontResource;
  
  public NamedIndividual(OntResource ontResource) {
    this.ontResource = ontResource;
  }

  public String getLocalName() {
    return ontResource.getLocalName();
  }
  
  public String getNamespaceURI() {
    return ontResource.getNameSpace();
  }
  
  public String getUri() {
    return ontResource.getURI();
  }
  
  public String getComment() {
    String comment = ontResource.getComment(null);
    if (comment == null) {
      comment = "";
    }
    return comment;
  }
  
  public String toString() {
    return getLocalName();
  }

}
