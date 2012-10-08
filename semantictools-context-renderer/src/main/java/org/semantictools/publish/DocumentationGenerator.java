package org.semantictools.publish;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.semantictools.context.renderer.MediaTypeFileManager;
import org.semantictools.context.renderer.ServiceDocumentationPrinter;
import org.semantictools.context.renderer.URLRewriter;
import org.semantictools.context.renderer.model.ServiceFileManager;
import org.semantictools.frame.api.ContextManager;
import org.semantictools.frame.api.MediaTypeDocumenter;
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


  public void run() throws IOException, ParserConfigurationException, SAXException  {


    UmlPrinter printer;
    URLRewriter rewriter;
    UmlManager manager;
    LinkedDataIndexPrinter indexPrinter;
    
    
    File umlDir = new File(pubDir, "uml");
    File mediaTypeDir = new File(pubDir, "mediatype");
    File umlCss = new File(umlDir, "uml.css");

    UmlFileManager umlFileManager = new UmlFileManager(umlDir);
    
    TypeManager typeManager = new TypeManager();
    typeManager.loadDir(rdfDir);
    typeManager.processOntologies();
    manager = new UmlManager(typeManager);
    
    rewriter = new URLRewriter() {
      
      @Override
      public String rewrite(String url) {
        return url;
      }
    };
    
    ServiceFileManager serviceFileManager = new ServiceFileManager(umlDir, umlCss);
    MediaTypeFileManager mediatypeFileManager = new MediaTypeFileManager(mediaTypeDir);
    ContextManager contextManager = new ContextManager(mediatypeFileManager);
    MediaTypeDocumenter documenter = new MediaTypeDocumenter(contextManager, umlFileManager);
    documenter.loadAll(rdfDir);
    typeManager.analyzeOntologies();
    documenter.produceAllDocumentation(mediaTypeDir);
    
    ServiceDocumentationPrinter servicePrinter = new ServiceDocumentationPrinter(rewriter);
    ServiceDocumentationManager serviceManager = new ServiceDocumentationManager(contextManager, serviceFileManager, servicePrinter);
    serviceManager.scan(rdfDir);
    serviceManager.writeAll();
    
    LinkedDataIndexImpl oracle = new LinkedDataIndexImpl(
        mediaTypeDir, 
        typeManager,
        contextManager, 
        serviceManager,
        umlFileManager);
    
    
    printer = new UmlPrinter(rewriter, manager, umlFileManager, oracle);
    indexPrinter = new LinkedDataIndexPrinter(pubDir, oracle);
    
    printer.printAll();
    indexPrinter.printIndex();
    
    if (publish) {
      AppspotUploadClient uploader = new AppspotUploadClient();
      if (uploadEndpoint != null) {
        uploader.setEndpointURL(uploadEndpoint);
      }
      uploader.setVersion(version);
      uploader.uploadAll(pubDir);
    }
    
    
    
  }

  
}
