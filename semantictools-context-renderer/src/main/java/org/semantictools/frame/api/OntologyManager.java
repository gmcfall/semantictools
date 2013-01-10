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
package org.semantictools.frame.api;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.FileEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.semantictools.context.renderer.model.ContextProperties;
import org.semantictools.context.renderer.model.OntologyEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.Ontology;
import com.hp.hpl.jena.rdf.model.ModelFactory;

public class OntologyManager {
  private static final String PROPERTIES_FILENAME = "asset.properties";
  private static final String URI = "uri";
  private static final String DEFAULT = "default";
  
  private static final String TURTLE_FORMAT = "text/turtle";
  private static final String XML_FORMAT = "text/xml";
  
  private static final Logger logger = LoggerFactory.getLogger(OntologyManager.class);
  private String ontologyServiceURI;

  private Map<String, OntologyEntity> uri2OntologyEntity = new HashMap<String, OntologyEntity>();
  private List<String> uploadList = new ArrayList<String>();
  private File localRepository;
  
  /**
   * Returns the URI to which ontology files should be uploaded.
   */
  public String getOntologyServiceURI() {
    return ontologyServiceURI;
  }
  
  
  /**
   * Returns the root directory for the local repository
   */
  public File getLocalRepository() {
    return localRepository;
  }


  /**
   * Sets the root directory for the local repository
   * @param localRepository
   */
  public void setLocalRepository(File localRepository) {
    this.localRepository = localRepository;
  }



  public void publishToLocalRepository(List<ContextProperties> contextList) {
    if (localRepository == null) return;
    publishSchemasToLocalRepo();
    publishJsonLdContextsToLocalRepo(contextList);
  }

  private void publishJsonLdContextsToLocalRepo(List<ContextProperties> contextList) {
    
    for (ContextProperties p : contextList) {
      File contextFile = p.getContextFile();
      String uri = p.getContextURI();
      if (contextFile == null || uri==null) continue;
      
      File repoDir = repoDir(uri);
      File targetFile = writeAssetPropertiesFile(repoDir, uri, LdContentType.JSON_LD_CONTEXT);
      try {
        copyFile(contextFile, targetFile);
      } catch (IOException e) {
        logger.error("Failed to copy file " + contextFile, e);
      }
      
    }
  }

  private void publishSchemasToLocalRepo() {

    for (OntologyEntity entity : uri2OntologyEntity.values()) {
      String uri = entity.getOntologyURI();
      File repoDir = repoDir(uri);
      
       LdContentType format = contentType(entity);
      
      File targetFile = writeAssetPropertiesFile(repoDir, uri, format);
      try {
        copyFile(entity.getFile(), targetFile);
      } catch (IOException e) {
        logger.error("Failed to copy file " + entity.getFile().getName(), e);
      }
    }
    
  }

  private LdContentType contentType(OntologyEntity entity) {
    
    return 
        TURTLE_FORMAT.equals(entity.getContentType()) ? LdContentType.TURTLE : 
        XML_FORMAT.equals(entity.getContentType())    ? LdContentType.XSD :
        null;
  }


  private File writeAssetPropertiesFile(File repoDir, String uri,  LdContentType contentType) {
    repoDir.mkdirs();
    String format = contentType.name();
    String fileName = contentType.repoFileName();
    Properties properties = new Properties();
    properties.setProperty(URI, uri);
    properties.setProperty(DEFAULT, format);
    properties.setProperty(format, fileName);
    FileWriter writer = null;
    try {
      File file = new File(repoDir, PROPERTIES_FILENAME);
      writer = new FileWriter(file);
      properties.store(writer, null);
    } catch (Throwable oops) {
      logger.error("Failed to save properties at " + repoDir, oops);
    } finally {
      safeClose(writer);
    }
    return new File(repoDir, fileName);

  }

  private void safeClose(FileWriter writer) {
    if (writer == null) return;
    try {
      writer.close();
    } catch (Throwable oops) {
      logger.warn("failed to close writer", oops);
    }
    
  }

  private void copyFile(File sourceFile, File targetFile) throws IOException  {
    InputStream input = new FileInputStream(sourceFile);
    
    File parent = targetFile.getParentFile();
    parent.mkdirs();
    FileOutputStream out = new FileOutputStream(targetFile);
    try {
      byte[] buffer = new byte[1024];
      
      int len;
      while ( (len = input.read(buffer)) > 0) {
        out.write(buffer, 0, len);
      }
      
    } finally {
      out.close();
    }
    input.close();
    
  }
  
  private File repoDir(String assetURI) {
    try {
      URI uri = new URI(assetURI);
      String path = uri.getAuthority() + "/" + uri.getPath();
      
      return new File(localRepository, path);
    } catch (Throwable oops) {
      return null;
    }
  }
  
 


  /**
   * Sets the URI to which ontology files should be uploaded.
   */
  public void setOntologyServiceURI(String ontologyServiceURI) {
    this.ontologyServiceURI = ontologyServiceURI;
  }

