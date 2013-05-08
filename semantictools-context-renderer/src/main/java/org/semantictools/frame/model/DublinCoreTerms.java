package org.semantictools.frame.model;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntProperty;
import com.hp.hpl.jena.rdf.model.ModelFactory;

public class DublinCoreTerms {

  private static final OntModel model = ModelFactory.createOntologyModel();
  
  public static final OntProperty title = 
      model.createOntProperty("http://purl.org/dc/terms/title");
}
