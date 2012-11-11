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
package org.semantictools.context.renderer;

import java.io.File;

import org.semantictools.context.renderer.model.JsonContext;
import org.semantictools.frame.model.Frame;

import com.ibm.icu.util.StringTokenizer;

/**
 * Specifies the paths to the various artifacts included in the documentation of
 * a media type.
 * @author Greg McFall
 *
 */
public class MediaTypeFileManager {
  
  private File mediaTypeDir;
  private File baseDir;
  
    
  public MediaTypeFileManager(File baseDir, File mediaTypeDir) {
    this.baseDir = baseDir;
    this.mediaTypeDir = mediaTypeDir;
  }
  
  public File getIndexFile(String mediaType) {
    String path = pathToMediaTypeDir(mediaType) + "/" + getIndexFileName();
    return new File(mediaTypeDir, path);
  }
  
  
  /**
   * Returns the path to the media type directory, relative to the root directory
   * passed to the constructor of this MediaTypeFileManager.
   */
  public String pathToMediaTypeDir(String mediaType) {
    return mediaType.replace('.', '/');
  }
  
  /**
   * From the directory for a media type, get the path to the base "mediatype" directory.
   * @param mediaType
   */
  public String pathToBaseDir(String mediaType) {
    StringBuilder builder = new StringBuilder();
    StringTokenizer tokens = new StringTokenizer(mediaType, ".");
    builder.append("..");
    while (tokens.hasMoreTokens()) {
      tokens.nextToken();
      builder.append("/..");
    }
    
    return builder.toString();
  }
  
  public String toRelativeURL(String url, String baseURL, String mediaType) {
    if (url==null || baseURL==null || !url.startsWith(baseURL)) return url;
    String result = pathToBaseDir(mediaType) + url.substring(baseURL.length());
    return result;
  }
  
  public String getStyleSheetFileName() {
    return "mediaType.css";
  }

  public String pathToStyleSheet(String mediaType) {
    String mediaTypeDir = pathToMediaTypeDir(mediaType);
    String[] array = mediaTypeDir.split("/");
    StringBuilder builder = new StringBuilder();
    for (int i=0; i<array.length; i++) {
      builder.append("../");
    }
    builder.append(getStyleSheetFileName());
    
    return builder.toString();
  }
  
  public String getIndexFileName() {
    return "index.html";
  }
  
  public String getJsonContextFileName(JsonContext context) {
    
    return  "context.json";
  }
  
  public File getMediaTypeDocumentationFile(String mediaType) {

    File parentDir = new File(baseDir, pathToMediaTypeDir(mediaType));
    parentDir.mkdirs();
    
    return new File(parentDir, getIndexFileName());
  }
  
  public String getServiceDocumentationFileName() {
    return "service.html";
  }
  
  public String getJsonSampleFileName() {
    return "sample.json";
  }
  
  public String getOverviewDiagramPath() {
    return "images/overview.png";
  }
  
  public String getClassDiagramPath(Frame frame) {
    return "images/" + frame.getLocalName() + ".png";
  }
  
  public String getImagesDir() {
    return "images";
  }

}
