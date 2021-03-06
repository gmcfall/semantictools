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
package org.semantictools.context.renderer.model;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.semantictools.frame.model.Frame;
import org.semantictools.frame.model.Uri;

public class ServiceDocumentation extends BaseDocumentMetadata  {

  
  private List<ContextProperties> contextPropertiesList = new ArrayList<ContextProperties>();

  private File serviceDocumentationFile;
  private Uri rdfType;
  private Frame frame;
  private String postResponseMediaType;
  private String postResponseMediaTypeRef;
  
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
  
  private Uri containerType;
  
  private MethodDocumentation postDocumentation;
  private MethodDocumentation getDocumentation;
  private MethodDocumentation containerGetDocumentation;
  private MethodDocumentation putDocumentation;
  private MethodDocumentation deleteDocumentation;
  
  private List<HttpMethod> methodList = new ArrayList<HttpMethod>();
  private List<String> putRules = new ArrayList<String>();
  
  private Map<String, String> referenceMap = new HashMap<String, String>();
  private Map<String, String> mediaTypeUriMap = new HashMap<String, String>();
  
  public ServiceDocumentation(DocumentMetadata global) {
    super(global);
  }

  public File getServiceDocumentationFile() {
    return serviceDocumentationFile;
  }
  public void setServiceDocumentationFile(File serviceDocumentationFile) {
    this.serviceDocumentationFile = serviceDocumentationFile;
    this.setLocalFile(serviceDocumentationFile);
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
  public Uri getRdfType() {
    if (rdfType == null && !contextPropertiesList.isEmpty()) {
      String rdfTypeURI = contextPropertiesList.get(0).getRdfTypeURI();
      rdfType = new Uri(rdfTypeURI);
    }
    return rdfType;
  }
  
  
  
  private String getLocalName(String type) {
    if (type == null) {
      return null;
    }
    int mark = type.lastIndexOf('#');
    if (mark < 0) {
      mark = type.lastIndexOf('/');
    }
    return mark > 0 ? type.substring(mark+1) : null;
  }

  public void setRdfType(Uri rdfType) {
    this.rdfType = rdfType;
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
  
  public void add(ContextProperties context) {
    contextPropertiesList.add(context);
  }
  public List<ContextProperties> listContextProperties() {
    return contextPropertiesList;
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
  
  public List<String> getPutRules() {
    return putRules;
  }
  public String getPutInstructions() {
    return putInstructions;
  }
  public void setPutInstructions(String putInstructions) {
    this.putInstructions = putInstructions;
  }

  public MethodDocumentation getContainerGetDocumentation() {
    return containerGetDocumentation;
  }

  public void setContainerGetDocumentation(
      MethodDocumentation collectionGetDocumentation) {
    this.containerGetDocumentation = collectionGetDocumentation;
  }

  public Uri getContainerType() {
    return containerType;
  }

  public void setContainerType(Uri containerType) {
    this.containerType = containerType;
  }

  public Frame getFrame() {
    return frame;
  }

  public void setFrame(Frame frame) {
    this.frame = frame;
  }
  
  

}
