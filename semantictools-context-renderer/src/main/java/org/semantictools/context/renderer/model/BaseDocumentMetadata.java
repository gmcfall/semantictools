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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BaseDocumentMetadata implements DocumentMetadata {
  
  private File localFile;
  private String logo;
  private String date;
  private String documentLocation;
  private String footer;
  private String latestVersionURI;
  private String legalNotice;
  private String purpose;
  private String release;
  private String status;
  private String subtitle;
  private String templateName;
  private String title;
  private String version;
  private Boolean historyLink;
  private String css;
  protected List<Person> authorList;
  protected List<Person> cochairList;
  protected List<Person> editorList;
  protected ReferenceManager refManager;
  protected Boolean validateJsonSamples;
  
  private DocumentMetadata parent;
  
  public BaseDocumentMetadata() {}

  public BaseDocumentMetadata(DocumentMetadata parent) {
    this.parent = parent;
  }

  @Override
  public DocumentMetadata getParent() {
    return parent;
  }
  
  @Override
  public void setParent(DocumentMetadata parent) {
    this.parent = parent;
  }

  @Override
  public String getLogo() {
    return logo==null && parent!=null ? parent.getLogo() : logo;
  }

  @Override
  public void setLogo(String logo) {
    this.logo = logo;
  }

  @Override
  public String getTitle() {
    return title==null && parent!=null ? parent.getTitle() : title;
  }

  @Override
  public void setTitle(String title) {
    this.title = title;
  }

  @Override
  public String getSubtitle() {
    return subtitle==null && parent!=null ? parent.getSubtitle() : subtitle;
  }

  @Override
  public void setSubtitle(String subtitle) {
    this.subtitle = subtitle;
  }

  @Override
  public String getVersion() {
    return version==null && parent!=null ? parent.getVersion() : version;
  }

  @Override
  public void setVersion(String version) {
    this.version = version;
  }

  @Override
  public String getRelease() {
    return release == null && parent!=null ? parent.getRelease() : release;
  }

  @Override
  public void setRelease(String release) {
    this.release = release;
  }

  @Override
  public String getLatestVersionURI() {
    return latestVersionURI==null && parent!=null ? parent.getLatestVersionURI() : latestVersionURI;
  }

  @Override
  public void setLatestVersionURI(String value) {
    this.latestVersionURI = value;
  }

  @Override
  public String getStatus() {
    return status==null && parent!=null ? parent.getStatus() : status;
  }

  @Override
  public void setStatus(String status) {
    this.status = status;
  }

  @Override
  public String getDate() {
    return date==null && parent!=null ? parent.getDate() : date;
  }

  @Override
  public void setDate(String date) {
    this.date = date;
  }

  @Override
  public String getPurpose() {
    return purpose==null && parent!=null ? parent.getPurpose() : purpose;
  }

  @Override
  public void setPurpose(String purpose) {
    this.purpose = purpose;
  }

  @Override
  public String getDocumentLocation() {
    return documentLocation==null && parent!=null ? parent.getDocumentLocation() : documentLocation;
  }

  @Override
  public void setDocumentLocation(String location) {
    this.documentLocation = location;
  }

  @Override
  public String getLegalNotice() {
    return legalNotice==null && parent!=null ? parent.getLegalNotice() : legalNotice;
  }

  @Override
  public void setLegalNotice(String legalNotice) {
    this.legalNotice = legalNotice;
  }

  @Override
  public String getFooter() {
    return footer==null && parent!=null ? parent.getFooter() : footer;
  }

  @Override
  public void setFooter(String footer) {
    this.footer = footer;
  }

  @Override
  public void addAuthor(Person person) {
    if (authorList == null) {
      authorList = new ArrayList<Person>();
    }
    authorList.add(person);
  }

  @Override
  public List<Person> getAuthors() {
    return authorList==null && parent!=null ? parent.getAuthors() : authorList;
  }

  @Override
  public void addEditor(Person person) {
    if (editorList == null) {
      editorList = new ArrayList<Person>();
    }
    editorList.add(person);
  }

  @Override
  public List<Person> getEditors() {
    return editorList==null && parent!=null ? parent.getEditors() : editorList;
  }

  @Override
  public void addCoChair(Person person) {
    if (cochairList == null) {
      cochairList = new ArrayList<Person>();
    }
    cochairList.add(person);
  }

  @Override
  public List<Person> getCoChairs() {
    return cochairList==null && parent!=null ? parent.getCoChairs() : cochairList;
  }

//  @Override
//  public String getReference(String citationLabel) {
//    
//    String result = referenceMap.get(citationLabel);
//    if (result == null && parent != null) {
//      result = referenceMap.get(citationLabel);
//    }
//    return result;
//  }
//
//  @Override
//  public void putReference(String citationLabel, String reference) {
//    if (parent != null) {
//      parent.putReference(citationLabel, reference);
//    } else {
//      referenceMap.put(citationLabel, reference);
//    }
//
//  }
  
  

  @Override
  public ReferenceManager getReferenceManager() {
    return refManager==null && parent!=null ? parent.getReferenceManager() : refManager;
  }

  @Override
  public void setReferenceManager(ReferenceManager refManager) {
    this.refManager = refManager;
  }

  @Override
  public Boolean hasHistoryLink() {
    Boolean value = historyLink==null && parent!=null ? parent.hasHistoryLink() : historyLink;
    return value==null ? Boolean.FALSE : value;
  }
  
  @Override 
  public void setHistoryLink(Boolean truth) {
    historyLink = truth;
  }

  @Override
  public String getTemplateName() {
    return templateName==null && parent!=null ? parent.getTemplateName() : templateName;
  }
  
  @Override
  public void setTemplateName(String name) {
    templateName = name;
  }

  @Override
  public String getCss() {
    return css==null && parent!=null ? parent.getCss() : css;
  }

  @Override
  public void setCss(String css) {
    this.css = css;
  }

  @Override
  public void putReference(String label, String referenceText) {

    ReferenceManager manager = getReferenceManager();
    if (manager == null) return;
    
    BibliographicReference ref = BibliographicReference.parse(referenceText);
    ref.setLabel(label);
    
    manager.add(ref);
    
  }

  @Override
  public File getLocalFile() {
    return localFile;
  }

  @Override
  public void setLocalFile(File localFile) {
    this.localFile = localFile;
  }

  @Override
  public boolean getValidateJsonSamples() {
    if (validateJsonSamples == null && parent!=null) {
      return parent.getValidateJsonSamples();
    }
    return (validateJsonSamples == null) ? true : validateJsonSamples.booleanValue();
  }

  @Override
  public void setValidateJsonSamples(boolean truthValue) {
    validateJsonSamples = truthValue;
  }

}
