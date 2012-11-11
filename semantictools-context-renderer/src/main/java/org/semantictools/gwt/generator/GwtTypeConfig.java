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
package org.semantictools.gwt.generator;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class GwtTypeConfig {
  private static final String INCLUDE = "include";
  private static final String EXCLUDE = "exclude";
  private static final String USE_JAVASCRIPT_OBJECT = "useJavaScriptObject";
  private Set<String> includes = new HashSet<String>();
  private Set<String> useJavaScriptObject = new HashSet<String>();
  private Set<String> excludeProperty = new HashSet<String>();

  /**
   * Returns the set of types that are included for output.
   */
  public Set<String> getIncludes() {
    return includes;
  }
  
  /**
   * Returns true if the specified type is included for output.
   */
  public boolean includeType(String typeURI) {
    return includes.isEmpty() || includes.contains(typeURI);
  }
  
  public void addType(String typeURI) {
    includes.add(typeURI);
  }
  
  /**
   * Returns true if the specified property should be accessed as a JavaScriptObject
   * instead of a more specific type.
   */
  public boolean useJavaScriptObject(String typeURI, String propertyLocalName) {
    return useJavaScriptObject.contains(typeURI + "!" + propertyLocalName);
  }
  
  /**
   * Returns true if the specified property is excluded from the specified type.
   */
  public boolean excludeProperty(String typeURI, String propertyLocalName) {
    return excludeProperty.contains(typeURI + "!" + propertyLocalName);
  }
  
  
  /**
   * Load configuration properties from the given file.
   */
  public void load(File file) throws IOException, ConfigParseException {
    String name = file.getName();
    if (name.endsWith(".properties")) {
      parseProperties(file);
      
    } else if (name.endsWith(".xml")) {
      try {
        parseXml(file);
      } catch (ParserConfigurationException e) {
        throw new ConfigParseException(e);
      } catch (SAXException e) {
        throw new ConfigParseException(e);
      }
    }
  }
  
  private void parseProperties(File file) throws IOException {
    Properties properties = new Properties();
    InputStream input = new FileInputStream(file);
    try {
      properties.load(input);
      
      parseProperties(properties);
      
    } finally {
      input.close();
    }
    
  }

  private void parseXml(File file) throws IOException, ParserConfigurationException, SAXException {
    DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
    dbFactory.setNamespaceAware(true);
    
    DocumentBuilder builder = dbFactory.newDocumentBuilder();
    Document doc = builder.parse(file);
    
    parseXml(doc);
    
  }
  
  private void parseXml(Document doc) {

    Element root = doc.getDocumentElement();
    
    NodeList list = root.getChildNodes();
    for (int i=0; i<list.getLength(); i++) {
      Node n = list.item(i);
      if (n instanceof Element) {
        Element e = (Element) n;
        String name = e.getTagName();
        if (INCLUDE.equals(name)) {
          parseInclude(e);
        }
      }
    }
    
    
  }

  private void parseInclude(Element e) {
    
    NodeList list = e.getChildNodes();
    for (int i=0; i<list.getLength(); i++) {
      Node n = list.item(i);
      if (n instanceof Element) {
        Element child = (Element) n;
        StringBuilder buffer = new StringBuilder();
        buffer.append(child.getNamespaceURI());
        buffer.append(child.getLocalName());
        String uri = buffer.toString();
        addType(uri);
        
        parseTypeConfig(uri, child);
      }
    }
    
  }

  private void parseTypeConfig(String typeURI, Element type) {
    
    NodeList list = type.getChildNodes();
    for (int i=0; i<list.getLength(); i++) {
      Node n = list.item(i);
      String name = n.getNodeName();
      
      if (USE_JAVASCRIPT_OBJECT.equals(name)) {
        parseUseJavaScriptObject(typeURI, (Element)n);
      }
      if (EXCLUDE.equals(name)) {
        parseExcludeProperties(typeURI, (Element) n);
      }
    }
    
    
  }

  private void parseExcludeProperties(String typeURI, Element container) {
    NodeList list = container.getChildNodes();
    for (int i=0; i<list.getLength(); i++) {
      Node n = list.item(i);
      if (n instanceof Element) {
        Element e = (Element) n;
        String localName = e.getLocalName();
        StringBuilder builder = new StringBuilder();
        builder.append(typeURI);
        builder.append('!');
        builder.append(localName);
        String path = builder.toString();
        excludeProperty.add(path);
      }
    }
    
    
  }

  private void parseUseJavaScriptObject(String typeURI, Element container) {
    
    NodeList list = container.getChildNodes();
    for (int i=0; i<list.getLength(); i++) {
      Node n = list.item(i);
      if (n instanceof Element) {
        Element e = (Element) n;
        String localName = e.getLocalName();
        StringBuilder builder = new StringBuilder();
        builder.append(typeURI);
        builder.append('!');
        builder.append(localName);
        String path = builder.toString();
        useJavaScriptObject.add(path);
      }
    }
    
  }

  /**
   * Scan the given directory and any subdirectories recursively for files
   * named "gwt.properties" and load those files into this configuration object.
   * @throws ConfigParseException 
   */
  public void scan(File dir) throws IOException, ConfigParseException {
    if (dir.isDirectory()) {
      File[] list = dir.listFiles();
      for (int i=0; i<list.length; i++) {
        scan(list[i]);
      }
    } else if (
      "gwt.properties".equals(dir.getName()) ||
      "gwt.xml".equals(dir.getName())
    ){
      load(dir);
    }
  }

  private void parseProperties(Properties properties) {
    
    for (Map.Entry<Object, Object> entry : properties.entrySet() ) {
      String key = entry.getKey().toString();
      String value = entry.getKey().toString();
      if (INCLUDE.equals(key)) {
        setInclude(value);
      }
    }
  }

  private void setInclude(String value) {
    StringTokenizer tokens = new StringTokenizer(value, " \t\r\n");
    while (tokens.hasMoreTokens()) {
      includeType(tokens.nextToken());
    }
    
  }
  
  
 
  
  

}
