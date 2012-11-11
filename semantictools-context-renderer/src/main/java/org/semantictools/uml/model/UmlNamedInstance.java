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
package org.semantictools.uml.model;

import org.semantictools.frame.model.NamedIndividual;

/**
 * A resource that is a named instance of some class.
 * @author Greg McFall
 *
 */
public class UmlNamedInstance {
  private NamedIndividual delegate;
  
  public UmlNamedInstance(NamedIndividual delegate) {
    this.delegate = delegate;
  }
  
  public String getURI() {
    return delegate.getUri();
  }
  
  public String getLocalName() {
    return delegate.getLocalName();
  }
  
  public String getComment() {
    return delegate.getComment();
  }

}
