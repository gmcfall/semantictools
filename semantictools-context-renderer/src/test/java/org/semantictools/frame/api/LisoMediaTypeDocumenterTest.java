package org.semantictools.frame.api;

import java.io.File;

import org.junit.Test;

public class LisoMediaTypeDocumenterTest {

  @Test
  public void test() throws Exception {

    File rdfDir = new File("src/test/resources/liso");
    File outDir = new File("target/media");
    
    MediaTypeDocumenter documenter = new MediaTypeDocumenter();
    
    documenter.loadAll(rdfDir);
    documenter.produceAllDocumentation(outDir);
    
    
    
  }

}
