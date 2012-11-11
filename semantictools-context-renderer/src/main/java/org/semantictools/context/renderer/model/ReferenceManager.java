package org.semantictools.context.renderer.model;

import java.util.List;

public interface ReferenceManager {
  
  void put(String key, BibliographicReference ref);
  void add(BibliographicReference ref);
  BibliographicReference getReference(String key);
  List<BibliographicReference> listReferences();

}
