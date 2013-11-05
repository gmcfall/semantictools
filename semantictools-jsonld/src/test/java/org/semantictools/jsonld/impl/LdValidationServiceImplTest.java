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
import static org.junit.Assert.fail;

import java.io.File;
import java.io.InputStream;
import java.io.PrintWriter;

import org.junit.Before;
import org.junit.Test;
import org.semantictools.jsonld.LdContext;
import org.semantictools.jsonld.LdNode;
import org.semantictools.jsonld.LdValidationMessage;
import org.semantictools.jsonld.LdValidationReport;
import org.semantictools.jsonld.io.LdParser;
import org.semantictools.jsonld.io.impl.HtmlReportWriter;
import org.semantictools.jsonld.io.impl.LdContextReaderImpl;

public class LdValidationServiceImplTest {

  private String contextURI = "http://purl.org/pearson/core/v1/ctx/outcomes/GradebookItemEvent";
  private String contextPath = "gdx/GradebookItemEventContext.json";
  private String gradebookItemEventInvalid = "GradebookItemEventInvalid.json";
  private String gradebookItemEventContext = "GradebookItemEventContext.json";
  private String noBody = "GradebookItemEventNoBody.json";
  
  private LdValidationServiceImpl validator;
  private LdParser parser;
  
  @Before
  public void setUp() throws Exception {

    InputStream contextStream = getClass().getClassLoader().getResourceAsStream(contextPath);
    
    File root = new File("src/test/resources");
    InMemoryVocabularyManager manager = new InMemoryVocabularyManager();
    manager.scan(root);
    
    LdContextEnhancerImpl enhancer = new LdContextEnhancerImpl(manager);
    

    InMemoryLdContextManager contextManager = new InMemoryLdContextManager();
    LdContextReaderImpl contextParser = new LdContextReaderImpl(contextManager);
    LdContext context = contextParser.parseExternalContext(contextURI, contextStream);
    context.close();
    contextManager.add(context);
    enhancer.enhance(context);
    contextManager.add(context);
    
    
    parser = new LdTreeReader(contextParser);
    validator = new LdValidationServiceImpl();
  }

  @Test
  public void testGradebookItemEvent() throws Exception {

    InputStream documentStream = getClass().getClassLoader().getResourceAsStream(gradebookItemEventInvalid);
    LdNode node = parser.parse(documentStream);  
    LdValidationReport report = validator.validate(node);
    
    assertTrue(report != null);
    
    HtmlReportWriter writer = new HtmlReportWriter();
    writer.writeReport(new PrintWriter(System.out), report);

    validateReport(report);
   
  }
  
  @Test 
  public void testNoBody() throws Exception {

    InputStream documentStream = getClass().getClassLoader().getResourceAsStream(noBody);
    LdNode node = parser.parse(documentStream);  
    LdValidationReport report = validator.validate(node);
    
    assertTrue(report != null);
    
  }
  
  /**
   * Verify that the validator works even if the JSON-LD context
   * is defined at the end of the JSON document.
   */
  @Test
  public void testContext() throws Exception {

    InputStream documentStream = getClass().getClassLoader().getResourceAsStream(gradebookItemEventContext);
    LdNode node = parser.parse(documentStream);  
    LdValidationReport report = validator.validate(node);
    
    assertEquals(0, report.listMessages().size());
  }
  
  public static void validateReport(LdValidationReport report) {

    assertMessage(report, "ERROR   metadata.msgId Expected minCardinality=1, but found cardinality=0");
    assertMessage(report, "ERROR   metadata.version Should have maxLength=64, but found length=70");
    assertMessage(report, "ERROR   metadata.conversationId Value does not match the GUID.Type pattern: \\S*");
    assertMessage(report, "ERROR   body.normalMaximum Invalid domain for this property");
  }

  private static void assertMessage(LdValidationReport report, String message) {
    for (LdValidationMessage m : report.listMessages()) {
      if (  message.equals(m.toString())  ) return;
    }
    fail("Message not reported: " + message);
  }
}
