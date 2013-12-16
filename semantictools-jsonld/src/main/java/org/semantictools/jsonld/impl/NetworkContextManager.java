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

import java.io.InputStream;
import java.net.URL;

import org.semantictools.jsonld.LdContext;
import org.semantictools.jsonld.LdContextManager;
import org.semantictools.jsonld.io.LdContextReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NetworkContextManager implements LdContextManager {
  private static Logger logger = LoggerFactory.getLogger(NetworkContextManager.class);
  
  private LdContextReader contextReader;
  
  public NetworkContextManager(LdContextReader reader) {
    contextReader = reader;
  }

  /**
   * Attempts to download and parse the specified context directly from
   * the Internet.
   */
  @Override
  public LdContext findContext(String contextURI) {
   
    try {
      URL url = new URL(contextURI);
      InputStream stream = url.openStream();
      try {
        LdContext context = contextReader.parseExternalContext(stream);
        return context;
      } finally {
        safeClose(stream);
      }
    } catch (Throwable oops) {
      logger.warn("Failed to download context: " + contextURI, oops);
      return null;
    }
    
    
  }

  private void safeClose(InputStream stream) {
    try {
      stream.close();
    } catch (Throwable ignore) {
      
    }
    
  }

  @Override
  public LdContext findEnhancedContext(String contextURI) {
    return null;
  }

  @Override
  public void setEnhance(boolean value) {
    throw new RuntimeException("Enhance property not supported");
    
  }


}
