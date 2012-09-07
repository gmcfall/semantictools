package org.semantictools.index.model;



public class MediaTypeReference {
  
  private String mediaTypeName;
  private String mediaTypeURI;
  private String serviceAPI;
  
  
  
  public MediaTypeReference(String mediaTypeName, String mediaTypeURI, String serviceAPI) {
    this.mediaTypeName = mediaTypeName;
    this.mediaTypeURI = mediaTypeURI;
    this.serviceAPI = serviceAPI;
  }
  
  public String getMediaTypeName() {
    return mediaTypeName;
  }
  public void setMediaTypeName(String mediaTypeName) {
    this.mediaTypeName = mediaTypeName;
  }
  public String getMediaTypeURI() {
    return mediaTypeURI;
  }
  public void setMediaTypeURI(String mediaTypeURI) {
    this.mediaTypeURI = mediaTypeURI;
  }

  public String getServiceAPI() {
    return serviceAPI;
  }

  public void setServiceAPI(String serviceAPI) {
    this.serviceAPI = serviceAPI;
  }
  
  
}

