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
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;

import org.semantictools.context.renderer.MediaTypeFileManager;
import org.semantictools.context.renderer.model.BibliographicReference;
import org.semantictools.context.renderer.model.ContextProperties;
import org.semantictools.context.renderer.model.DocumentMetadata;
import org.semantictools.context.renderer.model.FrameConstraints;
import org.semantictools.context.renderer.model.Person;
import org.semantictools.context.renderer.model.ReferenceManager;
import org.semantictools.context.renderer.model.SampleJson;

public class ContextManager {
  
//  private static final Logger logger = LoggerFactory.getLogger(ContextManager.class);

  private static final String RDFTYPE = "rdfType";
  private static final String RDFTYPE_REF = "rdfTypeRef";
  private static final String RDF_PROPERTY = "rdfProperty";
  private static final String MEDIATYPE = "mediaType";
  private static final String MEDIATYPEURI = "mediaTypeURI";
  private static final String MEDIATYPEREF = "mediaTypeRef";
  private static final String CONTEXTURI = "contextURI";
  private static final String CONTEXTREF = "contextRef";
  private static final String GRAPH_TYPES = "graphTypes";
  private static final String USE_PREFIX = "usePrefix";
  private static final String SET = "@set";
  private static final String ENABLE_VERSION_HISTORY = "enableVersionHistory";
  private static final String IDREF = "idref";
  private static final String REQUIRES_ID = "requiresId";
  private static final String MIXED_VALUE = "mixedValue";
  private static final String STATUS = "status";
  private static final String DATE = "date";
  private static final String ABSTRACT = "abstract";
  private static final String EDITORS = "editors";
  private static final String AUTHORS = "authors";
  private static final String TITLE = "title";
  private static final String SAMPLE_TEXT = "sampleText";
  private static final String EXCLUDE_TYPE = "excludeType";
  private static final String INTRODUCTION = "introduction";
  private static final String INCLUDES_SUFFIX = ".includes";
  private static final String EXCLUDES_SUFFIX = ".excludes";
  private static final String EXCLUDE_SUBTYPES_SUFFIX = ".excludeSubtypes";
  private static final String PURL_DOMAIN = "purlDomain";
  private static final String EXPANDED_VALUE = "expandedValue";
  private static final String CAPTION_SUFFIX = ".caption";
  private static final String TEMPLATE = "template";
  
  private MediaTypeFileManager fileManager;
  private Map<String, ContextProperties> contextMap = new HashMap<String, ContextProperties>();
  
  private DocumentMetadata defaultMetadata;
  
  public ContextManager(DocumentMetadata defaultMetadata, MediaTypeFileManager fileManager) {
    this.fileManager = fileManager;
    this.defaultMetadata = defaultMetadata;
  }
  
  public MediaTypeFileManager getMediaTypeFileManager() {
    return fileManager;
  }

  /**
   * Loads ContextProperties from a properties file.
   * The file must contain properties of the following form:
   * <UL>
   *   <LI> *.rdfType = &lt;The fully qualified URI for the RDF class to which the representation applies&gt;
   *   <LI> *.mediaType = &lt;The MIME type for the JSON-LD context&gt;
   *   <LI> *.contextURI = &lt;The fully qualified URI at which the JSON-LD context will be published&lt;
   *   <LI> *.idref = &lt;A space separated list of URIs for properties that will be coerced to "@id" values in the JSON-LD context&gt;
   * </UL>
   * @param propertiesFile
   * @throws IOException
   */
  public void loadContextProperties(File propertiesFile) 
    throws IOException, ContextPropertiesSyntaxException {
    Properties properties = new Properties();
    FileInputStream input = new FileInputStream(propertiesFile);
    try {
      properties.load(input);
      parseProperties(propertiesFile, properties);
    } finally {
      input.close();
    }
  }
  
  public void loadAll(File baseDir) throws ContextPropertiesSyntaxException, IOException {
    File[] list = baseDir.listFiles();
    if (list == null) return;
    for (int i=0; i<list.length; i++) {
      File file = list[i];
      if (file.isDirectory()) {
        loadAll(file);
      } else if ("context.properties".equals(file.getName())) {
        loadContextProperties(file);
      }
    }
  }
  
  /**
   * Generates the default name for a media type that contains the identifier
   * for a resource.  This is used as the media type for the response from a
   * POST method.
   * @param baseMediaType  The base media type for the resource
   */
  public String createIdMediaTypeName(String baseMediaType) {
    int plus = baseMediaType.lastIndexOf('+');
    if (plus < 0) return baseMediaType + ".id";
    return baseMediaType.substring(0, plus) + ".id" + baseMediaType.substring(plus);
  }
  
