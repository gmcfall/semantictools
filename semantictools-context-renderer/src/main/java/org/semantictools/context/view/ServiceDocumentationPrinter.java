package org.semantictools.context.view;

import java.util.List;

import org.semantictools.context.renderer.URLRewriter;
import org.semantictools.context.renderer.model.DocumentMetadata;
import org.semantictools.context.renderer.model.GlobalProperties;
import org.semantictools.context.renderer.model.HttpHeaderInfo;
import org.semantictools.context.renderer.model.HttpMethod;
import org.semantictools.context.renderer.model.MethodDocumentation;
import org.semantictools.context.renderer.model.Person;
import org.semantictools.context.renderer.model.QueryParam;
import org.semantictools.context.renderer.model.ResponseInfo;
import org.semantictools.context.renderer.model.ServiceDocumentation;

public class ServiceDocumentationPrinter extends HtmlPrinter {

  private ServiceDocumentation doc;
  private DocumentPrinter printer;
  
  public ServiceDocumentationPrinter(URLRewriter rewriter) {
    super(rewriter);
  }
  
  public String print(ServiceDocumentation doc) {
    this.doc = doc;
    init();
    
    String templateName = doc.getTemplateName();
    printer = DocumentPrinterFactory.getDefaultFactory().createPrinter(templateName);
    printer.setMetadata(doc);
    setPrintContext(printer.getPrintContext());
    
    
    printer.beginHTML();
    printStatus();
    printer.printTitlePage();
    printAbstract();
    printer.printTableOfContentsMarker();
    printIntroduction();
    printRepresentation();
    printUrlTemplates();
    printServiceMethods();
    printer.printReferences();
    printer.printFooter();
    printer.endHTML();
    
    printer.insertTableOfContents();
    
    return printer.popText();
  }


  private void printStatus() {
    String status = doc.getStatus();
    if (status == null) return;
    
    print("<div ");
    printAttr("class", "status");
    print(">");
    print(status);
    println("</div>");
    
  }

  private void printUrlTemplates() {
    String text = doc.getUrlTemplateText();
    if (text == null) return;
    
    Heading heading = printer.createHeading("URL Templates");
    beginHeading(heading);
    print(text);
    endHeading();
    
  }

  private void printServiceMethods() {
    Heading heading = printer.createHeading("Service Methods");
    beginHeading(heading);
    printPostMethod();
    printGetMethod();
    printPutMethod();
    printDeleteMethod();
    
    endHeading();
  }

  private void printGetMethod() {
    if (!doc.contains(HttpMethod.GET)) return;
    Heading heading = printer.createHeading("GET");
    print(heading);
    MethodDocumentation method = doc.getGetDocumentation();
    print(method.getSummary());

    indent().print("<UL>");
    pushIndent();
    
    List<HttpHeaderInfo> requestHeaders = method.getRequestHeaders();
    Caption requestHeadersCaption = null;
    Caption queryParamsCaption = null;
    
    List<QueryParam> paramList = doc.getQueryParams();
    if (!paramList.isEmpty()) {
      queryParamsCaption = new Caption(CaptionType.Table, "Query Parameters", "queryParams", null);
      printer.assignNumber(queryParamsCaption);
      indent().print("<LI>The request may contain the query parameters specified in ");
      printLink(queryParamsCaption);
      println(".</LI>");
    }
    
    
    if (!requestHeaders.isEmpty()) {
      requestHeadersCaption = new Caption(CaptionType.Table, "Required HTTP Headers for GET Request", "getHeader", null);
      printer.assignNumber(requestHeadersCaption);
      indent().print("<LI>The request must contain the HTTP headers as specified in ");
      printer.printLink(requestHeadersCaption);
      println(".</LI>");
      
    }
    
    printer.printListItem(method.getRequestBodyRequirement());
    
    popIndent();
    indent().println("</UL>");
    
    if (!paramList.isEmpty()) {
      printer.printParagraph("&nbsp;");
      printQueryParams(paramList, queryParamsCaption);
      
    }
   
    printer.printParagraph("&nbsp;");
    printRequestHeaders(method, requestHeadersCaption);
    
    
    printer.beginParagraph();
    printer.printParagraph("&nbsp;");
    
    printResponse(method, "GET");
    
  }

