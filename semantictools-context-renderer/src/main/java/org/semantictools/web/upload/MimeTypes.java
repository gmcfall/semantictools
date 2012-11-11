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

import java.util.HashMap;
import java.util.Map;

public class MimeTypes {
  
  private static Map<String, String> suffix2MediaType = new HashMap<String, String>();
  static {
    suffix2MediaType.put("json", "application/json");
    suffix2MediaType.put("html", "text/html");
    suffix2MediaType.put("png", "image/png");
  }

  
  public static String getMediaType(String suffix) {
    return suffix2MediaType.get(suffix);
  }

}
