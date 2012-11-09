package org.semantictools.context.renderer.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class GlobalProperties  implements DocumentMetadata {
  
  private Set<String> ignoredOntology = new HashSet<String>();
  private String logo;
  private String version;
  private String release;
  private String title;
  private String subtitle;
  private String purpose;
  private String documentLocation;
  private String template;
  private String latestVersionURI;
  private String legalNotice;
  private String status;
  private String date;
  private String footer;
  private List<Person> coChairs;
  private List<Person> editorList;
  private List<Person> authorList;
  
  public void addIgnoredOntology(String ontologyURI) {
    ignoredOntology.add(ontologyURI);
  }
  
  public boolean isIgnoredOntology(String ontologyURI) {
    return ignoredOntology.contains(ontologyURI);
  }

  /**
   * Returns the URI for a logo that should be displayed on specification pages.
   */
  public String getLogo() {
    return logo;
  }

  /**
   * Sets the URI for a logo that should be displayed on specification pages.
   */
  public void setLogo(String logo) {
    this.logo = logo;
  }

  /**
   * Returns a version identifier for the specification documents.
   */
  public String getVersion() {
    return version;
  }

  /**
   * Sets a version identifier for the specification documents.
   */
  public void setVersion(String version) {
    this.version = version;
  }


  public String getSubtitle() {
    return subtitle;
  }

  public void setSubtitle(String subtitle) {
    this.subtitle = subtitle;
  }

  /**
   * Returns the name of the template that should be used to render the 
   * specification documents.
   */
  public String getTemplateName() {
    return template;
  }

  /**
   * Sets the name of the template that should be used to render the 
   * specification documents.
   */
  public void setTemplateName(String template) {
    this.template = template;
  }

  @Override
  public String getTitle() {
    return title;
  }

  @Override
  public void setTitle(String title) {
    this.title = title;
  }

  @Override
  public String getStatus() {
    return status;
  }
  
  public void setStatus(String status) {
    this.status = status;
  }

  @Override
  public String getDate() {
    return date;
  }
  
  public void setDate(String date) {
    this.date = date;
  }

  @Override
  public String getLegalNotice() {
    return legalNotice;
  }
  
  public void setLegalNotice(String notice) {
    legalNotice = notice;
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
    return authorList;
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
    return editorList;
  }

  @Override
  public String getLatestVersionURI() {
    return latestVersionURI;
  }
  
  public void setLatestVersionURI(String value) {
    latestVersionURI = value;
  }

  @Override
  public Boolean hasHistoryLink() {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public String getReference(String citationLabel) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public List<Person> getCoChairs() {
    return coChairs;
  }
  

  @Override
  public void addCoChair(Person person) {
    if (coChairs == null) {
      coChairs = new ArrayList<Person>();
    }
    coChairs.add(person);
    
  }

  @Override
  public String getRelease() {
    return release;
  }

  public void setRelease(String release) {
    this.release = release;
  }


  @Override
  public String getPurpose() {
    return purpose;
  }

  public void setPurpose(String purpose) {
    this.purpose = purpose;
  }

  @Override
  public String getDocumentLocation() {
    return documentLocation;
  }

  public void setDocumentLocation(String documentLocation) {
    this.documentLocation = documentLocation;
  }

  @Override
  public String getFooter() {
    return footer;
  }

  public void setFooter(String footer) {
    this.footer = footer;
  }
  
  


}
