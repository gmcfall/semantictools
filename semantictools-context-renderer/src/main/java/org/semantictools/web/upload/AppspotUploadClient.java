package org.semantictools.web.upload;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

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
  
  private String servletURL = SERVLET_URL;
  private String version;
  
    
  public void uploadAll(File baseDir) throws IOException {
    LinkManager linkManager = new LinkManager(baseDir);
    uploadFiles(linkManager, baseDir);
    
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

  public String upload(String contentType, String path, File file) throws IOException {
   
    if (!path.startsWith("/")) {
      path = "/" + path;
    }
    
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
    
    logger.debug("uploading... " + path);
    String response = EntityUtils.toString(client.execute(post).getEntity(), "UTF-8");
    
    client.getConnectionManager().shutdown();
    return response;
  }
}
