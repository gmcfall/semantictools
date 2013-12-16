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
package org.semantictools.jsonld.io.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParser;
import org.semantictools.jsonld.LdContext;
import org.semantictools.jsonld.LdContextEnhancer;
import org.semantictools.jsonld.LdContextManager;
import org.semantictools.jsonld.LdContextParseException;
import org.semantictools.jsonld.impl.LdContextEnhanceException;
import org.semantictools.jsonld.io.ErrorHandler;
import org.semantictools.jsonld.io.LdContextReader;

public class EnhancedLdContextReader extends LdContextReaderImpl {
  
  private LdContextEnhancer enhancer;
  private LdContextReader reader;
  


  public EnhancedLdContextReader(LdContextManager manager, LdContextEnhancer enhancer,  LdContextReader reader) {
    super(manager);
    this.enhancer = enhancer;
    this.reader = reader;
  }

  @Override
  public LdContext parseExternalContext(InputStream stream)
      throws LdContextParseException, IOException {
    
    return reader.parseExternalContext(stream);
  }
  
  private LdContext enhance(LdContext context) throws LdContextEnhanceException {
    if (context != null && !context.isEnhanced()) {
      enhancer.enhance(context);
    }
    return context;
  }

  @Override
  public LdContext parserExternalContext(Reader reader)
      throws LdContextParseException, IOException {
    
    return this.reader.parserExternalContext(reader);
  }

  @Override
  public LdContext parseContextField(JsonParser parser)
      throws LdContextParseException, IOException {
    try {
    return enhance(reader.parseContextField(parser));
    } catch (LdContextEnhanceException oops) {
      throw new LdContextParseException(oops);
    }
  }

  @Override
  public void setErrorHandler(ErrorHandler handler) {
    reader.setErrorHandler(handler);
    
  }

  @Override
  public ErrorHandler getErrorHandler() {
    return reader.getErrorHandler();
  }

  protected void handleError(Throwable oops) throws IOException, LdContextParseException {
    ErrorHandler handler = getErrorHandler();
    if (handler == null) {
      super.handleError(oops);
    } else {
      handler.handleError(oops);
    }
  }

}
