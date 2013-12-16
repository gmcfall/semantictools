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

import org.semantictools.context.renderer.model.DocumentMetadata;

public interface DocumentPrinter {

  public static final String TEMPLATE_SIMPLE = "simple";
  public static final String TEMPLATE_IMS = "IMS";
  
  
  void clear();
  
  PrintContext getPrintContext();
  void setPrintContext(PrintContext context);
  void setMetadata(DocumentMetadata metadata);
  void setClassificationPrinter(ClassificationPrinter value);
  
  void printTitlePage();
  void printTableOfContentsMarker();
  void printReferences();
  void printReferences(HeadingPrinter headingPrinter);
  void printFooter();
  
  Heading createHeading(String heading);
  Heading createHeading(String text, String id);
  Heading createHeading(Level level, String text, String id);
  
  Heading getCurrentHeading();
  void print(Heading heading);
  void beginSection(Heading heading);
  void endSection();
  void printLink(Caption caption);
  void printForwardRef(Caption caption);
  void printFigure(String src, Caption caption);
  void assignNumber(Caption caption);
  void beginTable(String className);
  void endTable();
  void beginRow();
  void endRow();
  void printTH(String value);
  void printTD(String className, String text);
  void printTD(String value);
  void printAnchor(String href, String text);
  void beginDiv(String className);
  void beginDiv(String className, String id);
  void endDiv();
  void beginCodeSnippet();
  void endCodeSnippet();
  void printDefinition(String termName, String description);
  void printParagraph(String text);
  void beginParagraph();
  void endParagraph();
  void printListItem(String text);
  void printCaption(Caption caption);
  void beginHTML();
  void endHTML();
  
  String getText();
  String popText();
  void insertTableOfContents();

}
