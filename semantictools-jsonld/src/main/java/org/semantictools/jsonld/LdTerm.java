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

import java.io.Serializable;

/**
 * LdTerm represents a term that is declared within a JSON-LD context.
 *
 */
public class LdTerm implements Serializable {
  private static final long serialVersionUID = 1L;
  
  private String shortName;
  private String IRI;
  private String rawIRI;
  private String typeIRI;
  private String rawTypeIRI;
  private LdContainerType containerType = LdContainerType.UNDEFINED;
  private String language;

  private LdClass rdfClass;
  private LdDatatype datatype;
  private LdProperty property;
  
  
  /**
   * Returns the short name for this term as declared in the JSON-LD context.
   */
  public String getShortName() {
    return shortName;
  }
  
  /**
   * Sets the short name for this term.
   */
  public void setShortName(String shortName) {
    this.shortName = shortName;
  }
  

  /**
   * Returns the fully-qualified IRI to which the short name expands
   * in the JSON-LD context.
   */
  public String getIRI() {
    return IRI;
  }
  
  /**
   * Sets the fully-qualified IRI to which the short name expands
   * in the JSON-LD context.
   */
  public void setIRI(String iri) {
    IRI = iri;
  }
  
  /**
   * Returns the raw IRI value for this term as declared within the
   * JSON-LD context.  This value may be a compact IRI with a prefix and a colon.
   */
  public String getRawIRI() {
    return rawIRI;
  }
  

  /**
   * Sets the raw IRI value for this term as declared within the
   * JSON-LD context.  This value may be a compact IRI with a prefix and a colon.
   */
  public void setRawIRI(String rawIRI) {
    this.rawIRI = rawIRI;
  }
  
  /**
   * Returns the fully-qualified IRI for the type of this term, or null if the
   * type is not declared in the JSON-LD context.
   */
  public String getTypeIRI() {
    return typeIRI;
  }
  
  /**
   * Sets the fully-qualified IRI for the type of this term, as expanded within the
   * JSON-LD context.
   */
  public void setTypeIRI(String typeIRI) {
    this.typeIRI = typeIRI;
  }
  
  /**
   * Returns the raw IRI value for this term as declared with the JSON-LD context.
   * This value may be a compact IRI with a prefix and colon.
   */
  public String getRawTypeIRI() {
    return rawTypeIRI;
  }
  

  /**
   * Sets the raw IRI value for this term as declared with the JSON-LD context.
   * This value may be a compact IRI with a prefix and colon.
   */
  public void setRawTypeIRI(String rawTypeIRI) {
    this.rawTypeIRI = rawTypeIRI;
  }
  
  /**
   * Returns the container type for this term, as declared in the JSON-LD context.
   */
  public LdContainerType getContainerType() {
    return containerType;
  }
  
  /**
   * Sets the container type for this term, as declared in the JSON-LD context.
   */
  public void setContainerType(LdContainerType containerType) {
    this.containerType = containerType;
  }
  
  /**
   * Returns the language for this term, as declared in the JSON-LD context.
   */
  public String getLanguage() {
    return language;
  }
  
  /**
   * Sets the language for this term, as declared in the JSON-LD context.
   * @param language
   */
  public void setLanguage(String language) {
    this.language = language;
  }

  /**
   * Returns extra information about this term as an RDF property.
   */
  public LdProperty getProperty() {
    return property;
  }
  
  /**
   * Returns the property associated with this term, and creates one
   * as a side-effect if necessary.
   */
  public LdProperty ensureProperty() {
    if (property == null) {
      setProperty(new LdProperty());      
    }
    return property;
  }

  /**
   * Add extra information about this term as an RDF property.
   */
  public void setProperty(LdProperty property) {
    this.property = property;
    if (property != null) {
      property.setTerm(this);
    }
  }

  /**
   * Get a representation of the RDF class associated with this term.
   */
  public LdClass getRdfClass() {
    return rdfClass;
  }

  /**
   * Set a representation of the RDF class associated with this term.
   */
  public void setRdfClass(LdClass rdfClass) {
    this.rdfClass = rdfClass;
    if (rdfClass != null) {
      rdfClass.setTerm(this);
    }
  }

  /**
   * Returns the representation of an XSD SimpleType associated with this term,
   * or null if no such association exists.
   */
  public LdDatatype getDatatype() {
    return datatype;
  }

  /**
   * Sets the representation of an XSD SimpleType associated with this term.
   */
  public void setDatatype(LdDatatype datatype) {
    this.datatype = datatype;
  }
  
  public String toString() {
    return shortName;
  }
  
  
  
}
