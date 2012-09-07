package org.semantictools.context.renderer.model;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * This container holds properties used to generate the JSON-LD context for some data type.
 * @author Greg McFall
 *
 */
public class ContextProperties implements ReferenceManager {
  private File sourceFile;
  private String rdfTypeURI;
  private String rdfTypeRef;
  private String contextURI;
  private String contextRef;
  private String mediaType;
  private String mediaTypeURI;
  private String purlDomain;
  private List<String> idRefList = new ArrayList<String>();
  private String title;
  private String status;
  private String abstactText;
  private String introduction;
  private List<String> editors = new ArrayList<String>();
  private List<String> authors = new ArrayList<String>();
  private Set<String> expandedValues = new HashSet<String>();
  private Map<String, String> referenceMap = null;
  private Map<String, FrameConstraints> uri2FrameConstraints = new HashMap<String, FrameConstraints>();
  private Properties rawProperties;
  
  
  public ContextProperties(Properties rawProperties) {
    this.rawProperties = rawProperties;
  }
  
  public String getProperty(String name) {
    return rawProperties.getProperty(name);
  }
  
  public Set<String> getExpandedValues() {
    return expandedValues;
  }



  /**
   * The source file that contains the definition of this ContextProperties object.
   */
  public File getSourceFile() {
    return sourceFile;
  }

  /**
   * Returns the FrameConstraints for the specified URI, or null if 
   * the requested FrameConstraints do not exist.
   */
  public FrameConstraints getFrameConstraints(String uri) {
    return uri2FrameConstraints.get(uri);
  }
  
  /**
   * Returns the FrameConstraints for the specified URI.
   * If the FrameConstraints for the given URI does not exist, it will be created.
   */
  public FrameConstraints fetchFrameConstraints(String uri) {
    FrameConstraints result = uri2FrameConstraints.get(uri);
    if (result == null) {
      result = new FrameConstraints(uri);
      uri2FrameConstraints.put(uri, result);
    }
    return result;
  }
  
  public void addFrameConstraints(FrameConstraints value) {
    uri2FrameConstraints.put(value.getClassURI(), value);
  }
  
  public List<FrameConstraints> listFrameConstraints() {
    return new ArrayList<FrameConstraints>( uri2FrameConstraints.values() );
  }

  public void setSourceFile(File sourceFile) {
    this.sourceFile = sourceFile;
  }



  public String getStatus() {
    return status;
  }


  public void setReferences(Map<String,String> references) {
    referenceMap = references;
  }
  
  public String getReference(String key) {
    if (referenceMap == null) return null;
    return referenceMap.get(key);
  }
  
  public void putReference(String key, String value) {
    if (referenceMap == null) {
      referenceMap = new HashMap<String, String>();
    }
    referenceMap.put(key, value);
  }


  public void setStatus(String status) {
    this.status = status;
  }



  public String getIntroduction() {
    return introduction;
  }



  public void setIntroduction(String introduction) {
    this.introduction = introduction;
  }

  public String getAbstactText() {
    return abstactText;
  }



  public void setAbstactText(String abstactText) {
    this.abstactText = abstactText;
  }



  public List<String> getEditors() {
    return editors;
  }



  public List<String> getAuthors() {
    return authors;
  }


  public String getRdfTypeURI() {
    return rdfTypeURI;
  }



  public void setRdfTypeURI(String rdfTypeURI) {
    this.rdfTypeURI = rdfTypeURI;
  }



  public String getContextURI() {
    return contextURI;
  }



  public void setContextURI(String contextURI) {
    this.contextURI = contextURI;
  }



  public String getContextRef() {
    return contextRef;
  }



  public void setContextRef(String contextRef) {
    this.contextRef = contextRef;
  }



  public String getMediaType() {
    return mediaType;
  }



  public void setMediaType(String mediaType) {
    this.mediaType = mediaType;
  }



  public void addIdRef(String propertyURI) {
    idRefList.add(propertyURI);
  }

  /**
   * Returns the list of properties that must be coerced to "@id" values in the JSON-LD context.
   */
  public List<String> getIdRefList() {
    return idRefList;
  }
  
  public boolean isIdRef(String propertyURI) {
    return idRefList.contains(propertyURI);
  }



  public String getRdfTypeRef() {
    return rdfTypeRef;
  }



  public void setRdfTypeRef(String rdfTypeRef) {
    this.rdfTypeRef = rdfTypeRef;
  }



  public String getTitle() {
    return title;
  }



  public void setTitle(String title) {
    this.title = title;
  }



  public String getMediaTypeURI() {
    return mediaTypeURI;
  }



  public void setMediaTypeURI(String mediaTypeURI) {
    this.mediaTypeURI = mediaTypeURI;
  }


  /**
   * Returns the PURL domain under which this documentation should be published.
   */
  public String getPurlDomain() {
    return purlDomain;
  }


  /**
   * Sets the PURL domain under which this documentation should be published.
   * This value is used when publishing documentation to 
   * http://semantic-tools.appspot.com
   */
  public void setPurlDomain(String purlDomain) {
    this.purlDomain = purlDomain;
  }


  


  

}
