package org.semantictools.context.renderer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.ObjectWriter;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.node.ObjectNode;
import org.semantictools.context.renderer.impl.NodeComparatorFactoryImpl;
import org.semantictools.context.renderer.impl.SystemOutStreamFactory;
import org.semantictools.context.renderer.model.ContextProperties;
import org.semantictools.context.renderer.model.CreateDiagramRequest;
import org.semantictools.context.renderer.model.JsonContext;
import org.semantictools.context.renderer.model.ObjectPresentation;
import org.semantictools.context.renderer.model.TermInfo;
import org.semantictools.context.renderer.model.TermInfo.TermCategory;
import org.semantictools.context.renderer.model.TreeNode;
import org.semantictools.frame.api.FrameNotFoundException;
import org.semantictools.frame.api.GeneratorProperties;
import org.semantictools.frame.api.TypeManager;
import org.semantictools.frame.model.Datatype;
import org.semantictools.frame.model.Field;
import org.semantictools.frame.model.Frame;
import org.semantictools.frame.model.NamedIndividual;
import org.semantictools.frame.model.RdfType;
import org.semantictools.frame.model.RestCategory;
import org.semantictools.json.JsonManager;
import org.semantictools.json.JsonPrettyPrinter;
import org.semantictools.json.JsonSampleGenerator;

import com.hp.hpl.jena.ontology.OntProperty;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.RDFS;

public class ContextHtmlPrinter extends PrintEngine {

  private static final String TOC_MARKER = "<!-- TOC -->";
  private static final String XMLSCHEMA_URI = "http://www.w3.org/2001/XMLSchema#";
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

  private static final String[] STANDARD_URI = { XMLSCHEMA_URI,
      "http://www.w3.org/2002/07/owl#",
      "http://www.w3.org/1999/02/22-rdf-syntax-ns#",
      "http://www.w3.org/2000/01/rdf-schema#",
      "http://purl.org/semantictools/v1/vocab/bind#" };

  private static boolean isStandard(String uri) {
    for (int i = 0; i < STANDARD_URI.length; i++) {
      if (uri.startsWith(STANDARD_URI[i]))
        return true;
    }
    return false;
  }

  private TypeManager typeManager;
  private MediaTypeFileManager namer;
  private boolean includeOverviewDiagram;
  private boolean includeClassDiagrams;

  private StreamFactory streamFactory;
  private StringWriter body;
  private List<Frame> frameList;
  private List<Datatype> datatypeList;
  private JsonContext context;
  private ContextProperties contextProperties;
  private DiagramGenerator diagramGenerator;
  private TreeGenerator treeGenerator;
  private JsonSampleGenerator sampleGenerator;
  private JsonManager jsonManager;
  private GeneratorProperties generatorProperties;
  private NodeComparatorFactory nodeComparatorFactory;
  private Frame root;
  private Heading topHeading;
  private Heading currentHeading;
  private int h2;
  private int h3;
  private int figureNumber;
  private int tableNumber;

  private Caption overviewDiagram;
  private Set<Caption> forwardReferenceList;
  private CaptionManager captionManager;

  public ContextHtmlPrinter(GeneratorProperties properties, TypeManager typeManager, MediaTypeFileManager namer) {
    this(properties, typeManager, namer, new SystemOutStreamFactory(), null);
  }

  public ContextHtmlPrinter(GeneratorProperties properties, TypeManager typeManager,
      MediaTypeFileManager namer, StreamFactory streamFactory,
      DiagramGenerator generator) {
    super(new PrintContext());
    generatorProperties = properties;
    diagramGenerator = generator;

    this.streamFactory = streamFactory;
    body = new StringWriter();

    getPrintContext().setWriter(new PrintWriter(body));

    this.typeManager = typeManager;
    this.namer = namer;
  }

  public Frame getRootFrame() {
    return root;
  }

  public void printHtml(JsonContext context) throws IOException {
    printHtml(context, null);
  }

  public void printHtml(JsonContext context, ContextProperties properties)
      throws IOException {
    h2 = h3 = 0;
    topHeading = new Heading(Level.H1, "", "");
    currentHeading = topHeading;
    this.contextProperties = properties;
    this.context = context;
    treeGenerator = new TreeGenerator(context, properties);
    sampleGenerator = new JsonSampleGenerator(typeManager);
    root = typeManager.getFrameByUri(context.getRootType());
    if (root == null) {
      throw new FrameNotFoundException(context.getRootType());
    }
    overviewDiagram = new Caption(CaptionType.Figure,
        "Complete JSON representation of " + root.getLocalName(), "completeRep", null);
    forwardReferenceList = new HashSet<Caption>();
    captionManager = new CaptionManager();
    jsonManager = new JsonManager(typeManager, context);
    nodeComparatorFactory = new NodeComparatorFactoryImpl(jsonManager);
    
    addDefaultReferences();

    beginHTML();
    pushIndent();
    collectFrames();
    printTitle();
    printAbstract();
    printToc();
    printIntroduction();
    printMediaTypeConformance();
    printDataBindings();
    printReferences();
    popIndent();
    endHTML();

    writeOutput();

  }

  private void addDefaultReferences() {
   
    addDefaultReference(
        "[JSON-LD-syntax]",
        "Manu Sporny, Dave Longley, Gregg Kellogg, Markus Lanthaler, Mark Birbeck| Json-LD Syntax 1.0| " +
        "Unofficial Draft 19| March 2012| W3C Community Group Recommendation| " +
        "URL: http://json-ld.org/spec/latest/json-ld-syntax/");
    
    addDefaultReference(
        "[RFC4627]",
        "D. Crockford| The application/json Media Type for JavaScript Object Notation (JSON)|" +
        "July 2006| Internet RFC 4627| URL: http://www.ietf.org/rfc/rfc4627.txt");
    
    addDefaultReference(
        "[CURIE-syntax]", 
        "Mark Birbeck, Shane McCarron| CURIE Syntax 1.0| W3C Working Group Note| 16 December 2010| " +
        "URL: http://www.w3.org/TR/curie/");
     
    
  }

  private void addDefaultReference(String key, String value) {
    if (contextProperties!=null && contextProperties.getReference(key)==null) {
      contextProperties.putReference(key, value);
    }
    
    
  }

