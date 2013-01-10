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

public enum LdContentType {

  XSD("xsd"),
  TURTLE("ttl"),
  JSON_LD_CONTEXT("json"),
  ENHANCED_CONTEXT("json", JSON_LD_CONTEXT),
  UNKNOWN("???");
  
  private String extension;
  private LdContentType defaultType;
  
  private LdContentType(String extension) {
    this.extension = extension;
  }
  
  private LdContentType(String extension, LdContentType defaultType) {
    this.extension = extension;
    this.defaultType = defaultType;
  }
  
  /**
   * Returns the extension that should be used for assets
   * of this content type.
   */
  public String getExtension() {
    return extension;
  }
  
  /**
   * Returns the content type that should be regarded as the 
   * default format for assets of this type.
   * If this content type is the default, then the return value
   * is this LdContentType instance.
   */
  public LdContentType getDefaultType() {
    return defaultType == null ? this : defaultType;
  }
  
  /**
   * Returns true if this content type is a default content type.
   */
  public boolean isDefaultType() {
    return defaultType==null || defaultType==this;
  }
  
  public static LdContentType guessContentType(String fileName) {
    int dot = fileName.lastIndexOf('.');
    if (dot < 0) {
      return UNKNOWN;
    }
    
    String suffix = fileName.substring(dot+1);
    if (XSD.getExtension().equals(suffix)) return XSD;
    if (TURTLE.getExtension().equals(suffix)) return TURTLE;
    if (JSON_LD_CONTEXT.getExtension().equals(suffix)) return JSON_LD_CONTEXT;
    
    
    return UNKNOWN;
  }
}
