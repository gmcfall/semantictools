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

import java.util.HashSet;
import java.util.Set;

public class DomainUtil {
  private static final Set<String> domainSet = new HashSet<String>();
  
  static {
    domainSet.add("aero");
    domainSet.add("asia");
    domainSet.add("biz");
    domainSet.add("cat");
    domainSet.add("com");
    domainSet.add("coop");
    domainSet.add("info");
    domainSet.add("int");
    domainSet.add("jobs");
    domainSet.add("mobi");
    domainSet.add("museum");
    domainSet.add("name");
    domainSet.add("net");
    domainSet.add("org");
    domainSet.add("pro");
    domainSet.add("tel");
    domainSet.add("travel");
    domainSet.add("xxx");
    domainSet.add("edu");
    domainSet.add("gov");
    domainSet.add("mil");
    
    // TODO: Add country code top-level domains
  }
  
  public static boolean isTopLevelDomain(String value) {
    return domainSet.contains(value);
  }

}
