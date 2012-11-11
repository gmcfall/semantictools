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
package org.semantictools.frame.api;

import java.util.HashSet;
import java.util.Set;

import org.semantictools.frame.model.Datatype;

public class DatatypeUtil {
  private static final Set<String> doubleType = new HashSet<String>();
  private static final Set<String> integer = new HashSet<String>();
  private static final Set<String> shortType = new HashSet<String>();
  private static final Set<String> byteType = new HashSet<String>();
  private static final Set<String> longType = new HashSet<String>();
 
  
  static {
    doubleType.add("http://www.w3.org/2001/XMLSchema#double");
    doubleType.add("http://www.w3.org/2001/XMLSchema#decimal");

    integer.add("http://www.w3.org/2001/XMLSchema#integer");
    integer.add("http://www.w3.org/2001/XMLSchema#nonPositiveInteger");
    integer.add("http://www.w3.org/2001/XMLSchema#negativeInteger");
    integer.add("http://www.w3.org/2001/XMLSchema#nonNegativeInteger");
    integer.add("http://www.w3.org/2001/XMLSchema#positiveInteger");
    integer.add("http://www.w3.org/2001/XMLSchema#unsignedInt");
    integer.add("http://www.w3.org/2001/XMLSchema#int");

    longType.add("http://www.w3.org/2001/XMLSchema#long");
    longType.add("http://www.w3.org/2001/XMLSchema#unsignedLong");

    shortType.add("http://www.w3.org/2001/XMLSchema#short");
    shortType.add("http://www.w3.org/2001/XMLSchema#unsignedShort");

    byteType.add("http://www.w3.org/2001/XMLSchema#byte");
    byteType.add("http://www.w3.org/2001/XMLSchema#unsignedByte");
    
  }
  
  /**
   * Returns the simple name for the data type suitable for use in a GWT
   * class that represents a resource of the specified type.
   */
  public static String toGwtType(Datatype type) {
    while (type != null) {
      String uri = type.getUri();
      if (doubleType.contains(uri)) return "double";
      if ("http://www.w3.org/2001/XMLSchema#float".equals(uri)) return "float";
      if (integer.contains(uri)) return "int";
      if (longType.contains(uri)) return "long";
      if (shortType.contains(uri)) return "short";
      if (byteType.contains(uri)) return "byte";
      
      type = type.getBase();
    }
    return "String";
  }
  

}
