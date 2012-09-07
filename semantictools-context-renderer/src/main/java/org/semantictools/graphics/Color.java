package org.semantictools.graphics;

public interface Color {

  /**
   * Converts this Color instance to an instance of the specified type.
   * @param type  The type to which this Color instance should be converted. 
   * 
   */
  <T> T as(Class<T> type);
}
