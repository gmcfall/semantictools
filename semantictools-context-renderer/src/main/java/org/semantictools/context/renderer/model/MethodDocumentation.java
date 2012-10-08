package org.semantictools.context.renderer.model;

import java.util.ArrayList;
import java.util.List;

public class MethodDocumentation {
  
  private String summary;
  private String requestBodyRequirement;
  
  private List<HttpHeaderInfo> requestHeaders = new ArrayList<HttpHeaderInfo>();
  private List<ResponseInfo> statusCodes = new ArrayList<ResponseInfo>();
  
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
    requestHeaders.add(new HttpHeaderInfo(name, value));
  }
  public String getRequestBodyRequirement() {
    return requestBodyRequirement;
  }
  public void setRequestBodyRequirement(String requestBodyRequirement) {
    this.requestBodyRequirement = requestBodyRequirement;
  }
  
  
  public void add(ResponseInfo statusCode) {
    statusCodes.add(statusCode);
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
  
  
  
  
  

}