  private void printReferences() {
    
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
      String value = contextProperties.getReference(key);
      if (value == null) {
        
        if (key.equals(contextProperties.getContextRef())) {
          TermInfo term = context.getTermInfoByURI(root.getUri());
          
          value = "||The standard JSON-LD context for <code>" + term.getTermName() + "</code> objects in the <code>" +
            contextProperties.getMediaType() + "</code> media type| URL: " +
            contextProperties.getContextURI();
        } else {        
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
  
  private String addLinksToReference(String value) {
    
    Pattern pattern = Pattern.compile("URL:\\s*(.*)");
    Matcher matcher = pattern.matcher(value);
    String url = matcher.find() ? matcher.group(1) : null;
    
    if (url == null) {
      return value;
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
    String href = generatorProperties==null ? url :
        namer.toRelativeURL(url, generatorProperties.getBaseURL(), context.getMediaType());
    
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
    String jsonArray = "\\[\\s*\".*";
    
    Pattern pattern = Pattern.compile("\\[[^\\]]*\\]");
    Matcher matcher = pattern.matcher(text);
    while (matcher.find()) {
      String value = matcher.group();
      if (
        !list.contains(value) && 
        value.indexOf('\n')<0 &&
        !value.matches(numericRange) &&
        !value.matches(jsonArray) &&
        !value.contains(" ... ")
      ) {
        list.add(matcher.group());
      }
    }
    Collections.sort(list);
    return list;
  }

  private void printDataBindings() throws IOException {
    Heading heading = createHeading("JSON Data Bindings");
    beginHeading(heading);
    printOverviewDiagram();
    printFrames();
    printDatatypes();
    endHeading();
    
  }

  private void printDatatypes() {
    for (Datatype type : datatypeList) {
      
      String uri = type.getUri();
      if (uri.startsWith(XMLSCHEMA_URI)) continue;
      printDatatype(type);
    }
    
  }

  private void printMediaTypeConformance() {
    endHeading();
    String headingTemplate = "The {0} Media Type";
    String typeName = context.rewrite(root.getUri());
    String mediaType = contextProperties.getMediaType();
    String contextRef = contextProperties.getContextRef();
    
    Heading heading = createHeading(headingTemplate.replace("{0}", typeName));
    print(heading);
    
    String text = 
        "The following list defines the necessary and sufficient conditions for a document " +
        "to conform to the <code>{0}</code> media type.";
       
    printParagraph(text.replace("{0}", mediaType));
    indent().print("<OL");
    printAttr("class", "uncondensed");
    println(">");
    pushIndent();
    
    printListItem("The document MUST be a valid JSON document, in accordance with [RFC4627].");
    
    printListItem("The document MUST contain either a single top-level JSON object, or an array " +
      "of top-level JSON objects.  The first object encountered (either the single top-level object or " +
      "the first element of the array) is called the <em>root</em> object.");
    
    text = "The root object must have a <code>@type</code> property whose value is \"<code>{0}</code>\".";
    printListItem(text.replace("{0}", typeName));
    
    text = "Every top-level object MUST have a <code>@context</code> property that references one or more " +
        "JSON-LD contexts (either by URI or by value).";
    printListItem(text);
    
    text = "Collectively, the set of contexts imported by the root object MUST contain all of the " +
      "terms found in the <em>standard context</em> {0}.  In particular, the set of imported contexts must " +
      "contain all the simple names that appear in the standard context, and those simple names must " +
      "resolve to the same values that appear in the standard context.  This requirement may be " +
      "satisfied by ensuring that the root object imports the standard context explicitly, or by " +
      "importing a collection of other contexts that contain equivalent terms.";
    printListItem(text.replace("{0}", contextRef));
    
    text = "The set of contexts imported by the root object MAY include additional terms that do not " +
      "appear in the standard context {0}.";
    printListItem(text.replace("{0}", contextRef));
    
    text = "Duplicate mappings for names among the imported contexts MUST be overwritten on a " +
      "last-defined-overrides basis.";
    printListItem(text);
    
    text = "If the JSON-LD context coerces a property to a URI reference, then values of that " +
      "property MUST be expressed as a fully-qualified URI reference, or a CURIE  or a simple name " +
      "declared by the context.";
    printListItem(text);
    
    text = "A <em>collection property</em> is any property whose maximum cardinality is greater than 1. " +
        "Except for the <code>@context</code> property, " +
        "a non-empty collection MUST always be represented as a JSON array whose values are enclosed " +
        "in square brackets. Whereas, in general, the JSON-LD syntax specification allows a collection " +
        "containing a single value to omit the square brackets, the <code>" + mediaType + "</code> media type " +
        "requires square brackets for all non-empty collections other than the <code>@context</code> property.";
    printListItem(text);
    
    text = "An empty collection property may be represented either by an empty array (i.e. square brackets " +
        "containing no elements), or by omitting the property altogether.";
    printListItem(text);
    
    text = "Like all other properties, the <code>@id</code> property of a given object is mandatory " +
      "if the minimum cardinality of that property, as defined by this specification, is greater than " +
      "zero. The <code>@id</code> property is optional for all other objects (even if it is not " +
      "explicitly listed in the set of properties for an object).  Conforming implementations SHOULD " +
      "include the <code>@id</code> property for all addressable objects.";
    printListItem(text);
    
    text = "If the <code>@id</code> property is mandatory, then the value MUST NOT treat the object as " +
      "a blank node.  In this case, the <code>@id</code> value MUST NOT be a CURIE with an underscore " +
      "as the prefix.";
    printListItem(text);
    
    text = "Every top-level object MUST contain a <code>@type</code> property and a @context property.";
    printListItem(text);
    
    text = "An embedded object MUST contain a <code>@type</code> property if the object value is a " +
      "subtype of the declared range of the property.";
    printListItem(text);
    
    text = "Values for properties named in the standard context {0}, MUST not utilize the String Internationalization or Typed Value syntax as described in [JSON-LD-syntax].";
    printListItem(text.replace("{0}", contextRef));
    
    text = "If the context does not coerce the value of an object property to a URI reference, " +
      "then the object must be rendered as an embedded object.";
    printListItem(text);
    
    text = "The properties of embedded objects must respect the cardinality constraints specified in " +
      "the section titled JSON Data Bindings.";
    printListItem(text);
    
    popIndent();
    indent().println("</OL>");
    
    
  }

  private void printListItem(String text) {
    indent().print("<LI>").print(text).println("</LI>");
    
  }

  private void writeOutput() throws IOException {

    String text = getOutput();

    String path = namer.getIndexFileName();
    OutputStream stream = streamFactory.createOutputStream(path);

    PrintStream printStream = (stream instanceof PrintStream) ? (PrintStream) stream
        : new PrintStream(stream);
    printStream.print(text);
    printStream.close();

  }

  private void printToc() {

    println(TOC_MARKER);

  }

  private String getOutput() {
    body.flush();
    String text = body.toString();

    text = updateReferences(text);

    body.getBuffer().setLength(0);
    indent().println("<H2>Table of Contents</H2>");
    printHeadings(topHeading.getChildren());

    String toc = body.toString();
    return text.replace(TOC_MARKER, toc);

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

  private void printAbstract() {

    String abstractText = contextProperties == null ? null : contextProperties
        .getAbstactText();
    if (abstractText == null)
      return;

    indent().println("<H2>Abstract</H2>");
    indent().println("<DIV>");
    print(abstractText);
    println("</DIV>");

  }

  private void printIntroduction() throws IOException {
    String text = contextProperties == null ? null : contextProperties
        .getIntroduction();
    String headingText = "Introduction";

    Heading heading = createHeading(headingText);
    beginHeading(heading);
    if (text != null) {
      indent().println("<DIV>");
      print(text);
      println("</DIV>");
    }

    printHowToRead();


  }

  private void printHowToRead() throws IOException {

    Heading heading = createHeading("How To Read this Document");
    print(heading);

    printSampleObject();
    printPropertyRepresentation();
    printOptionalPropertyFigure();
    printRepeatedPropertyFigure();
    printObjectRepresentation();
    printReservedTerms();
    printContextDiscussion();

  }

  private void printContextDiscussion() {
    Heading heading = createHeading("The JSON-LD Context");
    print(heading);
    

    printContextSnippet();
    printTypeCoercionSample();
    
  }

  private void printTypeCoercionSample() {
    
    TermInfo term = getTypeCoercionSample(new HashSet<String>(), root);
    if (term == null) return;

    printParagraph(
        "A context may specify that the values of  certain object properties must be rendered as URI references.   " +
        "The following snippet presents an example of such a rule.");
    
    beginCodeSnippet();
    println("  {");
    println("    \"@context\" = {");
    println("      ...");
    printTerm(term);
    println();
    println("      ...");
    println("  }");
    endCodeSnippet();
    
    printParagraph(
        "This rule is an example of <em>type coercion</em>.  " +
        "For more details about the syntax of a JSON-LD context, see [JSON-LD-syntax].");
    
  }

  private TermInfo getTypeCoercionSample(Set<String> history, Frame frame) {
    if (history.contains(frame.getUri())) return null;
    history.add(frame.getUri());
    
    List<Field> list = frame.listAllFields();
    for (Field field : list) {
      TermInfo term = context.getTermInfoByURI(field.getURI());
      if (term == null) continue;
      if (term.hasObjectValue() && "@id".equals(term.getObjectValue().getType())) return term;
      RdfType type = field.getRdfType();
      if (type != null && type.canAsFrame()) {
        term = getTypeCoercionSample(history, type.asFrame());
        if (term != null) return term;
      }
    }
    
    return null;
  }

  private void printContextSnippet() {

    String contextRef = contextProperties.getContextRef();
    String typeName = context.rewrite(root.getUri());
    

    Field field = getSampleToken();
    
    String template = 
        "In JSON-LD, a context is used to map simple names that appear in a JSON document " +
        "to URI values for properties or data types in a formal vocabulary (typically an RDF ontology).  ";
    
    String example =
        "For example, the standard context {0} for a {1} contains the following rewrite rules (among others):";
    
    if (field != null) {
    
      example = example.replace("{0}", contextRef);
      template += example.replace("{1}", typeName);
    }
    
    printParagraph(template);
    
    if (field == null) return;
    
    TermInfo term = context.getTermInfoByURI(field.getURI());
    String namespace = field.getProperty().getNameSpace();
    String prefix = context.rewrite(namespace);
    
    beginCodeSnippet();
    println("  {");
    println("    \"@context\" = {");
    
    
    if (!prefix.equals(namespace)) {
      print("      \"").print(prefix).print("\" : \"").print(namespace).println("\",");
    }
    printTerm(term);
    println(",");
    
    println("      ...");
    println("    }");
    println("  }");
    endCodeSnippet();
    
    
    
  }

  private void printTerm(TermInfo term) {
    if (term.hasObjectValue()) {

      print("      \"");
      print(term.getTermName());
      println("\" : {");
      print("        \"@id\" : \"");
      print(term.getObjectValue().getId());
      println("\",");
      print("        \"@type\" : \"");
      print(term.getObjectValue().getType());
      println("\"");
      print("      }");
      
      
    } else {
    
      print("      \"");
      print(term.getTermName());
      print("\" : \"");
      print(term.getIri());
      print("\"");
    }
    
  }
  

  private void beginCodeSnippet() {
    indent().print("<DIV");
    printAttr("class", "code-snippet");
    println(">");
    println("<PRE>");
  }
  
  private void endCodeSnippet() {
    println("</PRE>");
    indent().println("</DIV>");
    
  }

  private Field getSampleToken() {
    List<Field> list = root.listAllFields();
    for (Field field : list) {
      TermInfo term = context.getTermInfoByURI(field.getURI());
      if (term == null) continue;
      if (!term.hasObjectValue()) return field;
    }
    
    return null;
  }

  private void printReservedTerms() {
    Heading heading = createHeading("Reserved Terms");
    print(heading);
    printParagraph("The JSON-LD standard reserves a handful of property names and tokens " +
      "that have special meaning.  These names and tokens, described below, begin with the '@' symbol.");
    indent().println("<DL");
    printAttr("class", "reservedTerms");
    println(">");
    pushIndent();
      printDefinition(
          "@context",
          "Used to reference (by URI or by value) a <em>context</em> which declares the simple names " +
          "that appear throughout a JSON document.");
      
      printDefinition(
          "@id",
          "Used to uniquely identify things that are being described in the JSON document.  " +
          "The value of an @id property is either a fully-qualified URI, a CURIE, or a simple name " +
          "that expands to a fully-qualified URI by virtue of the rules defined in the JSON-LD Context." +

          "<P>The @id property may identify a so-called blank node by using a CURIE with an underscore " +
          "as the prefix.  The binding of a JSON-LD document MAY include identifiers for blank nodes, " +
          "but these identifiers are not required.");
      
      printDefinition(
          "@type",
          "Used to set the data type of an object or property value.");
      
    popIndent();
    indent().println("</DL>");
   
    String text =
        "JSON-LD specifies four other reserved terms (@value, @language, @container, @list).  " +
        "Ordinarily, these terms are not used in the JSON binding for <code>{0}</code> objects.  However, " +
        "implementations that extend this specification by including additional properties may utilize " +
        "these reserved terms in accordance with the rules defined by [JSON-LD-syntax].";
    String typeName = root.getLocalName();
    printParagraph(text.replace("{0}", typeName));
    
  }

  private void printDefinition(String termName, String description) {
    indent().print("<DT>").print(termName).println("</DT>");
    pushIndent();
    indent().print("<DD>").print(description).println("</DD>");
    popIndent();
    
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

  private void printParagraph(String text) {
    indent().print("<P>").print(text).println("</P>");
    
  }

  private Heading createHeading(String text) {
    text = text.trim();
    Level level = currentHeading.getLevel().getNextLevel();
    
    Heading result = new Heading(level, text, text.replace(' ', '_'));
    currentHeading.add(result);
    
    return result;
  }

  private void printPropertyRepresentation() throws IOException {

    // Find a field that has a simple type, like a string, number or date.
    // This is our first choice for the node that we'll use as the sample
    // property.
    Field field = getSimpleTypeField(new HashSet<String>(), root);
  
    if (field == null) {
      field = getAnyField(root);
    }
    
    indent()
        .print(
            "<P>Each box representing a property specifies the name and type of the property , as shown in ");
    Caption caption = new Caption(CaptionType.Figure,
        "Graphical notation for a property", "sampleProperty", field.getURI());
    assignNumber(caption);
    printLink(caption);
    print(".</P>");

    String src = namer.getImagesDir() + "/sampleProperty.png";

    printFigure(src, caption);

    if (diagramGenerator == null)
      return;
    

    TreeNode node = treeGenerator.generateNode(field);
    CreateDiagramRequest request = new CreateDiagramRequest(context, node, src);

    diagramGenerator.generateNotationDiagram(request);

  }

  private void printObjectRepresentation() throws IOException {

    Field uriRefField = getUriRefField(new HashSet<String>(), root);
    Field snRefField = getSnRefField(new HashSet<String>(), root);

    indent()
        .println(
            "<P>An object within a JSON-LD document may have one of four possible representations:<P>");

    indent().println("<OL>");
    pushIndent();
    indent()
        .println(
            "<LI>The object may be identified by a fully-qualified URI reference.</LI>");
    indent()
        .println(
            "<LI>The object may be identified by a Compact URI reference,  known as a CURIE " +
            "[CURIE-syntax], that can be expanded to a fully qualified URI</LI>");
    indent()
        .println(
            "<LI>The object may be identified by a simple name that is mapped to a "
                + "fully-qualified URI.  This mapping is defined by the JSON-LD context.</LI>");
    indent()
        .println("<LI>The object may be embedded within the document.</LI>");
    popIndent();
    indent().println("</OL>");

    if (uriRefField != null) {
      printUriRefDiscussion(uriRefField);
    }
    printSnRefDiscussion(snRefField);
    

  }


  private void printUriRefDiscussion(Field field) throws IOException {

    Caption captionRef = captionManager.getFigureCaptionByURI(field.getURI());
    
    Caption caption = null;
    
    if (captionRef == null) {
      caption = captionRef = new Caption(CaptionType.Figure,
      "Property whose value is a URI reference", "uriRef", field.getURI()); 
    }
        

    indent()
        .println(
            "<P>When an object is to be identified by a fully-qualified URI or a CURIE, the box "
                + "representing the object will be decorated with the #uri hash tag, as shown in ");
    printLink(captionRef);
    println(".</P>");
    
    if (caption == null) return;
    assignNumber(caption);

    String src = namer.getImagesDir() + "/uriRef.png";
    TreeNode node = treeGenerator.generateNode(field);
    CreateDiagramRequest request = new CreateDiagramRequest(context, node, src);
//    Node node = createNode(field);
//
//
//    CreateDiagramRequest request = new CreateDiagramRequest(context, node, src,
//        0, false, false);
    diagramGenerator.generateDiagram(request);
    printFigure(src, caption);

  }

  private void printSnRefDiscussion(Field field) throws IOException {

    String fieldURI = field == null ? null : field.getURI();
        
    Caption captionRef = captionManager.getFigureCaptionByURI(fieldURI);
    
    Caption caption = null;
    if (captionRef == null) {
      
      captionRef = caption = new Caption(
          CaptionType.Figure,
          "Property whose value is a simple name reference for an individual object or enumerable value",
          "snRef", fieldURI);
      assignNumber(caption);
      
    }

    indent()
        .println(
            "<P>When an object or enumerable value is to be identified by a simple name, the box "
                + "representing the corresponding property will be decorated with the #sn hash tag, as shown in ");
    printLink(captionRef);
    println(".</P>");
    
    if (caption == null) return;

    String src = namer.getImagesDir() + "/snRef.png";
    
    TreeNode node = null;
    
    if (field == null) {
      node = new TreeNode();
      node.setObjectPresentation(ObjectPresentation.SIMPLE_NAME);
      node.setMinCardinality(1);
      node.setMaxCardinality(1);
      node.setLocalName("@type");
      node.setTypeName("owl:Class");
      node.setTypeURI(OWL.Class.getURI());
    } else {
      node = treeGenerator.generateNode(field);
    }
    CreateDiagramRequest request = new CreateDiagramRequest(context, node, src);
//    
//    Node node = createNode(field);
//    CreateDiagramRequest request = new CreateDiagramRequest(context, node, src,
//        0, false, false);
    diagramGenerator.generateDiagram(request);
    printFigure(src, caption);

  }

  private void printSampleObject() throws IOException {

    Caption caption = new Caption(CaptionType.Figure,
        "Representation of a JSON object", "sampleObj", null);
    assignNumber(caption);

    indent()
        .print(
            "<P>This specification defines the structure of a JSON document using a graphical notation. "
                + "In this notatation, an object is represented by a box that branches out to other boxes corresponding to "
                + "the properties of that object, as shown in ");
    printLink(caption);
    println(".</P>");

    String src = namer.getImagesDir() + "/sampleObj.png";
    
    TreeNode node = treeGenerator.generateRoot(root, 1);
    CreateDiagramRequest request = new CreateDiagramRequest(context, node, src);
//
//    CreateDiagramRequest request = new CreateDiagramRequest(context, root, src,
//        1, false, false);
    diagramGenerator.generateDiagram(request);
    printFigure(src, caption);

    // If the root object contains an embedded object then the sample diagram is
    // not
    // a complete representation. Add a disclaimer about this condition.
    List<Field> embedded = listEmbeddedObjects(root);
    if (embedded.size() > 1) {
      // This is the case where there is more than one embedded object.

      String article = article(root.getLocalName());
      indent().print(
          "<P>" + caption.getNumber() + " is not a complete representation of "
              + article + "  <code>" + root.getLocalName()
              + "</code> object because there are embedded objects (");
      String comma = "";
      for (Field e : embedded) {
        print(comma);
        TermInfo term = context.getTermInfoByURI(e.getURI());
        if (term != null) {
          print("<code>");
          print(term.getTermName());
          print("</code>");
          comma = ", ";
        }
      }
      print(").  A complete diagram would show branches emanating from the embedded objects to " +
           "reveal their properties, and so on, recursively. For a complete representation, see ");
      printForwardRef(overviewDiagram);
      print(" below.</P>");
      forwardReferenceList.add(overviewDiagram);

    } else if (embedded.size() == 1) {
      // This is the case where there is exactly one embedded object

      Field e = embedded.get(0);
      TermInfo term = context.getTermInfoByURI(e.getURI());

      String article = Character.toUpperCase(root.getLocalName().charAt(0)) == 'A' ? "an"
          : "a";
      indent()
          .print(
              "<P>"
                  + caption.getNumber()
                  + " is not a complete representation of "
                  + article
                  + "  <code>"
                  + root.getLocalName()
                  + "</code> object because <code>"
                  + term.getTermName()
                  + "</code> is an embedded object. A complete diagram would show branches emanating from <code>"
                  + term.getTermName()
                  + "</code> "
                  + "to reveal its properties, and so on, recursively. For a complete representation, see ");
      printForwardRef(overviewDiagram);
      print(" below.</P>");
      forwardReferenceList.add(overviewDiagram);

    }

  }
  
  private String article(String text) {
    char c = Character.toLowerCase(text.charAt(0));
    return VOWEL.indexOf(c)>=0 ? "an" : "a";
  }

  private String updateReferences(String text) {
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

  private void printLink(Caption caption) {
    print(createLink(caption));
  }

  private String createForwardRef(Caption caption) {
    return "<!-- REF:" + caption.getId() + " -->";
  }

  private void printForwardRef(Caption caption) {
    print(createForwardRef(caption));

  }

  private List<Field> listEmbeddedObjects(Frame frame) {

    List<Field> result = new ArrayList<Field>();
    List<Field> list = frame.listAllFields();
    for (Field field : list) {
      if (isEmbeddedObject(field, field.getRdfType())) {
        result.add(field);
      }
    }
    return result;
  }

  private boolean isEmbeddedObject(Field field, RdfType type) {
    if (type == null) return false;

    if (type.canAsListType()) {
      return isEmbeddedObject(field, type.asListType().getElementType());
    }
    if (!type.canAsFrame() || type.asFrame().getCategory()==RestCategory.ENUMERABLE) {
      return false;
    }
    if (isShortCircuit(type.asFrame())) return false;
    
    TermInfo term = context.getTermInfoByURI(field.getURI());
    if (term != null && term.isCoercedAsIriRef()) {
      return false;
    }
    return true;

  }

  private boolean isShortCircuit(Frame frame) {
    if (!frame.isAbstract()) return false;
    List<Frame> frameList = frame.listAllSubtypes();
    List<Datatype> typeList = frame.getSubdatatypeList();
    
    return frameList.size() + typeList.size() == 1;
  }

  private void printRepeatedPropertyFigure() throws IOException {

    String typeText = null;
    Field field = getRepeatedSimpleType(new HashSet<String>(), root);

    if (field != null) {

      typeText = "xs:" + field.getType().getLocalName();
    } else {
      field = getAnyRepeatedField(new HashSet<String>(), root);

    }

    if (field == null)
      return;

    if (typeText == null) {
      RdfType rdfType = field.getRdfType();
      if (rdfType.canAsListType()) {
        rdfType = rdfType.asListType().getElementType();
      }
      String typeURI = rdfType.getUri();
      typeText = context.rewrite(typeURI);
    }

    Caption captionRef = captionManager.getFigureCaptionByURI(field.getURI());
    
    Caption caption = null;
    
    if (captionRef == null) {
      captionRef = caption = new Caption(CaptionType.Figure,
          "Example of a repeatable property", "repeatable-property", field.getURI());
      assignNumber(caption);
    }
    

    indent()
        .print(
            "<P>If a property can have multiple values, then its box in the graphical "
                + "notation is decorated with a circle that contains a plus sign (+) as shown in ");
    printLink(captionRef);
    String fieldName = field.getLocalName();
    String value = (field.getRdfType() != null && field.getRdfType()
        .canAsFrame()) ? " object" : " value";
    value = "<code>" + typeText + "</code>" + value;
    println(".  In this example, the <code>"
        + fieldName
        + "</code> property may reference more than one "
        + value
        + ".  Ordinarily, these values are encapsulated within a JSON array, but if it turns out that "
        + " only one value is present, then the square brackets for the array are optional.</P>");

    if (caption == null) return;
    
    String src = namer.getImagesDir() + "/repeatableproperty.png";
    
//    Node node = new Node();
//    node.setNameText(field.getLocalName());
//    node.setTypeText(typeText);
//    node.setModifier(Modifier.REPEATABLE);
//    CreateDiagramRequest request = new CreateDiagramRequest(context, node, src,
//        0, false, false);
    
    TreeNode node = treeGenerator.generateNode(field);
    CreateDiagramRequest request = new CreateDiagramRequest(context, node, src);
    diagramGenerator.generateDiagram(request);
    printFigure(src, caption);

  }

  private void printOptionalPropertyFigure() throws IOException {

    Field field = getOptionalSimpleType(new HashSet<String>(), root);

    if (field == null) {
      field = getAnyOptionalField(new HashSet<String>(), root);
    } 
    
    if (field == null) {
      return;
    }


    String fieldURI = field.getURI();
    
    Caption captionRef = captionManager.getFigureCaptionByURI(fieldURI);
    Caption caption = null;
    
    if (captionRef == null) {
      captionRef = caption = new Caption(CaptionType.Figure,
          "Example of an optional property", "optional-property", fieldURI);
      assignNumber(caption);
    }

    indent()
        .print(
            "<P>If a property is optional, its box will be decorated with a circle that contains a question mark, "
                + "as shown in ");
    printLink(captionRef);
    println(".</P>");

    if (caption == null) return;
    
    String src = namer.getImagesDir() + "/optionalproperty.png";
   
    TreeNode node = null;
    
    if (field == null) {
      node = new TreeNode();
      node.setMinCardinality(0);
      node.setMaxCardinality(1);
      node.setTypeName("xs:anyURI");
      node.setLocalName("@id");
    } else {
      node = treeGenerator.generateNode(field);
    }
    
    CreateDiagramRequest request = new CreateDiagramRequest(context, node, src);
    
    diagramGenerator.generateDiagram(request);
    printFigure(src, caption);

  }

  private Field getUriRefField(Set<String> history, Frame frame) {

    if (history.contains(frame.getUri()))
      return null;
    history.add(frame.getUri());

    List<Field> list = frame.listAllFields();
    for (Field field : list) {

      String shortName = field.getLocalName();

      TermInfo info = context.getTermInfoByShortName(shortName);
      if (info == null) {
        continue;
      }

      RdfType type = field.getRdfType();
      if (info.hasObjectValue() &&
          "@id".equals(info.getObjectValue().getType()) &&
          type != null &&
          type.canAsFrame() &&
          type.asFrame().getCategory() != RestCategory.ENUMERABLE
      ) {
        return field;
      }
      
      if (type != null && type.canAsFrame()) {
        Field result = getUriRefField(history, type.asFrame());
        if (result != null)
          return result;
      }
    }

    return null;
  }

  private Field getSnRefField(Set<String> history, Frame frame) {

    if (history.contains(frame.getUri()))
      return null;
    history.add(frame.getUri());

    List<Field> list = frame.listAllFields();
    for (Field field : list) {

      String shortName = field.getLocalName();

      TermInfo info = context.getTermInfoByShortName(shortName);
      if (info == null) {
        continue;
      }

      RdfType type = field.getRdfType();
      if (type != null && type.canAsFrame()) {
        Frame obj = type.asFrame();
        if (obj.getCategory() == RestCategory.ENUMERABLE)
          return field;

        Field result = getSnRefField(history, obj);
        if (result != null)
          return result;
      }
    }

    return null;
  }

  private Field getRepeatedSimpleType(Set<String> history, Frame frame) {
    if (history.contains(frame.getUri()))
      return null;
    history.add(frame.getUri());

    List<Field> list = frame.listAllFields();
    for (Field field : list) {
      if (field.getMaxCardinality() > 0 && field.getMaxCardinality() < 2)
        continue;
      String typeURI = field.getType().getURI();
      if (
          typeURI != null && 
          typeURI.startsWith(XMLSCHEMA_URI) &&
          context.getTermInfoByURI(field.getURI()) != null
      ) {
        return field;
      }
      RdfType type = field.getRdfType();
      if (type != null && type.canAsFrame()) {
        Field result = getRepeatedSimpleType(history, type.asFrame());
        if (result != null)
          return result;
      }
    }

    return null;
  }

  private Field getOptionalSimpleType(HashSet<String> history, Frame frame) {
    if (history.contains(frame.getUri()))
      return null;
    history.add(frame.getUri());

    List<Field> list = frame.listAllFields();
    for (Field field : list) {
      if (field.getMinCardinality() == 0 && field.getMaxCardinality() == 1
          && field.getType().getURI() != null
          && field.getType().getURI().startsWith(XMLSCHEMA_URI) &&
          context.getTermInfoByURI(field.getURI()) != null) {
        return field;
      }

      RdfType type = field.getRdfType();
      if (type != null && type.canAsFrame()) {
        Field result = getOptionalSimpleType(history, type.asFrame());
        if (result != null)
          return result;
      }
    }

    return null;
  }

  private Field getAnyOptionalField(HashSet<String> history, Frame frame) {
    if (history.contains(frame.getUri()))
      return null;
    history.add(frame.getUri());

    List<Field> list = frame.listAllFields();
    if (list.isEmpty()) return null;
    
    for (Field field : list) {
      if (field.getMinCardinality() == 0 && 
          field.getMaxCardinality() == 1 && 
          !field.getRdfType().canAsListType() &&
          context.getTermInfoByURI(field.getURI()) != null) {
        return field;
      }
      RdfType type = field.getRdfType();
      if (type != null && type.canAsFrame()) {
        Field result = getAnyOptionalField(history, type.asFrame());
        if (result != null)
          return result;
      }
    }

    return null;
  }

  private Field getAnyRepeatedField(HashSet<String> history, Frame frame) {
    if (history.contains(frame.getUri()))
      return null;
    history.add(frame.getUri());

    List<Field> list = frame.listAllFields();
    for (Field field : list) {
      if (field.getRdfType().canAsListType()) {
        return field;
      }
      if (field.getMaxCardinality() != 1 && context.getTermInfoByURI(field.getURI()) != null) {
        return field;
      }
      RdfType type = field.getRdfType();
      if (type != null && type.canAsFrame()) {
        Field result = getAnyRepeatedField(history, type.asFrame());
        if (result != null)
          return result;
      }
    }

    return null;
  }

  private Field getAnyField(Frame root) {
    List<Field> list = root.listAllFields();
    if (list.isEmpty())
      return null;
    
    // Ideally, we'd rather not return an RDFS property (like "label")
    // or an OWL property (like "sameAs").
    // Better to return a property that is defined within a custom
    // namespace.
    for (Field field : list) {
      String uri = field.getType().getURI();
      if (
          uri != null &&
          !uri.startsWith(RDFS.getURI()) &&
          !uri.startsWith(OWL.NS) &&
          context.getTermInfoByURI(field.getURI()) != null
      ) {
        return field;
      }
    }
    return list.get(0);
  }

  private void printFigure(String src, Caption caption) {
    indent().print("<DIV");
    printAttr("class", "figure");
    printAttr("id", caption.getId());
    println(">");
    pushIndent();
    indent().print("<IMG");
    printAttr("src", src);
    println("/>");
    printCaption(caption);
    popIndent();
    indent().println("</DIV>");

  }

  private void printCaption(Caption caption) {

    captionManager.add(caption);
    
    indent().print("<DIV");
    printAttr("class", "caption");
    println(">");
    print(caption.getNumber());
    print(".&nbsp&nbsp;");
    print(caption.getText());
    indent().println("</DIV>");

  }

  private Field getSimpleTypeField(Set<String> history, Frame frame) {
    String frameURI = frame.getUri();
    if (history.contains(frameURI)) {
      // avoid infinite regress
      return null;
    }
    history.add(frameURI);
    List<Field> list = frame.listAllFields();
    for (Field field : list) {

      String typeURI = field.getType().getURI();
      if (typeURI == null)
        continue;
      TermInfo term = context.getTermInfoByURI(field.getURI());
      if (typeURI.startsWith(XMLSCHEMA_URI) && term != null)
        return field;

      RdfType type = field.getRdfType();
      if (type != null && type.canAsFrame()) {
        Field result = getSimpleTypeField(history, type.asFrame());
        if (result != null){
          return result;
        }
      }

    }
    return null;
  }

  private void assignNumber(Caption caption) {
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

  private void print(Heading heading) {

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

  public List<Frame> listFrames() {
    return frameList;
  }

  public boolean isIncludeOverviewDiagram() {
    return includeOverviewDiagram;
  }

  public void setIncludeOverviewDiagram(boolean includeOverviewDiagram) {
    this.includeOverviewDiagram = includeOverviewDiagram;
  }

  public boolean isIncludeClassDiagrams() {
    return includeClassDiagrams;
  }

  public void setIncludeClassDiagrams(boolean includeClassDiagrams) {
    this.includeClassDiagrams = includeClassDiagrams;
  }

  private void printTitle() {

    String title = contextProperties.getTitle();
    String mediaType = context.getMediaType();
    String rdfType = root.getUri();
    String contextURI = context.getContextURI();
    String contextHref = namer.getJsonContextFileName(context);
    String status = contextProperties == null ? null : contextProperties
        .getStatus();
    List<String> editorList = contextProperties == null ? null
        : contextProperties.getEditors();
    List<String> authorList = contextProperties == null ? null
        : contextProperties.getAuthors();


    indent().print("<H1>");
    print(title);
    println("</H1>");

    if (status != null) {
      indent().print("<DIV");
      printAttr("class", "subtitle");
      print(">").print(status).print("</DIV>");
    }
    

    indent().print("<TABLE");
    printAttr("class", "mediaTypeProperties");
    println(">");
    pushIndent();
    indent().println("<TR>");
    pushIndent();
    indent().println("<TH>Media Type</TH>");
    indent().print("<TD>").print(mediaType).println("</TD>");
    popIndent();
    indent().println("</TR>");
    indent().println("<TR>");
    pushIndent();
    indent().println("<TH>RDF Type</TH>");
    indent().print("<TD>").print(rdfType).println("</TD>");
    popIndent();
    indent().println("</TR>");
    indent().println("<TR>");
    pushIndent();
    indent().println("<TH>JSON-LD</TH>");
    indent().print("<TD>");
    print("<A");
    printAttr("HREF", contextHref);
    print(">").print(contextURI).println("</A></TD>");
    popIndent();
    indent().println("</TR>");
    popIndent();
    indent().println("</TABLE>");
    
    if (editorList != null && !editorList.isEmpty()) {
      indent().print("<DIV");
      printAttr("class", "contributorLabel");
      println(">Editors</DIV>");
      for (String editor : editorList) {
        indent().print("<DIV");
        printAttr("class", "contributor");
        print(">").print(editor).println("</DIV>");
      }
    }
    if (authorList != null && !authorList.isEmpty()) {
      indent().print("<DIV");
      printAttr("class", "contributorLabel");
      println(">Authors</DIV>");
      for (String author : authorList) {
        indent().print("<DIV");
        printAttr("class", "contributor");
        print(">").print(author).println("</DIV>");
      }
    }

    indent().println("<HR/>");

  }

  private void beginHTML() {
    println("<HTML>");
    println("<HEAD>");
    pushIndent();
    printStyleSheetLink();
    popIndent();
    println("</HEAD>");
    println("<BODY>");
  }

  private void printStyleSheetLink() {

    String mediaType = context.getMediaType();
    String href = namer.pathToStyleSheet(mediaType);

    indent().print("<LINK");
    printAttr("REL", "StyleSheet");
    printAttr("HREF", href);
    printAttr("TYPE", "text/css");
    println(">");

  }

  private void endHTML() {
    println("</BODY>");
    println("</HTML>");
  }

  private void printOverviewDiagram() throws IOException {
    if (!includeOverviewDiagram)
      return;
    
    String typeName = context.rewrite(root.getUri());
    
    String text = 
        "{0} presents a complete graphical representation of the JSON binding for a {1} object. " +
        "The subsections following this figure provide details about each object " +
        "that appears in the JSON binding for a {1} object.";

    Caption caption = overviewDiagram;
    assignNumber(caption);
    
    text = text.replace("{0}", caption.getNumber()).replace("{1}", typeName);
    printParagraph(text);
    
    String sampleText = getSampleText();

    indent().print("<div");
    printAttr("class", "overviewDiagram");
    printAttr("id", caption.getId());
    println(">");
    pushIndent();
    String src = namer.getOverviewDiagramPath();
    indent().print("<img");
    printAttr("src", src);
    println("/>");
    popIndent();

    println("</div>");

    printCaption(caption);
    
    indent().print("<DIV");
    printAttr("class", "jsonSample");
    println(">");
    println("<PRE>");
    println(sampleText);
    println("</PRE>");
    indent().print("</DIV>");
    
    
    String sampleCaptionText = "Example JSON document containing {0} {1} object";
    sampleCaptionText = sampleCaptionText.replace("{0}", article(typeName));
    sampleCaptionText = sampleCaptionText.replace("{1}", typeName);
    
    Caption sampleCaption = new Caption(CaptionType.Figure, sampleCaptionText, "completeSample", null);
    assignNumber(sampleCaption);
    printCaption(sampleCaption);
    
    if (diagramGenerator != null) {
      
      TreeNode node = treeGenerator.generateRoot(root, -1);
      sortAll(node);
      
      CreateDiagramRequest request = new CreateDiagramRequest(context, node, src);
      
//      CreateDiagramRequest request = new CreateDiagramRequest(context, root,
//          src, -1, true, true);
      diagramGenerator.generateDiagram(request);
    }

  }

  private void sortAll(TreeNode node) {
    String typeURI = node.getTypeURI();
    List<TreeNode> kids = node.getChildren();
    
    if (typeURI != null && kids != null && !kids.isEmpty()) {
      Collections.sort(kids, nodeComparatorFactory.getComparator(typeURI));
    }
    
    if (kids != null) {
      for (TreeNode child : kids) {
        sortAll(child);
      }
    }
    
  }

  private String getSampleText() throws IOException {
    String fileName = namer.getJsonSampleFileName();
    InputStream input = streamFactory.createInputStream(fileName);
    String result = null;
    if (input == null) {
      result = createJsonSample();
      
    } else {
    
      BufferedReader buffer = new BufferedReader(new InputStreamReader(input));
      StringBuilder builder = new StringBuilder();
      try {
        String line = null;
        while (  (line=buffer.readLine()) != null) {
          builder.append(line);
          builder.append("\n");
        }
      } finally {
        buffer.close();
      }
      
      result = builder.toString();
    }
    
    jsonManager.add(result);
    
    return result;
  }

  private String createJsonSample() throws IOException {
    
    ObjectNode node = sampleGenerator.generateSample(context);
    
    
    String fileName = namer.getJsonSampleFileName();
    OutputStream out = streamFactory.createOutputStream(fileName);
    StringWriter buffer = new StringWriter();
    try {
      ObjectMapper mapper = new ObjectMapper();  
      ObjectWriter writer = mapper.writer(new JsonPrettyPrinter());
      mapper.configure(SerializationConfig.Feature.INDENT_OUTPUT, true);
      writer.writeValue(out, node);
      
      writer.writeValue(buffer, node);
    } finally {
      out.close();
    }
    
    return buffer.toString();
  }

  private void printFrames() throws IOException {


    for (Frame frame : frameList) {
      printFrame(frame);
    }


  }

  private void beginHeading(Heading heading) {
    print(heading);
    currentHeading = heading;

  }

  private void endHeading() {
    switch (currentHeading.getLevel()) {
    case H2:
      h3 = 0;
      break;
      
    case H1:
      h2 = 0;
      
    }
    currentHeading = currentHeading.getParent();
  }



  private void printDatatype(Datatype type) {
    if (type.getUri().startsWith("http://www.w3.org/2000/01/rdf-schema#")) {
      return;
    }
    String termName = null;
    Heading heading = null;
    
    TermInfo term = context.getTermInfoByURI(type.getUri());
    
    if (term != null) {    
      termName = term.getTermName();
      heading = createHeading(termName);
    } else {
      // This datatype does not have a JSON-LD term defined in the
      // context.  
      
      TreeNode node = NodeUtil.createDefaultTypeNode(context, type.getUri());
      termName = node.getTypeName();
      Level level = currentHeading.getLevel().getNextLevel();
      String id = node.getTypeHref().substring(1);      
      heading = new Heading(level, termName, id);
      currentHeading.add(heading);
      
    }
    
    heading.setClassName("rdfType");
    print(heading);
    
    String baseURI = type.getBase().getUri();
   
    beginTable("propertiesTable");
    
    beginRow();
    printTH("Restriction&nbsp;Base");
    printTD(baseURI);
    endRow();
    
    printStringFacet("pattern", type.getPattern());
    printNumberFacet("length", type.getLength());
    printNumberFacet("minLength", type.getMinLength());
    printNumberFacet("maxLength", type.getMaxLength());
    printNumberFacet("minInclusive", type.getMinInclusive());
    printNumberFacet("maxInclusive", type.getMaxInclusive());
    printNumberFacet("minExclusive", type.getMinExclusive());
    printNumberFacet("maxExclusive", type.getMaxExclusive());
    printNumberFacet("totalDigits", type.getTotalDigits());
    printNumberFacet("fractionDigits", type.getFractionDigits());
    
    endTable();
    
    String captionText = "Facets of " + termName;
    Caption caption = new Caption(CaptionType.Table, captionText, termName, type.getUri());
    assignNumber(caption);
    printCaption(caption);
    
    
  }

  private void printNumberFacet(String name, Number value) {
    
    if (value == null) return;
    beginRow();
    printTH(name);
    printTD(value.toString());
    endRow();
    
  }

  private void printStringFacet(String name, String value) {
    if (value == null) return;
    beginRow();
    printTH(name);
    printTD(value);
    endRow();
    
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
  
  protected void printTD(String value) {
    indent().print("<TD>").print(value).println("</TD>");
  }
  
  
  private void printFrame(Frame frame) throws IOException {
    println();
    
    TreeGenerator generator = new TreeGenerator(context, contextProperties);
    TreeNode node = (frame == root) ? 
        generator.generateRoot(frame, 1) :
        generator.generateNode(frame, 1);
        
        
    List<TreeNode> fieldList = node.getChildren();
        
    if (fieldList != null) {
      Collections.sort(fieldList, nodeComparatorFactory.getComparator(frame.getUri()));
    }

    Heading heading = createHeading(node.getTypeName());
    String comment = node.getDescription();
    
    heading.setClassName("rdfType");
    print(heading);

    if (comment.length() > 0) {
      print("<div");
      printAttr("class", "classComment");
      println(">");
      print(comment);
      println("</div>");
    }
    printClassDiagram(frame, node);

    printProperties(node);
    printIndividuals(frame);

  }

  private void printIndividuals(Frame frame) {

    if (frame.getCategory() != RestCategory.ENUMERABLE)
      return;

    List<NamedIndividual> list = frame.listInstances(false);

    if (list.isEmpty())
      return;
    
    Collections.sort(list, new Comparator<NamedIndividual>() {

      @Override
      public int compare(NamedIndividual a, NamedIndividual b) {
        return a.getLocalName().compareTo(b.getLocalName());
      }
    });

    String typeName = context.rewrite(frame.getUri());
    
    String text = "Known simple names for {0} objects".replace("{0}", typeName);
    
    Caption caption = new Caption(CaptionType.Table, text, typeName + "-known-sn", frame.getUri());
    assignNumber(caption);
    
    text = "<code>{0}</code> instances are enumerable, and they must be referenced by a simple name. " +
      "The default vocabulary of simple names for instances of the <code>{0}</code> class are listed in {1}.";
    
   
    
    println();
    printParagraph(text.replace("{0}", typeName).replace("{1}", caption.getNumber()));
    indent().print("<TABLE");
    printAttr("class", "enumTable");
    println(">");
    pushIndent();

    indent().println("<TR>");
    pushIndent();
    indent().print("<TH>Simple Name</TH>");
    indent().print("<TH>Description / URI</TH>");
    popIndent();
    indent().println("</TR>");
    for (NamedIndividual n : list) {

      String localName = n.getLocalName();
      String uri = n.getUri();
      String comment = n.getComment();
      if (comment != null) {
        comment = comment.trim();
        if (comment.length()==0) {
          comment = null;
        }
      }
      

      indent().println("<TR>");
      pushIndent();
      indent().print("<TD>").print(localName).println("</TD>");
      indent().print("<TD>");
      if (comment != null) {
        print("<P>").print(comment).print("</P>");
      }
      print("<code>").print(uri).print("</code>");
      println("</TD>");
      popIndent();
      indent().println("</TR>");
    }
    popIndent();
    indent().println("</TABLE>");
    printCaption(caption);

  }

  private void printClassDiagram(Frame frame, TreeNode node) throws IOException {
    if (!includeClassDiagrams)
      return;
    String src = namer.getClassDiagramPath(frame);
    
    String typeName = context.rewrite(frame.getUri());
    
    Caption caption = new Caption(CaptionType.Figure, typeName, typeName, frame.getUri());
    assignNumber(caption);
    
    String jsonExample = getJsonSample(frame); 

    
    if (jsonExample != null) {
      print("<PRE");
      printAttr("class", "jsonSnippet");
      println(">");
      println(jsonExample);
      println("</PRE>");
    }
    
    indent().print("<DIV");
    printAttr("class", "classDiagram");
    println(">");
    pushIndent();
    indent().print("<img");
    printAttr("src", src);
    println("/>");
    printCaption(caption);
    popIndent();
    indent().println("</DIV>");
    
    

    if (diagramGenerator == null)
      return;

//    boolean extras = (frame == root) ? true : false;
//    CreateDiagramRequest request = new CreateDiagramRequest(context, frame,
//        src, 1, extras, extras);
    CreateDiagramRequest request = new CreateDiagramRequest(context, node, src);
    diagramGenerator.generateDiagram(request);

  }


  private String getJsonSample(Frame frame) {
    String text = jsonManager.getJsonText(frame.getUri());
    if (text != null) {
      text = text.replaceAll("\\[\\s*\\]", "[ ... ]").replaceAll("\\{\\s*\\}", "{ ... }");
    }
    return text;
  }

  private void printProperties(TreeNode node) {
   
    List<TreeNode> list = node.getChildren();
    if (list==null || list.isEmpty()) return;
    
    indent().print("<TABLE");
    printAttr("class", "propertiesTable");
    printAttr("border", "0");
    printAttr("width", "100%");
    printAttr("cellspacing", "0");
    println(">");
    pushIndent();
    indent().println("<TR>");
    pushIndent();
    indent().println("<TH>Property</TH>");
    indent().println("<TH>Mult</TH>");
    indent().println("<TH>Description</TH>");
    indent().println("<TH>Type</TH>");
    popIndent();
    indent().println("</TR>");

    for (TreeNode field : list) {
      printField(field);
    }

    popIndent();

    indent().println("</TABLE>");
    String typeName = node.getTypeName();
    
    Caption caption = new Caption(CaptionType.Table, typeName + " properties", typeName + "-properties", null);
    assignNumber(caption);
    printCaption(caption);

  }


  
  private void printField(TreeNode field) {

    
    String localName = field.getLocalName();
    String mult = getMultiplicity(field);
    String description = field.getDescription();
    

    String typeLabel = field.getTypeName();
    String href = field.getTypeHref();

    indent().println("<TR>");
    pushIndent();

    indent().print("<TD>");
    print(localName);
    println("</TD>");

    indent().print("<TD>");
    print(mult);
    if (field.isSequential()) {
      print("<DIV");
      printAttr("class", "qualifier");
      println(">(ordered)</DIV>");
    }
    println("</TD>");

    indent().print("<TD>");
    print(description);
    println("</TD>");

    indent().print("<TD>");
    if (href != null) {
      print("<A");
      printAttr("href", href);
      print(">");
    }
    print(typeLabel);
    if (href != null) {
      print("</A>");
    }
    
    switch (field.getObjectPresentation()) {
    case URI_REFERENCE :
      print("<DIV");
      printAttr("class", "qualifier");
      println(">(URI&nbsp;reference)</DIV>");
      break;
      
    case SIMPLE_NAME :
      print("<DIV");
      printAttr("class", "qualifier");
      println(">(Simple&nbsp;Name&nbsp;reference)</DIV>");
      break;
      
      
      
    }
    
    println("</TD>");

    popIndent();

    indent().println("</TR>");

  }

  
  private String getMultiplicity(TreeNode node) {
    int min = node.getMinCardinality();
    int max = node.getMaxCardinality();

    if (min == 0 && max < 0)
      return "*";
    if (min == 1 && max == 1)
      return "1";
    String maxLabel = (max < 0) ? "*" : Integer.toString(max);
    return min + ".." + maxLabel;
    
  }

  private void collectFrames() {
    frameList = new ArrayList<Frame>();
    datatypeList = new ArrayList<Datatype>();
    Set<String> reachable = getReachableTypes();

    List<TermInfo> termList = context.getTerms();
    for (TermInfo term : termList) {
      if (term.getCategory() == TermCategory.TYPE) {
        String qname = term.getIri();
        String uri = context.toAbsoluteIRI(qname);

        Frame frame = typeManager.getFrameByUri(uri);
        if (frame == null) {

          if (isStandard(uri))
            continue;

          // Check to see if the given type is a primitive type.
          Datatype type = typeManager.getDatatypeByUri(uri);
          if (type != null) {
            if (reachable.contains(uri)) {
              datatypeList.add(type);
            }
            
            continue;
          }
            

          throw new FrameNotFoundException(uri);
        }
        if (!reachable.contains(frame.getUri()))
          continue;
        frameList.add(frame);
      }
    }
    
    addMissingTypes(reachable);
    
    Collections.sort(frameList);
    Collections.sort(datatypeList, new Comparator<Datatype>() {

      @Override
      public int compare(Datatype a, Datatype b) {
        
        TermInfo aTerm = context.getTermInfoByURI(a.getUri());
        TermInfo bTerm = context.getTermInfoByURI(b.getUri());
        
        String aName = aTerm == null ? a.getLocalName() : aTerm.getTermName();
        String bName = bTerm == null ? b.getLocalName() : bTerm.getTermName();
        
        
        return aName.compareTo(bName);
      }
    });
  }

  private void addMissingTypes(Set<String> reachable) {
    for (String uri : reachable) {
      TermInfo term = context.getTermInfoByURI(uri);
      if (term != null) continue;
      
      Datatype type = typeManager.getDatatypeByUri(uri);
      if (type != null) {
        datatypeList.add(type);
      }
      
    }
    
  }

  private Set<String> getReachableTypes() {
    Set<String> set = new HashSet<String>();

    addReachableTypes(set, root);
    return set;
  }

  private void addReachableTypes(Set<String> set, Frame frame) {

    String uri = frame.getUri();
    if (set.contains(uri))
      return;

    set.add(uri);
    List<Field> fieldList = frame.listAllFields();
    for (Field field : fieldList) {
      OntProperty p = field.getProperty();
      String propertyURI = p.getURI();
      TermInfo info = context.getTermInfoByURI(propertyURI);
      RdfType type = field.getRdfType();
      if (type.canAsListType()) {
        type = type.asListType().getElementType();
      }
      
      if (
          info != null &&
          type.canAsDatatype()
      ) {
        set.add(type.getUri());
        continue;
      }
      
      if (
          info != null && 
          info.hasObjectValue() &&
          "@id".equals(info.getObjectValue().getType()) &&
          type != null &&
          type.canAsFrame() &&
          type.asFrame().getCategory() != RestCategory.ENUMERABLE
          
      ) {
        continue;
      }
      

      if (type==null || !type.canAsFrame()) {
        continue;
      }
      
//      Frame fieldFrame = type.asFrame();
//      
//      if (isShortCircuit(fieldFrame)) {
//        frame = fieldFrame.listAllSubtypes().get(0);
//      }
        
      addReachableTypes(set, type.asFrame());
      addSubtypes(set, type.asFrame());
      addSubdatatypes(set, type.asFrame());
     
    }

  }
  

  private void addSubdatatypes(Set<String> set, Frame frame) {
    List<Datatype> list = frame.getSubdatatypeList();
    for (Datatype type : list) {
      set.add(type.getUri());
    }
    
  }

  private void addSubtypes(Set<String> set, Frame baseFrame) {
    List<Frame> list = baseFrame.listAllSubtypes();
    for (Frame frame : list) {
      addReachableTypes(set, frame);
    }
    
  }


  static enum CaptionType {
    Figure, Table
  }

  static class Caption {
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

  static class Heading {
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
        children = new ArrayList<ContextHtmlPrinter.Heading>();
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

}
