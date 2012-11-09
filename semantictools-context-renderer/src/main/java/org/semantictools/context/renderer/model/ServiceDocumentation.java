package org.semantictools.context.renderer.model;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ServiceDocumentation implements ReferenceManager {

  
  private List<ContextProperties> contextPropertiesList = new ArrayList<ContextProperties>();

  private File serviceDocumentationFile;
  private String rdfTypeURI;
  private String postResponseMediaType;
  private String postResponseMediaTypeRef;
  
  private String title;
  private String subtitle;
  private String status;
  private String date;
  private boolean versionHistory;
  private String abstactText;
  private String introduction;
  private String representationHeading;
  private String representationText;
  private String urlTemplateText;
  private boolean allowArbitraryFormat;
  private boolean allowHtmlFormat;
  private String htmlFormatDocumentation;
  private String defaultMediaType;
  private String postProcessingRules;
  private boolean contentNegotiation;
  
  private String getInstructions;
  private String putInstructions;
  
  private MethodDocumentation postDocumentation;
  private MethodDocumentation getDocumentation;
  private MethodDocumentation putDocumentation;
  private MethodDocumentation deleteDocumentation;
  
  private List<Person> editors = new ArrayList<Person>();
  private List<String> authors = new ArrayList<String>();
  private List<HttpMethod> methodList = new ArrayList<HttpMethod>();
  private List<QueryParam> queryParams = new ArrayList<QueryParam>();
  private List<String> putRules = new ArrayList<String>();
  
  private String cssHref;
  private Map<String, String> referenceMap = new HashMap<String, String>();
  private Map<String, String> mediaTypeUriMap = new HashMap<String, String>();

  public File getServiceDocumentationFile() {
    return serviceDocumentationFile;
  }
  public void setServiceDocumentationFile(File serviceDocumentationFile) {
    this.serviceDocumentationFile = serviceDocumentationFile;
  }

  /**
   * Specifies whether the documentation for the service should contain a link
   * to the version history of the specification.
   */
  public void setHistoryLink(boolean value) {
    versionHistory = value;
  }
  
  /**
   * Returns true if the documentation for the service should contain a link
   * to the version history of the specification.
   */
  public boolean hasHistoryLink() {
    return versionHistory;
  }
  
  /**
   * Returns true if the REST Service supports content negotiation.
   */
  public boolean isContentNegotiation() {
    return contentNegotiation;
  }

  /**
   * Specifies whether the REST Service supports content negotiation.
   */
  public void setContentNegotiation(boolean contentNegotiation) {
    this.contentNegotiation = contentNegotiation;
  }
  public String getDefaultMediaType() {
    return defaultMediaType;
  }
  public void setDefaultMediaType(String defaultMediaType) {
    this.defaultMediaType = defaultMediaType;
  }
  public String getPostProcessingRules() {
    return postProcessingRules;
  }
  public void setPostProcessingRules(String postProcessingRules) {
    this.postProcessingRules = postProcessingRules;
  }
  public String getRdfTypeURI() {
    if (rdfTypeURI == null && !contextPropertiesList.isEmpty()) {
      rdfTypeURI = contextPropertiesList.get(0).getRdfTypeURI();
    }
    return rdfTypeURI;
  }
  public void setRdfTypeURI(String rdfTypeURI) {
    this.rdfTypeURI = rdfTypeURI;
  }
  
  public Map<String,String> getMediaTypeUriMap() {
    return mediaTypeUriMap;
  }
  
  public boolean hasMultipleFormats() {
    int count = contextPropertiesList.size() + mediaTypeUriMap.size() + (allowHtmlFormat?1:0);
    return count > 1;
  }
  
  
  /**
   * Returns true if the REST API supports a user-friendly HTML representation
   * of the resource.
   */
  public boolean isAllowHtmlFormat() {
    return allowHtmlFormat;
  }
  public void setAllowHtmlFormat(boolean allowHtmlFormat) {
    this.allowHtmlFormat = allowHtmlFormat;
  }
  
  
  public String getDate() {
    return date;
  }
  public void setDate(String date) {
    this.date = date;
  }
  /**
   * Returns the text that describes the text/html representation of the resource.
   */
  public String getHtmlFormatDocumentation() {
    return htmlFormatDocumentation;
  }

  /**
   * Sets the text that describes the text/html representation of the resource.
   */
  public void setHtmlFormatDocumentation(String text) {
    this.htmlFormatDocumentation = text;
  }
  /**
   * Returns true if the REST API supports arbitrary formats for resources, 
   * and false otherwise.  If this value is true, then a client may POST
   * a resource in any format, and GET the resource back in the given format
   * through content negotiation.
   */
  public boolean isAllowArbitraryFormat() {
    return allowArbitraryFormat;
  }
  
  
  public String getUrlTemplateText() {
    return urlTemplateText;
  }
  public void setUrlTemplateText(String urlTemplateText) {
    this.urlTemplateText = urlTemplateText;
  }
  /**
   * Specifies whether the REST API supports arbitrary formats for resources.
   * If this value is true, then a client may POST
   * a resource in any format, and GET the resource back in the given format
   * through content negotiation.
   */
  public void setAllowArbitraryFormat(boolean allowArbitraryFormat) {
    this.allowArbitraryFormat = allowArbitraryFormat;
  }
  public String getTitle() {
    return title;
  }
  public void setTitle(String title) {
    this.title = title;
  }
  
  public String getSubtitle() {
    return subtitle;
  }
  public void setSubtitle(String subtitle) {
    this.subtitle = subtitle;
  }
  public String getStatus() {
    return status;
  }
  public void setStatus(String status) {
    this.status = status;
  }
  public String getAbstactText() {
    return abstactText;
  }
  public void setAbstactText(String abstactText) {
    this.abstactText = abstactText;
  }
  public String getIntroduction() {
    return introduction;
  }
  public void setIntroduction(String introduction) {
    this.introduction = introduction;
  }
  public List<Person> getEditors() {
    return editors;
  }
  public void setEditors(List<Person> editors) {
    this.editors = editors;
  }
  public List<String> getAuthors() {
    return authors;
  }
  public void setAuthors(List<String> authors) {
    this.authors = authors;
  }

  public String getReference(String key) {
    return referenceMap.get(key);
  }
  
  public void putReference(String key, String value) {
    referenceMap.put(key, value);
  }
  
  public void add(ContextProperties context) {
    contextPropertiesList.add(context);
  }
  public List<ContextProperties> listContextProperties() {
    return contextPropertiesList;
  }
  
  public String getCssHref() {
    return cssHref;
  }
  public void setCssHref(String cssHref) {
    this.cssHref = cssHref;
  }
  public String getRepresentationHeading() {
    return representationHeading;
  }
  public void setRepresentationHeading(String representationHeading) {
    this.representationHeading = representationHeading;
  }
  public String getRepresentationText() {
    return representationText;
  }
  public void setRepresentationText(String representationText) {
    this.representationText = representationText;
  }
  public MethodDocumentation getPostDocumentation() {
    return postDocumentation;
  }
  public void setPostDocumentation(MethodDocumentation postDocumentation) {
    this.postDocumentation = postDocumentation;
  }
  public MethodDocumentation getGetDocumentation() {
    return getDocumentation;
  }
  public void setGetDocumentation(MethodDocumentation getDocumentation) {
    this.getDocumentation = getDocumentation;
  }
  public String getPostResponseMediaType() {
    return postResponseMediaType;
  }
  public void setPostResponseMediaType(String postResponseType) {
    this.postResponseMediaType = postResponseType;
  }
  public String getPostResponseMediaTypeRef() {
    return postResponseMediaTypeRef;
  }
  public void setPostResponseTypeRef(String postResponseTypeRef) {
    this.postResponseMediaTypeRef = postResponseTypeRef;
  }
  public MethodDocumentation getPutDocumentation() {
    return putDocumentation;
  }
  public void setPutDocumentation(MethodDocumentation putDocumentation) {
    this.putDocumentation = putDocumentation;
  }
  public MethodDocumentation getDeleteDocumentation() {
    return deleteDocumentation;
  }
  public void setDeleteDocumentation(MethodDocumentation deleteDocumentation) {
    this.deleteDocumentation = deleteDocumentation;
  }
  public List<HttpMethod> getMethodList() {
    return methodList;
  }
  public void add(HttpMethod method) {
    methodList.add(method);
  }
  public boolean contains(HttpMethod method) {
    return methodList.contains(method);
  }
  public HttpMethod getMethod(HttpMethod get) {
    for (HttpMethod method : methodList) {
      if ("GET".equals(method.getName())) return method;
    }
    return null;
  }
  /**
   * Returns the text which describes how to GET a representation.
   * The default text is:
   * <blockquote>
   * To get a representation of a particular {0} instance, the client submits an HTTP GET 
   * request to the resource's REST endpoint in accordance with the following rules:
   * </blockquote>
   * @return
   */
  public String getGetInstructions() {
    return getInstructions;
  }
  public void setGetInstructions(String getInstructions) {
    this.getInstructions = getInstructions;
  }
  public List<QueryParam> getQueryParams() {
    return queryParams;
  }
  public List<String> getPutRules() {
    return putRules;
  }
  public String getPutInstructions() {
    return putInstructions;
  }
  public void setPutInstructions(String putInstructions) {
    this.putInstructions = putInstructions;
  }
  

}
