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
package org.semantictools.context.renderer.model;

import java.io.File;
import java.util.List;

public interface DocumentMetadata {

  DocumentMetadata getParent();
  void setParent(DocumentMetadata parent);
  
  File getLocalFile();
  void setLocalFile(File file);
  
  String getLogo();
  void setLogo(String logo);
  
  String getTitle();
  void setTitle(String title);
  
  String getSubtitle();
  void setSubtitle(String subtitle);
  
  String getVersion();
  void setVersion(String version);
  
  String getRelease();
  void setRelease(String release);
  
  String getLatestVersionURI();
  void setLatestVersionURI(String uri);
  
  String getStatus();
  void setStatus(String status);
  
  String getDate();
  void setDate(String date);
  
  String getPurpose();
  void setPurpose(String purpose);
  
  String getDocumentLocation();
  void setDocumentLocation(String location);
  
  String getLegalNotice();
  void setLegalNotice(String legalNotice);
  
  String getFooter();
  void setFooter(String footer);
  
  void addAuthor(Person person);
  List<Person> getAuthors();
  
  void addEditor(Person person);
  List<Person> getEditors();
  
  void addCoChair(Person person);
  List<Person> getCoChairs();
  
//  String getReference(String citationLabel);
//  void putReference(String citationLabel, String reference);
  
  Boolean hasHistoryLink();
  void setHistoryLink(Boolean value);
  
  String getTemplateName();
  void setTemplateName(String name);
  
  String getCss();
  void setCss(String css);
  
  ReferenceManager getReferenceManager();
  void setReferenceManager(ReferenceManager manager);
  void putReference(String label, String referenceText);
  
  boolean getValidateJsonSamples();
  void setValidateJsonSamples(boolean truthValue);
}
