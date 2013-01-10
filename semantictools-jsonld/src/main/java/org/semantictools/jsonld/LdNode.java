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
 * LdNode is an interface that represents a JSON-LD node: object, literal, or array.
 * @author Greg McFall
 *
 */
public abstract interface LdNode {
  
  /**
   * Returns true if this node is an object.
   */
  boolean isObject();
  
  /**
   * Returns true if this node is a container (i.e. list or set).
   */
  boolean isContainer();
  
  /**
   * Returns true if this node is a literal.
   */
  boolean isLiteral();
  
  /**
   * Returns true if this node is an IRI
   */
  boolean isIRI();
  
  /**
   * Returns true if this node is a blank node.
   */
  boolean isBlankNode();
  
  /**
   * A convenience method to cast this LdNode as an LdLiteral.
   * @throws ClassCastException
   */
  LdLiteral asLiteral() throws ClassCastException;
  
  /**
   * A convenience method to cast this LdNode as an LdContainer.
   * @throws ClassCastException
   */
  LdContainer asContainer() throws ClassCastException;
  
  /**
   * A convenience method to cast this LdNode as an LdObject.
   * @throws ClassCastException
   */
  LdObject asObject() throws ClassCastException;
  
  /**
   * A convenience method to convert this LdNode into an LdIRI instance.
   */
  LdIRI asIRI() throws ClassCastException;
  
  /**
   * A convenience method to convert this LdNode into an LdBlankNode instance.
   */
  LdBlankNode asBlankNode() throws ClassCastException;

}
