package org.semantictools.frame.model;

import java.util.ArrayList;
import java.util.List;

import org.semantictools.frame.api.TypeManager;

import com.hp.hpl.jena.ontology.OntClass;

public class Enumeration extends Frame {
  
  private List<NamedIndividual> individualList = new ArrayList<NamedIndividual>();

  public Enumeration(TypeManager typeManager, OntClass type) {
    super(typeManager, type);
    setCategory(RestCategory.ENUMERABLE);
  }
  
  public void add(NamedIndividual individual) {
    individualList.add(individual);
  }

  public List<NamedIndividual> getIndividualList() {
    return individualList;
  }


  @Override
  public boolean canAsEnumeration() {
    return true;
  }

  @Override
  public Enumeration asEnumeration() {
    return this;
  }
  

  public List<NamedIndividual> listInstances(boolean direct) {
    return individualList;
  }
  
}
