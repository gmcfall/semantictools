package org.semantictools.frame.api;

import java.io.File;

import org.junit.Test;

public class LtiMediaTypeDocumenterTest {

  @Test
  public void test() throws Exception {

    File rdfDir = new File("src/test/resources/lti");
    File outDir = new File("target/media");
    
    MediaTypeDocumenter documenter = new MediaTypeDocumenter();
    
    documenter.loadAll(rdfDir);
    documenter.produceAllDocumentation(outDir);
  }

}
