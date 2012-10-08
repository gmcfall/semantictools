package org.semantictools.index.api;

import java.util.List;

import org.semantictools.context.renderer.model.ContextProperties;
import org.semantictools.index.model.MediaTypeReference;
import org.semantictools.index.model.SchemaReference;
import org.semantictools.index.model.ServiceDocumentationList;

/**
 * Interface for a service to lookup a list of media type bindings for a given resource type.
 * @author Greg McFall
 *
 */
public interface LinkedDataIndex {
  
  /**
   * Given the URI for an RDF Class, return a list of references to media types that provide
   * representations of that class.
   */
  public List<MediaTypeReference> listMediaTypesForClass(String rdfClassURI);
  
  public List<ContextProperties> listAllMediaTypes();

  public List<SchemaReference> listOntologies();
  
  public List<SchemaReference> listDatatypes();
  
  public List<ServiceDocumentationList> listServices();
  
  public ServiceDocumentationList getServiceDocumentationForClass(String rdfClassURI);
  
}
