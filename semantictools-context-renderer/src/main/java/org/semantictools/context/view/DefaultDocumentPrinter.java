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
package org.semantictools.context.view;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.semantictools.context.renderer.model.BibliographicReference;
import org.semantictools.context.renderer.model.DocumentMetadata;
import org.semantictools.context.renderer.model.Person;
import org.semantictools.context.renderer.model.ReferenceManager;
import org.semantictools.frame.api.LinkManager;

public class DefaultDocumentPrinter extends PrintEngine implements DocumentPrinter {

  protected static final String TOC_MARKER = "<!-- TOC -->";
  
  protected DocumentMetadata metadata;
  private ClassificationPrinter classificationPrinter;

  private CaptionManager captionManager;
  private Set<Caption> forwardReferenceList;
  private StringWriter body;
  private Heading topHeading;
  private Heading currentHeading;
  private int h2;
  private int h3;
  private int figureNumber;
  private int tableNumber;
  
  public DefaultDocumentPrinter() {
    this(null);
  }
  
  public DefaultDocumentPrinter(PrintContext context) {
    super(context);
    clear();
  }
  
  
  @Override
  public void clear() {
    topHeading = new Heading(Level.H1, "", "");
    currentHeading = topHeading;
    body = new StringWriter();
    if (context == null) {
      context = new PrintContext();
    }
    context.setWriter(new PrintWriter(body));
    forwardReferenceList = new HashSet<Caption>();

    captionManager = new CaptionManager();


  }
  

  static class CaptionManager {
    Map<String, Caption> uri2FigureCaption = new HashMap<String, Caption>();
    
    public void add(Caption caption) {
      if (caption.getUri() == null) return;
      
      switch (caption.getType()) {
      case Figure :
        uri2FigureCaption.put(caption.getUri(), caption);
        break;
      }
    }
    
    public Caption getFigureCaptionByURI(String uri) {
      return uri2FigureCaption.get(uri);
    }
    
  }
  

  public ClassificationPrinter getClassificationPrinter() {
    return classificationPrinter;
  }

  public void setClassificationPrinter(ClassificationPrinter classificationPrinter) {
    this.classificationPrinter = classificationPrinter;
  }
  
  @Override 
  public PrintContext getPrintContext() {
    return context;
  }

  @Override
  public void setPrintContext(PrintContext context) {
    this.context = context;
  }

  @Override
  public void setMetadata(DocumentMetadata metadata) {
    this.metadata = metadata;
  }

  @Override
  public void printTitlePage() {

    printStatus();
    printLogo();

    printTitle();
    printSubtitle();
    printClassifiers();
    printDateIssued();
    printLatestVersion();
    printVersionHistory();
    printTitlePageEditors();
    printTitlePageAuthors();
    printLegalNotice();
   
    indent().println("<HR/>");

  }

  protected void printStatus() {

    String status = metadata.getStatus();
    if (status == null) return;
    
    print("<div ");
    printAttr("class", "status");
    print(">");
    print(status);
    println("</div>");
    
  }
  
  protected void printTitlePageAuthors() {
    printAuthors();
  }

  protected void printAuthors() {

    List<Person> authorList = metadata.getAuthors();
    if (authorList != null && !authorList.isEmpty()) {
      indent().print("<DIV");
      printAttr("class", "contributorLabel");
      println(">Authors</DIV>");
      for (Person author : authorList) {
        indent().print("<DIV");
        printAttr("class", "contributor");
        print(">").print(author.getPersonName());
        if (author.getOrgName() != null) {
          print(", ").print(author.getOrgName());
        }
        println("</DIV>");
      }
    }
    
  }

  protected void printTitlePageEditors() {
    printEditors();
  }

  protected void printEditors() {

    List<Person> editorList = metadata.getEditors();
    if (editorList != null && !editorList.isEmpty()) {
      indent().print("<DIV");
      printAttr("class", "contributorLabel");
      println(">Editors</DIV>");
      for (Person editor : editorList) {
        indent().print("<DIV");
        printAttr("class", "contributor");
        print(">").print(editor.getPersonName());
        if (editor.getOrgName()!=null) {
          print(", ").print(editor.getOrgName());
        }
        println("</DIV>");
      }
    }
    
  }

