package org.semantictools.gwt.generator;

import static org.junit.Assert.*;

import java.io.File;

import org.junit.Test;

public class GwtTypeConfigTest {

  @Test
  public void test() throws Exception {

    File sourceDir = new File("src/test/resources/all");
    GwtTypeConfig config = new GwtTypeConfig();
    config.scan(sourceDir);
    
    assertTrue(config.includeType("http://purl.org/pearson/paf/v1/vocab/core#SequenceNode"));
    assertTrue(!config.includeType("http://schema.org/AlignmentObject"));
    assertTrue(config.useJavaScriptObject("http://purl.org/pearson/paf/v1/vocab/core#SequenceNode", "activity"));
    
    
  }

}
