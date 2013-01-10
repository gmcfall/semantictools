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
package org.semantictools.jsonld.impl;

import org.semantictools.jsonld.LdBlankNode;
import org.semantictools.jsonld.LdContainer;
import org.semantictools.jsonld.LdIRI;
import org.semantictools.jsonld.LdLiteral;
import org.semantictools.jsonld.LdObject;

public class LdIriImpl implements LdIRI {

  private String value;
  
  public LdIriImpl(String value) {
    this.value = value;
  }

  @Override
  public boolean isObject() {
    return false;
  }

  @Override
  public boolean isContainer() {
    return false;
  }

  @Override
  public boolean isLiteral() {
    return false;
  }

  @Override
  public boolean isIRI() {
    return true;
  }

  @Override
  public boolean isBlankNode() {
    return false;
  }

  @Override
  public LdLiteral asLiteral() throws ClassCastException {
    throw new ClassCastException("Cannot convert to Literal: type is IRI");
  }

  @Override
  public LdContainer asContainer() throws ClassCastException {
    throw new ClassCastException("Cannot convert to Container: type is IRI");
  }

  @Override
  public LdObject asObject() throws ClassCastException {
   LdObjectImpl obj = new LdObjectImpl(null);
   obj.setId(value);
   return obj;
  }

  @Override
  public LdIRI asIRI() throws ClassCastException {
    return this;
  }

  @Override
  public LdBlankNode asBlankNode() throws ClassCastException {
    throw new ClassCastException("Cannot convert to blank node: type is IRI");
  }

  @Override
  public String getValue() {
    return value;
  }

}