  private void printPutMethod() {
    if (!doc.contains(HttpMethod.PUT)) return;
    Heading heading = printer.createHeading("PUT");
    print(heading);
    MethodDocumentation method = doc.getPutDocumentation();
    print(method.getSummary());

    indent().print("<UL>");
    pushIndent();
    
    List<HttpHeaderInfo> requestHeaders = method.getRequestHeaders();
    Caption requestHeadersCaption = null;
    
    List<String> ruleList = doc.getPutRules();
    if (!ruleList.isEmpty()) {
      for (String rule : ruleList) {
        indent().print("<LI>").print(rule).println("</LI>");
      }
    }
    
    
    if (!requestHeaders.isEmpty()) {
      requestHeadersCaption = new Caption(CaptionType.Table, "Required HTTP Headers for PUT Request", "getHeader", null);
      printer.assignNumber(requestHeadersCaption);
      indent().print("<LI>The request must contain the HTTP Headers listed in ");
      printLink(requestHeadersCaption);
      println(".</LI>");
      
    }
    
    printer.printListItem(method.getRequestBodyRequirement());
    
    popIndent();
    indent().println("</UL>");
   
    printer.printParagraph("&nbsp;");
    printRequestHeaders(method, requestHeadersCaption);
    
    
    printer.beginParagraph();
    printer.printParagraph("&nbsp;");
    

    String pattern = " describes the possible responses from the {0} method.  In all cases, the response body is empty.";
    printResponse(method, pattern, "PUT");
    
  }
  
  private void printDeleteMethod() {
    if (!doc.contains(HttpMethod.DELETE)) return;
    Heading heading = printer.createHeading("DELETE");
    print(heading);
    MethodDocumentation method = doc.getDeleteDocumentation();
    print(method.getSummary());

    indent().print("<UL>");
    pushIndent();
    
    List<HttpHeaderInfo> requestHeaders = method.getRequestHeaders();
    Caption requestHeadersCaption = null;
    
    if (!requestHeaders.isEmpty()) {
      requestHeadersCaption = new Caption(CaptionType.Table, "Required HTTP Headers for DELETE Request", "getHeader", null);
      printer.assignNumber(requestHeadersCaption);
      indent().print("<LI>The request must contain the HTTP Headers listed in ");
      printLink(requestHeadersCaption);
      println(".</LI>");
      
    }
    
    printer.printListItem(method.getRequestBodyRequirement());
    
    popIndent();
    indent().println("</UL>");
   
    printer.printParagraph("&nbsp;");
    printRequestHeaders(method, requestHeadersCaption);
    
    
    printer.beginParagraph();
    printer.printParagraph("&nbsp;");
    

    String pattern = " describes the possible responses from the {0} method.  In all cases, the response body is empty.";
    printResponse(method, pattern, "DELETE");
    
  }
  
  private void printResponse(MethodDocumentation method, String methodName) {

    String pattern = " describes the possible responses from the {0} method.";
    printResponse(method, pattern, methodName);
    
  }

  private void printResponse(MethodDocumentation method, String pattern, String methodName) {

    Caption responseCaption = new Caption(CaptionType.Table, 
        format("Possible responses from a {0} method", methodName), "postResponse", null);
    printer.assignNumber(responseCaption);
    printLink(responseCaption);
    print(format(pattern, methodName));
    printer.endParagraph();
    
    printer.beginTable("propertiesTable");
    printer.beginRow();
    printer.printTH("HTTP Status");
    printer.printTH("Description");
    printer.endRow();
    for (ResponseInfo code : method.getStatusCodes()) {
      printer.beginRow();
      printer.printTD(code.getStatusCode() + "&nbsp;" + code.getLabel().replace(" ", "&nbsp;"));
      printer.printTD(code.getDescription());
      printer.endRow();
    }
    printer.endTable();
    printer.printCaption(responseCaption);
    
  }


