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
package org.semantictools.publish;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.ParserConfigurationException;

import org.semantictools.context.renderer.GlobalPropertiesReader;
import org.semantictools.context.renderer.MediaTypeFileManager;
import org.semantictools.context.renderer.URLRewriter;
import org.semantictools.context.renderer.model.ContextProperties;
import org.semantictools.context.renderer.model.GlobalProperties;
import org.semantictools.context.renderer.model.SampleJson;
import org.semantictools.context.renderer.model.ServiceFileManager;
import org.semantictools.context.view.ServiceDocumentationPrinter;
import org.semantictools.frame.api.ContextManager;
import org.semantictools.frame.api.MediaTypeDocumenter;
import org.semantictools.frame.api.OntologyManager;
import org.semantictools.frame.api.SchemaParseException;
import org.semantictools.frame.api.ServiceDocumentationManager;
import org.semantictools.frame.api.TypeManager;
import org.semantictools.index.api.LinkedDataIndexPrinter;
import org.semantictools.index.api.impl.LinkedDataIndexImpl;
import org.semantictools.jsonld.LdAsset;
import org.semantictools.jsonld.LdProcessor;
import org.semantictools.jsonld.LdValidationMessage;
import org.semantictools.jsonld.LdValidationReport;
import org.semantictools.jsonld.LdValidationResult;
import org.semantictools.jsonld.impl.LdContentType;
import org.semantictools.jsonld.io.LdParseException;
import org.semantictools.uml.api.UmlFileManager;
import org.semantictools.uml.api.UmlPrinter;
import org.semantictools.uml.model.UmlManager;
import org.semantictools.web.upload.AppspotUploadClient;
import org.xml.sax.SAXException;

/**
 * A utility that generates JSON-LD contexts, UML, media type, and
 * REST service documentation.  This utility will also optionally
 * publish these artifacts to semantictools.appspot.com.
 * 
 * @author Greg McFall
 *
 */
public class DocumentationGenerator {
  
  private static final Logger defaultLogger = Logger.getLogger(DocumentationGenerator.class.getName());

  private Logger logger;
  private File rdfDir;
  private File pubDir;
  private File repoDir;
  private boolean publish = false;
  private String uploadEndpoint=null;
  private String version=null;
  private String indexFileName = "index.html";
  private boolean generate = true;
  
  
  /**
   * Creates a new DocumentationGenerator.
   * @param sourceDir  Directory containing the RDF sources plus properties files that drive the generator.
   * @param targetDir  The output directory in which artifacts will be stored locally.
   * @param repoDir The directory that will serve as a local repository for schemas and contexts.
   * @param publish    A flag that specifies whether artifacts should be published to semantictools.appspot.com.
   */
  public DocumentationGenerator(File sourceDir, File targetDir, File repoDir, boolean publish) {
    this.rdfDir = sourceDir;
    this.pubDir = targetDir;
    this.repoDir = repoDir;
    this.publish = publish;
  }
  
  
  public Logger getLogger() {
    return logger==null ? defaultLogger : logger;
  }




  public void setLogger(Logger logger) {
    this.logger = logger;
  }




  /**
   * Returns true if this DocumentationGenerator is configured to generate documentation, and
   * false if it is configured merely to publish documentation that was previously generated.
   */
  public boolean isGenerate() {
    return generate;
  }


  /**
   * Specify whether this DocumentationGenerator should produce documentation files (generate=true)
   * or if it should skip the production phase and go directly to the publish phase (generate=false).
   */
  public void setGenerate(boolean generate) {
    this.generate = generate;
  }



  public String getIndexFileName() {
    return indexFileName;
  }



  public void setIndexFileName(String indexFileName) {
    this.indexFileName = indexFileName;
  }



  public String getVersion() {
    return version;
  }


  public void setVersion(String version) {
    this.version = version;
  }


  public String getUploadEndpoint() {
    return uploadEndpoint;
  }


