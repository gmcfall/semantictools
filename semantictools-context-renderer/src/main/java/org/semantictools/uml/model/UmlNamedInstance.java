package org.semantictools.uml.model;

import org.semantictools.frame.model.NamedIndividual;

/**
 * A resource that is a named instance of some class.
 * @author Greg McFall
 *
 */
public class UmlNamedInstance {
  private NamedIndividual delegate;
  
  public UmlNamedInstance(NamedIndividual delegate) {
    this.delegate = delegate;
  }
  
  public String getURI() {
    return delegate.getUri();
  }
  
  public String getLocalName() {
    return delegate.getLocalName();
  }
  
  public String getComment() {
    return delegate.getComment();
  }

}
