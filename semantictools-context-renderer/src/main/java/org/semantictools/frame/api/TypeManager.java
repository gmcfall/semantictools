package org.semantictools.frame.api;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import static org.semantictools.frame.model.OntologyType.*;

import javax.xml.parsers.ParserConfigurationException;

import org.semantictools.frame.model.BindVocabulary;
import org.semantictools.frame.model.Datatype;
import org.semantictools.frame.model.Frame;
import org.semantictools.frame.model.ListType;
import org.semantictools.frame.model.OntologyInfo;
import org.semantictools.frame.model.OntologyType;
import org.semantictools.frame.model.RdfType;
import org.xml.sax.SAXException;

import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.Ontology;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.vocabulary.OWL2;

public class TypeManager {
  
  private static OntologyInfo XMLSCHEMA = new OntologyInfo("xs", "http://www.w3.org/2001/XMLSchema#", XSD);
  private static OntologyInfo OWL = new OntologyInfo("owl", "http://www.w3.org/2002/07/owl#", RDF);
  private static OntologyInfo RDFS = new OntologyInfo("rdfs", "http://www.w3.org/2000/01/rdf-schema#", RDF);
  
  private Map<String, Frame> uri2Frame = new HashMap<String, Frame>();
  private Map<String, Datatype> uri2Datatype = new HashMap<String, Datatype>();
  private Map<String, OntologyInfo> uri2Ontology = new HashMap<String, OntologyInfo>();
  private Map<String, ListType> uri2ListType = new HashMap<String, ListType>();
  private Set<String> standard = new HashSet<String>();
  
  private OntModel ontModel;
  
  public TypeManager() {
    ontModel = ModelFactory.createOntologyModel();
    addStandard(XMLSCHEMA);
    addStandard(OWL);
    addStandard(RDFS);
    addOwlThing();
    addResource();
  }
  
  public boolean isStandard(String ontologyURI) {
    return standard.contains(ontologyURI);
  }
  
  private void addStandard(OntologyInfo info) {
    add(info);
    standard.add(info.getUri());
  }
  
  private void addResource() {
    OntClass resourceClass = ontModel.createClass(com.hp.hpl.jena.vocabulary.RDFS.Resource.getURI());
    Frame resourceFrame = new Frame(this, resourceClass);
    add(resourceFrame);
    
  }

  private void addOwlThing() {
    OntClass thingClass = ontModel.createClass(OWL2.Thing.getURI());
    Frame thingFrame = new Frame(this, thingClass);
    add(thingFrame);
  }

  public OntModel getOntModel() {
    return ontModel;
  }




  public void loadDir(File dir) throws IOException, ParserConfigurationException, SAXException {
   
    FrameBuilder builder = new FrameBuilder(this);
    builder.setOntModel(ontModel);
    builder.loadDir(dir);
  }
  
  public void processOntologies() {
    List<Ontology> list = ontModel.listOntologies().toList();
    for (Ontology ontology : list) {

      Statement statement = ontology.getProperty(BindVocabulary.suggestedPrefix);
      if (statement == null) {
        continue;
      }
      
      String label = ontology.getLabel(null);
      String prefix = statement.getString();
      OntologyInfo info = new OntologyInfo();
      info.setPrefix(prefix);
      info.setUri(ontology.getURI());
      info.setLabel(label);
      add(info);
    }
  }
  
  
  public String getQName(String uri) {
    int hash = uri.lastIndexOf('#');
    int slash = uri.lastIndexOf('/');
    int delim = Math.max(hash, slash);
    String namespaceURI = uri.substring(0, delim+1);
    OntologyInfo info = getOntologyByUri(namespaceURI);
    if (info == null) return null;
    
    String localName = uri.substring(delim+1);
    return info.getPrefix() + ":" + localName;
  }
  
  public static String getLocalName(String uri) {

    int hash = uri.lastIndexOf('#');
    int slash = uri.lastIndexOf('/');
    int delim = Math.max(hash, slash);
    
    String localName = uri.substring(delim+1);
    return localName;
  }
  
  public static String getDefaultNamespacePrefix(String uri) {
    int end = uri.length()-1;
    int start = uri.lastIndexOf('/', end);
    return uri.substring(start+1, end);
  }
  
  public static String getNamespace(String uri) {
    int hash = uri.lastIndexOf('#');
    int slash = uri.lastIndexOf('/');
    int delim = Math.max(hash, slash);
    String namespaceURI = uri.substring(0, delim+1);
    return namespaceURI;
  }
  

  public void add(ListType listType) {
    uri2ListType.put(listType.getElementType().getUri(), listType);
  }
  
  public ListType getListTypeByElementUri(String uri) {
    return uri2ListType.get(uri);
  }
  
  public void add(OntologyInfo info) {
    // XSD Ontology Type takes precedence 
    OntologyInfo oldInfo = uri2Ontology.get(info.getUri());
    if (oldInfo != null) {
      OntologyType type = 
          (oldInfo.getType() == OntologyType.XSD || info.getType()==OntologyType.XSD) ? OntologyType.XSD :
          OntologyType.RDF;
      
      info.setType(type);
      oldInfo.setType(type);
      
    } else {
      uri2Ontology.put(info.getUri(), info);
    }
  }
  
  public OntologyInfo getOntologyByUri(String uri) {
    return uri2Ontology.get(uri);
  }
  
  public Collection<OntologyInfo> listOntologies() {
    return uri2Ontology.values();
  }
  
  public void add(Frame frame) {
    uri2Frame.put(frame.getUri(), frame);
  }
  
  public void add(Datatype datatype) {
    uri2Datatype.put(datatype.getUri(), datatype);
  }
  
  
  public Datatype getDatatypeByUri(String uri) {
    Datatype type = uri2Datatype.get(uri);
    if (type == null && (  uri.startsWith(XMLSCHEMA.getUri()) || uri.startsWith(RDFS.getUri())  )) {
      type = new Datatype();
      type.setUri(uri);
      type.setLocalName(getLocalName(uri));
      
      add(type);
    }
    return type;
  }
  
  public Collection<Datatype> listDatatypes() {
    return uri2Datatype.values();
  }
  
  public Frame getFrameByUri(String uri) {
    return uri2Frame.get(uri);
  }
  
  public RdfType getTypeByURI(String uri) {
    RdfType type = getFrameByUri(uri);
    if (type == null) {
      type = getDatatypeByUri(uri);
    }
    return type;
  }
  
  public Collection<Frame> listFrames() {
    return uri2Frame.values();
  }
  
  public List<Frame> listFramesInOntology(String ontologyURI) {
    List<Frame> result = new ArrayList<Frame>();
    Iterator<Frame> sequence = listFrames().iterator();
    while (sequence.hasNext()) {
      Frame frame = sequence.next();
      if (frame.getUri().startsWith(ontologyURI)) {
        result.add(frame);
      }
    }
    return result;
  }

}
