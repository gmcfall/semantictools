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

import java.util.HashMap;
import java.util.Map;

import org.semantictools.jsonld.LdContext;
import org.semantictools.jsonld.LdContextManager;

/**
 * An LdContextManager that holds a set of LdContext instances in memory.
 * This class is used mostly for test purposes.
 * @author Greg McFall
 *
 */
public class InMemoryLdContextManager implements LdContextManager {
  
  private Map<String, LdContext> map = new HashMap<String, LdContext>();
  private Map<String, LdContext> enhancedContextMap = new HashMap<String, LdContext>();
 
  
  public void add(LdContext context) {
    if (context.isEnhanced()) {
      enhancedContextMap.put(context.getContextURI(), context);
    } else {
      map.put(context.getContextURI(), context);
    }
  }

  @Override
  public LdContext findContext(String contextURI) {
    LdContext result = map.get(contextURI);
    
    return result;
  }

  @Override
  public LdContext findEnhancedContext(String contextURI) {
    return enhancedContextMap.get(contextURI);
  }

  
  

}
