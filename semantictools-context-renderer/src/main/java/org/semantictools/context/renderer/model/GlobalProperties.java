package org.semantictools.context.renderer.model;

import java.util.HashSet;
import java.util.Set;

public class GlobalProperties {
  
  private Set<String> ignoredOntology = new HashSet<String>();
  
  public void addIgnoredOntology(String ontologyURI) {
    ignoredOntology.add(ontologyURI);
  }
  
  public boolean isIgnoredOntology(String ontologyURI) {
    return ignoredOntology.contains(ontologyURI);
  }
  


}
