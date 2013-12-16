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

import java.util.Set;


public interface LdValidationService {
  
  
  /**
   * Validate the given node, and return a report.
   */
  LdValidationReport validate(LdNode node);
  
  /**
   * A set of URI values for properties that should be ignored by the validator.
   */
  void setIgnoredProperties(Set<String> propertySet);
  
  Set<String> getIgnoredProperties();

}
