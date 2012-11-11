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

import javax.xml.parsers.ParserConfigurationException;

import org.semantictools.context.renderer.GlobalPropertiesReader;
import org.semantictools.context.renderer.MediaTypeFileManager;
import org.semantictools.context.renderer.URLRewriter;
import org.semantictools.context.renderer.model.GlobalProperties;
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

  private File rdfDir;
  private File pubDir;
  private boolean publish = false;
  private String uploadEndpoint=null;
  private String version=null;
  private String indexFileName = "index.html";
  private boolean generate = true;
  
  
  /**
   * Creates a new DocumentationGenerator.
   * @param sourceDir  Directory containing the RDF sources plus properties files that drive the generator.
   * @param targetDir  The output directory in which artifacts will be stored locally.
   * @param publish    A flag that specifies whether artifacts should be published to semantictools.appspot.com.
   */
  public DocumentationGenerator(File sourceDir, File targetDir, boolean publish) {
    this.rdfDir = sourceDir;
    this.pubDir = targetDir;
    this.publish = publish;
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
    UmlFileManager umlFileManager = new UmlFileManager(umlDir);
    
    TypeManager typeManager = new TypeManager();
    typeManager.loadDir(rdfDir);
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
    ServiceDocumentationManager serviceManager = new ServiceDocumentationManager(global, contextManager, serviceFileManager, servicePrinter);
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
    
    ontoManager.scan(rdfDir);
    ontoManager.upload();
    ontoManager.uploadJsonLdContextFiles(contextManager.listContextProperties());
    ontoManager.publishToLocalRepository(contextManager.listContextProperties());
    
    
    
  }

  
}
