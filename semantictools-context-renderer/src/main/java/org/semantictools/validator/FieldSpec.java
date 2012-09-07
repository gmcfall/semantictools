package org.semantictools.validator;

public class FieldSpec {
  private String localName;
  private String propertyURI;
  private int minCardinality;
  private int maxCardinality;
  private String rangeURI;
  
  public String getLocalName() {
    return localName;
  }
  public void setLocalName(String localName) {
    this.localName = localName;
  }
  public String getPropertyURI() {
    return propertyURI;
  }
  public void setPropertyURI(String propertyURI) {
    this.propertyURI = propertyURI;
  }
  public int getMinCardinality() {
    return minCardinality;
  }
  public void setMinCardinality(int minCardinality) {
    this.minCardinality = minCardinality;
  }
  public int getMaxCardinality() {
    return maxCardinality;
  }
  public void setMaxCardinality(int maxCardinality) {
    this.maxCardinality = maxCardinality;
  }
  public String getRangeURI() {
    return rangeURI;
  }
  public void setRangeURI(String rangeURI) {
    this.rangeURI = rangeURI;
  }
  
  

}
