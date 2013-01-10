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
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.StringTokenizer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A utility that scans a file and attempts to extract the URI for a file from its contents.
 * @author Greg McFall
 *
 */
public class FileScanner {
  private static final Logger logger = LoggerFactory.getLogger(FileScanner.class);
  
  private TurtleUriFinder turtleFinder = new TurtleUriFinder();
  private XsdUriFinder xsdFinder = new XsdUriFinder();
  
  /**
   * Return the URI for the resource contained in the given file,
   * or null if the URI cannot be determined from the contents
   * of the file.
   */
  public String extractURI(File file) throws IOException {
    
    LdContentType format = LdContentType.guessContentType(file.getName());
    UriFinder finder =
        (format==LdContentType.TURTLE) ? turtleFinder :
        (format==LdContentType.XSD) ? xsdFinder :
        null;
    
    if (finder == null) {
      return null;
    }
    
    BufferedReader input = new BufferedReader(new FileReader(file));
    try {
      return extractURI(finder, input);
    } finally {
      safeClose(input);
    }
    
  }
  


  private String extractURI(UriFinder finder, BufferedReader input) throws IOException {
    String line = null;
    while ( (line=input.readLine()) != null) {
      String uri = finder.parseURI(line);
      if (uri != null) return uri;
    }
    return null;
  }




  private void safeClose(Reader input) {
    
    try {
      input.close();
    } catch (Throwable oops) {
      logger.warn("Failed to close reader", oops);
    }
    
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

}
