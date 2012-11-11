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
package org.semantictools.index.api;

import java.util.List;

import org.semantictools.context.renderer.model.ContextProperties;
import org.semantictools.index.model.SchemaReference;
import org.semantictools.index.model.ServiceDocumentationList;

/**
 * Interface for a service to lookup a list of media type bindings for a given resource type.
 * @author Greg McFall
 *
 */
public interface LinkedDataIndex {
  
  /**
   * Given the URI for an RDF Class, return a list of references to media types that provide
   * representations of that class.
   */
  public List<ContextProperties> listMediaTypesForClass(String rdfClassURI);
  
  public List<ContextProperties> listAllMediaTypes();

  public List<SchemaReference> listOntologies();
  
  public List<SchemaReference> listDatatypes();
  
  public List<ServiceDocumentationList> listServices();
  
  public ServiceDocumentationList getServiceDocumentationForClass(String rdfClassURI);
  
}
