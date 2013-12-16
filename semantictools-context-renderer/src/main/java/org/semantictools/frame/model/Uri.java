package org.semantictools.frame.model;

public class Uri {
  private String value;
  private String namespace;
  private String localName;
  
  public Uri(String value) {
    this.value = value.trim();
    int mark = value.lastIndexOf('#');
    if (mark < 0) {
      mark = value.lastIndexOf('/');
    }
    if (mark < 0) {
      namespace = "";
      localName = value;
    } else {
      namespace = value.substring(0, mark);
      localName = value.substring(mark+1).trim();
    }
  }

  public String stringValue() {
    return value;
  }

  public String getNamespace() {
    return namespace;
  }

  public String getLocalName() {
    return localName;
  }
  
  public String toString() {
    return value;
  }
  
  

}
