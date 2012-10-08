package org.semantictools.index.model;



public class MediaTypeReference {
  
  private String mediaTypeName;
  private String mediaTypeURI;
  private String rdfTypeURI;
  
  
  
  public MediaTypeReference(String rdfTypeURI, String mediaTypeName, String mediaTypeURI) {
    this.mediaTypeName = mediaTypeName;
    this.mediaTypeURI = mediaTypeURI;
    this.rdfTypeURI = rdfTypeURI;
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

  public String getRdfTypeURI() {
    return rdfTypeURI;
  }

  public void setRdfTypeURI(String rdfTypeURI) {
    this.rdfTypeURI = rdfTypeURI;
  }

  
  
  
}

