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

import org.semantictools.jsonld.impl.LdContextEnhanceException;

/**
 * A utility that enhances JSON-LD context objects with information needed to
 * perform validation.
 * 
 * @author Greg McFall
 *
 */
public interface LdContextEnhancer {

  /**
   * Enhance the given LdContext with information needed to perform validation.
   */
  public void enhance(LdContext context) throws LdContextEnhanceException;
  
}