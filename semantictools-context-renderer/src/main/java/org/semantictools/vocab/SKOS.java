package org.semantictools.vocab;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntProperty;
import com.hp.hpl.jena.rdf.model.ModelFactory;

public class SKOS {

  private static final OntModel model = ModelFactory.createOntologyModel();
  
  public static final OntProperty definition = model.createOntProperty("http://www.w3.org/2004/02/skos/core#definition");
}
