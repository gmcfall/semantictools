package org.semantictools.context.view;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.semantictools.context.renderer.URLRewriter;
import org.semantictools.context.renderer.model.ReferenceManager;

abstract public class HtmlPrinter extends PrintEngine {

  private static final String TOC_MARKER = "<!-- TOC -->";
  private static final String VOWEL = "aeiou";

  private static enum Level {
    H1(null), 
    H2(H1), 
    H3(H2);
    
    Level nextLevel;
    
    private Level(Level prev) {
      if (prev != null) {
        prev.nextLevel = this;
      }
    }

    public Level getNextLevel() {
      return nextLevel;
    }
    
    
  }

  private StringWriter body;
  private Heading topHeading;
  private Heading currentHeading;
  private ReferenceManager referenceManager;
  private int h2;
  private int h3;
  private int figureNumber;
  private int tableNumber;
  private URLRewriter urlRewriter;

  private Set<Caption> forwardReferenceList;
  private CaptionManager captionManager;

  protected HtmlPrinter(URLRewriter rewriter) {
    super(new PrintContext());
    this.urlRewriter = rewriter;
  }
  
  protected HtmlPrinter() {
    this(new URLRewriter() {

      @Override
      public String rewrite(String url) {
        return url;
      }});
  }
  

  protected void init() {
    init(new ReferenceManager() {
      
      @Override
      public String getReference(String key) {
        // TODO Auto-generated method stub
        return null;
      }
    });
  }

  protected void init(ReferenceManager referenceManager) {
    this.referenceManager = referenceManager;
    h2 = h3 = 0;
    topHeading = new Heading(Level.H1, "", "");
    currentHeading = topHeading;
    body = new StringWriter();
    forwardReferenceList = new HashSet<HtmlPrinter.Caption>();
    captionManager = new CaptionManager();

    getPrintContext().setWriter(new PrintWriter(body));


  }
  
  protected void beginTable(String className) {
    indent().print("<TABLE");
    printAttr("class", className);
    println(">");
    pushIndent();
  }
  
  protected void endTable() {
    popIndent();
    indent().println("</TABLE>");
  }
  
  protected void beginRow() {
    indent().println("<TR>");
    pushIndent();
    
  }
  protected void endRow() {
    popIndent();
    indent().println("</TR>");
  }
  
  protected void printTH(String value) {
    indent().print("<TH>").print(value).println("</TH>");
  }
  
  protected void printTD(String className, String text) {
    indent().print("<TD class=\"").print(className).print("\">").print(text).println("</TD>");
    
  }
  
  protected void printTD(String value) {
    indent().print("<TD>").print(value).println("</TD>");
  }
  
  protected void printAnchor(String href, String text) {
    print("<A href=\"").print(href).print("\">").print(text).print("</A>");
  }
  
  protected void beginDiv(String className) {
    indent().print("<DIV class=\"").print(className).println("\">");
    pushIndent();
  }
  
  protected void beginDiv(String className, String id) {

    indent().print("<DIV class=\"").print(className).print("\" id=\"").print(id).println("\">");
    pushIndent();
  }
  
  
  protected void endDiv() {
    popIndent();
    indent().println("</DIV>");
  }
  protected void beginParagraph() {
    indent().print("<P>");
  }
  
  protected void endParagraph() {
    println("</P>");
  }

  protected String createDefaultReference(String key) {
    return null;
  }

  protected void printReferences() {
    
    String text = getBodyText();
    
    List<String> list = listReferences(text);
    
    Heading heading = createHeading("References");
    print(heading);
    indent().print("<DL");
    printAttr("class", "references");
    println(">");
    for (String tag : list) {
      String id = normalizeId(tag);
      String key = tag.replace(" ", "&nbsp;");
      String value = referenceManager.getReference(key);
      if (value == null) {
        value = createDefaultReference(key);
        if (value == null) {
          value = "<em>Undefined</em>";
        }
      } 
      
      if (value != null) {
        value = addLinksToReference(value);
      }
      String replacement = "<A href=\"#" + id + "\">" + tag + "</A>";
      text = text.replace(tag, replacement);
      printDefinition(key, id, value);
      
    }
    indent().print("</DL>");
    
    
    String referencesText = getBodyText();
    print(text);
    print(referencesText);
    
  }

  private void printDefinition(String termName, String id, String description) {
    indent().print("<DT");
    printAttr("id", id);
    print(">");
    
    print(termName).println("</DT>");
    pushIndent();
    indent().print("<DD>").print(description).println("</DD>");
    popIndent();
    
  }
  
