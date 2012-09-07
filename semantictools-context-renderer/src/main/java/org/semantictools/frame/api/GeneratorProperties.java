package org.semantictools.frame.api;

import java.util.Properties;

/**
 * Stores global properties that govern the generation of documentation.
 * @author Greg McFall
 *
 */
public class GeneratorProperties {
  
  private Properties properties;
  
  private String baseURL;
  
  public GeneratorProperties(Properties config) {
    properties = config;
    baseURL = properties.getProperty("baseURL");
    
  }

  public String getBaseURL() {
    return baseURL;
  }
  
  public String getProperty(String name) {
    return properties.getProperty(name);
  }

}
