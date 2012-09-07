package org.semantictools.context.renderer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public interface StreamFactory {

  OutputStream createOutputStream(String path) throws IOException;
  InputStream createInputStream(String path) throws IOException;
  
}
