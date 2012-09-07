package org.semantictools.publish;

import java.io.File;

import org.junit.Test;

public class DocumentationGeneratorTest {

  private File rdfDir= new File("src/test/resources/gdx");
  private File pubDir = new File("target/gdx");
  private boolean publish = false;
  
  @Test
  public void testRun() throws Exception {
    DocumentationGenerator generator = new DocumentationGenerator(rdfDir, pubDir, publish);
    generator.run();
  }

}
