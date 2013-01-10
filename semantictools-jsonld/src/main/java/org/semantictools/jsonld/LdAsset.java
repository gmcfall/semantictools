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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.io.StringReader;
import java.net.URL;

import org.semantictools.jsonld.impl.LdContentType;

/**
 * LdAsset provides access to an external asset referenced from a JSON-LD
 * document.  The types of assets include an RDF ontology, XML Schema 
 * (containing simpleType definitions), or a JSON-LD context.
 * 
 * @author Greg McFall
 *
 */
public class LdAsset implements Serializable {
  private static final long serialVersionUID = 1L;


  private String URI;
  private LdContentType format;
  private URL location;
  private String content;
  
  

  public LdAsset(String uri, LdContentType format, URL location) {
    URI = uri;
    this.format = format;
    this.location = location;
  }

  /**
   * Returns the logical URI for the asset.
   * This may be different from the physical location of the asset.
   * Use {@link #getLocation()} to obtain the physical location.
   */
  public String getURI() {
    return URI;
  }
  
  /**
   * Returns for the format of the data in this vocabulary
   */
  public LdContentType getFormat() {
    return format;
  }

  /**
   * Returns a URI that can be used to access the asset.
   * This is the physical location of the asset, as opposed to the
   * logical identifier given by {@link #getURI()}
   */
  public URL getLocation() {
    return location;
  }

  /**
   * Returns a reader to access the contents of the asset.
   */
  public BufferedReader getReader() throws IOException {
   return (content == null) ?
       new BufferedReader(new InputStreamReader(location.openStream())) :
       new BufferedReader(new StringReader(content));
  }
  
  /**
   * Returns the content of the asset, or null
   * if the content has not been loaded.
   */
  public String getContent() {
    return content;
  }

  public void setContent(String content) {
    this.content = content;
  }
  
  /**
   * Returns the content of the asset as a string, loading
   * the content from its URL if necessary.
   * The content will be stored witin this LdAsset object internally
   * as a side-effect.  If the caller wishes to free the memory
   * consumed by the content, she should call setContent(null).
   */
  public String loadContent() throws IOException {
    if (content == null) {
      StringBuilder buffer = new StringBuilder();
      
      BufferedReader reader = getReader();
      try {
        String line = null;
        while ( (line=reader.readLine()) != null) {
          buffer.append(line);
          buffer.append('\n');
        }
      } finally {
        reader.close();
      }
      content = buffer.toString();
      
    }
    return content;
  }

  public String toString() {
    return "LdAsset(" + URI + ")";
  }
  
}
