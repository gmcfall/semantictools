package org.semantictools.frame.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.File;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.semantictools.frame.api.TypeManager;

public class FieldTest {
  
  private TypeManager typeManager;
  
  @Before
  public void setUp() throws Exception {
    typeManager = new TypeManager();

    File rdfDir = new File("src/test/resources/liso");
    typeManager.loadDir(rdfDir);
    
  }

  @Test
  public void test() {
    Frame frame = typeManager.getFrameByUri("http://www.imsglobal.org/imspurl/lis/v2/vocab/outcomes#Result");
    Field field = findField(frame, "resultScore");
    if (field == null) {
      fail("Field not found");
    }
    Encapsulation encaps = field.getEncapsulation();
    assertEquals(Encapsulation.COMPOSITION, encaps);
  }
  
  private Field findField(Frame frame, String localName) {
    List<Field> list = frame.getDeclaredFields();
    for (Field field : list) {
      if (localName.equals(field.getLocalName())) return field;
        
    }
    
    return null;
  }

}
