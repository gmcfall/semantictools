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

import java.util.Date;
import java.util.Iterator;
import java.util.Random;

import org.semantictools.jsonld.LdBlankNode;
import org.semantictools.jsonld.LdContainer;
import org.semantictools.jsonld.LdContext;
import org.semantictools.jsonld.LdField;
import org.semantictools.jsonld.LdIRI;
import org.semantictools.jsonld.LdLiteral;
import org.semantictools.jsonld.LdNode;
import org.semantictools.jsonld.LdObject;
import org.semantictools.jsonld.LdTerm;

public class LdObjectImpl implements LdObject, LdIRI, LdBlankNode {
  private static Random random = new Random(new Date().getTime());
  private LdContext context;
  private String identifier;
  private String rawId;
  private String rawType;
  private String typeIRI;
  private LdField owner;

  FieldList fieldList;
  
  
  /**
   * Create a new LdObjectImpl with the specified LdContext.
   */
  public LdObjectImpl(LdContext context) {
    this.context = context;
  }
  
  void setFieldList(FieldList list) {
    fieldList = list;
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
    if (context == null && owner != null && owner.getOwner()!=null) {
      return owner.getOwner().getContext();
    }
    return context;
  }
  
  @Override
  public void setContext(LdContext context) {
    this.context = context;
  }

  @Override
  public String getIRI() {
    return (context==null) ? null : context.expand(rawId);
  }

  @Override
  public String getRawId() {
    return rawId;
  }
  
  void setId(String rawId) {
    this.rawId = rawId;
  }

  @Override
  public Iterator<LdField> fields() {
    return fieldList==null ? null : fieldList.iterator();
  }

  @Override
  public String getTypeIRI() {
    if (typeIRI == null && context != null) {
      if (rawType != null) {
        typeIRI = context.expand(rawType);
      } else {
        LdField field = owner();
        if (field != null) {
          LdContext context = getContext();
          if (context != null) {
            String propertyURI = field.getPropertyURI();
            LdTerm term = context.getTerm(propertyURI);
            if (term != null) {
              typeIRI = term.getTypeIRI();
            } 
          }
        }
      }
      
    }
    return typeIRI;
  }
  
  @Override
  public void setTypeIRI(String typeIRI) {
    this.typeIRI = typeIRI;
  }

  @Override
  public String getRawType() {
    return rawType;
  }
  
  void setRawType(String type) {
    this.rawType = type;
  }

  @Override
  public LdField owner() {
    return owner;
  }

  void setOwner(LdField owner) {
    this.owner = owner;
  }

  public String getIdentifier() {
    if (identifier==null) {
      if (rawId != null && rawId.startsWith("_:")) {
        identifier = rawId;
        
      } else if (rawId == null) {
        identifier = "_:" + random.nextInt();
      }
    }
    return identifier;
  }

  void setIdentifier(String identifier) {
    this.identifier = identifier;
  }

  @Override
  public boolean isIRI() {
    String id = getIdentifier();
    return id == null;
  }

  @Override
  public boolean isBlankNode() {
    String id = getIdentifier();
    return id != null;
  }

  @Override
  public LdIRI asIRI() throws ClassCastException {
    
    return isIRI() ? this : null;
  }

  @Override
  public LdBlankNode asBlankNode() throws ClassCastException {
    return isBlankNode() ? this : null;
  }

  @Override
  public String getValue() {
    return getIRI();
  }
  
  public boolean equals(Object obj) {
    if (obj instanceof LdNode) {
      LdNode node = (LdNode) obj;
      return 
          (isBlankNode() &&  node.isBlankNode() && getIdentifier().equals(node.asBlankNode().getIdentifier())) ||
          (isIRI() && node.isIRI() && getValue().equals(node.asIRI().getValue()));
    }
    return false;
  }

  @Override
  public String getId() {
    String iri = getIRI();
    return (iri == null) ? getIdentifier() : iri;
  }
 

}
