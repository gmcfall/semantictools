package org.semantictools.frame.api;

import static org.junit.Assert.*;

import java.io.File;
import java.util.List;

import org.junit.Test;
import org.semantictools.frame.model.Field;
import org.semantictools.frame.model.Frame;

public class FrameBuilderTest {
  
  

  @Test
  public void testLoadDir() throws Exception {

    File dir = new File("src/test/resources/rdf");
    TypeManager manager = new TypeManager();
    FrameBuilder builder = new FrameBuilder(manager);
    builder.loadDir(dir);
    
    Frame frame = manager.getFrameByUri("http://www.imsglobal.org/imspurl/lti/v2/vocab/lti#ToolProxy");
    assertTrue(frame != null);
    
//    System.out.println(frame.toMultilineString());
    
    
  }
  
  @Test
  public void testProductInfo() throws Exception {

    File dir = new File("src/test/resources/rdf");
    TypeManager manager = new TypeManager();
    FrameBuilder builder = new FrameBuilder(manager);
    builder.loadDir(dir);
    
    Frame frame = manager.getFrameByUri("http://www.imsglobal.org/imspurl/lti/v2/vocab/lti#ProductInfo");
    System.out.println(frame.toMultilineString());
    assertTrue(frame != null);
    
    List<Field> fieldList = frame.listAllFields();
    
    assertContainsField(fieldList, "http://www.imsglobal.org/imspurl/lti/v2/vocab/lti#owner_family");
  }

  private void assertContainsField(List<Field> fieldList, String uri) {
    for (Field field : fieldList) {
      if (uri.equals(field.getURI())) return;
    }
    fail("Property not found: " + uri);
    
  }

}
