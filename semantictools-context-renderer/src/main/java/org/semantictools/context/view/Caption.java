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


public class Caption {
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
