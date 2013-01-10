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

/**
 * Represents an RDF Quad.
 * However, for now, it is just a triple.  The graphName attribute will be added when named graphs are supported.
 * 
 * @author Greg McFall
 *
 */
public class LdQuad {
  
  private LdNode subject;
  private LdNode predicate;
  private LdNode object;
  
  public LdQuad() {}
  
  public LdQuad(LdNode subject, LdNode predicate, LdNode object) {
    this.subject = subject;
    this.predicate = predicate;
    this.object = object;
  }
  
  public LdNode getSubject() {
    return subject;
  }
  public void setSubject(LdNode subject) {
    this.subject = subject;
  }
  public LdNode getPredicate() {
    return predicate;
  }
  public void setPredicate(LdNode predicate) {
    this.predicate = predicate;
  }
  public LdNode getObject() {
    return object;
  }
  public void setObject(LdNode object) {
    this.object = object;
  }
  
  

}
