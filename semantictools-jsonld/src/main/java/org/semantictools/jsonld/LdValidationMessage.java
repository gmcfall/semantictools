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
package org.semantictools.jsonld;

import java.util.regex.Matcher;
import java.util.regex.Pattern;



public class LdValidationMessage {

  private static final Pattern EXCEPTION_PATTERN = Pattern.compile("^\\w+(\\.\\w+)+:");
  
  private String path;
  private LdValidationResult result;
  private String text;
  
  public LdValidationMessage(
      LdValidationResult result,
      String path,
      String text) {
    this.path = path;
    this.result = result;
    this.text = normalizeMessage(text);
  }
  
  private String normalizeMessage(String message) {

    Matcher matcher = EXCEPTION_PATTERN.matcher(message);
    if (matcher.find()) {
      String match = matcher.group();
      if (match.endsWith("Exception:")) {
        int colon = message.indexOf(':');
        message = message.substring(colon+1).trim();
      }
    }
    return message;
  }
  
  public String getPath() {
    return path;
  }
  public LdValidationResult getResult() {
    return result;
  }
  public String getText() {
    return text;
  }
  
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append(result);
    int size = builder.length();
    for (int i=size; i<8; i++) {
      builder.append(' ');
    }
    builder.append(path);
    builder.append(' ');
    builder.append(text);
    
    return builder.toString();
  }

}
