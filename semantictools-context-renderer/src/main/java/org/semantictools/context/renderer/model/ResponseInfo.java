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

public class ResponseInfo {
  public final static ResponseInfo OK = new ResponseInfo(200, "OK", "The request was successful.");
  public final static ResponseInfo ACCEPTED = new ResponseInfo(202, "Accepted", 
      "The request has been accepted for processing, but the processing has not been completed. " +
      "The request might or might not eventually be acted upon, as it might be disallowed when " +
      "processing actually takes place. There is no facility for re-sending a status code from " +
      "an asynchronous operation such as this.");
  
  public final static ResponseInfo MOVED_PERMANENTLY = new ResponseInfo(301, "Moved Permanently", 
      "The URI for the requested resource has changed.  <p>In this case, the response body is empty, " +
      "and the new URI is provided in the <code>Location</code> header field.  The client should " +
      "GET the resource from the new location, and furthermore, it should send all future requests " +
      "to the new location as well.</p>");
  
  public final static ResponseInfo TEMPORARY_REDIRECT = new ResponseInfo(307, "Temporary Redirect",
      "The requested resource resides temporarily under a different URI.  <p>In this case, the response " +
      "body is empty, and the temporary URI is defined by the <code>Location</code> header field. " +
      "The client should GET the resource at the temporary URI, but future requests should continue " +
      "to be sent to the original URI.</p>");
  
  public final static ResponseInfo NOT_FOUND = new ResponseInfo(404, "Not Found", 
      "The server has not found anything matching the request URI.");
  
  public final static ResponseInfo BAD_REQUEST = new ResponseInfo(400, "Bad Request",
      "The request could not be understood by the server due to malformed syntax. " +
      "The client SHOULD NOT repeat the request without modifications.");
  
  public final static ResponseInfo UNAUTHORIZED = new ResponseInfo(401, "Unauthorized",
      "The client did not authenticate properly.");
  public final static ResponseInfo INTERNAL_SERVER_ERROR = new ResponseInfo(500, "Internal Service Error", 
      "The server encountered an unexpected condition which prevented it from fulfilling the request.");
  
  public final static ResponseInfo NOT_ACCEPTABLE = new ResponseInfo(406, "Not Acceptable",
      "The requested resource is only capable of generating content not acceptable according to the Accept headers sent in the request.");
  
  private int code;
  private String label;
  private String description;
  
  public ResponseInfo(int code, String label, String description) {
    this.code = code;
    this.label = label;
    this.description = description;
  }

  public int getStatusCode() {
    return code;
  }

  public String getLabel() {
    return label;
  }

  public String getDescription() {
    return description;
  }
  
  public ResponseInfo copy(String description) {
    return new ResponseInfo(code, label, description);
  }
  
  
  
  

}
