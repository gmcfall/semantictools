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

public class BibliographicReference implements Comparable<BibliographicReference> {
  private String label;
  private String author;
  private String title;
  private String edition;
  private String date;
  private String uri;
  private String text;
  private File localFile;
  
  public static BibliographicReference parse(String text) {
    BibliographicReference result = new BibliographicReference();
    String[] array = text.split("\\|");
    if (array.length == 5) {
      result.setAuthor(array[0].trim());
      result.setTitle(array[1].trim());
      result.setEdition(array[2].trim());
      result.setDate(array[3].trim());
      result.setUri(array[4].trim());
      
    } else {
      result.setText(text.replace("|", "."));
    }
    return result;
  }
  
  /**
   * Returns the label in html form, where spaces are replaced with "&amp;nbsp;"
   */
  public String htmlLabel() {
    return label.replace(" ", "&nbsp;");
  }

  /**
   * Returns the label in text form, where "&amp;nbsp;" is replaced with a space character.
   */
  public String textLabel() {
    return label.replace("&nbsp;", " ");
  }
  
  private String toId(String tag) {
    tag = tag.replace("&nbsp;", "_");
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
  
  /**
   * Returns an id for this BibliographicReference.  This value is suitable for use
   * in an anchor tag as the target for linking to the reference in the bibliography.
   */
  public String getId() {
    return toId(label);
  }
  
  public String getAuthor() {
    return author;
  }
  public void setAuthor(String author) {
    this.author = author;
  }
  public String getTitle() {
    return title;
  }
  public void setTitle(String title) {
    this.title = title;
  }
  public String getEdition() {
    return edition;
  }
  public void setEdition(String edition) {
    this.edition = edition;
  }
  
  public String getDate() {
    return date;
  }

  public void setDate(String date) {
    this.date = date;
  }

  public String getUri() {
    return uri;
  }
  public void setUri(String uri) {
    this.uri = uri;
  }
  
  public String htmlText() {
    if (text != null) return text.replace("|", ". ");
    
    StringBuilder builder = new StringBuilder();
    String dot = "";
    if (author != null) {
      builder.append(author);
      dot = ". ";
    }
    if (title != null) {
      builder.append(dot);
      if (uri != null) {
        builder.append("<a href=\"");
        builder.append(uri);
        builder.append("\"><em>");
      }
      builder.append(title);
      if (uri != null) {
        builder.append("</em></a>");
      }
      dot = ". ";
    }
    if (edition != null) {
      builder.append(dot);
      builder.append(edition);
      dot = ". ";
    }
    if (date != null) {
      builder.append(dot);
      builder.append(date);
      dot = ". ";
    }
    builder.append(".");
    
    
    String result = builder.toString();
    result = result.replace("<br>", " ");
    return result;
  }

  public String getText() {
    return text;
  }

  public void setText(String text) {
    this.text = text;
  }
  
  public String toString() {
    if (text != null) {
      return text;
    }
    StringBuilder builder = new StringBuilder();
    String dot = "";
    if (author != null) {
      builder.append(author);
      dot = ". ";
    }
    if (title != null) {
      builder.append(dot);
      builder.append(title);
      dot = ". ";
    }
    if (edition != null) {
      builder.append(dot);
      builder.append(edition);
      dot = ". ";
    }
    if (date != null) {
      builder.append(dot);
      builder.append(date);
      dot = ". ";
    }
    if (uri != null) {
      builder.append(dot);
      builder.append(uri);
    }
    
    return builder.toString();
  }

  public String getLabel() {
    return label;
  }

  public void setLabel(String label) {
    this.label = label;
  }

  public void setAuthor(List<Person> list) {
    StringBuilder buffer = new StringBuilder();
    String comma = "";
    for (Person p : list) {
      buffer.append(comma);
      buffer.append(p.getPersonName());
      comma = ", ";
    }
    
    author = buffer.toString();
    
  }

  public File getLocalFile() {
    return localFile;
  }

  public void setLocalFile(File localFile) {
    this.localFile = localFile;
  }

  @Override
  public int compareTo(BibliographicReference other) {
    
    if (other == null) return -1;
    return compare(label, other.label);
  }
  
  private int compare(String a, String b) {
    return 
        (a==null && b==null) ? 0 : 
        (a!=null && b!=null) ? a.compareTo(b) :
        (a!=null && b==null) ? -1 :
        1;
  }
  
  
  
  
  
  
  
  

}
