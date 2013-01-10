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
package org.semantictools.jsonld.impl;

import java.util.Iterator;

import org.semantictools.jsonld.LdField;

public class FieldList {

  private LinkedLdField firstField;
  private LinkedLdField lastField;

  LinkedLdField getFirstField() {
    return firstField;
  }

  LinkedLdField getLastField() {
    return lastField;
  }

  /**
   * Add a field to the lookahead list.
   */
  public void add(LinkedLdField field) {
    if (firstField == null) {
      firstField = field;
    }
    if (lastField != null) {
      field.setPrevField(lastField);
      lastField.setNextField(field);
    }
    lastField = field;
  }
  
  public void remove(LinkedLdField field) {
    LinkedLdField prev = field.getPrevField();
    LinkedLdField next = field.getNextField();
    if (field == firstField) {
      firstField = next;
    }
    if (field == lastField) {
      lastField = prev;
    }
    prev.setNextField(next);
  }
  
  public Iterator<LdField> iterator() {
    return new FieldIterator(this);
  }
}
