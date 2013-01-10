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

import java.util.List;

/**
 * An exception which indicates that the underlying RDF model specifies 
 * ambiguous (i.e. conflicting) qualified type restrictions.
 * @author Greg McFall
 *
 */
public class AmbiguousRestrictionException extends Exception {
  private static final long serialVersionUID = 1L;

  private List<LdQualifiedRestriction>  options;

  /**
   * 
   * @param options
   */
  public AmbiguousRestrictionException(List<LdQualifiedRestriction> options) {
    this.options = options;
  }

  /**
   * Returns the list of conflicting qualified restrictions.
   * @return
   */
  public List<LdQualifiedRestriction> getConflictingRestrictions() {
    return options;
  }
  
  
  

}
