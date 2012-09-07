package org.semantictools.context.renderer.model;

public class HttpHeaderInfo {
  private String headerName;
  private String headerValue;
  public HttpHeaderInfo(String headerName, String headerValue) {
    this.headerName = headerName;
    this.headerValue = headerValue;
  }
  public String getHeaderName() {
    return headerName;
  }
  public String getHeaderValue() {
    return headerValue;
  }
  

}
