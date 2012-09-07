package org.semantictools.frame.api;

import java.io.File;

import org.junit.Test;

public class PersonMediaTypeDocumenterTest {

  @Test
  public void test() throws Exception {

    File rdfDir = new File("src/test/resources/person");
    File outDir = new File("target/person");
    
    MediaTypeDocumenter documenter = new MediaTypeDocumenter();
    documenter.setPublish(true);
    
    documenter.loadAll(rdfDir);
    documenter.produceAllDocumentation(outDir);
  }

}