  protected void printVersionHistory() {

    Boolean historyLink = metadata.hasHistoryLink();
    if (historyLink!=null && historyLink) {
      indent().print("<DIV");
      printAttr("class", "contributorLabel");
      println(">See Also: <a href=\"index.html?history\">Version History</a></DIV>");
    }
    
  }

  /**
   * Print document classifiers; i.e. metadata that classifies this document
   * using some specialized vocabulary.
   */
  protected void printClassifiers() {
    if (classificationPrinter != null) {
      classificationPrinter.printClassifiers();
    }
  }

  protected void printLegalNotice() {
    String legalNotice = metadata.getLegalNotice();
    if (legalNotice != null) {
      print(legalNotice);
    }
    
  }

  protected void printLatestVersion() {
    String latestVersionURI = metadata.getLatestVersionURI();
    if (latestVersionURI == null) return;
    
    indent().print("<div class=\"titlePageData\">Latest version: ");
    print("<a");
    printAttr("href", latestVersionURI);
    print(">");
    print(latestVersionURI);
    println("</a></div>");
    
  }
  
  protected void printDateIssued() {
    // By default, the date issued appears in the
    // subtitle.  We print a separate date issued property only if
    // the subtitle is declared explicitly.
    
    if (metadata.getSubtitle() != null) {
      String date = metadata.getDate();
      if (date == null) return;
      
      indent().print("<div class=\"titlePageData\">Date Issued: ").print(date);
      println("</div>");
    }
    
  }

  protected void printLogo() {
    
    String logo = metadata.getLogo();
    if (logo == null) return;
    
    print("<p><img ");
    printAttr("src", logo);
    println("></p>");
    
  }
  

  protected void printSubtitle() {
    
    String subtitle = metadata.getSubtitle();
    if (subtitle == null) {

      String status = metadata.getStatus();
      
      String date = metadata.getDate();
      subtitle =  
          (status !=null && date !=null) ? status + " " + date :
          (status != null) ? status :
          (date != null) ? date :
          null; 
    }


    if (subtitle != null) {
      indent().print("<DIV");
      printAttr("class", "subtitle");
      print(">").print(subtitle).print("</DIV>");
    }
  }

  protected void printTitle() {
    String title = metadata.getTitle();
    indent().print("<H1>");
    print(title);
    println("</H1>");
    
  }

  @Override
  public void printFooter() {
    String footer = metadata.getFooter();
    if (footer == null) return;
    print(footer);

  }

  @Override
  public Heading createHeading(String text) {

    text = text.trim();
    if (currentHeading == null) {
      currentHeading = new Heading(Level.H1, text, text.replace(' ', '_') );
      return currentHeading;
    }
    
    Level level = currentHeading.getLevel().getNextLevel();
    
    Heading result = new Heading(level, text, text.replace(' ', '_'));
    currentHeading.add(result);
    
    return result;
  }

  @Override
  public void print(Heading heading) {

    boolean showNumber = heading.isShowNumber();
    Level level = heading.getLevel();
    String className = heading.getClassName();
    String number = null;

    switch (level) {
    case H2:
      h2++;
      number = h2 + ".";
      break;

    case H3:
      h3++;
      number = h2 + "." + h3;
      break;
    }

    
    heading.setHeadingNumber(number);

    indent().print("<" + level);
    printAttr("id", heading.getHeadingId());
    if (className != null) {
      printAttr("class", className);
    }
    print(">");
    if (showNumber) {
      print(number);
      print(" ");
    }
    print(heading.getHeadingText());
    println("</" + level + ">");

  }

  

  @Override
  public void beginSection(Heading heading) {
    print(heading);
    currentHeading = heading;

  }

  @Override
  public void endSection() {
    switch (currentHeading.getLevel()) {
    case H2:
      h3 = 0;
      break;
      
    case H1:
      h2 = 0;
      
    }
    currentHeading = currentHeading.getParent();
  }

  @Override
  public void printTableOfContentsMarker() {
    println(TOC_MARKER);
  }
  
