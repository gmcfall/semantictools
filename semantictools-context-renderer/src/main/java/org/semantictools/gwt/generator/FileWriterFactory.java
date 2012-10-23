package org.semantictools.gwt.generator;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class FileWriterFactory implements WriterFactory {
  
  private File baseDir;
  

  public FileWriterFactory(File baseDir) {
    this.baseDir = baseDir;
  }


  @Override
  public PrintWriter getPrintWriter(String filePath) throws IOException {
    File file = new File(baseDir, filePath);
    file.getParentFile().mkdirs();
    FileWriter fileWriter = new FileWriter(file);
    return new PrintWriter(fileWriter);
  }

}
