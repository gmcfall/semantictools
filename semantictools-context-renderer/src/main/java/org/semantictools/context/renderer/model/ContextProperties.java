package org.semantictools.context.renderer.model;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.semantictools.frame.api.TypeManager;

/**
 * This container holds properties used to generate the JSON-LD context for some data type.
 * @author Greg McFall
 *
 */
public class ContextProperties extends BaseDocumentMetadata implements Comparable<ContextProperties> {
  
  
  private File sourceFile;
  private File contextFile;
  private String rdfTypeURI;
  private String rdfTypeRef;
  private String rdfProperty;
  private String contextURI;
  private String contextRef;
  private String mediaType;
  private String mediaTypeURI;
  private String mediaTypeRef;
  private String sampleText;
  private String purlDomain;
  private File mediaTypeDocFile;
  private List<String> idRefList = new ArrayList<String>();
  private Set<String> mixedSet = new HashSet<String>();
  private Set<String> requiresId = new HashSet<String>();
  private List<String> graphTypes = new ArrayList<String>();
  private Set<String> setProperty = new HashSet<String>();
  private String abstactText;
  private String introduction;
  private Set<String> excludedTypes = new HashSet<String>();
  private Set<String> expandedValues = new HashSet<String>();
  private List<SampleJson> sampleJsonList = new ArrayList<SampleJson>();
  private Map<String, FrameConstraints> uri2FrameConstraints = new HashMap<String, FrameConstraints>();
  private Properties rawProperties;
  
  
  public ContextProperties(DocumentMetadata parent, Properties rawProperties) {
    super(parent);
    this.rawProperties = rawProperties;
  }
  
  /**
   * Register a property that uses the "@set" keyword
   */
  public void addSetProperty(String propertyURI) {
    setProperty.add(propertyURI);
  }
  
  /**
   * Returns true if the specified property uses the "@set" keyword.
   */
  public boolean isSetProperty(String propertyURI) {
    return setProperty.contains(propertyURI);
  }
  
  /**
   * Returns the set of URI values for classes whose "@id" property
   * is required.  (By default, "@id" properties are optional.)
   */
  public Set<String> getRequiresId() {
    return requiresId;
  }
  
  /**
   * Returns true if the "@id" property is required for instances of the 
   * specified RDF type.
   */
  public boolean requiresId(String rdfTypeURI) {
    return requiresId.contains(rdfTypeURI);
  }
  
  /**
   * Return the list of types that can appear in the 
   * @param rdfTypeURI
   */
  public void addGraphType(String rdfTypeURI) {
    graphTypes.add(rdfTypeURI);
  }
  
  public List<String> getGraphTypes() {
    return graphTypes;
  }

  /**
   * Returns the local name for one property whose value is described by the media type.
   * This must be the name of a property belonging to the class identified by the rdfTypeURI.
   * If omitted, then the media type describes an entire instance of the specified RDF type, not
   * just one distinguished property.
   */
  public String getRdfProperty() {
    return rdfProperty;
  }


  /**
   * Sets the local name for one property whose value is described by the media type.
   * This must be the name of a property belonging to the class identified by the rdfTypeURI.
   * If omitted, then the media type describes an entire instance of the specified RDF type, not
   * just one distinguished property.
   */
  public void setRdfProperty(String rdfPropertyName) {
    this.rdfProperty = rdfPropertyName;
  }


  public File getMediaTypeDocFile() {
    return mediaTypeDocFile;
  }
  
  public Set<String> getExcludedTypes() {
    return excludedTypes;
  }


  public void setMediaTypeDocFile(File mediaTypeDocFile) {
    this.mediaTypeDocFile = mediaTypeDocFile;
  }



  /**
   * Returns the citation reference to the media type referenced by this ContextProperties object.
   * By default, the return value has the form [{rdfTypeLocalName}-media-type], but this default
   * may be overridden by calling setMediaTypeRef(..).
   */
  public String getMediaTypeRef() {
    if (mediaTypeRef == null) {
      return "[" + TypeManager.getLocalName(rdfTypeURI) + "-media-type]";
    }
    return mediaTypeRef;
  }



  public void setMediaTypeRef(String mediaTypeRef) {
    this.mediaTypeRef = mediaTypeRef;
  }



  public String getProperty(String name) {
    return rawProperties.getProperty(name);
  }
  
  public Set<String> getExpandedValues() {
    return expandedValues;
  }



  public List<SampleJson> getSampleJsonList() {
    return sampleJsonList;
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
  
  /**
   * Add a property that has a mixed representation; i.e. values can be either
   * a URI reference or an embedded resource.
   */
  public void addMixed(String propertyURI) {
    mixedSet.add(propertyURI);
  }
  
  /**
   * Returns true if the specified property has a mixed representation; i.e.
   * values can be either a URI reference or an embedded resource.
   */
  public boolean isMixed(String propertyURI) {
    return mixedSet.contains(propertyURI);
  }

  public String getRdfTypeRef() {
    return rdfTypeRef;
  }



  public void setRdfTypeRef(String rdfTypeRef) {
    this.rdfTypeRef = rdfTypeRef;
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

  @Override
  public int compareTo(ContextProperties other) {
    return mediaType.compareTo(other.mediaType);
  }

  public String getSampleText() {
    return sampleText;
  }

  public void setSampleText(String sampleText) {
    this.sampleText = sampleText;
  }


  /**
   * Returns the output file that contains the generated JSON-LD context.
   */
  public File getContextFile() {
    return contextFile;
  }

  /**
   * Sets the output file that contains the generated JSON-LD context.
   */
  public void setContextFile(File contextFile) {
    this.contextFile = contextFile;
  }

  


}