  protected void printHeadings(List<Heading> toc) {
    if (toc == null)
      return;
    indent().print("<UL");
    printAttr("class", "toc");
    println(">");
    pushIndent();
    for (Heading heading : toc) {
      if (!heading.isInToc()) continue;
      String number = heading.getHeadingNumber();
      String text = heading.getHeadingText();
      String href = "#" + heading.getHeadingId();
      indent().print("<LI");
      printAttr("class", "tocline");
      print(">");
      if (heading.isShowNumber()) {
        print("<span");
        printAttr("class", "secno");
        print(">");
        print(number);
        print("</span>");
      }
      print(" <a");
      printAttr("href", href);
      print(">").print(text).print("</a>");
      println("</LI>");
      printHeadings(heading.getChildren());
    }
    popIndent();
    println("</UL>");

  }

  @Override
  public void printReferences() {

    ReferenceManager manager = metadata.getReferenceManager();
    if (manager == null) {
      return;
    }
    
    String text = popText();
    
    List<BibliographicReference> targetList = new ArrayList<BibliographicReference>();
    List<BibliographicReference> list = manager.listReferences();
    for (BibliographicReference ref : list) {
      String htmlLabel = ref.htmlLabel();
      String textLabel = ref.textLabel();
      if (text.contains(htmlLabel) || text.contains(textLabel)) {
        targetList.add(ref);
      }
    }
    
    
    if (targetList.isEmpty()) {
      print(text);
      return;
    }
    
    linkToBibliography(targetList, text);
    
    
    
    Heading heading = createHeading("References");
    print(heading);
    indent().print("<DL");
    printAttr("class", "references");
    println(">");
    File thisFile = metadata.getLocalFile();
    LinkManager linkManager = thisFile==null ? null : new LinkManager(thisFile);
    
    for (BibliographicReference r : targetList) {
      
      File otherFile = r.getLocalFile();
      if (otherFile != null && thisFile !=null) {
        String uri = linkManager.relativize(otherFile);
        r.setUri(uri);
      }
      String html = r.htmlText();
      
      indent().print("<DT>");
      printReferenceAnchor(r).println("</DT>");
      indent().print("<DD>").print(html).println("</DD>");
    }
    
    
  }
  


  private DefaultDocumentPrinter printReferenceAnchor(BibliographicReference r) {
    String id = r.getId();
    String label = r.htmlLabel();
    print("<a id=\"");
    print(id);
    print("\">");
    print(label);
    print("</a>");
    return this;
  }

  private void linkToBibliography(List<BibliographicReference> list,  String text) {
    for (BibliographicReference ref : list) {
      String id = ref.getId();
      String htmlLabel = ref.htmlLabel();
      String textLabel = ref.textLabel();
      
      String link = citationLink(htmlLabel, id);
      text = text.replace(htmlLabel, link);
      if (!textLabel.equals(htmlLabel)) {
        text = text.replace(textLabel, link);
      }
    }
    print(text);
    
  }
  
  private String citationLink(String label, String id) {
    StringBuilder builder = new StringBuilder();
    builder.append("<a href=\"#");
    builder.append(id);
    builder.append("\">");
    builder.append(label);
    builder.append("</a>");
    return builder.toString();
  }

  


  @Override
  public String getText() {
    body.flush();
    String text = body.toString();

    text = updateReferences(text);

    body.getBuffer().setLength(0);
    indent().println("<H2>Table of Contents</H2>");
    printHeadings(topHeading.getChildren());

    String toc = body.toString();
    return text.replace(TOC_MARKER, toc);

  }
  
  @Override
  public String popText() {
    String text = body.toString();
    body.getBuffer().setLength(0);
    return text;
  }

  @Override
  public void insertTableOfContents() {
    body.flush();
    String text = popText();

    text = updateReferences(text);

    indent().println("<H2>Table of Contents</H2>");
    printHeadings(topHeading.getChildren());

    String toc = popText();
    
    print( text.replace(TOC_MARKER, toc) );

  }


  private String updateReferences(String text) {
    for (Caption caption : forwardReferenceList) {
      String ref = createForwardRef(caption);
      text = text.replace(ref, createLink(caption));
    }
    return text;
  }
  

  private String createForwardRef(Caption caption) {
    return "<!-- REF:" + caption.getId() + " -->";
  }

  @Override
  public void printForwardRef(Caption caption) {
    print(createForwardRef(caption));
    forwardReferenceList.add(caption);
  }


  private String createLink(Caption caption) {
    return "<A href=\"#" + caption.getId() + "\">" + caption.getNumber()
        + "</A>";
  }

  @Override
  public void printLink(Caption caption) {
    print(createLink(caption));
  }
  