  public String createIdMediaTypeRef(String typeName) {
    return "[" + typeName + "-id-media-type]";
  }
  
  public ContextProperties getContextPropertiesByMediaType(String mediaType) {
    return contextMap.get(mediaType);
  }
  
  public List<ContextProperties> listContextPropertiesForClass(String rdfClassURI) {
    List<ContextProperties> list = new ArrayList<ContextProperties>();
    for (ContextProperties p : contextMap.values()) {
      if (rdfClassURI.equals(p.getRdfTypeURI())) {
        list.add(p);
      }
    }
    return list;
  }
  
  public List<ContextProperties> listContextProperties() {
    return new ArrayList<ContextProperties>( contextMap.values() );
  }

  private void parseProperties(File sourceFile, Properties properties) {
    ContextProperties sink = new ContextProperties(defaultMetadata, properties);
    sink.setSourceFile(sourceFile);
    
    for (Map.Entry<Object, Object> e : properties.entrySet()) {
      
      String key = e.getKey().toString();
      String value = e.getValue().toString();
      
      if (key.startsWith("[")) {
        sink.putReference(key, value);
        continue;
      }
      
      
      if (CONTEXTURI.equals(key)) {
        sink.setContextURI(value);
      } else if (IDREF.equals(key)) {
        setIdref(sink, value);
      } else if (REQUIRES_ID.equals(key)) {
        setRequiresId(sink, value);
      } else if (MIXED_VALUE.equals(key)) {
        setMixedValue(sink, value);
      } else if (MEDIATYPE.equals(key)) {
        sink.setMediaType(value);
      } else if (MEDIATYPEURI.equals(key)) {
        sink.setMediaTypeURI(value);
      } else if (MEDIATYPEREF.equals(key)) {
        sink.setMediaTypeRef(value);
      } else if (USE_PREFIX.equals(key)) {
        setUsePrefix(sink, value);
      } else if (RDFTYPE.equals(key)) {
        sink.setRdfTypeURI(value);
      } else if (RDFTYPE_REF.equals(key)) {
        sink.setRdfTypeRef(value);
      } else if (RDF_PROPERTY.equals(key)) {
        sink.setRdfProperty(value);
      } else if (GRAPH_TYPES.equals(key)) {
        setGraphTypes(sink, value);
      } else if (SET.equals(key)) {
        setSetProperties(sink, value);
      } else if (CONTEXTREF.equals(key)) {
        sink.setContextRef(value);
      } else if (STATUS.equals(key)) {
        sink.setStatus(value);
      } else if (DATE.equals(key)) {
        sink.setDate(value);
      } else if (ABSTRACT.equals(key)) {
        sink.setAbstactText(value);
      } else if (ENABLE_VERSION_HISTORY.equals(key)) {
        sink.setHistoryLink("true".equalsIgnoreCase(value));
      } else if (EDITORS.equals(key)) {
        setEditors(sink, value);
      } else if (AUTHORS.equals(key)) {
        setAuthors(sink, value);
      } else if (INTRODUCTION.equals(key)) {
        sink.setIntroduction(value);
      } else if (TITLE.equals(key)) {
        sink.setTitle(value);
      } else if (SAMPLE_TEXT.equals(key)) {
        sink.setSampleText(value);
      } else if (EXCLUDE_TYPE.equals(key)) {
        setExcludedTypes(sink, value);        
      } else if (key.endsWith(INCLUDES_SUFFIX)) {
        addIncludesConstraint(sink, key, value);
      } else if (key.endsWith(EXCLUDES_SUFFIX)) {
        addExcludesConstraint(sink, key, value);
      } else if (key.endsWith(EXCLUDE_SUBTYPES_SUFFIX)) {
        addExcludeSubtypesConstraint(sink, key, value);
      } else if (key.endsWith(CAPTION_SUFFIX)) {
        addCaption(sink, key, value);
      } else if (PURL_DOMAIN.equals(key)) {
        sink.setPurlDomain(value);
      } else if (EXPANDED_VALUE.equals(key)) {
        setExpandedValue(sink, value);
      } else if (TEMPLATE.equals(key)) {
        sink.setTemplateName(value);
      }
    }
    validate(sink);
    setDefaults(sink);
    
  }



  private void setUsePrefix(ContextProperties sink, String value) {
    StringTokenizer tokenizer = new StringTokenizer(value, " \t\r\n");
    while (tokenizer.hasMoreElements()) {
      sink.addUsePrefix(tokenizer.nextToken());
    }    
  }

  private void setSetProperties(ContextProperties sink, String value) {
    StringTokenizer tokenizer = new StringTokenizer(value, " \t\r\n");
    while (tokenizer.hasMoreElements()) {
      sink.addSetProperty(tokenizer.nextToken());
    }
  }

