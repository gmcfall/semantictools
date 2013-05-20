package org.semantictools.frame.model;

import java.util.ArrayList;
import java.util.List;

import org.semantictools.vocab.SKOS;

import com.hp.hpl.jena.ontology.OntProperty;
import com.hp.hpl.jena.ontology.OntResource;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.vocabulary.DCTerms;
import com.hp.hpl.jena.vocabulary.RDFS;

public class FrameUtil {
  
  public static String getClassDescription(OntResource resource) {
    String value = getDescription(resource);
    if (value == null) {
      List<OntResource> list = new ArrayList<OntResource>();
      list.add(resource);
      value = breadthFirstDescription(RDFS.subClassOf, list);
    }
    
    return value;
  }
  
  public static String getPropertyDescription(OntProperty property) {
    String value = getDescription(property);
    if (value == null) {
      List<OntResource> list = new ArrayList<OntResource>();
      list.add(property);
      value = breadthFirstDescription(RDFS.subPropertyOf, list);
    }
    
    return value;
  }
  

  private static String breadthFirstDescription(Property relation, List<OntResource> list) {
    if (list.isEmpty()) return null;
    List<OntResource> next = new ArrayList<OntResource>();
    for (OntResource type : list) {
      StmtIterator sequence = type.listProperties(relation);
      while (sequence.hasNext()) {
        Statement s = sequence.next();
        Resource subject = s.getSubject();
        if (subject.equals(type)) continue;
        if (subject.canAs(OntResource.class)) {
          OntResource superType = subject.as(OntResource.class);
          String value = getDescription(superType);
          if (value == null) {
            next.add(superType);
          } else {
            return value;
          }
        }
      }
      
    }
    return breadthFirstDescription(relation, next);
  }


  public static String getDescription(OntResource resource) {
    // TODO: handle possibility of multiple languages
    RDFNode node = resource.getPropertyValue(DCTerms.description);
    if (node != null && node.canAs(Literal.class)) {
      return node.asLiteral().getString();
    }
    node = resource.getPropertyValue(SKOS.definition);
    if (node != null && node.canAs(Literal.class)){
      return node.asLiteral().getString();
    }
    
    
    return resource.getComment(null);
  }

}
