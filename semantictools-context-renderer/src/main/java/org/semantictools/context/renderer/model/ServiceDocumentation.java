package org.semantictools.context.renderer.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ServiceDocumentation implements ReferenceManager {

  private ContextProperties contextProperties;
  private String mediaType;
  private String mediaTypeRef;
  private String postResponseMediaType;
  private String postResponseMediaTypeRef;
  
  private String title;
  private String subtitle;
  private String status;
  private String abstactText;
  private String introduction;
  private String representationHeading;
  private String representationText;
  private MethodDocumentation postDocumentation;
  private MethodDocumentation getDocumentation;
  private MethodDocumentation putDocumentation;
  private MethodDocumentation deleteDocumentation;
  
  private List<String> editors = new ArrayList<String>();
  private List<String> authors = new ArrayList<String>();
  private List<HttpMethod> methodList = new ArrayList<HttpMethod>();
  
  private String cssHref;
  private Map<String, String> referenceMap = new HashMap<String, String>();
  
  public String getMediaType() {
    return mediaType;
  }
  public void setMediaType(String mediaType) {
    this.mediaType = mediaType;
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
  public List<String> getEditors() {
    return editors;
  }
  public void setEditors(List<String> editors) {
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
  public ContextProperties getContextProperties() {
    return contextProperties;
  }
  public void setContextProperties(ContextProperties contextProperties) {
    this.contextProperties = contextProperties;
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
  public String getMediaTypeRef() {
    return mediaTypeRef;
  }
  public void setMediaTypeRef(String mediaTypeRef) {
    this.mediaTypeRef = mediaTypeRef;
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
  

}
