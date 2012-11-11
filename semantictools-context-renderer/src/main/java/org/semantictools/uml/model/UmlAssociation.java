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
