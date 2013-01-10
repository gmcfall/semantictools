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

import org.junit.Before;
import org.junit.Test;
import org.semantictools.jsonld.LdContext;
import org.semantictools.jsonld.LdTerm;
import org.semantictools.jsonld.io.impl.LdContextReaderImpl;

public class NetworkContextManagerTest {

  private String contextURI = "http://purl.org/pearson/core/v1/ctx/outcomes/GradebookItemEvent";
  private NetworkContextManager manager;
  
  @Before
  public void setUp() {
    manager = new NetworkContextManager(new LdContextReaderImpl(null));
  }
  @Test
  public void test() {
    LdContext context = manager.findContext(contextURI);
    
    assertTrue(context != null);
    
    verifyContentLocation(context);
  }
  private void verifyContentLocation(LdContext context) {
    
    LdTerm term = context.getTerm("contentLocation");
    assertTrue(term != null);
    assertEquals("@id", term.getRawTypeIRI());
    
  }

}
