package org.semantictools.index.model;

import java.util.ArrayList;

import org.semantictools.context.renderer.model.ServiceDocumentation;
import org.semantictools.frame.api.TypeManager;

public class ServiceDocumentationList extends ArrayList<ServiceDocumentation> {
  private static final long serialVersionUID = 1L;
  
  private String rdfTypeURI;
  
  
  public ServiceDocumentationList(String rdfTypeURI) {
    this.rdfTypeURI = rdfTypeURI;
  }

  public String getRdfTypeURI() {
    return rdfTypeURI;
  }
  
  public String getRdfTypeLocalName() {
    return TypeManager.getLocalName(rdfTypeURI);
  }

}
