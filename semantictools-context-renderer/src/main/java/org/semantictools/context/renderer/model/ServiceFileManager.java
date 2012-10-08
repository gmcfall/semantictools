package org.semantictools.context.renderer.model;

import java.io.File;

import org.semantictools.frame.api.LinkManager;

public class ServiceFileManager {
  private File baseDir;
  private File cssFile;
  
  /**
   * Creates a new ServiceFileManager
   * @param baseDir The base directory under which all service documentation is written.  
   *                Typically, this is the "uml" directory.
   * @param cssPath The path to the css file for the service documentation, relative to the base directory.
   */
  public ServiceFileManager(File baseDir, File cssFile) {
    this.baseDir = baseDir;
    this.cssFile = cssFile;
  }

  private String stripProtocol(String uri) {
    int slash = uri.indexOf('/')+2;
    String path = uri.substring(slash);
    return path;
  }
  
  public String getRelativeCssPath(File htmlFile) {
    LinkManager linkManager = new LinkManager(htmlFile);
    return linkManager.relativize(cssFile);
  }
  
  /**
   * Returns the path to the Service documentation for the specified RDF type.
   * This path is relative to the baseDir passed to the constructor of this ServiceFileManager.
   * The path does not start with a leading slash.
   */
  public String getServiceDocumentationPath(String rdfTypeURI) {
    return stripProtocol(rdfTypeURI).replace('#', '/') + "/service.html";
  }
  
  public File getServiceDocumentationFile(String rdfTypeURI) {
    String path = getServiceDocumentationPath(rdfTypeURI);
    return new File(baseDir, path);
  }

}