  public void setUploadEndpoint(String uploadEndpoint) {
    this.uploadEndpoint = uploadEndpoint;
  }


  public void run() throws IOException, ParserConfigurationException, SAXException, SchemaParseException  {


    UmlPrinter umlPrinter;
    URLRewriter rewriter;
    UmlManager umlManager;
    LinkedDataIndexPrinter indexPrinter;
    
    
    File umlDir = new File(pubDir, "uml");
    File mediaTypeDir = new File(pubDir, "mediatype");
    File umlCss = new File(umlDir, "uml.css");

    OntologyManager ontoManager = new OntologyManager();
    GlobalPropertiesReader globalReader = new GlobalPropertiesReader(ontoManager);
    GlobalProperties global = globalReader.scan(rdfDir);
    if (global == null) {
    	global = new GlobalProperties();
    }
    UmlFileManager umlFileManager = new UmlFileManager(umlDir);
    
    TypeManager typeManager = new TypeManager();
    typeManager.loadDir(rdfDir);

//    typeManager.getOntModel().writeAll(System.out, "TURTLE", "");
    
    typeManager.processOntologies();
    umlManager = new UmlManager(typeManager);
    
    rewriter = new URLRewriter() {
      
      @Override
      public String rewrite(String url) {
        return url;
      }
    };
    
    ServiceFileManager serviceFileManager = new ServiceFileManager(umlDir, umlCss);
    MediaTypeFileManager mediatypeFileManager = new MediaTypeFileManager(mediaTypeDir, mediaTypeDir);
    ContextManager contextManager = new ContextManager(global, mediatypeFileManager);
    MediaTypeDocumenter documenter = new MediaTypeDocumenter(contextManager, umlFileManager, global);
    documenter.loadAll(rdfDir);
    typeManager.analyzeOntologies();
    if (generate) {
      documenter.produceAllDocumentation(mediaTypeDir);
    }
    
    ServiceDocumentationPrinter servicePrinter = new ServiceDocumentationPrinter(rewriter);
    ServiceDocumentationManager serviceManager = new ServiceDocumentationManager(typeManager, global, contextManager, serviceFileManager, servicePrinter);
    serviceManager.scan(rdfDir);
    if (generate) {
      serviceManager.writeAll();
    }
    
    LinkedDataIndexImpl oracle = new LinkedDataIndexImpl(
        typeManager,
        contextManager, 
        serviceManager,
        umlFileManager);
    
    File indexFile = new File(pubDir, indexFileName);
    umlPrinter = new UmlPrinter(global, rewriter, umlManager, umlFileManager, oracle);
    if (generate) {
      indexPrinter = new LinkedDataIndexPrinter(indexFile, oracle);
      umlPrinter.printAll();
      indexPrinter.printIndex();
    }
    
    if (publish) {
      AppspotUploadClient uploader = new AppspotUploadClient();
      if (uploadEndpoint != null) {
        uploader.setEndpointURL(uploadEndpoint);
      }
      uploader.setVersion(version);
      uploader.uploadAll(pubDir);
    }
    
    validate(contextManager, global);
    
    ontoManager.scan(rdfDir);
    ontoManager.upload();
    ontoManager.uploadJsonLdContextFiles(contextManager.listContextProperties());
    ontoManager.publishToLocalRepository(contextManager.listContextProperties());
  }
  
  private void validate(ContextManager manager, GlobalProperties global) {
    if (repoDir == null) return;
    deleteDir(repoDir);
    repoDir.mkdirs();
    
    LdProcessor processor = createLdProcessor(global);
    processor.getContextManager().setEnhance(true);
    
    List<ContextProperties> list = manager.listContextProperties();
    // Need to publish all the contexts first because some contexts
    // might depend on other contexts.
    for (ContextProperties context : list) {
      publishJsonLdContext(processor, context);
    }
    // Now we can perform the validation.
    for (ContextProperties context : list) {
      if (context.getValidateJsonSamples()) {
        validate(processor, context);
      }
    }
    

  }




