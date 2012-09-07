package org.semantictools.frame.model;

import com.hp.hpl.jena.ontology.OntClass;

public interface RdfType {
  
  public String getLocalName();
  public String getUri();
  
  public boolean canAsOntClass();
  public boolean canAsFrame();
  public boolean canAsDatatype();
  public boolean canAsListType();
  public boolean canAsEnumeration();
  
  public OntClass asOntClass();
  public Frame asFrame();
  public Datatype asDatatype();
  public ListType asListType();
  public Enumeration asEnumeration();
  
  public String getNamespace();

}