  private String addLinksToReference(String value) {
    
    Pattern pattern = Pattern.compile("URL:\\s*(.*)");
    Matcher matcher = pattern.matcher(value);
    String url = matcher.find() ? matcher.group(1) : null;
    
    if (url == null) {
      return value.replace("|", ". ");
    }
    
    String[] array = value.split("\\|");
    
    if (array.length<3) return value;
      
    String authors = array[0].trim();
    String title = array[1].trim();
    
    String dot = "";
    StringBuilder builder = new StringBuilder();
    if (authors.length()>0) {
      builder.append(authors);
      dot = ". ";
    }
    String href = urlRewriter==null ? url : urlRewriter.rewrite(url);
    if (title.length()>0) {
      builder.append(dot);
      builder.append("<A href=\"");
      builder.append(href);
      builder.append("\">");
      builder.append(title);
      builder.append("</A>");
      dot = ". ";
      
    }
    for (int i=2; i<array.length-1; i++) {
      String text = array[i].trim();
      if (text.length()==0) continue;
      builder.append(dot);
      builder.append(text);
      dot = ". ";
    }
    builder.append(dot);
    builder.append("URL: <A href=\"");
    builder.append(url);
    builder.append("\">");
    builder.append(url);
    builder.append("</A>");
    
    return builder.toString();
  }

  private String getBodyText() {
    getPrintContext().getWriter().flush();
    String result = body.toString();
    body.getBuffer().setLength(0);
    return result;
  }

  private String normalizeId(String tag) {
    StringBuilder builder = new StringBuilder();
    for (int i=0; i<tag.length(); i++) {
      char c = tag.charAt(i);
      if (Character.isJavaIdentifierPart(c)) {
        builder.append(c);
      } else {
        builder.append('_');
      }
    }
    return builder.toString();
  }
  

  private List<String> listReferences(String text) {
    List<String> list = new ArrayList<String>();
    
    // It is possible that the documentation might contain
    // a numeric range, like [0, 1].  We don't want to interpret
    // numeric ranges as citations.  So we define a regex pattern
    // that we can use to filter out numeric ranges.
    //
    String numericRange = "\\[\\d+,\\s*\\d\\]";
    
    Pattern pattern = Pattern.compile("\\[[^\\]]*\\]");
    Matcher matcher = pattern.matcher(text);
    while (matcher.find()) {
      String value = matcher.group();
      if (
        !list.contains(value) && 
        value.indexOf('\n')<0 &&
        !value.matches(numericRange) &&
        !value.contains(" ... ")
      ) {
        list.add(matcher.group());
      }
    }
    Collections.sort(list);
    return list;
  }
  
  protected void printListItem(String text) {
    indent().print("<LI>").print(text).println("</LI>");
    
  }


  protected void printTocMarker() {

    println(TOC_MARKER);

  }

  protected void insertTableOfContents() {
    body.flush();
    String text = popText();

    text = updateReferences(text);

    indent().println("<H2>Table of Contents</H2>");
    printHeadings(topHeading.getChildren());

    String toc = popText();
    
    print( text.replace(TOC_MARKER, toc) );

  }
  
  protected String popText() {
    String result = body.toString();
    body.getBuffer().setLength(0);
    return result;
  }

  private void printHeadings(List<Heading> toc) {
    if (toc == null)
      return;
    indent().print("<UL");
    printAttr("class", "toc");
    println(">");
    pushIndent();
    for (Heading heading : toc) {
      String number = heading.getHeadingNumber();
      String text = heading.getHeadingText();
      String href = "#" + heading.getHeadingId();
      indent().print("<LI");
      printAttr("class", "tocline");
      print(">");
      print("<span");
      printAttr("class", "secno");
      print(">");
      print(number);
      print("</span>");
      print(" <a");
      printAttr("href", href);
      print(">").print(text).print("</a>");
      println("</LI>");
      printHeadings(heading.getChildren());
    }
    popIndent();
    println("</UL>");

  }



  protected void beginCodeSnippet() {
    indent().print("<DIV");
    printAttr("class", "code-snippet");
    println(">");
    println("<PRE>");
  }
  
  protected void endCodeSnippet() {
    println("</PRE>");
    indent().println("</DIV>");
    
  }


  protected void printDefinition(String termName, String description) {
    indent().print("<DT>").print(termName).println("</DT>");
    pushIndent();
    indent().print("<DD>").print(description).println("</DD>");
    popIndent();
    
  }


  protected void printParagraph(String text) {
    indent().print("<P>").print(text).println("</P>");
    
  }

  protected Heading createHeading(String text) {
    text = text.trim();
    Level level = currentHeading.getLevel().getNextLevel();
    
    Heading result = new Heading(level, text, text.replace(' ', '_'));
    currentHeading.add(result);
    
    return result;
  }
  