  /**
   * Returns the list of URI values for ontologies whose Turtle or XSD files are to be uploaded 
   * to the ontology service.
   */
  public List<String> getUploadList() {
    return uploadList;
  }
  
  public void uploadJsonLdContextFiles(List<ContextProperties> list) {
    if (ontologyServiceURI == null) return;
    for (ContextProperties p : list) {
      File file = p.getContextFile();
      if (file != null) {
        try {
          uploadFile(file, "application/ld+json");
        } catch (Throwable e) {
          logger.warn("Failed to upload file", e);
        } 
      }
    }
  }

  /**
   * Scan the specified directory for schemas, and upload them to the ontology service,
   * but only if they are included in the upload list.
   * 
   * @param rdfDir The directory that should be scanned for schema files.
   * @return The number of files uploaded.
   * @throws SchemaParseException
   * @throws IOException 
   */
  public int upload() throws SchemaParseException, IOException {
    if (ontologyServiceURI == null || uploadList.isEmpty()) return 0;
    
    Collections.sort(uploadList);
    int count = 0;
    
    
    for (String ontologyURI  : uploadList) {
      OntologyEntity entity = uri2OntologyEntity.get(ontologyURI);
      if (entity == null) {
        logger.warn("Cannot upload ontology because file not found: " + ontologyURI);
        continue;
      }
      try {
        uploadFile(entity.getFile(), entity.getContentType());
        count++;
      } catch (Throwable oops) {
        logger.warn("Failed to upload " + entity.getFile(), oops);
      }
    }
    return count;
  }
  
  private void uploadFile(File file, String contentType) throws ClientProtocolException, IOException {
    System.out.println("Uploading... " + file);

    HttpClient client = new DefaultHttpClient();
    HttpPost post = new HttpPost(ontologyServiceURI);

    FileEntity fileEntity = new FileEntity(file, contentType);
    post.setEntity(fileEntity);
    
    HttpResponse response = client.execute(post);
    int status = response.getStatusLine().getStatusCode();
    switch (status) {
    case HttpStatus.SC_OK :
    case HttpStatus.SC_CREATED :
      
      break;
      
    default:
      System.out.println(" ERROR: " + status);
        
    }
  }
  
  public void scan(File file) throws SchemaParseException {
    if (localRepository==null && ontologyServiceURI==null) return;
    if (file.isDirectory()) {
      for (File child : file.listFiles()) {
        scan(child);
      }
    } else {
      String fileName = file.getName();
      if (fileName.endsWith(".xsd")) {
        loadXsd(file);
      } else if (fileName.endsWith(".ttl")) {
        loadTurtle(file);
      }
    }
  }

  private void loadTurtle(File file) throws SchemaParseException {
    if (file.getName().endsWith("_binding.ttl")) {
      // For now, ignore binding files.
      return;
    }
    try {
      OntModel model = ModelFactory.createOntologyModel();
      FileReader reader = new FileReader(file);
      model.read(reader, null, "TURTLE");
      
      List<Ontology> list = model.listOntologies().toList();
      if (list.isEmpty()) {
        logger.warn("Ignoring file because it contains no ontology declarations: " + file);
      } else if (list.size() == 1) {
        Ontology onto = list.get(0);
        String ontologyURI = onto.getURI();
        OntologyEntity entity = new OntologyEntity(TURTLE_FORMAT, file, ontologyURI);
        uri2OntologyEntity.put(ontologyURI, entity);
        
      } else {
        logger.warn("Ignoring file because it contains more than one ontology: " + file);
      }
    } catch (Throwable oops) {
      throw new SchemaParseException(oops);
    }
    
  }

  private void loadXsd(File file) throws SchemaParseException {
    try {
      SAXParserFactory factory = SAXParserFactory.newInstance();
      SAXParser parser = factory.newSAXParser();
      XMLReader reader = parser.getXMLReader();
      reader.setFeature("http://xml.org/sax/features/namespaces", true);
      NamespaceReader handler = new NamespaceReader();
      reader.setContentHandler(handler);
      
      parser.parse(file, handler);
      
      String namespace = handler.getTargetNamespace();
      if (namespace == null) {
        logger.warn("Ignoring schema since targetNamespace is not declared: " + file.getPath());
      } else {
        OntologyEntity entity = new OntologyEntity(XML_FORMAT, file, namespace);
        uri2OntologyEntity.put(namespace, entity);
      }
    } catch (Throwable oops) {
      throw new SchemaParseException(oops);
    }
    
  }
  
  
  
  private class NamespaceReader extends DefaultHandler {

    private String targetNamespace;
    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
      if ("schema".equals(localName)) {
        targetNamespace = attributes.getValue("targetNamespace");
      }
    }
    
    public String getTargetNamespace() {
      return targetNamespace;
    }
    
  }
  
  

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
    
    public String repoFileName() {
      return name() + "." + extension;
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
}
