package org.semantictools.context.view;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.semantictools.context.renderer.IndexEntry;

public class MediaTypeIndexPrinter extends PrintEngine {

  private File outDir;
  
  public MediaTypeIndexPrinter() {
  }

  public MediaTypeIndexPrinter(PrintContext context) {
    super(context);
  }
  
  public void printIndex(File outfile) throws IOException {
    outDir = outfile.getParentFile();
    FileWriter writer = new FileWriter(outfile);
    try {
    
      super.context = new PrintContext();
      super.context.setFileWriter(writer);
      
      doPrintIndex();
      
    } finally {   
      close();
    }
    
    
  }

  private void doPrintIndex() {    
    println("<HTML>");
    println("<HEAD>");
    printStyle();
    println("</HEAD>");
    println("<BODY>");
    pushIndent();
    printPageHeading();
    printMediaTypeList();
    popIndent();
    println("</BODY>");
    println("</HTML>");
  }
  
  private void printStyle() {
    println("<STYLE>");
    pushIndent();
    indent().println("BODY {");
    indent().println("  margin-left: 3%;");
    indent().println("}");
    indent().println("H1 {");
    pushIndent();
    indent().println("font-family: sans-serif;");
    indent().println("color: #336699;");
    indent().println("font-weight: normal;");
    
    popIndent();
    indent().println("}");
    
    popIndent();
    println("</STYLE>");
    
  }

  private void printMediaTypeList() {

    List<IndexEntry> list = getMediaTypeList();
    indent().print("<UL>");
    pushIndent();
    for (IndexEntry entry : list) {
      
      String name = entry.getName();
      String href = entry.getHref();
      indent().print("<LI>");
      print("<A");
      printAttr("href", href);
      print(">");
      print(name);
      println("</A></LI>");
    }
    popIndent();
    indent().println("</UL>");
    
  }

  private void printPageHeading() {
    indent().println("<H1>Specifications</H1>");
    
  }

  private List<IndexEntry> getMediaTypeList() {
    File rootDir = new File(outDir, "application");

    List<IndexEntry> list = new ArrayList<IndexEntry>();
    addMediaTypes(list, null, rootDir);
    return list;
  }

  private void addMediaTypes(List<IndexEntry> list, String path, File dir) {
    
    File indexFile = (path==null) ? null : new File(dir, "index.html");
    if (indexFile != null && indexFile.exists()) {
      
      String mediaType = path + "." + dir.getName();
      String parentPath = mediaType.replace(".", "/");
      String href =  parentPath + "/index.html";
      IndexEntry entry = new IndexEntry(mediaType + " Media Type", href);
      list.add(entry);
      
      File parentDir = indexFile.getParentFile();
      File serviceFile = new File(parentDir, "service.html");
      
      if (serviceFile.exists()) {
        entry = new IndexEntry(mediaType + " REST API", parentPath + "/service.html");
        list.add(entry);
      }
      
      
    } else {
      
      File[] kids = dir.listFiles();
      if (kids == null) return;
      
      if (path == null) {
        path = dir.getName() + "/";
        
      } else if (path.endsWith("/")){
        path = path + dir.getName();
         
      } else {
        path = path + "." + dir.getName();
      }
      
      for (int i=0; i<kids.length; i++) {
        if (kids[i].isDirectory()) {
          addMediaTypes(list, path, kids[i]);
        }
      }
    }
    
  }


}
