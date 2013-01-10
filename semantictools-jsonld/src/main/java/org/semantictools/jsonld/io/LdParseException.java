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
package org.semantictools.jsonld.io;

import org.codehaus.jackson.JsonLocation;
import org.codehaus.jackson.JsonParseException;

/**
 * An exception that occurs while parsing a JSON-LD stream.
 * @author Greg McFall
 *
 */
public class LdParseException extends Exception {
  private static final long serialVersionUID = 1L;

  
  public LdParseException(String message) {
    super(message);
  }
  
  public LdParseException(Throwable cause) {
    super(getMessage(cause));
  }
  
  private static String getMessage(Throwable cause) {
    if (cause instanceof JsonParseException) {
      StringBuilder builder = new StringBuilder();
      JsonParseException p = (JsonParseException) cause;
      String message = p.getMessage();
      int mark = message.indexOf('\n');
      if (mark > 0) {
        message = message.substring(0, mark);
      }
      builder.append(message);
      
      JsonLocation location = p.getLocation();
      if (location != null) {
        builder.append(" line: ");
        builder.append(location.getLineNr());
        builder.append(", column: ");
        builder.append(location.getColumnNr());
      }
      return builder.toString();
    }
    return cause.getMessage();
  }

  
  
}
