package org.semantictools.context.renderer.impl;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;

import org.semantictools.context.renderer.StreamFactory;

public class SystemOutStreamFactory implements StreamFactory {

  @Override
  public OutputStream createOutputStream(String path) throws IOException {
    return new SystemOutPrintStream();
  }
  
  static class SystemOutPrintStream extends PrintStream {
    
    SystemOutPrintStream() {
      super(System.out);
    }
    
    public void close() {
      // Do nothing.  We don't want to close System.out
    }
  }

  @Override
  public InputStream createInputStream(String path) throws IOException {
    return null;
  }

  @Override
  public File getOutputFile(String path) {
    return null;
  }

}