  @Override
  public Heading getCurrentHeading() {
    return currentHeading;
  }
  

  @Override
  public void printCaption(Caption caption) {

    captionManager.add(caption);
    
    indent().print("<DIV");
    printAttr("class", "caption");
    println(">");
    print(caption.getNumber());
    print(".&nbsp&nbsp;");
    print(caption.getText());
    indent().println("</DIV>");

  }

  @Override
  public void printFigure(String src, Caption caption) {
    indent().print("<DIV");
    printAttr("class", "figure");
    if (caption != null) {
      printAttr("id", caption.getId());
    }
    println(">");
    pushIndent();
    indent().print("<IMG");
    printAttr("src", src);
    println("/>");
    if (caption != null) {
      printCaption(caption);
    }
    popIndent();
    indent().println("</DIV>");

  }


  @Override
  public void assignNumber(Caption caption) {
    String number = null;
    switch (caption.getType()) {
    case Figure:
      figureNumber++;
      number = "Figure " + figureNumber;
      break;

    case Table:
      tableNumber++;
      number = "Table " + tableNumber;
    }

    caption.setNumber(number);

  }

  @Override
  public void beginTable(String className) {
    indent().print("<TABLE");
    printAttr("class", className);
    println(">");
    pushIndent();
  }
  
  @Override
  public void endTable() {
    popIndent();
    indent().println("</TABLE>");
  }
  
  @Override
  public void beginRow() {
    indent().println("<TR>");
    pushIndent();
    
  }
  
  @Override
  public void endRow() {
    popIndent();
    indent().println("</TR>");
  }
  

  @Override
  public void printTH(String value) {
    indent().print("<TH>").print(value).println("</TH>");
  }
  
  @Override
  public void printTD(String className, String text) {
    indent().print("<TD class=\"").print(className).print("\">").print(text).println("</TD>");
    
  }
  
  @Override
  public void printTD(String value) {
    indent().print("<TD>").print(value).println("</TD>");
  }
  
  @Override
  public void printAnchor(String href, String text) {
    print("<A href=\"").print(href).print("\">").print(text).print("</A>");
  }
  
  @Override
  public void beginDiv(String className) {
    indent().print("<DIV class=\"").print(className).println("\">");
    pushIndent();
  }
  
  @Override
  public void beginDiv(String className, String id) {

    indent().print("<DIV class=\"").print(className).print("\" id=\"").print(id).println("\">");
    pushIndent();
  }
  
  @Override
  public void endDiv() {
    popIndent();
    indent().println("</DIV>");
  }
  


  @Override
  public void beginCodeSnippet() {
    indent().print("<DIV");
    printAttr("class", "code-snippet");
    println(">");
    println("<PRE>");
  }
  
  @Override
  public void endCodeSnippet() {
    println("</PRE>");
    indent().println("</DIV>");
    
  }


  @Override
  public void printDefinition(String termName, String description) {
    indent().print("<DT>").print(termName).println("</DT>");
    pushIndent();
    indent().print("<DD>").print(description).println("</DD>");
    popIndent();
    
  }

  @Override
  public void printParagraph(String text) {
    indent().print("<P>").print(text).println("</P>");
  }
  

  @Override
  public void beginParagraph() {
    indent().print("<P>");
  }
  
  @Override
  public void endParagraph() {
    println("</P>");
  }

  
  @Override
  public void printListItem(String text) {
    indent().print("<LI>").print(text).println("</LI>");
    
  }

  @Override
  public void beginHTML() {
    println("<HTML>");
    println("<HEAD>");
    pushIndent();
    printStyleSheetLink();
    popIndent();
    println("</HEAD>");
    println("<BODY>");
  }
  

  @Override
  public void endHTML() {
    println("</BODY>");
    println("</HTML>");
  }
  

  protected void printStyleSheetLink() {

    String href = metadata.getCss();

    indent().print("<LINK");
    printAttr("REL", "StyleSheet");
    printAttr("HREF", href);
    printAttr("TYPE", "text/css");
    println(">");

  }
  

  @Override
  public Heading createHeading(String text, String id) {

    text = text.trim();
    Level level = currentHeading.getLevel().getNextLevel();
    
    Heading result = new Heading(level, text, id);
    currentHeading.add(result);
    
    return result;
  }

}
