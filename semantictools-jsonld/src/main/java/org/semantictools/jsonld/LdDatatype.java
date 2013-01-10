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

import java.util.regex.Pattern;

/**
 * Represents a SimpleType declared in some XML Schema.
 * 
 * @author Greg McFall
 *
 */
public class LdDatatype  {
  
  
  private String localName;
  private String uri;
  private LdDatatype base;
  private String namespace;
  private XsdType xsdType;
  
  private Integer length;
  private Integer minLength;
  private Integer maxLength;
  private Pattern pattern;
  private Whitespace whitespace;
  
  private Number maxInclusive;
  private Number maxExclusive;
  private Number minInclusive;
  private Number minExclusive;
  private Integer totalDigits;
  private Integer fractionDigits;
  
  public LdDatatype() {
    
  }


  public String getLocalName() {
    return localName;
  }
  public void setLocalName(String localName) {
    this.localName = localName;
  }
  public String getUri() {
    return uri;
  }
  public void setUri(String uri) {
    this.uri = uri;
  }
  public LdDatatype getBase() {
    return base;
  }
  public void setBase(LdDatatype base) {
    this.base = base;
  }
  
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append("LdDatatype(name=");
    builder.append(localName);
    if (maxLength != null) {
      builder.append(", maxLength=");
      builder.append(maxLength);
    }
    if (pattern != null) {
      builder.append(", pattern=");
      builder.append(pattern);
    }
    builder.append(")");
    
    return builder.toString();
  }


  public Integer getLength() {
    return length;
  }


  public void setLength(Integer length) {
    this.length = length;
  }


  public Integer getMinLength() {
    return minLength;
  }


  public void setMinLength(Integer minLength) {
    this.minLength = minLength;
  }


  public Integer getMaxLength() {
    return maxLength;
  }


  public void setMaxLength(Integer maxLength) {
    this.maxLength = maxLength;
  }


  public Pattern getPattern() {
    return pattern;
  }


  public void setPattern(Pattern pattern) {
    this.pattern = pattern;
  }


  public Whitespace getWhitespace() {
    return whitespace;
  }


  public void setWhitespace(Whitespace whitespace) {
    this.whitespace = whitespace;
  }


  public Number getMaxInclusive() {
    return maxInclusive;
  }


  public void setMaxInclusive(Number maxInclusive) {
    this.maxInclusive = maxInclusive;
  }


  public Number getMaxExclusive() {
    return maxExclusive;
  }


  public void setMaxExclusive(Number maxExclusive) {
    this.maxExclusive = maxExclusive;
  }


  public Number getMinInclusive() {
    return minInclusive;
  }


  public void setMinInclusive(Number minInclusive) {
    this.minInclusive = minInclusive;
  }


  public Number getMinExclusive() {
    return minExclusive;
  }


  public void setMinExclusive(Number minExclusive) {
    this.minExclusive = minExclusive;
  }


  public Integer getTotalDigits() {
    return totalDigits;
  }


  public void setTotalDigits(Integer totalDigits) {
    this.totalDigits = totalDigits;
  }


  public Integer getFractionDigits() {
    return fractionDigits;
  }


  public void setFractionDigits(Integer fractionDigits) {
    this.fractionDigits = fractionDigits;
  }


  public XsdType getXsdType() {
    return xsdType!=null || base==null ? xsdType : base.getXsdType();
  }


  public void setXsdType(XsdType xsdType) {
    this.xsdType = xsdType;
  }

  public void setNamespace(String namespace) {
    this.namespace = namespace;
  }

  public String getNamespace() {
    if (namespace == null) {
      int hash = uri.lastIndexOf('#');
      int slash = uri.lastIndexOf('/');
      int delim = Math.max(hash, slash);
      namespace = uri.substring(0, delim+1);
    }
    return namespace;
  }
  

}
