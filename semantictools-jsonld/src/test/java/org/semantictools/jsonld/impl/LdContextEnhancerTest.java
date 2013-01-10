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

import java.io.File;
import java.io.InputStream;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.semantictools.jsonld.LdClass;
import org.semantictools.jsonld.LdContext;
import org.semantictools.jsonld.LdDatatype;
import org.semantictools.jsonld.LdQualifiedRestriction;
import org.semantictools.jsonld.LdRestriction;
import org.semantictools.jsonld.LdTerm;
import org.semantictools.jsonld.XsdType;
import org.semantictools.jsonld.impl.InMemoryVocabularyManager;
import org.semantictools.jsonld.impl.LdContextEnhancerImpl;
import org.semantictools.jsonld.io.impl.LdContextReaderImpl;

public class LdContextEnhancerTest {
  
  protected LdContextEnhancerImpl enhancer;
  protected LdContext context;
  
  @Before
  public void setUp() throws Exception {

    InputStream contextStream = getClass().getClassLoader().getResourceAsStream("gdx/GradebookItemEventContext.json");
    File root = new File("src/test/resources/gdx");
    InMemoryVocabularyManager manager = new InMemoryVocabularyManager();
    manager.scan(root);
    
    enhancer = new LdContextEnhancerImpl(manager);
    
    
    LdContextReaderImpl contextParser = new LdContextReaderImpl(null);
    context = contextParser.parseExternalContext(contextStream);
    context.close();
  }

  @Test
  public void test() throws Exception {
    
    enhancer.enhance(context);
    afterEnhance();
    
    verify();
  }


  protected void afterEnhance() throws Exception {
    
  }

  protected void verify() {

    verifyBody();
    verifyMetadataType();
    verifyGUID();
    verifyGradebookItemEventDomain();
    verifyLastModified();
    verifyConversationId();
    verifyAssignmentResult();
    verifyGradebookItem();
    verifyGradebookItemEvent();
    
  }

  private void verifyGradebookItemEvent() {
    LdClass rdfClass = context.getClass("http://purl.org/pearson/core/v1/vocab/outcomes#GradebookItemEvent");
    assertTrue(rdfClass != null);
    LdRestriction r = rdfClass.findRestrictionByPropertyURI("http://purl.org/pearson/core/v1/vocab/message#body");
    assertTrue(r != null);
    LdQualifiedRestriction qr = r.findQualifiedRestrictionByRangeURI("http://purl.org/pearson/core/v1/vocab/outcomes#GradebookItem");
    assertTrue(qr != null);
    
    assertEquals(new Integer(1), qr.getMinCardinality());
    
    
  }

  private void verifyGradebookItem() {
    
    LdTerm term = context.getTerm("GradebookItem");
    assertTrue(term != null);
    
    LdClass rdfClass = term.getRdfClass();
    assertTrue(rdfClass != null);
    
    LdRestriction r = rdfClass.findRestrictionByPropertyURI("http://purl.org/pearson/core/v1/vocab/outcomes#lastModified");
    assertTrue(r != null);
    assertEquals(new Integer(1), r.getMaxCardinality());
    
  }


  private void verifyAssignmentResult() {
   
    LdTerm term = context.getTerm("AssignmentResult");
    assertTrue(term == null);
    
  }

  private void verifyConversationId() {
    
    LdTerm term = context.getTerm("conversationId");
    assertTrue(term != null);
    assertEquals("http://purl.org/pearson/core/v1/vocab/datatypes#GUID.Type", term.getTypeIRI());
    
  }

  private void verifyGUID() {
    LdTerm term = context.getTerm("GUID.Type");
    assertTrue(term != null);
    LdDatatype datatype = term.getDatatype();
    assertTrue(datatype != null);
    assertEquals(new Integer(64), datatype.getMaxLength());
    assertEquals(XsdType.NORMALIZEDSTRING, datatype.getXsdType());
  }

  private void verifyBody() {
    LdTerm term = context.getTerm("body");
    assertTrue(term != null);
    
    
    
    
  }

  private void verifyLastModified() {
    LdTerm term = context.getTerm("lastModified");
    assertTrue(term != null);
    assertTrue(term.ensureProperty().getDomain().contains("http://purl.org/pearson/core/v1/vocab/outcomes#GradebookItem"));
    assertTrue(term.ensureProperty().getDomain().contains("http://purl.org/pearson/core/v1/vocab/outcomes#AssignmentResult"));
    
  }

  private void verifyMetadataType() {
    
    LdTerm term = context.getTerm("metadata");
    assertTrue(term != null);
    assertEquals("http://purl.org/pearson/core/v1/vocab/message#MessageHeader", term.getTypeIRI());
    
  }

  private void verifyGradebookItemEventDomain() {
    
    LdClass dr = context.getClass("http://purl.org/pearson/core/v1/vocab/outcomes#GradebookItemEvent");
    assertTrue(dr != null);
    
    List<LdClass> superList = dr.listSupertypes();
    assertTrue(superList != null);
    assertEquals(1, superList.size());
    
  }

}
