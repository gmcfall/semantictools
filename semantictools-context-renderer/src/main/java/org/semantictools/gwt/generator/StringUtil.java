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
package org.semantictools.gwt.generator;

public class StringUtil {

  public static final String capitalize(String text) {
    char first = text.charAt(0);
    if (Character.isUpperCase(first)) {
      return text;
    }
    first = Character.toUpperCase(first);
    StringBuilder builder = new StringBuilder();
    builder.append(first);
    builder.append(text.substring(1));
    return builder.toString();
  }
  
  public static final String getter(String fieldName) {
    StringBuilder builder = new StringBuilder();
    builder.append("get");
    builder.append(capitalize(fieldName));
    return builder.toString();
  }
  
  public static final String setter(String fieldName) {

    StringBuilder builder = new StringBuilder();
    builder.append("set");
    builder.append(capitalize(fieldName));
    return builder.toString();
  }
}