  protected Heading createHeading(String text, String id) {

    text = text.trim();
    Level level = currentHeading.getLevel().getNextLevel();
    
    Heading result = new Heading(level, text, id);
    currentHeading.add(result);
    
    return result;
  }
  
  public static String capitalizedArticle(String text) {

    char c = Character.toLowerCase(text.charAt(0));
    return VOWEL.indexOf(c)>=0 ? "An" : "A";
  }
  
  public static String article(String text) {
    char c = Character.toLowerCase(text.charAt(0));
    return VOWEL.indexOf(c)>=0 ? "an" : "a";
  }

  protected String updateReferences(String text) {
    for (Caption caption : forwardReferenceList) {
      String ref = createForwardRef(caption);
      text = text.replace(ref, createLink(caption));
    }
    return text;
  }

  private String createLink(Caption caption) {
    return "<A href=\"#" + caption.getId() + "\">" + caption.getNumber()
        + "</A>";
  }

  protected void printLink(Caption caption) {
    print(createLink(caption));
  }

  protected String createForwardRef(Caption caption) {
    return "<!-- REF:" + caption.getId() + " -->";
  }

  protected void printForwardRef(Caption caption) {
    print(createForwardRef(caption));

  }

  protected void printFigure(String src, Caption caption) {
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

  protected void printCaption(Caption caption) {

    captionManager.add(caption);
    
    indent().print("<DIV");
    printAttr("class", "caption");
    println(">");
    print(caption.getNumber());
    print(".&nbsp&nbsp;");
    print(caption.getText());
    indent().println("</DIV>");

  }

  protected void assignNumber(Caption caption) {
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

  protected void print(Heading heading) {

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
    print(number);
    print(" ");
    print(heading.getHeadingText());
    println("</" + level + ">");

  }
  protected void beginHTML() {
    println("<HTML>");
    println("<HEAD>");
    pushIndent();
    printStyleSheetLink();
    popIndent();
    println("</HEAD>");
    println("<BODY>");
  }

  protected void printStyleSheetLink() {

    String href = getPathToStyleSheet();

    indent().print("<LINK");
    printAttr("REL", "StyleSheet");
    printAttr("HREF", href);
    printAttr("TYPE", "text/css");
    println(">");

  }
  
  abstract protected String getPathToStyleSheet();

  protected void endHTML() {
    println("</BODY>");
    println("</HTML>");
  }

  protected void beginHeading(Heading heading) {
    print(heading);
    currentHeading = heading;

  }

  protected void endHeading() {
    currentHeading = currentHeading.getParent();
  }


  public static enum CaptionType {
    Figure, Table
  }

  public static class Caption {
    private CaptionType type;
    private String text;
    private String number;
    private String id;
    private String uri;

    public Caption(CaptionType type, String text, String id, String uri) {
      this.type = type;
      this.text = text;
      this.id = id;
      this.uri = uri;
    }
    

    /**
     * Returns the URI for the object represented in the figure or table.
     * This is useful if you want to look-up a caption based on the URI.
     */
    public String getUri() {
      return uri;
    }

    /**
     * Sets the URI for the object represented in the figure or table.
     */
    public void setUri(String uri) {
      this.uri = uri;
    }



    public String getNumber() {
      return number;
    }

    public void setNumber(String number) {
      this.number = number;
    }

    public CaptionType getType() {
      return type;
    }

    public String getText() {
      return text;
    }

    public String getId() {
      return id;
    }

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

  static public class Heading {
    private Level level;
    private String headingNumber;
    private String headingText;
    private String headingId;
    private String className;
    private List<Heading> children;
    private Heading parent;

    public Heading(Level level, String headingText, String headingId) {
      this.level = level;
      this.headingText = headingText;
      this.headingId = headingId;
    }

    public void setHeadingNumber(String headingNumber) {
      this.headingNumber = headingNumber;
    }

    public String getHeadingNumber() {
      return headingNumber;
    }

    public String getHeadingText() {
      return headingText;
    }

    public Level getLevel() {
      return level;
    }

    public String getHeadingId() {
      return headingId;
    }

    public void add(Heading subheading) {
      if (children == null) {
        children = new ArrayList<HtmlPrinter.Heading>();
      }
      children.add(subheading);
      subheading.parent = this;
    }

    public List<Heading> getChildren() {
      return children;
    }

    public Heading getParent() {
      return parent;
    }

    public void setParent(Heading parent) {
      this.parent = parent;
    }

    public String getClassName() {
      return className;
    }

    public void setClassName(String className) {
      this.className = className;
    }

  }

  protected String format(String pattern, Object...args) {
    return MessageFormat.format(pattern, args);
  }

}
