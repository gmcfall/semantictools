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

/**
 * An exception that occurs while attempting to save an LdVocabulary object.
 * @author Greg McFall
 *
 */
public class LdVocabularySaveException extends Exception {
  private static final long serialVersionUID = 1L;
  
  public LdVocabularySaveException(String message) {
    super(message);
  }
  
  public LdVocabularySaveException(Throwable cause) {
    super(cause);
  }

}
