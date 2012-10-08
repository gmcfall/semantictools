package org.semantictools.frame.model;

public class OntologyInfo {
  private String uri;
  private String prefix;
  private String label;
  private OntologyType type = OntologyType.RDF;
  private boolean hasClasses;
  
  public OntologyInfo() {}
  
  
  public OntologyInfo(String prefix, String uri, OntologyType type) {
    this.uri = uri;
    this.prefix = prefix;
    this.type = type;
  }
  public String getUri() {
    return uri;
  }
  public void setUri(String uri) {
    this.uri = uri;
  }
  public String getPrefix() {
    return prefix;
  }
  public void setPrefix(String prefix) {
    this.prefix = prefix;
  }


  public String getLabel() {
    return label;
  }


  public void setLabel(String label) {
    this.label = label;
  }


  public OntologyType getType() {
    return type;
  }


  public void setType(OntologyType type) {
    this.type = type;
  }


  /**
   * Returns true if this Ontology defines any RDF classes and false otherwise.
   */
  public boolean hasClasses() {
    return hasClasses;
  }


  public void setHasClasses(boolean hasClasses) {
    this.hasClasses = hasClasses;
  }


  
  
  
  

}
