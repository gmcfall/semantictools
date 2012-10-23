package org.semantictools.gwt.generator;

import java.io.File;
import java.io.PrintStream;
import java.util.Collections;
import java.util.List;

import org.semantictools.frame.api.TypeManager;

public class GwtTypeGeneratorMain {
  
  public static void main(String[] args) throws Exception {

    File sourceDir = new File("src/test/resources/all");
    File outDir = new File("target/generated-sources/java");
    
    
    TypeManager typeManager = new TypeManager();
    typeManager.loadDir(sourceDir);
    
    FileWriterFactory factory = new FileWriterFactory(outDir);
    GwtTypeConfig config = new GwtTypeConfig();
    config.scan(sourceDir);
    GwtTypeGenerator generator = new GwtTypeGenerator(config, typeManager, factory);
    
    DefaultGwtTypeGeneratorListener listener = new DefaultGwtTypeGeneratorListener();
    generator.setListener(listener);
    
    generator.generateAll();
    
    printIgnoredTypes(listener.getIgnoreList());
  }

  private static void printIgnoredTypes(List<String> ignoreList) {
    Collections.sort(ignoreList);
    PrintStream out = System.out;
    out.println("<exclude>");
    for (int i=0; i<ignoreList.size(); i++) {
      String uri = ignoreList.get(i);
      out.print("  <");
      out.print(uri);
      out.println("/>");
    }
    out.println("</exclude>");
    
  }

}
