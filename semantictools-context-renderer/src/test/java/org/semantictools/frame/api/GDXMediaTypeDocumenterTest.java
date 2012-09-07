package org.semantictools.frame.api;

import java.io.File;

import org.junit.Test;

public class GDXMediaTypeDocumenterTest {

  @Test
  public void test() throws Exception {

    File rdfDir = new File("src/test/resources/gdx");
    File outDir = new File("target/gdx");
    
    MediaTypeDocumenter documenter = new MediaTypeDocumenter();
    documenter.setPublish(true);
    documenter.loadAll(rdfDir);
    documenter.produceAllDocumentation(outDir);
    
    
    
  }

}
