package org.semantictools.frame.model;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntProperty;
import com.hp.hpl.jena.rdf.model.ModelFactory;

public class VannVocabulary {
  private static final OntModel model = ModelFactory.createOntologyModel();
  
  public static final OntProperty preferredNamespacePrefix = 
      model.createOntProperty("http://purl.org/vocab/vann/preferredNamespacePrefix");
  
  public static final OntProperty preferredNamespaceUri =
      model.createOntProperty("http://purl.org/vocab/vann/preferredNamespaceUri");
  
}
