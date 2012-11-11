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
package org.semantictools.index.model;



public class MediaTypeReference {
  
  private String mediaTypeName;
  private String mediaTypeURI;
  private String rdfTypeURI;
  
  
  
  public MediaTypeReference(String rdfTypeURI, String mediaTypeName, String mediaTypeURI) {
    this.mediaTypeName = mediaTypeName;
    this.mediaTypeURI = mediaTypeURI;
    this.rdfTypeURI = rdfTypeURI;
  }
  
  public String getMediaTypeName() {
    return mediaTypeName;
  }
  public void setMediaTypeName(String mediaTypeName) {
    this.mediaTypeName = mediaTypeName;
  }
  public String getMediaTypeURI() {
    return mediaTypeURI;
  }
  public void setMediaTypeURI(String mediaTypeURI) {
    this.mediaTypeURI = mediaTypeURI;
  }

  public String getRdfTypeURI() {
    return rdfTypeURI;
  }

  public void setRdfTypeURI(String rdfTypeURI) {
    this.rdfTypeURI = rdfTypeURI;
  }

  
  
  
}

