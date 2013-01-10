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

import java.util.Iterator;

public interface LdContainer extends LdNode {

  /**
   * Return this container's type.
   */
  LdContainerType getContainerType();
  
  /**
   * Returns the number of elements in this container.
   */
  int size();
  
  /**
   * Return an iterator for resources within this container.
   * This is a one-time use iterator.  It is an error to call this
   * method more than once.
   */
  Iterator<LdNode> iterator();
  
  /**
   * Returns the field through which this LdContainer is accessed.
   */
  LdField owner();
  
  
}
