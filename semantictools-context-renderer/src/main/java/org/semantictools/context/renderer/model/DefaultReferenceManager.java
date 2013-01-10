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
package org.semantictools.context.renderer.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public class DefaultReferenceManager implements ReferenceManager {
  private Map<String, BibliographicReference> map = new HashMap<String, BibliographicReference>();

  @Override
  public void add(BibliographicReference ref) {

    String key = toKey(ref.getLabel());
    map.put(key, ref);

  }

  @Override
  public BibliographicReference getReference(String key) {
    BibliographicReference result = map.get(key);
    if (result == null) {
      key = toKey(key);
      result = map.get(key);
    }
    return result;
  }
  

  private String toKey(String tag) {
    tag = tag.replace("&nbsp;", "_");
    StringBuilder builder = new StringBuilder();
    for (int i=0; i<tag.length(); i++) {
      char c = tag.charAt(i);
      if (Character.isJavaIdentifierPart(c)) {
        builder.append(c);
      } else {
        builder.append('_');
      }
    }
    return builder.toString();
  }

  @Override
  public List<BibliographicReference> listReferences() {
    return new ArrayList<BibliographicReference>(new HashSet<BibliographicReference>(map.values()));
  }

  @Override
  public void put(String key, BibliographicReference ref) {
    map.put(key, ref);
  }

}
