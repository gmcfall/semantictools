package org.semantictools.context.view;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.MessageFormat;
import java.util.Set;

import org.semantictools.context.renderer.URLRewriter;
import org.semantictools.context.renderer.model.ReferenceManager;

abstract public class HtmlPrinter extends PrintEngine {

  private static final String TOC_MARKER = "<!-- TOC -->";
  private static final String VOWEL = "aeiou";


  private StringWriter body;
  private Heading topHeading;
  private Heading currentHeading;
  private int h2;
  private int h3;
  private URLRewriter urlRewriter;

  private Set<Caption> forwardReferenceList;

  protected HtmlPrinter(URLRewriter rewriter) {
    super(new PrintContext());
    this.urlRewriter = rewriter;
  }
  
  protected void setPrintContext(PrintContext context) {
    this.context = context;
  }
  
  protected HtmlPrinter() {
    this(new URLRewriter() {

      @Override
      public String rewrite(String url) {
        return url;
      }});
  }
  

  

  protected void init() {
    h2 = h3 = 0;
    topHeading = new Heading(Level.H1, "", "");
    currentHeading = topHeading;
    body = new StringWriter();

    getPrintContext().setWriter(new PrintWriter(body));


  }
  

  protected String createDefaultReference(String key) {
    return null;
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



  protected void beginHeading(Heading heading) {
    print(heading);
    currentHeading = heading;

  }

  protected void endHeading() {
    currentHeading = currentHeading.getParent();
  }



  protected String format(String pattern, Object...args) {
    return MessageFormat.format(pattern, args);
  }

}
