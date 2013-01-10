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

import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;

import org.junit.Before;
import org.semantictools.jsonld.LdContextManager;
import org.semantictools.jsonld.impl.InMemoryLdContextManager;
import org.semantictools.jsonld.io.LdContextReader;
import org.semantictools.jsonld.io.impl.LdContextReaderImpl;
import org.semantictools.jsonld.io.impl.LdContextWriterImpl;

public class EnhancedContextIoTest extends LdContextEnhancerTest {
  

  private LdContextReader contextParser;
 
  @Override
  @Before
  public void setUp() throws Exception {
    super.setUp();
    LdContextManager manager = new InMemoryLdContextManager();
    contextParser = new LdContextReaderImpl(manager);
    
  }
  
  @Override
  protected void afterEnhance() throws Exception {
    
    
    StringWriter buffer = new StringWriter();
    LdContextWriterImpl writer = new LdContextWriterImpl();
    writer.write(context, new PrintWriter(buffer));
    
    String text = buffer.toString();
    context = contextParser.parserExternalContext(new StringReader(text));
    
    
  }

}
