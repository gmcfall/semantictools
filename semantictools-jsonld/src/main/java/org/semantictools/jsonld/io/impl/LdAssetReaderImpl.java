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
package org.semantictools.jsonld.io.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.semantictools.jsonld.LdAsset;
import org.semantictools.jsonld.impl.LdContentType;
import org.semantictools.jsonld.io.LdAssetReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LdAssetReaderImpl implements LdAssetReader {
  private static final Logger logger = LoggerFactory.getLogger(LdAssetReaderImpl.class);

  private static final int MATCHES_SCHEMA = 0x11;
  private static final int MATCHES_XMLNS = 0x111;
  private static final int MATCHES_CONTEXT = 0x2;
  
  private static Pattern schemaPattern = Pattern.compile("<[^:]*:schema");
  
  
  @Override
  public LdAsset readAsset(BufferedReader reader, boolean preserveContent) throws IOException {
    try {
      StringBuilder builder = preserveContent ? new StringBuilder() : null;
      int state = 0;
      String uri = null;
      int depth=0;
      LdContentType format = LdContentType.UNKNOWN;
      String line = null;
      while ((line=reader.readLine()) != null) {
        depth += deltaDepth(line);
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
          uri = parseURI(format, line, depth);
        }
        
        if (preserveContent) {
          builder.append(line);
          builder.append('\n');
        }
      }

      LdAsset asset = new LdAsset(uri, format, null);
      if (preserveContent) {
        String content = builder.toString();
        asset.setContent(content);
      }
      return asset;
    } finally {
      safeClose(reader);
    }
    
  }


  private int deltaDepth(String line) {
    int count = 0;
    for (int i=0; i<line.length(); i++) {
      char c = line.charAt(i);
      if (c == '{') count++;
      if (c == '}') count--;
    }
    return count;
  }

  protected void safeClose(Reader input) {
    
    try {
      input.close();
    } catch (Throwable oops) {
      logger.warn("Failed to close connection", oops);
    }
    
  }
  

  private String parseURI(LdContentType format, String line, int depth) {
    String uri = getJsonContextURI(format, line, depth);
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

  private String getJsonContextURI(LdContentType format, String line, int depth) {
    if (depth == 1) {
      int index = line.indexOf("@id");
      if (index > 0) {
        for (int i=index+4; i<line.length(); i++) {
          char c = line.charAt(i);
          if (c == '\'' || c == '"') {
            int begin = i+1;
            int end = line.indexOf(c, begin);
            if (end > 0) {
              return line.substring(begin, end);
            }
          }
        }
      }
      
    }
    return null;
  }

}
