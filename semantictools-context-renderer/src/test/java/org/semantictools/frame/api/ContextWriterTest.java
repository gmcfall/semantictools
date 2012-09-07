package org.semantictools.frame.api;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.PrintWriter;

import org.junit.Test;
import org.semantictools.context.renderer.model.ContextProperties;
import org.semantictools.context.renderer.model.JsonContext;
import org.semantictools.frame.model.Frame;

public class ContextWriterTest {

  @Test
  public void test() throws Exception {
    

    File dir = new File("src/test/resources/rdf");
    TypeManager manager = new TypeManager();
    FrameBuilder builder = new FrameBuilder(manager);
    builder.loadDir(dir);
    
    Frame frame = manager.getFrameByUri("http://www.imsglobal.org/imspurl/lti/v2/vocab/lti#ToolProxy");
    assertTrue(frame != null);
    

    File propertiesFile = new File("src/test/resources/rdf/context.properties");
    
    ContextManager contextManager = new ContextManager();
    contextManager.loadContextProperties(propertiesFile);
    
    ContextProperties properties = contextManager.getContextPropertiesByMediaType("application/vnd.ims.lti.v2.ToolProxy");
    
    ContextBuilder contextBuilder = new ContextBuilder(manager);
    
    JsonContext context = contextBuilder.createContext(properties);
    
    ContextWriter writer = new ContextWriter();
    
    PrintWriter out = new PrintWriter(System.out);
    writer.writeContext(out, context);
    
    out.flush();
    
    
  }

}
