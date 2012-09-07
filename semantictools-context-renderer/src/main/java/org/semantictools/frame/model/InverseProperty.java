package org.semantictools.frame.model;

/**
 * A container that holds information from the UML about an unnamed inverse property.
 *
 */
public class InverseProperty {
  private Integer minCardinality;
  private Integer maxCardinality;
  private Encapsulation encapsulation = Encapsulation.NONE;
  private Boolean unboundedCardinality;
  
  public Integer getMinCardinality() {
    return minCardinality;
  }
  public void setMinCardinality(Integer minCardinality) {
    this.minCardinality = minCardinality;
  }
  public Integer getMaxCardinality() {
    return maxCardinality;
  }
  public void setMaxCardinality(Integer maxCardinality) {
    this.maxCardinality = maxCardinality;
  }
  public Encapsulation getEncapsulation() {
    return encapsulation;
  }
  public void setEncapsulation(Encapsulation encapsulation) {
    this.encapsulation = encapsulation;
  }
  public Boolean isUnboundedCardinality() {
    return unboundedCardinality;
  }
  public void setUnboundedCardinality(Boolean unboundedCardinality) {
    this.unboundedCardinality = unboundedCardinality;
  }
  
  public String getMultiplicity() {
    String result = minCardinalityString() + maxCardinalityString();
    
    return result.length() == 0 ? null : result;
    
  }
  private String minCardinalityString() {
    return minCardinality == null ? "" : minCardinality + ":";
  }
  
  private String maxCardinalityString() {
    return 
      maxCardinality != null ?  maxCardinality.toString() :
      (unboundedCardinality==null) ? "" : 
      unboundedCardinality ? "*" : 
      "";
  }
  
  

}
