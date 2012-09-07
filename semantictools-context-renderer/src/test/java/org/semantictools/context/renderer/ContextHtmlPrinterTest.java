package org.semantictools.context.renderer;

import static org.junit.Assert.*;

import java.io.File;

import org.junit.Test;
import org.semantictools.context.renderer.model.ContextProperties;
import org.semantictools.context.renderer.model.JsonContext;
import org.semantictools.frame.api.ContextBuilder;
import org.semantictools.frame.api.ContextManager;
import org.semantictools.frame.api.FrameBuilder;
import org.semantictools.frame.api.TypeManager;
import org.semantictools.frame.model.Frame;

public class ContextHtmlPrinterTest {

  @Test
  public void test() throws Exception {

    String classURI = "http://www.imsglobal.org/imspurl/lti/v2/vocab/lti#ToolProxy";
    File dir = new File("src/test/resources/rdf");
    TypeManager manager = new TypeManager();
    FrameBuilder builder = new FrameBuilder(manager);
    builder.loadDir(dir);
    

    File propertiesFile = new File("src/test/resources/rdf/context.properties");
    
    ContextManager contextManager = new ContextManager();
    contextManager.loadContextProperties(propertiesFile);
    
    ContextProperties properties = contextManager.getContextPropertiesByMediaType("application/vnd.ims.lti.v2.ToolProxy");
    
    ContextBuilder contextBuilder = new ContextBuilder(manager);
    
    JsonContext context = contextBuilder.createContext(properties);
    context.setRootType(classURI);
    
    ContextHtmlPrinter printer = new ContextHtmlPrinter(null, manager, new MediaTypeFileManager());
    printer.setIncludeOverviewDiagram(true);
    
    printer.printHtml(context);
  }

}
