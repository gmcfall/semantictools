package org.semantictools.frame.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.junit.Test;
import org.semantictools.frame.model.Datatype;

public class DatatypeReaderTest {

  @Test
  public void test() throws Exception {
    

    File dir = new File("src/test/resources/rdf");
    TypeManager manager = new TypeManager();
    FrameBuilder builder = new FrameBuilder(manager);
    builder.loadDir(dir);
    
    Datatype type = manager.getDatatypeByUri("http://www.imsglobal.org/imspurl/lti/v2/xsd/ltid#GUID.Type");
    
    assertTrue(type != null);
    assertEquals("GUID.Type", type.getLocalName());
    assertEquals("http://www.imsglobal.org/imspurl/lti/v2/xsd/ltid#GUID.Type", type.getUri());
    assertTrue(type.getBase() != null);
    assertEquals("http://www.w3.org/2001/XMLSchema#NCName", type.getBase().getUri());
    
    
    
  }

}
