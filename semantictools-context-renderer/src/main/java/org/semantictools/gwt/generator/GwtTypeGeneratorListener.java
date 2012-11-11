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

public interface GwtTypeGeneratorListener {

  /**
   * Receive a notification when the generator starts working on
   * the specified type.
   */
  void beginType(String typeURI);
  
  
  /**
   * Receive a notification that the generator is ignoring the specified
   * type based on rules in the configuration file.
   */
  void ignoreType(String typeURI);
  
}
