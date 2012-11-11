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
package org.semantictools.context.renderer.model;

public class TermInfo implements Comparable<TermInfo> {
  
  public static enum TermCategory {
    NAMESPACE,
    TYPE,
    PROPERTY,
    UNKNOWN
  }
  
  private TermCategory category = TermCategory.UNKNOWN;
  private String termName;
  private String iri;
  private TermValue value;
  
  
  public TermInfo(String termName) {
    this.termName = termName;
    if (termName == null) {
      throw new RuntimeException("termName must be non-null");
    }
  }
  public TermCategory getCategory() {
    return category;
  }
  public void setCategory(TermCategory category) {
    this.category = category;
  }
  public String getTermName() {
    return termName;
  }
  public void setTermName(String term) {
    this.termName = term;
  }
  
  public boolean hasIriValue() {
    return iri != null;
  }
  
  public boolean isCoercedAsIriRef() {
    return value != null && "@id".equals(value.getType());
  }
  
  public boolean hasObjectValue() {
    return value != null;
  }
  
  public String getIri() {
    return value==null ? iri : value.getType();
  }
  public void setIriValue(String iri) {
    this.iri = iri;
  }
  public TermValue getObjectValue() {
    return value;
  }
  public void setObjectValue(TermValue target) {
    this.value = target;
  }
  @Override
  public int compareTo(TermInfo other) {
    int diff = category.ordinal() - other.category.ordinal();
    if (diff == 0) {
      diff = termName.compareTo(other.termName);
    }
    return diff;
  }
  
  public String toString() {
    return getTermName();
  }
  
  

  
  

}
