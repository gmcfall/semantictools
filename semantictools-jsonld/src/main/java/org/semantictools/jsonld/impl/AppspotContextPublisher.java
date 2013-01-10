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
package org.semantictools.jsonld.impl;

import java.net.HttpURLConnection;
import java.nio.charset.Charset;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.semantictools.jsonld.LdPublishException;
import org.semantictools.jsonld.LdAsset;
import org.semantictools.jsonld.LdPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * A utility that publishes to the semantic-tools application at appspot.com.
 * @author Greg McFall
 *
 */
public class AppspotContextPublisher implements LdPublisher {
  private static final Logger logger = LoggerFactory.getLogger(AppspotContextPublisher.class);
  
  private static final String SERVLET_URL = "http://semantic-tools.appspot.com/admin/uploadContext.do";
  //private static final String SERVLET_URL = "http://127.0.0.1:8888//admin/uploadContext.do";
  private static final String URI = "uri";
  private static final String FILE_UPLOAD = "fileUpload";

  
  @Override
  public void publish(LdAsset asset)  throws LdPublishException {

    String uri = asset.getURI();
    
    HttpClient client = new DefaultHttpClient();
    HttpPost post = new HttpPost(SERVLET_URL);
    post.setHeader("CONTENT-TYPE", "multipart/form-data; boundary=xxxBOUNDARYxxx");
    MultipartEntity entity = new MultipartEntity(
        HttpMultipartMode.BROWSER_COMPATIBLE, "xxxBOUNDARYxxx", Charset.forName("UTF-8"));
    
    try {

      String content = asset.loadContent();
      
      entity.addPart(URI, new StringBody(uri));
      entity.addPart(FILE_UPLOAD, new StringBody(content));
      post.setEntity(entity);
      
      logger.debug("uploading... " + uri);

      HttpResponse response = client.execute(post);
      int status = response.getStatusLine().getStatusCode();

      client.getConnectionManager().shutdown();
      if (status != HttpURLConnection.HTTP_OK) {
        throw new LdPublishException(uri, null);
      }
      
      
    } catch (Exception e) {
      throw new LdPublishException(uri, e);
    }
    
  }


  

}
