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

import java.io.File;

public class OntologyEntity extends FileEntity {

  private String ontologyURI;
  
  public OntologyEntity(String contentType, File file, String ontologyURI) {
    super(contentType, file);
    this.ontologyURI = ontologyURI;
  }

  /**
   * Returns the URI for the ontology represented by this entity
   */
  public String getOntologyURI() {
    return ontologyURI;
  }

  
}
