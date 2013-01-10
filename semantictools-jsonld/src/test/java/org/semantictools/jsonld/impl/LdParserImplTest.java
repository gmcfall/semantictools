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
package org.semantictools.jsonld.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Iterator;

import org.junit.Before;
import org.junit.Test;
import org.semantictools.jsonld.LdContainer;
import org.semantictools.jsonld.LdContext;
import org.semantictools.jsonld.LdField;
import org.semantictools.jsonld.LdNode;
import org.semantictools.jsonld.LdObject;
import org.semantictools.jsonld.io.LdParseException;
import org.semantictools.jsonld.io.impl.LdContextReaderImpl;

public class LdParserImplTest {

  private String jsonDocument = "GradebookItemEventSample.json";
  private LdParserImpl parser;
  
  @Before
  public void setUp() throws Exception {

    // For the purposes of this test, we are going to use an InMemoryLdContextManager,
    // and we will preload the JSON-LD context into memory.
    
    String contextURI = "http://purl.org/pearson/core/v1/ctx/outcomes/GradebookItemEvent";
    InputStream contextStream = getClass().getClassLoader().getResourceAsStream("gdx/GradebookItemEventContext.json");

    InMemoryLdContextManager contextManager = new InMemoryLdContextManager();
    LdContextReaderImpl contextParser = new LdContextReaderImpl(contextManager);
    LdContext context = contextParser.parseExternalContext(contextURI, contextStream);
    contextManager.add(context);
    
    parser = new LdParserImpl(contextParser);
  }
  
  @Test
  public void testEmptyDocument() throws Exception {
    InputStream input = new ByteArrayInputStream("".getBytes("UTF-8"));
    
   try {
     parser.parse(input);
   } catch (LdParseException oops) {
     String message = oops.getMessage();
     assertEquals("JSON-LD node not found. line: 1, column: 1", message);
   }
    
  }
  
  @Test
  public void test() throws Exception {
    InputStream input = getClass().getClassLoader().getResourceAsStream(jsonDocument);
    
    LdNode node = parser.parse(input);
    
    assertTrue(node.isObject());
    
    LdObject object = node.asObject();
    Iterator<LdField> sequence = object.fields();
    
    verifyMetadata(sequence.next());
    verifyBody(sequence.next());
  }

  private void verifyBody(LdField field) {
    assertEquals("body", field.getLocalName());
    Iterator<LdField> fieldSequence = field.getValue().asObject().fields();
    

    assertStringField(fieldSequence.next(), "label", "Chapter 3 Quiz");
    assertIriField(fieldSequence.next(), "integrationContract", "urn:pso:vendor/pearson.com/product/econ_xl");
    assertIriField(fieldSequence.next(), "context", "urn:udson:pearson.com/sms/prod:course/jsmith38271");
    verifyAssignment(fieldSequence.next());
    
    
  }

  private void verifyAssignment(LdField assignmentField) {
    
    assertEquals("assignment", assignmentField.getLocalName());
    LdObject object = assignmentField.getValue().asObject();
    
    assertEquals("http://mathxl.com/assignments/94722", object.getIRI());
    Iterator<LdField> field = object.fields();
    
    verifyTaxons(field.next());
    
  }

  private void verifyTaxons(LdField taxonField) {
    
    assertEquals("taxon", taxonField.getLocalName());
    LdContainer container = taxonField.getValue().asContainer();
    Iterator<LdNode> elements = container.iterator();
    assertIRI("http://purl.org/ASN/resources/S114362E", elements.next());
    assertIRI("http://purl.org/ASN/resources/S114362F", elements.next());
    assertTrue(!elements.hasNext());
    
  }

  private void assertStringNode(String expected, LdNode actual) {
    assertEquals(expected, actual.asLiteral().getStringValue());
    
  }

  private void assertIRI(String expected, LdNode actual) {
    assertEquals(expected, actual.asIRI().getValue());
    
  }

  private void verifyMetadata(LdField metadata) {
    
    LdObject object = metadata.getValue().asObject();
    Iterator<LdField> fieldSequence = object.fields();
    
    assertEquals("http://purl.org/pearson/core/v1/vocab/message#metadata", metadata.getPropertyURI());
    assertStringField(fieldSequence.next(), "version", "1.0");
    assertStringField(fieldSequence.next(), "msgId", "11ecf52f-d88b-4ce1-a33b-5566d2f53733");
    assertStringField(fieldSequence.next(), "timestamp", "2012-04-11T12:38:59-04:00");
    assertStringField(fieldSequence.next(), "sourceId", "urn:udson:pearson.com/xl");
    assertStringField(fieldSequence.next(), "destinationId", "urn:udson:pearson.com/eclg");
    assertTrue(!fieldSequence.hasNext());
    
  }

  private void assertStringField(LdField field, String fieldName, String fieldValue) {
    assertTrue(field != null);
    assertEquals(fieldName, field.getLocalName());
    assertEquals(fieldValue, field.getValue().asLiteral().getStringValue());
  }
  
  private void assertIriField(LdField field, String fieldName, String fieldValue) {
    assertTrue(field != null);
    assertEquals(fieldName, field.getLocalName());
    assertEquals(fieldValue, field.getValue().asIRI().getValue());
  }

}
