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

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import org.semantictools.jsonld.LdAsset;
import org.semantictools.jsonld.LdAssetManager;
import org.semantictools.jsonld.LdVocabularySaveException;

/**
 * InMemoryVocabularyManager is an LdVocabularyManager that holds a collection
 * of LdVocabulary objects in memory.  
 * @author Greg McFall
 *
 */
public class InMemoryVocabularyManager implements LdAssetManager {
  
  
  
  private Map<String, LdAsset> map = new HashMap<String, LdAsset>();
  private FileScanner scanner = new FileScanner();
  

  @Override
  public LdAsset findAsset(String vocabularyURI) {
    return map.get(vocabularyURI);
  }

  private void saveVocabulary(LdAsset vocab)
      throws LdVocabularySaveException {
    
    map.put(vocab.getURI(), vocab);

  }
  
  /**
   * Recursively scan directories starting with the given root directory and
   * load into memory all XSD and RDF data files.
   * This method assumes that any file with the "xsd" extension is an XML Schema Definition 
   * for RDF datatypes, and any file with the "ttl" extension is an RDF ontology in Turtle format.
   * 
   * @throws IOException 
   * @throws LdVocabularySaveException 
   */
  public void scan(File root) throws IOException, LdVocabularySaveException {
    
    if (!root.isDirectory()) return;
    
    File[] list = root.listFiles();
    if (list == null) return;
    for (int i=0; i<list.length; i++) {
      File file = list[i];
      if (file.isDirectory()) {
        scan(file);
        
      } else if (file.getName().endsWith("xsd")) {
        addFile(LdContentType.XSD, file);
        
      } else if (file.getName().endsWith("ttl")) {
        addFile(LdContentType.TURTLE, file);
      }
    }
    
  }
  

  private void addFile(LdContentType format, File file) throws IOException, LdVocabularySaveException {
    
    String uri = scanner.extractURI(file);
    URL location = new URL("file:///" + file.getAbsolutePath());
    LdAsset vocab = new LdAsset(uri, format, location);
    saveVocabulary(vocab);
    
  }


  interface UriFinder {
    String parseURI(String line);
  }
  
  
  /**
   * A very simple parser which is optimized for speed.
   * Finds the first URI that matches a triple of the form:
   * <pre>
   *   &lt;???&gt; * *:Ontology
   * </pre>
   * where ??? is the URI to be extracted, and * denotes any non-space text.
   * The colon in front of "Ontology" may also be replaced by '#'.
   * @author Greg McFall
   *
   */
  static class TurtleUriFinder implements UriFinder {
    

    private String nextToken(StringTokenizer tokenizer) {
      return tokenizer.hasMoreTokens() ? tokenizer.nextToken() : null;
    }

    @Override
    public String parseURI(String line) {
      
      String uri = null;
      StringTokenizer t = new StringTokenizer(line, " ");
      String subject = nextToken(t);
      nextToken(t);
      String object = nextToken(t);
      
      if (
        subject != null &&
        object != null &&
        subject.startsWith("<") && 
        subject.endsWith(">") &&
        (object.endsWith(":Ontology") || object.endsWith("#Ontology")) 
      ) {
        uri = subject.substring(1, subject.length()-1);
      }
      
      return uri;
    }
    
  }
  
  static class XsdUriFinder implements UriFinder {

    @Override
    public String parseURI(String line) {
      String uri = null;
      if (line.contains("targetNamespace")) {
        int start = line.indexOf('"')+1;
        int end = line.lastIndexOf('"');
        uri = line.substring(start, end);
      }
      
      return uri;
    }
    
  }

  @Override
  public LdAsset findAsset(String assetURI, LdContentType format) {
    LdAsset asset = findAsset(assetURI);
    if ( (asset != null) && (asset.getFormat() != format)) {
      asset = null;
    }
    
    return asset;
  }
}
