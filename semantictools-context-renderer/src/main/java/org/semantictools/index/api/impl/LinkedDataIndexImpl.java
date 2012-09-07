package org.semantictools.index.api.impl;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.semantictools.context.renderer.model.ContextProperties;
import org.semantictools.context.renderer.model.ServiceDocumentation;
import org.semantictools.frame.api.ContextManager;
import org.semantictools.frame.api.ServiceDocumentationManager;
import org.semantictools.frame.api.TypeManager;
import org.semantictools.frame.model.OntologyInfo;
import org.semantictools.frame.model.OntologyType;
import org.semantictools.index.api.LinkedDataIndex;
import org.semantictools.index.model.MediaTypeReference;
import org.semantictools.index.model.SchemaReference;
import org.semantictools.uml.api.UmlFileManager;

public class LinkedDataIndexImpl implements LinkedDataIndex {
  
  private ContextManager contextManager;
  private ServiceDocumentationManager serviceDocumentManager;
  private File mediatypeDir;
  private UmlFileManager umlFileManager;
  private TypeManager typeManager;
  

  public LinkedDataIndexImpl(
      File mediatypeDir, 
      TypeManager typeManager,
      ContextManager contextManager, 
      ServiceDocumentationManager sman,
      UmlFileManager umlFileManager) {
    
    this.typeManager = typeManager;
    this.contextManager = contextManager;
    this.mediatypeDir = mediatypeDir;
    this.serviceDocumentManager = sman;
    this.umlFileManager = umlFileManager;
  }



  @Override
  public List<MediaTypeReference> listMediaTypesForClass(String rdfClassURI) {
    List<MediaTypeReference> list = new ArrayList<MediaTypeReference>();
    List<ContextProperties> pList = contextManager.listContextPropertiesForClass(rdfClassURI);
    for (ContextProperties p : pList) {
      list.add(createReferences(p));
    }
    
    return list;
  }
  
  private MediaTypeReference createReferences(ContextProperties p) {
    String path = p.getMediaType().replace('.', '/') + "/index.html";
    File file = new File(mediatypeDir, path);
    String uri = file.toString().replace('\\', '/');
    
    String serviceURI = null;
    ServiceDocumentation service = serviceDocumentManager.getServiceDocumentationByMediaType(p.getMediaType());
    if (service != null) {
      int slash = uri.lastIndexOf('/');
      serviceURI = uri.substring(0, slash) + "/service.html";
    }
    return new MediaTypeReference(p.getMediaType(), uri, serviceURI);
    
  }



  @Override
  public List<MediaTypeReference> listAllMediaTypes() {
    List<MediaTypeReference> result = new ArrayList<MediaTypeReference>();
    for (ContextProperties p : contextManager.listContextProperties()) {
      result.add(createReferences(p));
    }
    return result;
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
      if (info == null) continue;
      if (info.getType() == type) {
        umlFileManager.setOntology(info.getUri());
        
        String label = info.getLabel()==null ? info.getUri() : info.getLabel();
        File docFile = umlFileManager.getOntologyAllFile();
        
        result.add(new SchemaReference(label, docFile));
      }
      
    }
    return result;
    
  }



}
