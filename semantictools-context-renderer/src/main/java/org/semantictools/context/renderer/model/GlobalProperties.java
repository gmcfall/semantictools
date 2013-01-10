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
import java.util.HashSet;
import java.util.Set;

public class GlobalProperties  extends BaseDocumentMetadata {
  
  private Set<String> ignoredOntology = new HashSet<String>();
  
  
  public GlobalProperties() {
    authorList = new ArrayList<Person>();
    editorList = new ArrayList<Person>();
    cochairList = new ArrayList<Person>();
    refManager = new DefaultReferenceManager();
    createDefaultReferences();
  }
  



  private void createDefaultReferences() {

    addDefaultReference(
        "[JSON-LD-syntax]",
        "Manu Sporny, Dave Longley, Gregg Kellogg, Markus Lanthaler, Mark Birbeck| Json-LD Syntax 1.0| " +
        "12 July 2012| W3C Working Draft| " +
        "http://www.w3.org/TR/2012/WD-json-ld-syntax-20120712/");
    
    addDefaultReference(
        "[RFC4627]",
        "D. Crockford| The application/json Media Type for JavaScript Object Notation (JSON)|" +
        "Internet RFC 4627| July 2006| http://www.ietf.org/rfc/rfc4627.txt");
    
    addDefaultReference(
        "[CURIE-syntax]", 
        "Mark Birbeck, Shane McCarron| CURIE Syntax 1.0| W3C Working Group Note| 16 December 2010| " +
        "http://www.w3.org/TR/curie/");
    
  }

  private void addDefaultReference(String key, String value) {
    BibliographicReference r = BibliographicReference.parse(value);
    r.setLabel(key);
    refManager.add(r);
  }

  public void addIgnoredOntology(String ontologyURI) {
    ignoredOntology.add(ontologyURI);
  }
  
  public boolean isIgnoredOntology(String ontologyURI) {
    return ignoredOntology.contains(ontologyURI);
  }

  


}
