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
package org.semantictools.jsonld.io.impl;

import java.io.PrintWriter;

import org.semantictools.jsonld.LdValidationMessage;
import org.semantictools.jsonld.LdValidationReport;
import org.semantictools.jsonld.io.LdValidationReportWriter;

public class HtmlReportWriter implements LdValidationReportWriter {

  @Override
  public void writeReport(PrintWriter writer, LdValidationReport report) {
    HtmlWriter delegate = new HtmlWriter(writer);
    delegate.writeReport(report);
    writer.flush();

  }
  
  static class HtmlWriter {

    private int indentSize=2;
    private int indent;
    private PrintWriter writer;

    public HtmlWriter(PrintWriter out) {
      this.writer = out;
    }
    

    private HtmlWriter pushIndent() {
      indent++;
      return this;
    }
    
    private HtmlWriter popIndent() {
      indent--;
      return this;
    }
    
    private HtmlWriter indent() {
      for (int i=0; i<indent*indentSize; i++) {
        writer.print(' ');
      }
      return this;
    }
    
    private HtmlWriter print(String text) {
      writer.print(text);
      return this;
    }
    
    private HtmlWriter println(String text) {
      writer.println(text);
      return this;
    }
    
    private HtmlWriter println() {
      writer.println();
      return this;
    }
    
    public void writeReport(LdValidationReport report) {
      beginHtml();
      println("<head>");
      printStyle();
      
      println("</head>");
      println("<body>");
      println("<H1>JSON-LD Validation Report</H1>");
      pushIndent();
      if (report.listMessages().isEmpty()) {
        printOk();
      } else {
        printTable(report);
      }
      popIndent();      
      println("</body>");
      
      endHtml();
    }
    
    private void printOk() {
      indent().println("<div class=\"ok\">OK!</div>");
      indent().println("<div>No errors or warnings detected.</div>");
      
    }


    private void printTable(LdValidationReport report) {

      openTag("<table>");
      openTag("<tr>");
      indent().println("<th>Severity</th><th>Path</th><th>Description</th>");
      endTag("</tr>");
      for (LdValidationMessage message : report.listMessages()) {
        printMessage(message);
      }
      
      endTag("</table>");
    }
    
    public static void main(String[] args) {
      LdValidationReport report = new LdValidationReport();
      HtmlReportWriter writer = new HtmlReportWriter();
      PrintWriter out = new PrintWriter(System.out);
      writer.writeReport(out, report);
      out.flush();
    }


    private void printStyle() {

      openTag("<style>");
      println("BODY {");
      pushIndent();
      indent().println("font-family: sans-serif;");
      indent().println("padding-left: 3em;");      
      popIndent();
      println("}");
      println("H1 {");
      pushIndent();
      indent().println("color: #336699;");      
      popIndent();
      println("}");
      println("TABLE {");
      pushIndent();
      indent().println("border-collapse: collapse;");
      indent().println("border-spacing: 0px;");
      popIndent();
      println("}");
      println("TH {");
      pushIndent();
      indent().println("border-collapse: collapse;");
      indent().println("border: 1px solid #336699;");
      indent().println("padding: 5px;");
      indent().println("background-color: #336699;");
      indent().println("color: white;");
      indent().println("font-weight: bold;");
      popIndent();
      println("}");
      println("TD {");
      pushIndent();
      indent().println("border-collapse: collapse;");
      indent().println("border: 1px solid #336699;");
      indent().println("padding: 5px;");
      popIndent();
      println("}");
      println(".ok {");
      pushIndent();
      indent().println("font-weight: bold;");
      indent().println("margin-bottom: 0.5em;");
      popIndent();
      println("}");
     
      endTag("</style>");
      
    }


    private void printMessage(LdValidationMessage message) {
      String text = message.getText().replace("\n", "<br>\n");
      openTag("<tr>");
      indent().print("<td>").print(message.getResult().name()).println("</td>");
      indent().print("<td>").print(message.getPath()).println("</td>");
      indent().print("<td>").print(text).println("</td>");
      endTag("</tr>");
      
    }


    private void endTag(String text) {
      popIndent();
      indent().println(text);
      
    }


    private void openTag(String text) {
      indent().println(text);
      pushIndent();
      
    }


    private void closeEmptyTag() {
      println("/>");
      
    }


    private void closeTag() {
      println(">");
      
    }


    private HtmlWriter attr(String name, String value) {
      print(" ");
      print(name);
      print("=");
      print("\"");
      print(value);
      print("\"");
      return this;
    }


    private HtmlWriter beginTag(String tag) {
      indent().print("<");
      print(tag);
      return this;
    }


    private void endHtml() {
      println("</html>");
      
    }


    private void beginHtml() {
      println("<html>");
      
    }
    
  }

}
