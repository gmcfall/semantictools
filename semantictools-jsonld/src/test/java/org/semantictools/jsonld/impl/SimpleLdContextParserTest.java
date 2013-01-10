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

import static org.junit.Assert.*;

import java.io.InputStream;

import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonToken;
import org.junit.Test;
import org.semantictools.jsonld.LdContext;
import org.semantictools.jsonld.LdTerm;
import org.semantictools.jsonld.io.impl.LdContextReaderImpl;

public class SimpleLdContextParserTest {

  @Test
  public void testParse() throws Exception {
    InputStream stream = getClass().getClassLoader().getResourceAsStream("gdx/GradebookItemEventContext.json");
    
    LdContextReaderImpl parser = new LdContextReaderImpl(null);
    LdContext context = parser.parseExternalContext(stream);
    
    LdTerm term = context.getTerm("po");
    assertTrue(term != null);
    assertEquals("http://purl.org/pearson/core/v1/vocab/outcomes#", term.getRawIRI());
    assertEquals("http://purl.org/pearson/core/v1/vocab/outcomes#", term.getIRI());
    
    term = context.getTerm("body");
    assertEquals("http://purl.org/pearson/core/v1/vocab/message#body", term.getIRI());
    
    term = context.getTerm("conversationId");
    assertEquals("http://purl.org/pearson/core/v1/vocab/message#conversationId", term.getIRI());
    assertEquals("lisd:GUID.Type", term.getRawTypeIRI());
    assertEquals("http://purl.org/pearson/core/v1/vocab/datatypes#GUID.Type", term.getTypeIRI());
    
  }

}
