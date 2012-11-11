/*******************************************************************************
 * Copyright 2012 Pearson Education
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
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
