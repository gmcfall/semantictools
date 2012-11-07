package org.semantictools.uml.api;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.junit.Test;
import org.semantictools.publish.DocumentationGenerator;

public class DocumentationTest {

  @Test
  public void test() throws Exception {

    boolean publish = false;
    File rdfDir = new File("src/test/resources/all");
    File pubDir = new File("target/all");
    String endpointURL = "http://127.0.0.1:8888/admin/upload.do";
//    String endpointURL = "http://semantic-tools.appspot.com/admin/upload.do";
    String indexFile = "index.html";
    
    
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    String version = dateFormat.format(new Date());
    
    DocumentationGenerator generator = new DocumentationGenerator(rdfDir, pubDir, publish);
    generator.setIndexFileName(indexFile);
    generator.setVersion(version);
    generator.setUploadEndpoint(endpointURL);
    generator.run();
  }

}
