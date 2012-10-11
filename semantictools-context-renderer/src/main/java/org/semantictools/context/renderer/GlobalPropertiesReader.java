package org.semantictools.context.renderer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;

import org.semantictools.context.renderer.model.GlobalProperties;

public class GlobalPropertiesReader {
  private static final String IGNORE = "ignore";
  
  public GlobalProperties scan(File source) throws IOException {
    if ("global.properties".equals(source.getName())) {
      return parseProperties(source);
    }
    if (source.isDirectory()) {
      for (File child : source.listFiles()) {
        GlobalProperties result = scan(child);
        if (result != null) return result;
      }
    }
    return null;
  }

  private GlobalProperties parseProperties(File source) throws IOException {
    GlobalProperties global = new GlobalProperties();
    Properties properties = new Properties();
    FileReader reader = new FileReader(source);
    properties.load(reader);

    for (Map.Entry<Object, Object> e : properties.entrySet()) {
      
      String key = e.getKey().toString();
      String value = e.getValue().toString();
      if (IGNORE.equals(key)) {
        setIgnoredOntologies(global, value);
      }
    }
    return global;
  }

  private void setIgnoredOntologies(GlobalProperties global, String value) {
    
    StringTokenizer tokenizer = new StringTokenizer(value, " \t\r\n");
    while (tokenizer.hasMoreTokens()) {
      global.addIgnoredOntology(tokenizer.nextToken());
    }
    
  }

}
