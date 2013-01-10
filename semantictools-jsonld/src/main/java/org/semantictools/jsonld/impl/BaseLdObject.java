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

import org.semantictools.jsonld.LdContainer;
import org.semantictools.jsonld.LdContext;
import org.semantictools.jsonld.LdLiteral;
import org.semantictools.jsonld.LdObject;

public abstract class BaseLdObject implements LdObject {

  private LdContext context;
  
  
  /**
   * Create a new LdObjectImpl with the specified LdContext.
   */
  public BaseLdObject(LdContext context) {
    this.context = context;
  }

  @Override
  public boolean isObject() {
    return true;
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
  public LdLiteral asLiteral() throws ClassCastException {
    throw new ClassCastException("Cannot cast as LdLiteral: type was LdObject");
  }

  @Override
  public LdContainer asContainer() throws ClassCastException {
    throw new ClassCastException("Cannot cast as LdContainer: type was LdObject");
  }

  @Override
  public LdObject asObject() throws ClassCastException {
    return this;
  }

  @Override
  public LdContext getContext() {
    return context;
  }
}
