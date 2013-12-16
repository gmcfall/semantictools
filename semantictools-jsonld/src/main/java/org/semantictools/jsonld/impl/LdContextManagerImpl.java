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

import java.io.IOException;

import org.semantictools.jsonld.LdAsset;
import org.semantictools.jsonld.LdAssetManager;
import org.semantictools.jsonld.LdContext;
import org.semantictools.jsonld.LdContextEnhancer;
import org.semantictools.jsonld.LdContextManager;
import org.semantictools.jsonld.LdContextParseException;
import org.semantictools.jsonld.io.LdContextReader;

public class LdContextManagerImpl implements LdContextManager{
  
  private LdAssetManager assetManager;
  private LdContextReader contextReader;
  private LdContextEnhancer contextEnhancer;
  private boolean enhance;
  
  public LdContextManagerImpl(LdAssetManager assetManager, LdContextReader contextReader,
      LdContextEnhancer contextEnhancer) {
    this.assetManager = assetManager;
    this.contextEnhancer = contextEnhancer;
    this.contextReader = contextReader;
  }
  
  public void setEnhance(boolean enhance) {
    this.enhance = enhance;
  }
  
  @Override
  public LdContext findContext(String contextURI) throws LdContextParseException, IOException {
    
    if (enhance) {
      try {
        return findEnhancedContext(contextURI);
      } catch (LdContextEnhanceException e) {
        throw new LdContextParseException(e);
      }
    }
    
    LdAsset asset = assetManager.findAsset(contextURI);
    if (asset == null) {
      return null;
    }
    
    LdContext context = contextReader.parserExternalContext(asset.getReader());
    context.setContextURI(contextURI);
    return context;
  }
  
  @Override
  public LdContext findEnhancedContext(String contextURI) throws LdContextParseException, IOException, LdContextEnhanceException  {
    boolean enhance = false;
    LdAsset asset = assetManager.findAsset(contextURI, LdContentType.ENHANCED_CONTEXT);
    if (asset == null) {
      asset = assetManager.findAsset(contextURI, LdContentType.JSON_LD_CONTEXT);
      enhance = true;
    }
    if (asset == null) return null;

    LdContext context = contextReader.parserExternalContext(asset.getReader());
    if (enhance) {
      contextEnhancer.enhance(context);
    }
    
    return context;
  }
  
  

}
