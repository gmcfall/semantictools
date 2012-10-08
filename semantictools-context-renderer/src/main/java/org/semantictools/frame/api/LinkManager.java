package org.semantictools.frame.api;

import java.io.File;

public class LinkManager {
  
  private String baseURI;
  
  public LinkManager() {}
  
  public LinkManager(String baseURI) {
    this.baseURI = baseURI;
  }
  
  public LinkManager(File baseFile) {
    this(toUnixStyle(baseFile));
  }
  
  
  
  public void setBaseURI(String baseURI) {
    this.baseURI = baseURI;
  }
  
  public String getBaseURI() {
    return baseURI;
  }
  
  /**
   * Replace backslashes with forward slashes.
   */
  static private String toUnixStyle(File file) {
    return file.toString().replace('\\', '/');
  }
  
  public String relativize(File file) {
    String path = toUnixStyle(file);
    return relativize(path);
  }

  /**
   * Return a new URI relative to the given URI
   */
  public String relativize(String uri) {
    if (uri == null) return null;

    
    if (uri.startsWith(this.baseURI)) {
      return uri.substring(this.baseURI.length());
    }
    
    
    int hash = uri.lastIndexOf('#');
    int end = (hash>0) ? hash : uri.length();
    
    String baseURI = uri.substring(0, end);
    if (this.baseURI.equals(baseURI)) {
      return uri.substring(hash);
    }
    
    int slash = this.baseURI.lastIndexOf('/');
    int count = 0;
    
    while (slash > 0) {
      String prefix = this.baseURI.substring(0, slash+1);
      if (uri.startsWith(prefix)) {
        return generateRelative(prefix, count, slash, uri);
      }
      slash = this.baseURI.lastIndexOf('/', slash-1);
      count++;
    }
    
    return uri;
    
  }

  private String generateRelative(String prefix, int count, int slash, String uri) {
    
    StringBuilder builder = new StringBuilder();
    for (int i=0; i<count; i++) {
      builder.append("../");
    }
    String tail = uri.substring(slash + 1);
    builder.append(tail);
    
    return builder.toString();
  }
  
  
  

}
