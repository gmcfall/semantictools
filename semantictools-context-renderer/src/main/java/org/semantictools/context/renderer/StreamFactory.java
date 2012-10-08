package org.semantictools.context.renderer;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public interface StreamFactory {

  /**
   * Returns the output file for the specified path or null if the path does not
   * point to a local file.
   */
  File getOutputFile(String path);
  
  OutputStream createOutputStream(String path) throws IOException;
  InputStream createInputStream(String path) throws IOException;
  
}
