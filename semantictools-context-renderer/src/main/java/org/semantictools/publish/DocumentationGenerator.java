package org.semantictools.publish;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.semantictools.context.renderer.URLRewriter;
import org.semantictools.frame.api.ContextManager;
import org.semantictools.frame.api.MediaTypeDocumenter;
import org.semantictools.frame.api.TypeManager;
import org.semantictools.index.api.LinkedDataIndexPrinter;
import org.semantictools.index.api.impl.LinkedDataIndexImpl;
import org.semantictools.uml.api.UmlFileManager;
import org.semantictools.uml.api.UmlPrinter;
import org.semantictools.uml.model.UmlManager;
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

  public void run() throws IOException, ParserConfigurationException, SAXException  {


    UmlPrinter printer;
    URLRewriter rewriter;
    UmlManager manager;
    LinkedDataIndexPrinter indexPrinter;
    
    File umlDir = new File(pubDir, "uml");
    File mediaTypeDir = new File(pubDir, "mediatype");
    
    
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
    
    UmlFileManager fileManager = new UmlFileManager(umlDir);
    
    

    MediaTypeDocumenter documenter = new MediaTypeDocumenter();
    documenter.setPublish(publish);
    documenter.loadAll(rdfDir);
    documenter.produceAllDocumentation(mediaTypeDir);
    
    ContextManager contextManager = documenter.getContextManager();
    
    LinkedDataIndexImpl oracle = new LinkedDataIndexImpl(
        mediaTypeDir, 
        typeManager,
        contextManager, 
        documenter.getServiceDocumentManager(),
        fileManager);
    
    printer = new UmlPrinter(rewriter, manager, fileManager, oracle);
    indexPrinter = new LinkedDataIndexPrinter(pubDir, oracle);
    
    if (publish) {
      printer.setUploadClient(documenter.getUploadClient());
    }
    

    printer.printAll();
    indexPrinter.printIndex();
    
    
    
  }

  
}
