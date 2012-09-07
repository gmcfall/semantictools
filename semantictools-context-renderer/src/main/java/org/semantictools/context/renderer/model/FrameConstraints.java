package org.semantictools.context.renderer.model;

import java.util.HashSet;
import java.util.Set;

public class FrameConstraints {
  private String classURI;
  private Set<String> includedProperties = new HashSet<String>();
  private Set<String> excludedProperties = new HashSet<String>();
  
  public FrameConstraints(String classURI) {
    this.classURI = classURI;
  }
  
  /**
   * Returns the URI for the RDF class to which this set of constraints applies.
   */
  public String getClassURI() {
    return classURI;
  }
  
  public void setClassURI(String classURI) {
    this.classURI = classURI;
  }

  /**
   * Adds the specified property to the set of properties that are included
   * in the representation.
   * @param propertyURI
   */
  public void addIncludedProperty(String propertyURI) {
    includedProperties.add(propertyURI);
  }

  /**
   * Adds the specified property to the set of properties that are excluded from
   * the representation.
   * @param propertyURI
   */
  public void addExcludedProperty(String propertyURI) {
    excludedProperties.add(propertyURI);
  }
  
  /**
   * Returns true if the specified property is included in the representation
   * of the RDF class associated with this FrameConstraints object.
   */
  public boolean isIncludedProperty(String propertyURI) {
    return !excludedProperties.contains(propertyURI) && 
        (includedProperties.isEmpty() || includedProperties.contains(propertyURI));
  }
  
  
  

}
