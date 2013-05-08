package org.semantictools.frame.api;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntProperty;
import com.hp.hpl.jena.rdf.model.ModelFactory;

public class DublinCoreElements {


  private static final OntModel model = ModelFactory.createOntologyModel();
  
  public static final OntProperty title = 
      model.createOntProperty("http://purl.org/dc/elements/1.1/title");
}
