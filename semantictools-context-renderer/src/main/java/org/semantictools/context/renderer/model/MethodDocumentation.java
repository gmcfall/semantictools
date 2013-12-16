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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MethodDocumentation {
  
  private String summary;
  private String requestBodyRequirement;
  
  
  private List<String> requestMediaTypes = new ArrayList<String>();
  private List<String> responseMediaTypes = new ArrayList<String>();
  private List<HttpHeaderInfo> requestHeaders = new ArrayList<HttpHeaderInfo>();
  private List<ResponseInfo> statusCodes = new ArrayList<ResponseInfo>();
  private List<QueryParam> queryParams = null;
  
  public String getSummary() {
    return summary;
  }
  public void setSummary(String summary) {
    this.summary = summary;
  }
  public List<HttpHeaderInfo> getRequestHeaders() {
    return requestHeaders;
  }
  
  public void addRequestHeader(String name, String value) {
    for (HttpHeaderInfo info : requestHeaders) {
      if (info.getHeaderName().equals(name)) return;
    }
    requestHeaders.add(new HttpHeaderInfo(name, value));
  }
  public String getRequestBodyRequirement() {
    return requestBodyRequirement;
  }
  public void setRequestBodyRequirement(String requestBodyRequirement) {
    this.requestBodyRequirement = requestBodyRequirement;
  }
  
  
  public void add(ResponseInfo statusCode) {
    if (!contains(statusCode)) {
      statusCodes.add(statusCode);
    }
  }
  
  public boolean contains(ResponseInfo code) {
    for(ResponseInfo c : statusCodes) {
      if (c.getStatusCode() == code.getStatusCode()) return true;
    }
    return false;
  }
  
  
  public List<ResponseInfo> getStatusCodes() {
    return statusCodes;
  }
  public boolean containsHeader(String headerName) {
    for (HttpHeaderInfo info : requestHeaders) {
      if (info.getHeaderName().equals(headerName)) return true;
    }
    return false;
  }
  
  public List<String> getRequestContentTypes() {
    return requestMediaTypes;
  }
  public List<String> getResponseMediaTypes() {
    return responseMediaTypes;
  }
  
  public void add(QueryParam param) {
    if (queryParams == null) {
      queryParams = new ArrayList<QueryParam>();
    }
    queryParams.add(param);
  }
  
  public List<QueryParam> getQueryParams() {
    return queryParams;
  }

}
