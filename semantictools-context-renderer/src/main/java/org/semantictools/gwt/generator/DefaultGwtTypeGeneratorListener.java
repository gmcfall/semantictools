package org.semantictools.gwt.generator;

import java.util.ArrayList;
import java.util.List;

public class DefaultGwtTypeGeneratorListener implements
    GwtTypeGeneratorListener {
  
  private List<String> includedList = new ArrayList<String>();
  private List<String> ignoreList = new ArrayList<String>();

  @Override
  public void beginType(String typeURI) {
    includedList.add(typeURI);

  }

  /**
   * Returns the list of types that were handled by the GwtTypeGenerator.
   */
  public List<String> getIncludedList() {
    return includedList;
  }
  
  
  /**
   * Returns the list of types that the generator has ignored based on the 
   * configuration.
   */
  public List<String> getIgnoreList() {
    return ignoreList;
  }

  @Override
  public void ignoreType(String typeURI) {
    ignoreList.add(typeURI);
  }
  
  

}
