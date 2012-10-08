package org.semantictools.context.renderer.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.semantictools.context.renderer.StreamFactory;

public class FileStreamFactory implements StreamFactory {
  
  private File inputDir;
  private File outputDir;
  

  public FileStreamFactory(File baseDir) {
    inputDir = outputDir = baseDir;
  }
  
  public FileStreamFactory(File inputDir, File outputDir) {
    this.inputDir = inputDir;
    this.outputDir = outputDir;
  }


  @Override
  public OutputStream createOutputStream(String path) throws IOException {
    File file = getOutputFile(path);
    file.getParentFile().mkdirs();
    return new FileOutputStream(file);
  }


  @Override
  public InputStream createInputStream(String path) throws IOException {
    File file = new File(inputDir, path);
    if (!file.exists()) return null;
    
    return new FileInputStream(file);
  }

  @Override
  public File getOutputFile(String path) {
    return new File(outputDir, path);
  }

}
