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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JsonContext {
  private String contextURI;
  private String mediaType;
  private String rootType;

  private Map<String, TermInfo> termName2TermInfo = new HashMap<String, TermInfo>();
  private Map<String, String> rewriteMap = new HashMap<String, String>();
  
  
  public String getRootType() {
    return rootType;
  }

  public void setRootType(String rootType) {
    this.rootType = rootType;
  }

  
  
  public TermInfo getTermInfoByShortName(String shortName) {
    return termName2TermInfo.get(shortName);
  }
  
  public TermInfo getTermInfoByURI(String uri) {
    if (uri == null) return null;
    String shortName = rewrite(uri);
    return getTermInfoByShortName(shortName);
  }
  
  public String getContextURI() {
    return contextURI;
  }

  public void setContextURI(String contextURI) {
    this.contextURI = contextURI;
  }

  public String getMediaType() {
    return mediaType;
  }

  public void setMediaType(String mediaType) {
    this.mediaType = mediaType;
  }
  
  public TermInfo add(String term, String iri) {
    TermInfo info = new TermInfo(term);
    info.setIriValue(iri);
    add(info);
    return info;
  }
  public void add(TermInfo rule) {
    termName2TermInfo.put(rule.getTermName(), rule);
    addRewriteRule(rule);
  }

  private void addRewriteRule(TermInfo rule) {
    String key = rule.getTermName();
    String value = rule.hasIriValue() ? rule.getIri() : rule.getObjectValue().getId();
    rewriteMap.put(key, value);
    
  }

  public boolean containsTerm(String term) {
    return termName2TermInfo.get(term) != null;
  }
  
  public List<TermInfo> getTerms() {
    return new ArrayList<TermInfo>(termName2TermInfo.values());
  }
  
  
  public String toAbsoluteIRI(String token) {
    if (token.indexOf('/') > 0) {
      return token;
    }
    return rewrite(token);
  }

  public String rewrite(String fieldName) {
    
    int colon = fieldName.indexOf(':');
    if (colon >= 0) {
      String prefix = fieldName.substring(0, colon);
      String namespace = rewriteMap.get(prefix);
      if (namespace != null) {
        String localName = fieldName.substring(colon+1);
        return namespace + localName;
      }
    }
    
    String replacement = rewriteMap.get(fieldName);
    if (replacement != null) {
      return replacement;
    }
    
    
    
    return fieldName;
  }

  
  public void expand() {
    expand(rewriteMap);
  }


  private void expand(Map<String, String> map) {
    
    List<Map.Entry<String, String>> entryList = new ArrayList<Map.Entry<String,String>>( map.entrySet() );
    for (Map.Entry<String, String> entry : entryList) {
      String value = entry.getValue();
      
      int colon = value.indexOf(':');
      if (colon < 0) continue;
      
      String prefix = value.substring(0, colon);
      
      String uri = rewrite(prefix);
      if (prefix.equals(uri)) continue;
      
      String suffix = value.substring(colon + 1);
      String replacement = uri + suffix;
      String key = entry.getKey();
      
      map.put(key, replacement);
    }
  }

  /**
   * Generates rewrite rules that map URIs to simple names.
   * Normally, one uses addRewriteRule to map a simple name to a URI.
   * This method creates the inverse mappings -- from URI to simple names.
   * Thus, after calling invertRewriteRules, you can invoke rewrite(..) on
   * a URI to get it's simple name.
   * 
   */
  public void invertRewriteRules() {
    List<Map.Entry<String, String>> list = new ArrayList<Map.Entry<String,String>>(rewriteMap.entrySet());
    
    for (Map.Entry<String, String> entry : list) {
      String key = entry.getKey();
      String value = entry.getValue();
      rewriteMap.put(value, key);
    }
  }
  

}