  private void setGraphTypes(ContextProperties sink, String value) {

    StringTokenizer tokenizer = new StringTokenizer(value, " \t\r\n");
    while (tokenizer.hasMoreElements()) {
      sink.addGraphType(tokenizer.nextToken());
    }
  }

  private void setRequiresId(ContextProperties sink, String value) {

    StringTokenizer tokenizer = new StringTokenizer(value, " \t\r\n");
    while (tokenizer.hasMoreElements()) {
      sink.getRequiresId().add(tokenizer.nextToken());
    }
    
  }

  private void setExcludedTypes(ContextProperties sink, String value) {
    StringTokenizer tokenizer = new StringTokenizer(value, " \t\r\n");
    while (tokenizer.hasMoreElements()) {
      sink.getExcludedTypes().add(tokenizer.nextToken());
    }
    
  }

  private void addCaption(ContextProperties sink, String key, String value) {
    int dot = key.lastIndexOf('.');
    String fileName = key.substring(0, dot);
    for (SampleJson sample : sink.getSampleJsonList()) {
      if (fileName.equals(sample.getFileName())) {
        sample.setFileName(value);
        return;
      }
    }
    SampleJson sample = new SampleJson();
    sample.setFileName(fileName);
    sample.setCaption(value);
    sink.getSampleJsonList().add(sample);
    
  }

  private void setExpandedValue(ContextProperties sink, String value) {
    StringTokenizer tokens = new StringTokenizer(value, " \t\r\n");
    while (tokens.hasMoreTokens()) {
      String propertyURI = tokens.nextToken();
      sink.getExpandedValues().add(propertyURI);
    }
    
  }

  private void addIncludesConstraint(ContextProperties sink, String key, String value) {
    int dot = key.lastIndexOf('.');
    String name = key.substring(0, dot);
    
    FrameConstraints constraints = sink.fetchFrameConstraints(name);
    StringTokenizer tokens = new StringTokenizer(value, " \r\n\t");
    while (tokens.hasMoreTokens()) {
      String propertyURI = tokens.nextToken();
      constraints.addIncludedProperty(propertyURI);
    }
    
    
  }

  private void addExcludesConstraint(ContextProperties sink, String key, String value) {
    int dot = key.lastIndexOf('.');
    String name = key.substring(0, dot);
    
    FrameConstraints constraints = sink.fetchFrameConstraints(name);
    StringTokenizer tokens = new StringTokenizer(value, " \r\n\t");
    while (tokens.hasMoreTokens()) {
      String propertyURI = tokens.nextToken();
      constraints.addExcludedProperty(propertyURI);
    }
    
    
  }

  private void addExcludeSubtypesConstraint(ContextProperties sink, String key,
      String value) {
    
    int dot = key.lastIndexOf('.');
    String name = key.substring(0, dot);
    
    FrameConstraints constraints = sink.fetchFrameConstraints(name);
    StringTokenizer tokens = new StringTokenizer(value, " \r\n\t");
    while (tokens.hasMoreTokens()) {
      String propertyURI = tokens.nextToken();
      constraints.addExcludesSubtype(propertyURI);
    }
    
  }

  private void setDefaults(ContextProperties sink) {
    setTitle(sink);
    setMediaTypeDocFile(sink);
    setMediaTypeReference(sink);
//    setJsonLdContextReference(sink);
    
    
  }
  
