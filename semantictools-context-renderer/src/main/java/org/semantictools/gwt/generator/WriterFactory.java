package org.semantictools.gwt.generator;

import java.io.IOException;
import java.io.PrintWriter;

public interface WriterFactory {

  PrintWriter getPrintWriter(String filePath) throws IOException;
}
