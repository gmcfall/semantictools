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
package org.semantictools.web.upload;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.security.NoSuchAlgorithmException;
import java.util.Properties;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.semantictools.context.renderer.model.ContextProperties;
import org.semantictools.frame.api.LinkManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A client that uploads files associated with a media type to 
 * semantic-tools.appspot.com
 * 
 * @author Greg McFall
 *
 */
public class AppspotUploadClient {
  private static final Logger logger = LoggerFactory.getLogger(AppspotUploadClient.class);
  
    private static final String SERVLET_URL = "http://semantic-tools.appspot.com/admin/upload.do";
//  private static final String SERVLET_URL = "http://127.0.0.1:8888/admin/upload.do";
  private static final String CONTENT_TYPE = "contentType";
  private static final String PATH = "path";
  private static final String VERSION = "version";
  private static final String FILE_UPLOAD = "fileUpload";
  private static final String CHECKSUM_PROPERTIES = "checksum.properties";
  
  private String servletURL = SERVLET_URL;
  private String version;
  
  private Properties checksumProperties;
  
    
  public void uploadAll(File baseDir) throws IOException {
    LinkManager linkManager = new LinkManager(baseDir);
    loadCheckSumProperties(baseDir);
    uploadFiles(linkManager, baseDir);
    saveCheckSumProperties(baseDir);
    
  }
  
  
  private void saveCheckSumProperties(File baseDir) throws IOException {

    File file = new File(baseDir, CHECKSUM_PROPERTIES);
    FileOutputStream out = new FileOutputStream(file);
    checksumProperties.store(out, null);
    checksumProperties = null;
  }


  private void loadCheckSumProperties(File baseDir) throws IOException {
    checksumProperties = new Properties();
    
    File file = new File(baseDir, CHECKSUM_PROPERTIES);
    if (file.exists()) {
      FileReader reader = new FileReader(file);
      try {
        checksumProperties.load(reader);
      } finally {
        reader.close();
      }
    }
    
  }


  public String getVersion() {
    return version;
  }

  public void setVersion(String version) {
    this.version = version;
  }

  public String getEndpointURL() {
    return servletURL;
  }

  public void setEndpointURL(String endpointURL) {
    this.servletURL = endpointURL;
  }




  private void uploadFiles(LinkManager linkManager, File dir) throws IOException {
    File[] array = dir.listFiles();
    for (File file : array) {
      if (file.isDirectory()) {
        uploadFiles(linkManager, file);
      } else {
        String fileName = file.getName();
        int dot = fileName.lastIndexOf('.');
        if (dot < 0) continue;
        String suffix = fileName.substring(dot+1);
        String contentType = getContentType(suffix);
        if (contentType == null) continue;
        
        String path = linkManager.relativize(file);
        upload(contentType, path, file);
      }
    }
    
  }
  
  private String getContentType(String suffix) {
    return 
      "png".equals(suffix) ? "image/png" :
      "html".equals(suffix) ? "text/html" :
      "css".equals(suffix) ? "text/css" :
      "json".equals(suffix) ? "application/json" :
      null;
  }

  public void upload(File baseDir, ContextProperties contextProperties) throws IOException {
    
    
//    String domain = contextProperties.getPurlDomain();
//    if (domain == null) return;
//    if (!domain.endsWith("/")) {
//      domain = domain + "/";
//    }
//    if (!domain.startsWith("/")) {
//      domain = "/" + domain;
//    }
    

    String mediaType = contextProperties.getMediaType();
//    String path = domain + "mediatype/" + mediaType.replace('.', '/') + "/";
    String path = "mediatype/" + mediaType.replace('.', '/') + "/";
    
    upload(path, baseDir, contextProperties);
  }

  private void upload(String path, File dir,  ContextProperties contextProperties) throws IOException {
    
    File[] fileList = dir.listFiles();
    if (fileList == null) return;
    
    for (int i=0; i<fileList.length; i++) {
      File file = fileList[i];

      String fileName = file.getName();
      if (file.isDirectory()) {
        upload(path + fileName + "/" , file, contextProperties);
        continue;
      }
      int dot = fileName.lastIndexOf('.');
      String uri = path + fileName;
      if (dot < 0) {
        logger.debug("Skipping file because it has no suffix: " + uri);
        continue;
      }
      
      String suffix = fileName.substring(dot+1);
      
      String mediaType = MimeTypes.getMediaType(suffix);
      if (mediaType == null) {
        logger.debug("Skipping file because the media type is not known: " + uri);
        continue;
      }
      
      upload(mediaType, uri, file);
    }
    
  }

  public void upload(String contentType, String path, File file) throws IOException {
    
    if (file.getName().equals(CHECKSUM_PROPERTIES)) {
      return;
    }
   
    if (!path.startsWith("/")) {
      path = "/" + path;
    }
    
    // Do not upload if we can confirm that we previously uploaded
    // the same content.

    String checksumKey = path.concat(".sha1");
    String checksumValue = null;
    try {
      checksumValue = Checksum.sha1(file);
      String prior = checksumProperties.getProperty(checksumKey);
      if (checksumValue.equals(prior)) {
        return;
      }
      
    } catch (NoSuchAlgorithmException e) {
      // Ignore.
    }
    

    logger.debug("uploading... " + path);
    
    HttpClient client = new DefaultHttpClient();
    HttpPost post = new HttpPost(servletURL);
    post.setHeader("CONTENT-TYPE", "multipart/form-data; boundary=xxxBOUNDARYxxx");
    MultipartEntity entity = new MultipartEntity(
        HttpMultipartMode.BROWSER_COMPATIBLE, "xxxBOUNDARYxxx", Charset.forName("UTF-8"));
    
    FileBody body = new FileBody(file, contentType);
    
    entity.addPart(CONTENT_TYPE, new StringBody(contentType));
    entity.addPart(PATH, new StringBody(path));
    if (version != null) {
      entity.addPart(VERSION, new StringBody(version));
    }
    entity.addPart(FILE_UPLOAD, body);
    
    post.setEntity(entity);
    
    String response = EntityUtils.toString(client.execute(post).getEntity(), "UTF-8");
    
    client.getConnectionManager().shutdown();
    
    if (checksumValue != null) {
      checksumProperties.put(checksumKey, checksumValue);
    }
    
  }
}
