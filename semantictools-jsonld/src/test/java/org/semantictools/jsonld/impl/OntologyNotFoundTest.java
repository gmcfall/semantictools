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

public class OntologyNotFoundTest {
  
  protected LdContextEnhancerImpl enhancer;
  protected LdContext context;
  
  @Before
  public void setUp() throws Exception {

    InputStream contextStream = getClass().getClassLoader().getResourceAsStream("gdx/GradebookItemEventContext.json");
    InMemoryVocabularyManager manager = new InMemoryVocabularyManager();
    
    enhancer = new LdContextEnhancerImpl(manager);
    
    
    LdContextReaderImpl contextParser = new LdContextReaderImpl(null);
    context = contextParser.parseExternalContext(contextStream);
    context.close();
  }

  @Test
  public void test() throws Exception {
    try {
      enhancer.enhance(context);
    } catch (LdContextEnhanceException oops) {
      String message = oops.getMessage();
      
      assertTrue(message.contains("Namespace not found: http://purl.org/pearson/core/v1/vocab/outcomes#"));
      assertTrue(message.contains("Namespace not found: http://purl.org/pearson/core/v1/vocab/datatypes#"));
      assertTrue(message.contains("Namespace not found: http://purl.org/pearson/core/v1/vocab/enterprise#"));
      assertTrue(message.contains("Namespace not found: http://purl.org/pearson/core/v1/vocab/message#"));
      assertTrue(message.contains("Namespace not found: http://www.imsglobal.org/imspurl/lti/v2/vocab/lti#"));
      assertTrue(!message.contains("Namespace not found: http://www.w3.org/2001/XMLSchema#"));
      
    }
  }



}
