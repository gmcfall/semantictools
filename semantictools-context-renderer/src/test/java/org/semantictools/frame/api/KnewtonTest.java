package org.semantictools.frame.api;

import java.io.File;

import org.junit.Test;

public class KnewtonTest {

  @Test
  public void test() throws Exception {

    File rdfDir = new File("src/test/resources/knewton/rdf");
    File outDir = new File("target/knewton/media");
    
    MediaTypeDocumenter documenter = new MediaTypeDocumenter();
    documenter.loadAll(rdfDir);
    documenter.produceAllDocumentation(outDir);
    
    
    
  }

}
