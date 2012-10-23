package org.semantictools.gwt.generator;

public interface GwtTypeGeneratorListener {

  /**
   * Receive a notification when the generator starts working on
   * the specified type.
   */
  void beginType(String typeURI);
  
  
  /**
   * Receive a notification that the generator is ignoring the specified
   * type based on rules in the configuration file.
   */
  void ignoreType(String typeURI);
  
}
