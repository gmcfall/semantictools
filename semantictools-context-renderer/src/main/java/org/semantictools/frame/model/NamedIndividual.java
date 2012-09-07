package org.semantictools.frame.model;

import com.hp.hpl.jena.ontology.OntResource;

public class NamedIndividual {
  
  private OntResource ontResource;
  
  public NamedIndividual(OntResource ontResource) {
    this.ontResource = ontResource;
  }

  public String getLocalName() {
    return ontResource.getLocalName();
  }
  
  public String getUri() {
    return ontResource.getURI();
  }
  
  public String getComment() {
    String comment = ontResource.getComment(null);
    if (comment == null) {
      comment = "";
    }
    return comment;
  }

}
