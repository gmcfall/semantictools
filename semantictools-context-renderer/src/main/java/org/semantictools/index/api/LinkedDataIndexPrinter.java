package org.semantictools.index.api;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import org.semantictools.context.renderer.HtmlPrinter;
import org.semantictools.frame.api.LinkManager;
import org.semantictools.index.model.MediaTypeReference;
import org.semantictools.index.model.SchemaReference;
import org.semantictools.uml.api.UmlFileManager;

public class LinkedDataIndexPrinter extends HtmlPrinter {
  
  private LinkedDataIndex index;
  private LinkManager linkManager;
  private File pubDir;
  
  public LinkedDataIndexPrinter(File pubDir, LinkedDataIndex index) {
    super();
    this.pubDir = pubDir;
    this.index = index;
    linkManager = new LinkManager();
    linkManager.setBaseURI(pubDir.toString().replace("\\", "/") + "/");
    pubDir.mkdirs();
  }
  
  public void printIndex() throws IOException {
    init();
    
    beginHTML();
    pushIndent();
    printOntologies();
    printDatatypes();
    printMediaTypes();
    
    popIndent();    
    endHTML();
    
    writeFile();
    
  }




  private void printDatatypes() {

    List<SchemaReference> list = index.listDatatypes();
    
    indent();
    println("<H2>Data Types</H2>");
    indent();
    println("<UL>");
    pushIndent();
    for (SchemaReference r : list) {
      String label = r.getSchemaLabel();
      String href = linkManager.relativize(r.getSchemaDocPath());
      indent();
      print("<LI>");
      printAnchor(href, label);
      println();
    }
    popIndent();
    indent();
    println("</UL>");
    
  }

  private void printOntologies() {
    
    List<SchemaReference> list = index.listOntologies();
    
    indent();
    println("<H2>Ontologies</H2>");
    indent();
    println("<UL>");
    pushIndent();
    for (SchemaReference r : list) {
      String label = r.getSchemaLabel();
      String href = linkManager.relativize(r.getSchemaDocPath());
      indent();
      print("<LI>");
      printAnchor(href, label);
      println();
    }
    popIndent();
    indent();
    println("</UL>");
    
  }

  private void writeFile() throws IOException {
    
    File outFile = new File(pubDir, "index.html");
    FileWriter writer = new FileWriter(outFile);
    try {
      String text = popText();
      writer.write(text);
      writer.flush();
    } finally {
      writer.close();
    }
    
  }

  private void printMediaTypes() {
    
    List<MediaTypeReference> list = index.listAllMediaTypes();
    if (list.isEmpty()) return;
    
    indent();
    println("<H2>Data Bindings</H2>");
    indent();
    println("<UL>");
    pushIndent();
    for (MediaTypeReference r : list) {
      String mediaType = r.getMediaTypeName();
      String mediaTypeURI = linkManager.relativize(r.getMediaTypeURI());
      String serviceURI = linkManager.relativize(r.getServiceAPI());
      
      indent();
      print("<LI><code>");
      print(mediaType);
      print(" (");
      printAnchor(mediaTypeURI, "Media Type");
      if (serviceURI != null) {
        print(", ");
        printAnchor(serviceURI, "REST API");
      }
      println(")");
      
    }
    popIndent();
    println("</UL>");
    
    
  }

  @Override
  protected String getPathToStyleSheet() {
    return "uml/uml.css";
  }

}
