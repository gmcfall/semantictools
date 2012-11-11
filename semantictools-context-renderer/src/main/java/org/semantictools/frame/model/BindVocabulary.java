/*******************************************************************************
 * Copyright 2012 Pearson Education
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
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
  public static final OntClass Package = model.createClass("http://purl.org/semantictools/v1/vocab/bind#Package");
  public static final OntProperty uniqueKey = model.createOntProperty("http://purl.org/semantictools/v1/vocab/bind#uniqueKey");
  public static final OntProperty onProperty = model.createOntProperty("http://purl.org/semantictools/v1/vocab/bind#onProperty");
  public static final OntProperty associationType = model.createOntProperty("http://purl.org/semantictools/v1/vocab/bind#associationType");
  public static final OntProperty org = model.createOntProperty("http://purl.org/semantictools/v1/vocab/bind#org");
  public static final OntProperty jsonContext = model.createOntProperty("http://purl.org/semantictools/v1/vocab/bind#jsonContext");
  public static final OntProperty suggestedPrefix = model.createOntProperty("http://purl.org/semantictools/v1/vocab/bind#suggestedPrefix");
  public static final OntProperty javaName = model.createOntProperty("http://purl.org/semantictools/v1/vocab/bind#javaName");
}
