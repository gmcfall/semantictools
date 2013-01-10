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
import static org.semantictools.jsonld.impl.LdContentType.TURTLE;
import static org.semantictools.jsonld.impl.LdContentType.XSD;

import java.io.File;

import org.junit.Test;
import org.semantictools.jsonld.LdAsset;
import org.semantictools.jsonld.impl.InMemoryVocabularyManager;
import org.semantictools.jsonld.impl.LdContentType;

public class InMemoryVocabularyManagerTest {

  @Test
  public void test() throws Exception {
   
    File root = new File("src/test/resources/gdx");
    InMemoryVocabularyManager manager = new InMemoryVocabularyManager();
    manager.scan(root);
    
    verifyVocab(manager, "http://purl.org/pearson/core/v1/vocab/outcomes#", TURTLE);
    verifyVocab(manager, "http://www.imsglobal.org/imspurl/lti/v2/xsd/ltid#", XSD);
    
    
    
  }

  private void verifyVocab(InMemoryVocabularyManager manager, String uri, LdContentType format) {
    
    LdAsset vocab = manager.findAsset(uri);
    assertTrue(vocab != null);
    assertEquals(vocab.getFormat(), format);
    assertEquals(vocab.getURI(), uri);
    assertTrue(vocab.getLocation()!= null);
    
    
  }

}
