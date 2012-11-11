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

public class HttpMethod {

  public static final HttpMethod POST = new HttpMethod("POST", "creating");
  public static final HttpMethod GET = new HttpMethod("GET", "reading");
  public static final HttpMethod PUT = new HttpMethod("PUT", "updating");
  public static final HttpMethod DELETE = new HttpMethod("DELETE", "deleting");
  
  private String name;
  private String gerund;
  
  
  private HttpMethod(String name, String gerund) {
    this.name = name;
    this.gerund = gerund;
  }

  public static HttpMethod getByName(String name) {
    if ("POST".equals(name)) return POST;
    if ("GET".equals(name)) return GET;
    if ("PUT".equals(name)) return PUT;
    if ("DELETE".equals(name)) return DELETE;
    return null;
  }

  public String getName() {
    return name;
  }


  public String getGerund() {
    return gerund;
  }
  
  

}
