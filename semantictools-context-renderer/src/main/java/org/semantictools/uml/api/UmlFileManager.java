package org.semantictools.uml.api;

import java.io.File;

import org.semantictools.frame.api.LinkManager;
import org.semantictools.frame.model.RdfType;
import org.semantictools.uml.model.UmlClass;

import com.hp.hpl.jena.rdf.model.Property;

public class UmlFileManager extends LinkManager {
  
  
  private File rootDir;
  private String ontologyURI;
  private File ontologyDir;
  private File imagesDir;
  
  public UmlFileManager(File rootDir) {
    this.rootDir = rootDir;
  }
  
  /**
   * Set the current ontology.  This establishes the base directory, and
   * subsequent operations are relative to this base.
   */
  public void setOntology(String ontologyURI) {
    this.ontologyURI = ontologyURI;
    String path = getOntologyPath(ontologyURI);
    ontologyDir = new File(rootDir, path);
    imagesDir = new File(ontologyDir, "images");
    imagesDir.mkdirs();
    
    String baseURI = ontologyDir.toString().replace('\\', '/') + "/";
    setBaseURI(baseURI);
  }
  
  public File getOntologyAllFile() {
    return createOntologyAllFile(ontologyDir);
  }
  
  
  private File createOntologyAllFile(File ontDir) {
    return new File(ontDir, "index.html");
  }
  
  public File getUmlClassImageFile(UmlClass umlClass) {
    return new File(imagesDir, umlClass.getLocalName() + ".png");
  }
  
  public File getRootDir() {
    return rootDir;
  }
  
  public File getOntologyDir() {
    return ontologyDir;
  }
  
  public File getImagesDir() {
    return imagesDir;
  }

  private String getOntologyPath(String uri) {
    int slash = uri.indexOf('/')+2;
    String path = uri.substring(slash, uri.length()-1);
    return path;
  }


  public String getTypeId(RdfType umlClass) {
    return umlClass.getLocalName();
  }
  
  public String getPropertyId(UmlClass declaringClass, Property rdfProperty) {
    if (rdfProperty == null) return null;
    
    return declaringClass.getLocalName() + '.' + rdfProperty.getLocalName();
  }
  
  public String getPropertyHref(UmlClass declaringClass, Property rdfProperty) {
    String classURI = declaringClass.getURI();
    String prefix = classURI.startsWith(ontologyURI) ? "" : getTypeHref(declaringClass.getType());
    return prefix + "#" + getPropertyId(declaringClass, rdfProperty);
  }
  
  public String getTypeHref(RdfType type) {
    String uri = type.getUri();
    if (uri.startsWith(ontologyURI)) {
      return "#" + getTypeId(type);
    }
    String ontURI = type.getNamespace();
    String filePath = createOntologyAllFile(ontURI).toString().replace('\\', '/');
    String anchorPath =  filePath + "#" + getTypeId(type);
    return relativize(anchorPath);
    
  }
  
  /**
   * Returns the URI for the documentation of the specified RDF type, relative
   * to the given source file.
   */
  public String getTypeRelativePath(File sourceFile, RdfType type) {
    File ontologyFile = createOntologyAllFile(type.getNamespace());
   
    LinkManager linkManager = new LinkManager(sourceFile);
    String uri = linkManager.relativize(ontologyFile) + "#" + getTypeId(type);
    return uri;
  }
  
  public String getTypeLink(RdfType type) {
    return "<A href=\"" + getTypeHref(type) + "\">" + type.getLocalName() + "</A>";
  }
  

  private File createOntologyAllFile(String ontologyURI) {
    String path = getOntologyPath(ontologyURI);
    File dir = new File(rootDir, path);
    return createOntologyAllFile(dir);
  }

  
}
