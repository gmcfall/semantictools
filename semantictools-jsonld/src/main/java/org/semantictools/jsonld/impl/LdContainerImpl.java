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
import org.semantictools.jsonld.LdContainerType;
import org.semantictools.jsonld.LdField;
import org.semantictools.jsonld.LdIRI;
import org.semantictools.jsonld.LdLiteral;
import org.semantictools.jsonld.LdObject;

public abstract class LdContainerImpl implements LdContainer {
  
  private LdField owner;
  private LdContainerType type;

  public LdContainerImpl(LdContainerType type) {
    this.type = type;
  }

  @Override
  public boolean isObject() {
    return false;
  }

  @Override
  public boolean isContainer() {
    return true;
  }

  @Override
  public boolean isLiteral() {
    return false;
  }

  @Override
  public LdLiteral asLiteral() throws ClassCastException {
    throw new ClassCastException("Cannot cast as LdLiteral: type is LdContainer");
  }

  @Override
  public LdContainer asContainer() throws ClassCastException {
    return this;
  }

  @Override
  public LdObject asObject() throws ClassCastException {
    throw new ClassCastException("Cannot cast as LdLiteral: type is LdContainer");
  }

  @Override
  public LdContainerType getContainerType() {
    return type;
  }

  @Override
  public LdField owner() {
    return owner;
  }
  
  void setOwner(LdField field) {
    owner = field;
  }

  @Override
  public boolean isIRI() {
    return false;
  }

  @Override
  public boolean isBlankNode() {
    return false;
  }

  @Override
  public LdIRI asIRI() throws ClassCastException {
    throw new ClassCastException("Cannot cast as LdIRI: type is LdContainer");
  }

  @Override
  public LdBlankNode asBlankNode() throws ClassCastException {
    throw new ClassCastException("Cannot cast as LdBlankNode: type is LdContainer");
  }

}
