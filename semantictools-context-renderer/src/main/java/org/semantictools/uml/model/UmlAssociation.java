package org.semantictools.uml.model;

public class UmlAssociation {
  
  private UmlAssociationEnd[] ends;
  
  public UmlAssociation(UmlAssociationEnd end1, UmlAssociationEnd end2) {
    ends = new UmlAssociationEnd[] {end1, end2};
  }
  
  public UmlAssociationEnd[] getEnds() {
    return ends;
  }
  
  public UmlAssociationEnd getSelfEnd(UmlClass endpoint) {

    if (ends[0].getParticipant().getURI().equals(endpoint.getURI())) return ends[0];
    if (ends[1].getParticipant().getURI().equals(endpoint.getURI())) return ends[1];
    
    throw new IllegalArgumentException(endpoint.getURI() + " is not a valid endpoint");
  }
  
  public UmlAssociationEnd getOtherEnd(UmlClass endpoint) {
    if (ends[0].getParticipant().getURI().equals(endpoint.getURI())) return ends[1];
    if (ends[1].getParticipant().getURI().equals(endpoint.getURI())) return ends[0];
    
    throw new IllegalArgumentException(endpoint.getURI() + " is not a valid endpoint");
    
  }
  
  public boolean equals(UmlAssociation other) {
    return 
        (ends[0].equals(other.ends[0]) && ends[1].equals(other.ends[1])) ||
        (ends[0].equals(other.ends[1]) && ends[1].equals(other.ends[0]));
        
  }
  
  public String toString() {
    return "Association[" + ends[0] + ", " + ends[1] + "]";
  }

}
