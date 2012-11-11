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
package org.semantictools.context.renderer;

import java.util.Comparator;

import org.semantictools.context.renderer.model.TreeNode;

public interface NodeComparatorFactory {
  
  /**
   * Return a comparator that can be used to sort the nodes representing fields of
   * the specified class.
   * @param frameURI  The URI for the RDF class whose fields are to be sorted.
   */
  public Comparator<TreeNode> getComparator(String frameURI);

}
