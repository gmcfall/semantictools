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
package org.semantictools.bind;

import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntProperty;
import com.hp.hpl.jena.rdf.model.ModelFactory;

public class BindOntology {

  private static final OntModel model = ModelFactory.createOntologyModel();

  public static final OntClass Addressable = model.createClass("http://purl.org/semantictools/v1/vocab/bind#Addressable");
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
  public static final OntProperty property = model.createOntProperty("http://purl.org/semantictools/v1/vocab/bind#property");
  public static final OntProperty domain = model.createOntProperty("http://purl.org/semantictools/v1/vocab/bind#domain");
  public static final OntProperty key = model.createOntProperty("http://purl.org/semantictools/v1/vocab/bind#key");
  public static final OntProperty keyKey = model.createOntProperty("http://purl.org/semantictools/v1/vocab/bind#keyKey");
  public static final OntProperty inverseAssociationType = model.createOntProperty("http://purl.org/semantictools/v1/vocab/bind#inverseAssociationType");
  public static final OntProperty inverseMinCardinality = model.createOntProperty("http://purl.org/semantictools/v1/vocab/bind#inverseMinCardinality");
  public static final OntProperty inverseMaxCardinality = model.createOntProperty("http://purl.org/semantictools/v1/vocab/bind#inverseMaxCardinality");
  public static final OntProperty inverseUnboundedCardinality = model.createOntProperty("http://purl.org/semantictools/v1/vocab/bind#inverseUnboundedCardinality");
  
}
