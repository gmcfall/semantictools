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
package org.semantictools.jsonld;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.semantictools.jsonld.impl.AppspotContextPublisher;
import org.semantictools.jsonld.impl.LdAssetManagerImpl;
import org.semantictools.jsonld.impl.LdAssetRepository;
import org.semantictools.jsonld.impl.LdContentType;
import org.semantictools.jsonld.impl.LdContextEnhanceException;
import org.semantictools.jsonld.impl.LdContextEnhancerImpl;
import org.semantictools.jsonld.impl.LdContextManagerImpl;
import org.semantictools.jsonld.impl.LdParserImpl;
import org.semantictools.jsonld.impl.LdPublisherPipeline;
import org.semantictools.jsonld.impl.LdTreeReader;
import org.semantictools.jsonld.impl.LdValidationServiceImpl;
import org.semantictools.jsonld.io.ErrorHandler;
import org.semantictools.jsonld.io.LdContextReader;
import org.semantictools.jsonld.io.LdContextWriter;
import org.semantictools.jsonld.io.LdParseException;
import org.semantictools.jsonld.io.LdParser;
import org.semantictools.jsonld.io.impl.EnhancedLdContextReader;
import org.semantictools.jsonld.io.impl.LdContextReaderImpl;
import org.semantictools.jsonld.io.impl.LdContextWriterImpl;

/**
 * A processor that enhances and publishes JSON-LD contexts and
 * performs JSON-LD validation.
 * 
 * @author Greg McFall
 *
 */
public class LdProcessor implements LdPublisher {
  
  private LdContextReader contextReader;
  private LdContextManager contextManager;
  private LdContextEnhancer contextEnhancer;
  private LdContextWriter contextWriter;
  private LdAssetManager assetManager;
  private LdPublisher publisher;
  private LdParser jsonldParser;
  private LdValidationService validationService;
  
  /**
   * Create a default LdEnhancerApp which reads JSON-LD contexts
   * from the Internet and publishes an enhanced representation
   * to appspot.
   */
  public LdProcessor() {}
  
  
  /**
   * Create an LdEnhancerApp which first attempts to read JSON-LD contexts
   * from a local repository and falls back to loading from the Internet
   * if the context is not found.  Enhanced contexts are published
   * to the local repository and optionally to the online semantic-tools
   * repository.
   * 
   * @param repositoryDir  The root directory of the local repository.
   * 
   * @param publishToInternet  If true, publish to the online semantic-tools
   * repository.  Otherwise, publish only to the local repository.
   */
  public LdProcessor(File repositoryDir, boolean publishToInternet) {
    this(null, repositoryDir, publishToInternet);
  }

  /**
   * Create an LdEnhancerApp which first attempts to read JSON-LD contexts
   * from a local repository and falls back to loading from the Internet
   * if the context is not found.  Enhanced contexts are published
   * to the local repository and optionally to the online semantic-tools
   * repository.
   * 
   * @param sourceDir  A directory that should be scanned for assets 
   *   include RDF ontologies, XML Schemas, and JSON-LD context definitions.
   *   These assets will be added to the specified local repository.
   * 
   * @param repositoryDir  The root directory of the local repository.
   * 
   * @param publishToInternet  If true, publish to the online semantic-tools
   * repository.  Otherwise, publish only to the local repository.
   */
  public LdProcessor(File sourceDir, File repositoryDir, boolean publishToInternet) {
    LdAssetRepository repository = new LdAssetRepository(repositoryDir);
    
    publisher = publishToInternet ?
        new LdPublisherPipeline(repository, new AppspotContextPublisher()) :
        repository;
        
    assetManager = repository;
    if (sourceDir != null) {
      repository.scan(sourceDir);
    }
  }
  
  public LdContextReader getContextReader() {
    if (contextReader == null) {
      LdContextReaderImpl impl = new LdContextReaderImpl(null);
      contextReader = impl;   
      impl.setManager(getContextManager());
    }
    return contextReader;
  }

  public LdContextManager getContextManager() {
    if (contextManager == null) {      
      contextManager = new LdContextManagerImpl(getAssetManager(), getContextReader(), getContextEnhancer());
    }
    return contextManager;
  }

  private LdContextEnhancer getContextEnhancer() {
    if (contextEnhancer == null) {
      contextEnhancer = new LdContextEnhancerImpl(getAssetManager());
    }
    return contextEnhancer;
  }

  private LdAssetManager getAssetManager() {
    if (assetManager == null) {
      assetManager = new LdAssetManagerImpl();
    }
    return assetManager;
  }
  
  private LdPublisher getPublisher() {
    if (publisher == null) {
      publisher = new AppspotContextPublisher();
    }
    return publisher;
  }
  
  private LdContextWriter getContextWriter() {
    if (contextWriter == null) {
      contextWriter = new LdContextWriterImpl();
    }
    return contextWriter;
  }
  
  private LdParser getLdParser() {
    if (jsonldParser == null) {
      LdContextReader reader = new EnhancedLdContextReader(getContextManager(), getContextEnhancer(), getContextReader());
      jsonldParser = new LdTreeReader(reader);
    }
    return jsonldParser;
  }
  
  public LdValidationService getValidationService() {
    if (validationService == null) {
      validationService = new LdValidationServiceImpl();
    }
    return validationService;
  }

  /**
   * Get (or create) an enhanced rendition of the specified JSON-LD context, and
   * publish the result to a repository that has been configured with this LdProcessor.
   * 
   * @param contextURL The URL of the JSON-LD context that should be enhanced and published.
   * @throws IOException
   * @throws LdContextNotFoundException
   * @throws LdContextParseException
   * @throws LdPublishException
   * @throws LdContextEnhanceException 
   */
  public void publishEnhancedContext(String contextURL) throws IOException, LdContextNotFoundException, LdContextParseException, LdPublishException, LdContextEnhanceException {
    
    LdContext context = getContextManager().findEnhancedContext(contextURL);
    if (context == null) {
      throw new LdContextNotFoundException(contextURL);
    }
    StringWriter buffer = new StringWriter();
    PrintWriter out = new PrintWriter(buffer);
    LdContextWriter contextWriter = getContextWriter();
    contextWriter.write(context, out);
    
    LdAsset asset = new LdAsset(contextURL, LdContentType.ENHANCED_CONTEXT, null);
    asset.setContent(buffer.toString());
    
    LdPublisher publisher = getPublisher();
    publisher.publish(asset);
    
  }
  
  public LdValidationReport validate(URL jsonDocument) throws LdParseException, IOException {
    
    LdParser parser = getLdParser();
    LdValidationService service = getValidationService();
    ValidationErrorHandler handler = new ValidationErrorHandler();
    getContextReader().setErrorHandler(handler);
    LdNode node = parser.parse(jsonDocument.openStream());
    getContextReader().setErrorHandler(null);
    
    LdValidationReport report = service.validate(node);
    handler.reportErrors(report);
    
    return report;
  }
  
  static class ValidationErrorHandler implements ErrorHandler {
    private List<Throwable> errorList = new ArrayList<>();

    @Override
    public void handleError(Throwable error) {
      errorList.add(error);
    }
    
    void reportErrors(LdValidationReport report) {
      for (Throwable e : errorList) {
        LdValidationMessage message = new LdValidationMessage(LdValidationResult.WARNING, "", e.getMessage());
        report.add(message);
      }
    }
    
  }


  @Override
  public void publish(LdAsset asset) throws LdPublishException {
    getPublisher().publish(asset);
  }
}