  private LdProcessor createLdProcessor(GlobalProperties global) {
   LdProcessor processor = new LdProcessor(rdfDir, repoDir, false);
   String list = global.getProperties().getProperty(ContextManager.SKIP_VALIDATION);
   if (list != null) {
     StringTokenizer tokenizer = new StringTokenizer(list, " \t\r\n");
     Set<String> ignoredProperties = new HashSet<String>();
     while (tokenizer.hasMoreTokens()) {
       ignoredProperties.add(tokenizer.nextToken());
     }
     processor.getValidationService().setIgnoredProperties(ignoredProperties);
   }
   
   return processor;
  }


  private void publishJsonLdContext(LdProcessor processor,
      ContextProperties context) {

    File contextFile = context.getContextFile();
    if (contextFile == null) {
      return;
    }
    try {
      String contextURI = context.getContextURI();
      URL url = contextFile.toURI().toURL();
      LdAsset asset = new LdAsset(contextURI, LdContentType.JSON_LD_CONTEXT, url);
      asset.loadContent();
      processor.publish(asset);
      processor.publishEnhancedContext(contextURI);
      
    } catch (Exception e) {
      getLogger().severe("Failed to add context to repo: " + contextFile);
    } 
    
    
  }


  private void validate(LdProcessor processor, ContextProperties context) {
    List<SampleJson> list = context.getSampleJsonList();
    File baseDir = context.getSourceFile().getParentFile();
    if (list.isEmpty()) {
      File defaultFile = new File(baseDir, "sample.json");
      if (defaultFile.exists()) {
        list = new ArrayList<SampleJson>();
        SampleJson sample = new SampleJson();
        sample.setFileName("sample.json");
        list.add(sample);
      }
    }
    
    for (SampleJson sample : list) {
      File file = new File(baseDir, sample.getFileName());
      try {
        URL url = file.toURI().toURL();
        LdValidationReport report = processor.validate(url);
        log(file, report);
        
      } catch (MalformedURLException e) {
        getLogger().log(Level.SEVERE, "Failed to validate JSON sample " + file);
        getLogger().log(Level.SEVERE, e.getMessage());
      } catch (LdParseException e) {
        getLogger().log(Level.SEVERE, "Failed to validate JSON sample " + file);
        getLogger().log(Level.SEVERE, e.getMessage());
      } catch (IOException e) {
        getLogger().log(Level.SEVERE, "Failed to validate JSON sample " + file);
        getLogger().log(Level.SEVERE, e.getMessage());
      } catch (Throwable e) {
        getLogger().log(Level.SEVERE, "Failed to validate JSON sample " + file);
        getLogger().log(Level.SEVERE, e.getMessage());
      }
    }
    
  }


  private void log(File file, LdValidationReport report) {
    if (!hasProblem(report)) return;
    
    Logger logger = getLogger();
    
    logger.warning("Detected problems in file " + file);
    
    for (LdValidationMessage message : report.listMessages()) {
      LdValidationResult result = message.getResult();
      Level level = 
          (result == LdValidationResult.ERROR) ? Level.SEVERE :
          (result == LdValidationResult.WARNING) ? Level.WARNING :
          null;
      
      if (level != null) {
        StringBuilder builder = new StringBuilder();
        builder.append(message.getPath());
        builder.append(": ");
        builder.append(message.getText());
        String text = builder.toString();
        logger.log(level, text);
      }
    }
    
  }
  
  private boolean hasProblem(LdValidationReport report) {
    for (LdValidationMessage msg : report.listMessages()) {
      if (msg.getResult() != LdValidationResult.OK) return true;
    }
    return false;
  }


  private void deleteDir(File file) {
    if (!file.exists()) return;
    if (file.isDirectory()) {
      File[] list = file.listFiles();
      for (File doomed : list) {
        deleteDir(doomed);
      }
    }
    file.delete();
    
  }

  
}
