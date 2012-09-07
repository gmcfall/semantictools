package org.semantictools.frame.model;

import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntProperty;
import com.hp.hpl.jena.rdf.model.ModelFactory;

public class BindVocabulary {

  private static final OntModel model = ModelFactory.createOntologyModel();

  public static final OntClass AbstractClass = model.createClass("http://purl.org/semantictools/v1/vocab/bind#AbstractClass");
  public static final OntClass Addressable = model.createClass("http://purl.org/semantictools/v1/vocab/bind#Addressable");
  public static final OntClass EmbeddableClass = model.createClass("http://purl.org/semantictools/v1/vocab/bind#EmbeddableClass");
  public static final OntClass Enum = model.createClass("http://purl.org/semantictools/v1/vocab/bind#Enum");
  public static final OntClass Aggregation = model.createClass("http://purl.org/semantictools/v1/vocab/bind#Aggregation");
  public static final OntClass Composition = model.createClass("http://purl.org/semantictools/v1/vocab/bind#Composition");
  public static final OntClass AssociationQualifier = model.createClass("http://purl.org/semantictools/v1/vocab/bind#AssociationQualifier");
  public static final OntProperty uniqueKey = model.createOntProperty("http://purl.org/semantictools/v1/vocab/bind#uniqueKey");
  public static final OntProperty onProperty = model.createOntProperty("http://purl.org/semantictools/v1/vocab/bind#onProperty");
  public static final OntProperty associationType = model.createOntProperty("http://purl.org/semantictools/v1/vocab/bind#associationType");
  public static final OntProperty org = model.createOntProperty("http://purl.org/semantictools/v1/vocab/bind#org");
  public static final OntProperty jsonContext = model.createOntProperty("http://purl.org/semantictools/v1/vocab/bind#jsonContext");
  public static final OntProperty suggestedPrefix = model.createOntProperty("http://purl.org/semantictools/v1/vocab/bind#suggestedPrefix");
}
