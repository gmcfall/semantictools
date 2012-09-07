package org.semantictools.frame.api;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.semantictools.context.renderer.model.ContextProperties;
import org.semantictools.context.renderer.model.FrameConstraints;

import java.util.StringTokenizer;

public class ContextManager {
  
//  private static final Logger logger = LoggerFactory.getLogger(ContextManager.class);

  private static final String RDFTYPE = "rdfType";
  private static final String RDFTYPE_REF = "rdfTypeRef";
  private static final String MEDIATYPE = "mediaType";
  private static final String MEDIATYPEURI = "mediaTypeURI";
  private static final String CONTEXTURI = "contextURI";
  private static final String CONTEXTREF = "contextRef";
  private static final String IDREF = "idref";
  private static final String STATUS = "status";
  private static final String ABSTRACT = "abstract";
  private static final String EDITORS = "editors";
  private static final String AUTHORS = "authors";
  private static final String TITLE = "title";
  private static final String INTRODUCTION = "introduction";
  private static final String INCLUDES_SUFFIX = ".includes";
  private static final String EXCLUDES_SUFFIX = ".excludes";
  private static final String PURL_DOMAIN = "purlDomain";
  private static final String EXPANDED_VALUE = "expandedValue";
  
  private Map<String, ContextProperties> contextMap = new HashMap<String, ContextProperties>();
  
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
    ContextProperties sink = new ContextProperties(properties);
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
      } else if (MEDIATYPE.equals(key)) {
        sink.setMediaType(value);
      } else if (MEDIATYPEURI.equals(key)) {
        sink.setMediaTypeURI(value);
      } else if (RDFTYPE.equals(key)) {
        sink.setRdfTypeURI(value);
      } else if (RDFTYPE_REF.equals(key)) {
        sink.setRdfTypeRef(value);
      } else if (CONTEXTREF.equals(key)) {
        sink.setContextRef(value);
      } else if (STATUS.equals(key)) {
        sink.setStatus(value);
      } else if (ABSTRACT.equals(key)) {
        sink.setAbstactText(value);
      } else if (EDITORS.equals(key)) {
        setEditors(sink, value);
      } else if (AUTHORS.equals(key)) {
        setAuthors(sink, value);
      } else if (INTRODUCTION.equals(key)) {
        sink.setIntroduction(value);
      } else if (TITLE.equals(key)) {
        sink.setTitle(value);
      } else if (key.endsWith(INCLUDES_SUFFIX)) {
        addIncludesConstraint(sink, key, value);
      } else if (key.endsWith(EXCLUDES_SUFFIX)) {
        addExcludesConstraint(sink, key, value);
      } else if (PURL_DOMAIN.equals(key)) {
        sink.setPurlDomain(value);
      } else if (EXPANDED_VALUE.equals(key)) {
        setExpandedValue(sink, value);
      }
    }
    validate(sink);
    setDefaults(sink);
    
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

  private void setDefaults(ContextProperties sink) {
    setTitle(sink);
    
  }

  private String getLocalName(String uri) {

    int hash = uri.lastIndexOf('#');
    int slash = uri.lastIndexOf('/');
    int delim = Math.max(hash, slash);
    
    String localName = uri.substring(delim+1);
    return localName;
  }
  
  private void setTitle(ContextProperties sink) {
    if (sink.getTitle() == null) {
      String typeName = getLocalName(sink.getRdfTypeURI());
      String mediaType = sink.getMediaType();
      
      String title = typeName + " JSON Binding<BR/>in the <code>" + mediaType + "</code> format";
      sink.setTitle(title);
    }
    
  }

  private void setEditors(ContextProperties sink, String value) {
    StringTokenizer tokens = new StringTokenizer(value, "\n");
    while (tokens.hasMoreTokens()) {
      String text = tokens.nextToken().trim();
      if (text.length()>0) {
        sink.getEditors().add(text);
      }
      
    }
    
  }

  private void setAuthors(ContextProperties sink, String value) {
    StringTokenizer tokens = new StringTokenizer(value, "\n");
    while (tokens.hasMoreTokens()) {
      String text = tokens.nextToken().trim();
      if (text.length()>0) {
        sink.getAuthors().add(text);
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
    
    if (p.getContextURI() == null) {
      append(error,  CONTEXTURI);
    }
    if (p.getMediaType() == null) {
      append(error,  MEDIATYPE);
    }
    if (p.getRdfTypeURI() == null) {
      append(error,  RDFTYPE);
    }
    
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

  private void setIdref(ContextProperties sink, String value) {
    StringTokenizer tokens = new StringTokenizer(value, " \t\r\n");
    while (tokens.hasMoreTokens()) {
      String idref = tokens.nextToken();
      sink.addIdRef(idref);
    }
    
  }

}
