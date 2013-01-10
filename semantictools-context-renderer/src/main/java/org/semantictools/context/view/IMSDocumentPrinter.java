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

import java.util.List;

import org.semantictools.context.renderer.model.Person;

public class IMSDocumentPrinter extends DefaultDocumentPrinter {

  public IMSDocumentPrinter(PrintContext context) {
    super(context);
  }
  
  @Override
  protected void printTitlePageEditors() {}
  
  @Override
  protected void printTitlePageAuthors() {}
  

  public void printFooter() {
    
    println("<hr/>");
    Heading heading = createHeading("About this Document");
    heading.setShowNumber(false);
    beginSection(heading);
    
    String title = metadata.getTitle().replace("<br>", " ").replace("<br/>", " ");
    
    indent().println("<table class=\"about\">");
    pushIndent();
    indent().println("<tr>");
    pushIndent().print("<th>").print("Title: ").print("</td><td>").print(title).println("</td>");
    indent().println("</tr>");
    printCoChairs();
    printEditors();
    printVersion();
    printVersionDate();
    printRelease();
    printAboutStatus();
    printPurpose();
    printDocumentLocation();
    popIndent();
    indent().println("</table>");
    
    printContributors();
    super.printFooter();
    endSection();
  }

  private void printContributors() {
    
    List<Person> list = metadata.getAuthors();
    if (list==null || list.isEmpty()) return;
    
    Heading heading = createHeading("List of Contributors");
    heading.setInToc(false);
    heading.setShowNumber(false);
    beginSection(heading);
    println("<p>The following list of individuals contributed to the authoring of this document:</p>");
    println("<table class=\"about\">");
    pushIndent();
    int max = list.size()/2 + list.size()%2;
    for (int i=0; i<max; i++) {
      int j = max + i;
      Person left = list.get(i);
      Person right = (j<list.size()) ? list.get(j) : null;
      
      indent().println("<tr>");
      pushIndent();
      indent();
      printPersonInTable(left);
      printPersonInTable(right);
      println();
      popIndent();
      indent().println("</tr>");
      
    }
    popIndent();
    println("</table>");
    endSection();
    
  }

  private void printPersonInTable(Person person) {
    
   print("<td>");
   if (person != null) {
     print(person.getPersonName());
   }
   print("</td><td>");
   if (person != null && person.getOrgName()!=null) {
     print(person.getOrgName());
   }
   print("</td>");
    
  }

  private void printDocumentLocation() {
    String location = metadata.getDocumentLocation();
    if (location == null) return;

    indent().println("<tr>");
    pushIndent().print("<th>").print("Document Location: ").print("</td><td>").print(location).println("</td>");
    indent().println("</tr>");
    
  }
  private void printPurpose() {
    String purpose = metadata.getPurpose();
    if (purpose == null) return;

    indent().println("<tr>");
    pushIndent().print("<th>").print("Purpose: ").print("</td><td>").print(purpose).println("</td>");
    indent().println("</tr>");
    
  }

  private void printAboutStatus() {
    String status = metadata.getStatus();
    if (status == null) return;

    indent().println("<tr>");
    pushIndent().print("<th>").print("Status: ").print("</td><td>").print(status).println("</td>");
    indent().println("</tr>");
    
  }

  private void printRelease() {
    String release = metadata.getRelease();
    if (release == null) return;

    indent().println("<tr>");
    pushIndent().print("<th>").print("Release: ").print("</td><td>").print(release).println("</td>");
    indent().println("</tr>");
  }

  private void printVersion() {
    String version = metadata.getVersion();
    if (version == null) return;

    indent().println("<tr>");
    pushIndent().print("<th>").print("Version: ").print("</td><td>").print(version).println("</td>");
    indent().println("</tr>");
  }
  
  private void printVersionDate() {
    String date = metadata.getDate();
    if (date == null) return;

    indent().println("<tr>");
    pushIndent().print("<th>").print("Version Date: ").print("</td><td>").print(date).println("</td>");
    indent().println("</tr>");
  }

  public void printEditors() {
    List<Person> list = metadata.getEditors();
    if (list == null || list.isEmpty()) return;
    
    String label = (list.size()>1) ? "Editors: " : "Editor:";

    indent().println("<tr>");
    pushIndent().print("<th>").print(label).print("</td><td>");
    String comma = "";
    for (Person person : list) {
      print(comma);
      comma = ", ";
      print(person.getPersonName());
      String orgName = person.getOrgName();
      if (orgName != null) {
        print(" (").print(orgName).print(")");
      }
    }
    
    println("</td>");
    indent().println("</tr>");
  }

  private void printCoChairs() {
    List<Person> list = metadata.getCoChairs();
    if (list==null || list.isEmpty()) return;

    indent().println("<tr>");
    pushIndent().print("<th>").print("Co-chairs: ").print("</td><td>");
    String comma = "";
    for (Person person : list) {
      print(comma);
      comma = ", ";
      print(person.getPersonName());
      String orgName = person.getOrgName();
      if (orgName != null) {
        print(" (").print(orgName).print(")");
      }
    }
    
    println("</td>");
    indent().println("</tr>");
    
  }
}
