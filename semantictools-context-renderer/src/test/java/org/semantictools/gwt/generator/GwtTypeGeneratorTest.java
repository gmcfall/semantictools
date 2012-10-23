package org.semantictools.gwt.generator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.semantictools.frame.api.TypeManager;

public class GwtTypeGeneratorTest {

  @Test
  public void testDefaultJavaName() {
    
    String uri = "http://www.imsglobal.org/imspurl/lti/v2/vocab/lti#";
    GwtTypeGenerator generator = new GwtTypeGenerator(null, null, null);
    
    String javaName = generator.defaultJavaName(uri);
    
    assertEquals("org.imsglobal.www.imspurl.lti.v2.vocab.lti", javaName);
    
    
  }
  
  @Test 
  public void testGwtClass() throws Exception {
    File sourceDir = new File("src/test/resources/dir");
    
    TypeManager typeManager = new TypeManager();
    typeManager.loadDir(sourceDir);
    
    MockWriterFactory factory = new MockWriterFactory();
    GwtTypeConfig config = new GwtTypeConfig();
    GwtTypeGenerator generator = new GwtTypeGenerator(config, typeManager, factory);
    generator.generateAll();
    
    String text = factory.getFileContents("com/example/vocab/v1/directory/client/Person.java");
    text = text.replace("\r", "");
    
    
    assertText(text, 
      "  public static Person create() {",
      "    return JavaScriptObject.createObject().cast();",
      "  }");
    
    assertText(text, 
        "  public final native JsArray<Phone> getPhone() /*-{",
        "    if (typeof this.phone == \"object\") {",
        "      return [this.phone];",
        "    } else if (typeof this.phone == \"string\") {",
        "      return [{ \"@id\" : this.phone}];",
        "    }",
        "    return this.phone;",
        "  }-*/;");
    
    text = factory.getFileContents("com/example/vocab/v1/directory/client/PhoneType.java");

    System.out.println(text);
  }
  
  private void assertText(String actual, String...line) {
    StringBuilder builder = new StringBuilder();
    for (String value : line) {
      builder.append(value);
      builder.append("\n");
    }
    String expected = builder.toString();
    assertTrue(actual.contains(expected));
  }
  
  class MockWriterFactory implements WriterFactory {
    private Map<String, String> repository = new HashMap<String, String>();
    
    public String getFileContents(String path) {
      return repository.get(path);
    }
    
    @Override
    public PrintWriter getPrintWriter(String filePath) throws IOException {
      return new PrintWriter(new MockFileWriter(filePath));
    }
    

    class MockFileWriter extends StringWriter {
      private String filePath;
      
      public MockFileWriter(String fileName) {
        this.filePath = fileName;
      }

      @Override
      public void close() throws IOException {
        super.close();
        repository.put(filePath, this.toString());
      }
    }
    
  }
  

}
