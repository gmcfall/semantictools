package org.semantictools.index.api.impl;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.semantictools.context.renderer.model.ContextProperties;
import org.semantictools.frame.api.ContextManager;
import org.semantictools.frame.api.ServiceDocumentationManager;
import org.semantictools.frame.api.TypeManager;
import org.semantictools.frame.model.OntologyInfo;
import org.semantictools.frame.model.OntologyType;
import org.semantictools.index.api.LinkedDataIndex;
import org.semantictools.index.model.SchemaReference;
import org.semantictools.index.model.ServiceDocumentationList;
import org.semantictools.uml.api.UmlFileManager;

public class LinkedDataIndexImpl implements LinkedDataIndex {
  
  private ContextManager contextManager;
  private ServiceDocumentationManager serviceDocumentManager;
  private UmlFileManager umlFileManager;
  private TypeManager typeManager;
  

  public LinkedDataIndexImpl(
      TypeManager typeManager,
      ContextManager contextManager, 
      ServiceDocumentationManager sman,
      UmlFileManager umlFileManager) {
    
    this.typeManager = typeManager;
    this.contextManager = contextManager;
    this.serviceDocumentManager = sman;
    this.umlFileManager = umlFileManager;
  }



  @Override
  public List<ContextProperties> listMediaTypesForClass(String rdfClassURI) {
    List<ContextProperties> list = contextManager.listContextPropertiesForClass(rdfClassURI);
    return list;
  }
  


  @Override
  public List<ContextProperties> listAllMediaTypes() {
    return contextManager.listContextProperties();
  }



  @Override
  public List<SchemaReference> listOntologies() {
    return listSchemas(OntologyType.RDF);
  }



  @Override
  public List<SchemaReference> listDatatypes() {
    return listSchemas(OntologyType.XSD);
  }
  
  private List<SchemaReference> listSchemas(OntologyType type) {
    List<SchemaReference> result = new ArrayList<SchemaReference>();
    Iterator<OntologyInfo> sequence = typeManager.listOntologies().iterator();
    while (sequence.hasNext()) {
      String ontURI = sequence.next().getUri();
      if (typeManager.isStandard(ontURI)) continue;
      OntologyInfo info = typeManager.getOntologyByUri(ontURI);
      if (info == null || (type==OntologyType.RDF && !info.hasClasses())) continue;
      if (info.getType() == type) {
        umlFileManager.setOntology(info.getUri());
        
        String label = info.getLabel()==null ? info.getUri() : info.getLabel();
        File docFile = umlFileManager.getOntologyAllFile();
        
        result.add(new SchemaReference(label, docFile));
      }
      
    }
    return result;
    
  }



  @Override
  public List<ServiceDocumentationList> listServices() {
    return serviceDocumentManager.getServiceDocumentationLists();
  }



  @Override
  public ServiceDocumentationList getServiceDocumentationForClass(String rdfClassURI) {
    return serviceDocumentManager.getServiceDocumentationByRdfType(rdfClassURI);
  }





}