  private void setMediaTypeReference(ContextProperties properties) {
    ReferenceManager manager = properties.getReferenceManager();
    if (manager == null) return;
    
    String mediaType = properties.getMediaType();
    String mediaTypeRef = properties.getMediaTypeRef();
    if (mediaTypeRef == null) {
      String[] array = mediaType.split("\\./\\+");
      String rdfType = properties.getRdfTypeURI();
      if (rdfType != null) {
        String localName = TypeManager.getLocalName(rdfType);
        String suffix = getSuffix(localName, array);
        mediaTypeRef = "[" + localName + suffix + "-Media-Type" + "]";
        properties.setMediaTypeRef(mediaTypeRef);
      }
    }
    if (mediaTypeRef == null) {
      mediaTypeRef = "[" + mediaType + "]";
    }
    File localFile = fileManager.getMediaTypeDocumentationFile(mediaType);
    properties.setLocalFile(localFile);
   
    BibliographicReference ref = new BibliographicReference();
    ref.setLabel(mediaTypeRef);
    ref.setAuthor(properties.getEditors());
    ref.setTitle(properties.getTitle());
    ref.setDate(properties.getDate());
    ref.setEdition(properties.getStatus());
    ref.setLocalFile(localFile);
    
    manager.add(ref);
    
    
  }

private String getSuffix(String localName, String[] array) {
    for (int i=0; i<array.length-1; i++) {
      if (localName.equals(array[i])) {
        return "-" + array[i+1];
      }
    }
    return "";
  }

//
//  private void setJsonLdContextReference(ContextProperties sink) {
//    String contextRef = sink.getContextRef();
//    if (contextRef == null) return;
//    contextRef = contextRef.replace(" ", "&nbsp;");
//    
//    if (sink.getReference(contextRef) != null) return;
//    
//    StringBuilder builder = new StringBuilder();
//    List<Person> authors = sink.getAuthors();
//    String comma = "";
//    for (Person author : authors) {
//      builder.append(comma);
//      builder.append(author.getPersonName());
//      comma = ", ";
//    }
//    builder.append("| ");
//    String rdfTypeURI = sink.getRdfTypeURI();
//    String typeName = TypeManager.getLocalName(rdfTypeURI);
//    String title = "JSON-LD Context for " + typeName + " Resources";
//    builder.append(title);
//    builder.append("| ");
//    String status = sink.getStatus();
//    if (status != null) {
//      builder.append(status);
//    }
//    String date = sink.getDate();
//    if (date != null) {
//      if (status != null) {
//        builder.append(", ");
//      }
//      builder.append(date);
//    }
//    builder.append("| ");
//    builder.append("URL: ");
//    String contextURL = sink.getContextURI();
//    builder.append(contextURL);
//    
//    sink.putReference(contextRef, builder.toString());
//    
//  }

  private void setMediaTypeDocFile(ContextProperties sink) {
    File file = fileManager.getIndexFile(sink.getMediaType());
    sink.setMediaTypeDocFile(file);
  }

  private String getLocalName(String uri) {
    return TypeManager.getLocalName(uri);
  }
  
  private void setTitle(ContextProperties sink) {
    if (sink.getTitle() == null) {
      String typeURI = sink.getRdfTypeURI();

      String mediaType = sink.getMediaType();
      String title = null;
      if (typeURI == null) {
        title = "The <code>" + mediaType + "</code> format";
      } else {
        String typeName = getLocalName(typeURI);
        title = typeName + " JSON Binding<br>in the <code>" + mediaType + "</code> format";
      }
      sink.setTitle(title);
    }
    
  }

  private void setEditors(ContextProperties sink, String value) {
    StringTokenizer tokens = new StringTokenizer(value, "\n");
    while (tokens.hasMoreTokens()) {
      String text = tokens.nextToken().trim();
      if (text.length()>0) {
        sink.addEditor(parsePerson(text));
      }
      
    }
    
  }

  private Person parsePerson(String line) {
    String personName = line;
    String orgName = null;
    int comma = line.indexOf(',');
    if (comma > 0) {
      personName = line.substring(0, comma).trim();
      orgName = line.substring(comma+1).trim();
    }
    Person person = new Person();
    person.setPersonName(personName);
    person.setOrgName(orgName);
    return person;
  }

  private void setAuthors(ContextProperties sink, String value) {
    StringTokenizer tokens = new StringTokenizer(value, "\n");
    while (tokens.hasMoreTokens()) {
      String text = tokens.nextToken().trim();
      if (text.length()>0) {
        sink.addAuthor(parsePerson(text));
      }
      
    }
    
  }


  private void validate(ContextProperties p) {
    StringBuilder error = new StringBuilder();
    validate(error, p);
    
    if (error.length()>0) {
      throw new ContextPropertiesSyntaxException(error.toString());
    }
    
    contextMap.put(p.getMediaType(), p);
    
  }

  private void validate(StringBuilder error, ContextProperties p) {
    
//    if (p.getContextURI() == null) {
//      append(error,  CONTEXTURI);
//    }
    if (p.getMediaType() == null) {
      append(error,  MEDIATYPE);
    }
//    if (p.getRdfTypeURI() == null) {
//      append(error,  RDFTYPE);
//    }
    
  }

  private void append(StringBuilder error,  String propertyName) {
    
    if (error.length() > 0) {
      error.append("\n");
    } else {
      error.append("Missing properties:\n");
    }
    error.append("  ");
    error.append(propertyName);
    
  }


  private void setMixedValue(ContextProperties sink, String value) {
    StringTokenizer tokens = new StringTokenizer(value, " \t\r\n");
    while (tokens.hasMoreTokens()) {
      String propertyURI = tokens.nextToken();
      sink.addMixed(propertyURI);
    }
    
  }

  private void setIdref(ContextProperties sink, String value) {
    StringTokenizer tokens = new StringTokenizer(value, " \t\r\n");
    while (tokens.hasMoreTokens()) {
      String idref = tokens.nextToken();
      sink.addIdRef(idref);
    }
    
  }

}