  private void printPostMethod() {
    if (!doc.contains(HttpMethod.POST)) return;
    
    Heading heading = printer.createHeading("POST");
    print(heading);
    MethodDocumentation method = doc.getPostDocumentation();
    print(method.getSummary());

    indent().print("<UL>");
    pushIndent();
    
    List<HttpHeaderInfo> requestHeaders = method.getRequestHeaders();
    Caption requestHeadersCaption = null;
    
    if (!requestHeaders.isEmpty()) {
      requestHeadersCaption = new Caption(CaptionType.Table, "Required HTTP Headers for POST Request", "postHeader", null);
      printer.assignNumber(requestHeadersCaption);
      indent().print("<LI>The request must contain the HTTP Headers listed in ");
      printLink(requestHeadersCaption);
      println(".</LI>");
      
    }
    
    printer.printListItem(method.getRequestBodyRequirement());
    
    popIndent();
    indent().println("</UL>");
   
    printer.printParagraph("&nbsp;");
    printRequestHeaders(method, requestHeadersCaption);
    
    printer.printParagraph("&nbsp;");
    printResponse(method, "POST");
    
    String rules = doc.getPostProcessingRules();
    if (rules != null) {
      print(rules);
    }
    
//    
//    Caption responseCaption = new Caption(CaptionType.Table, "Possible responses from a POST method", "postResponse", null);
//    assignNumber(responseCaption);
//
//    printParagraph("&nbsp;");
//    beginParagraph();
//    printLink(responseCaption);
//    print(" describe the possible responses from the POST method.");
//    endParagraph();
//    
//    beginTable("propertiesTable");
//    beginRow();
//    printTH("HTTP Status");
//    printTH("Description");
//    endRow();
//    for (ResponseInfo code : method.getStatusCodes()) {
//      beginRow();
//      printTD(code.getStatusCode() + " " + code.getLabel());
//      printTD(code.getDescription());
//      endRow();
//    }
//    endTable();
//    printCaption(responseCaption);
    
  }


  private void printQueryParams(List<QueryParam> paramList, Caption caption) {

    indent().print("<TABLE");
    printAttr("class", "propertiesTable");
    println(">");
    pushIndent();
    indent().println("<TR>");
    pushIndent();
    indent().println("<TH>Parameter</TH><TH>Description</TH>");
    popIndent();
    indent().println("</TR>");
    
    for (QueryParam param : paramList) {
      indent().println("<TR>");
      pushIndent();
      indent().print("<TD>");
      print(param.getName());
      print("</TD><TD>");
      print(param.getDescription());
      println("</TD>");        
      popIndent();
      indent().println("</TR>");
    }
    popIndent();
    indent().println("</TABLE>");
    printer.printCaption(caption);

    
  }
  
  
  private void printRequestHeaders(MethodDocumentation method, Caption caption) {

    List<HttpHeaderInfo> requestHeaders = method.getRequestHeaders();
    
    if (!requestHeaders.isEmpty()) {
      
      indent().print("<TABLE");
      printAttr("class", "propertiesTable");
      println(">");
      pushIndent();
      indent().println("<TR>");
      pushIndent();
      indent().println("<TH>Request Header Name</TH><TH>Value</TH>");
      popIndent();
      indent().println("</TR>");
      
      for (HttpHeaderInfo header : requestHeaders) {
        indent().println("<TR>");
        pushIndent();
        indent().print("<TD>");
        print(header.getHeaderName());
        print("</TD><TD>");
        print(header.getHeaderValue());
        println("</TD>");        
        popIndent();
        indent().println("</TR>");
      }
      popIndent();
      indent().println("</TABLE>");
      printer.printCaption(caption);
    }
  }

  private void printRepresentation() {
    Heading heading = printer.createHeading(doc.getRepresentationHeading());
    print(heading);
    print(doc.getRepresentationText());
    
    String htmlFormat = doc.getHtmlFormatDocumentation();
    if (htmlFormat != null) {
      print(htmlFormat);
    }
    
    
  }

  private void printIntroduction() {
    
    Heading heading = printer.createHeading("Introduction");
    print(heading);
    print(doc.getIntroduction());
    
  }

  private void printAbstract() {

    String abstractText = doc.getAbstactText();
    if (abstractText == null)
      return;

    indent().println("<H2>Abstract</H2>");
    indent().println("<DIV>");
    print(abstractText);
    println("</DIV>");
    
  }




}
