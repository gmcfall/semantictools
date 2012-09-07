package org.semantictools.uml.api;

import java.io.File;

import org.junit.Before;
import org.junit.Test;
import org.semantictools.context.renderer.URLRewriter;
import org.semantictools.frame.api.ContextManager;
import org.semantictools.frame.api.MediaTypeDocumenter;
import org.semantictools.frame.api.TypeManager;
import org.semantictools.index.api.LinkedDataIndexPrinter;
import org.semantictools.index.api.impl.LinkedDataIndexImpl;
import org.semantictools.uml.model.UmlManager;

public class DocumentationTest {
  
  private UmlPrinter printer;
  private URLRewriter rewriter;
  private UmlManager manager;
  private LinkedDataIndexPrinter indexPrinter;
  
  
  @Before
  public void setUp() throws Exception {

    boolean publish = false;
    File rdfDir = new File("src/test/resources/liso");
    File pubDir = new File("target/liso");
    

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
    
    
    
    
    
  }

  @Test
  public void test() throws Exception {
    printer.printAll();
    indexPrinter.printIndex();
  }

}
