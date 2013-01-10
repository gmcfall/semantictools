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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.semantictools.jsonld.LdAsset;
import org.semantictools.jsonld.LdAssetManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LdAssetManagerImpl implements LdAssetManager {
  private static Logger logger = LoggerFactory.getLogger(LdAssetManagerImpl.class);
  
  private static final int MATCHES_SCHEMA = 0x11;
  private static final int MATCHES_XMLNS = 0x111;
  private static final int MATCHES_CONTEXT = 0x2;
  
  private static Pattern schemaPattern = Pattern.compile("<[^:]*:schema");
  private boolean eagerLoading=true;
  
  @Override
  public LdAsset findAsset(String assetURI) {
    return loadAsset(assetURI);
  }
  
  protected LdAsset loadAsset(String assetURI) {

    try {
      URL url = new URL(assetURI);
      
      LdAsset ns = readAsset(assetURI, url);
      return ns;
      
      
    } catch (Throwable oops) {
      logger.error("Failed to download asset: " + assetURI);
      logger.error(oops.getMessage());
      return null;
    }
  }
  
  /**
   * Returns true if this manager will eagerly load the content
   * of an asset whenever the asset is accessed via the {@link LdAssetManagerImpl#findAsset(String) findAsset}
   * method.
   */
  public boolean isEagerLoading() {
    return eagerLoading;
  }
  
  /**
   * Specifies whether the content of an asset should be loaded eagerly from
   * the its location when the asset is accessed from this manager.
   * @param truth
   */
  protected void enableEagerLoading(boolean truth) {
    eagerLoading = truth;
  }

  /**
   * Scan the asset from the given location and attempt to infer the LdContentType from the contents.
   * @param uri  The URI of the asset, or null if the URI is not known.  If the URI is null, then
   * this method will attempt to extract the URI from the asset content.
   * @param location The physical location of the asset, which may be different from its URI.
   * @return The LdAsset from the specified location.  If eager loading is enabled, the content
   * of the asset will be available from {@link LdAsset#getContent()}.
   * @throws IOException
   */
  protected LdAsset readAsset(String uri, URL location) throws IOException {

    URLConnection connection = location.openConnection();
    if (connection instanceof HttpURLConnection) {
      HttpURLConnection http = (HttpURLConnection) connection;
      int status = http.getResponseCode();
      if (status != HttpURLConnection.HTTP_OK) {
        throw new IOException("Unable to access " + uri + ": HTTP Status " + status);
      }
    }
    
    InputStream input = connection.getInputStream();

    LdContentType format = LdContentType.UNKNOWN;
    StringBuilder builder = eagerLoading ? new StringBuilder() : null;
    int state = 0;
    BufferedReader reader = new BufferedReader(new InputStreamReader(input));
    try {
      String line = null;
      while ((line=reader.readLine()) != null) {
        Matcher matcher = schemaPattern.matcher(line);
        if (matcher.matches()) {
          state = state | MATCHES_SCHEMA;
        }
        if (((state&MATCHES_SCHEMA)==MATCHES_SCHEMA) && line.contains("xmlns")) {
          state = state | MATCHES_XMLNS;
        }
        if (((state&MATCHES_XMLNS)==MATCHES_XMLNS) && line.contains("simpleType")) {
          format = LdContentType.XSD;
        }
        if (line.contains("@context")) {
          state = state | MATCHES_CONTEXT;
          format = LdContentType.JSON_LD_CONTEXT;
        }
        
        if (line.contains("@prefix")) {
          format = LdContentType.TURTLE;
        }
        if (format==LdContentType.UNKNOWN && line.contains("http://www.w3.org/2002/07/owl#")) {
          format = LdContentType.TURTLE;
        }
        if (uri == null) {
          uri = parseURI(format, line);
        }
        
        if (eagerLoading) {
          builder.append(line);
          builder.append('\n');
        }
      }
    } finally {
      safeClose(reader);
    }
    
    LdAsset asset = new LdAsset(uri, format, location);
    if (eagerLoading) {
      String content = builder.toString();
      asset.setContent(content);
    }
    
    return asset;
  }

  private String parseURI(LdContentType format, String line) {
    String uri = getJsonContextURI(format, line);
    if (uri != null) return uri;
    
    uri = getTurtleUri(format, line);
    if (uri != null) return uri;
    
    uri = getXsdUri(format, line);
    
    return uri;
  }

  private String getXsdUri(LdContentType format, String line) {
    String uri = null;
    if (format==LdContentType.UNKNOWN  || format==LdContentType.XSD) {
      int mark = line.indexOf("targetNamespace");
      if (mark >= 0) {
        int start = line.indexOf('"', mark + 14 )+1;
        int end = line.indexOf('"', start);
        uri = line.substring(start, end);
      }
    }
    
    return uri;
  }

  private String nextToken(StringTokenizer tokenizer) {
    return tokenizer.hasMoreTokens() ? tokenizer.nextToken() : null;
  }

  private String getTurtleUri(LdContentType format, String line) {
    String uri = null;
    if (format==LdContentType.UNKNOWN || format==LdContentType.TURTLE) {
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
    }
    return uri;
  }

  private String getJsonContextURI(LdContentType format, String line) {

    if (format != LdContentType.JSON_LD_CONTEXT && line.contains("\"@id\"")) {
      int colon = line.indexOf(':');
      if (colon > 0) {
        int begin = line.indexOf('"', colon+1)+1;
        if (begin > 0) {
          int end = line.indexOf('"', begin+1);
          if (end > 0) {
            return line.substring(begin, end);
          }
        }
        
      }
      
    }
    return null;
  }

  protected void safeClose(Reader input) {
    
    try {
      input.close();
    } catch (Throwable oops) {
      logger.warn("Failed to close connection", oops);
    }
    
  }
  
  protected void load(LdAsset asset) {
    try {
      if (asset.getContent() == null) {
        StringBuilder builder = new StringBuilder();
        String line = null;
        BufferedReader reader = asset.getReader();
        try {
          while ( (line=reader.readLine()) != null) {
            builder.append(line);
            builder.append('\n');
          }
        } finally {
          safeClose(reader);
        }
        String content = builder.toString();
        asset.setContent(content);
        
      }
      
    } catch (Throwable oops) {
      logger.warn("Failed to load asset: " + asset.getURI(), oops);
    }
    
  }

  @Override
  public LdAsset findAsset(String assetURI, LdContentType format) {
    return loadAsset(assetURI, format);
  }
  
  protected LdAsset loadAsset(String assetURI, LdContentType format) {
    if (format == null) {
      return loadAsset(assetURI);
    }
    if (format == LdContentType.ENHANCED_CONTEXT) {
      // It would be nice to implement content negotiation for retrieving an enhanced
      // context.  However, at the time this code was written, there was no standard
      // content type for an enhanced context.  So we'll just return null.
      // To get an enhanced context, the caller will need to get the unenhanced context
      // and then use an LdContextEnhancer to enhance it.
      return null;
    }
    LdAsset asset = loadAsset(assetURI);
    if ( (asset!=null) && (asset.getFormat()!=format)) {
      asset = null;
    }
    return asset;
  }


}
