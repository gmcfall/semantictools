package org.semantictools.frame.model;

public class ContainerRestriction {
  
  private Frame containerType;
  private Uri membershipPredicate;
  private Uri membershipSubject;
  
  public ContainerRestriction(Frame containerType, Uri membershipSubject, Uri membershipPredicate) {
    this.containerType = containerType;
    this.membershipPredicate = membershipPredicate;
    this.membershipSubject = membershipSubject;
  }

  public Uri getMembershipPredicate() {
    return membershipPredicate;
  }

  public Uri getMembershipSubject() {
    return membershipSubject;
  }
  
  public Frame getContainerType() {
    return containerType;
  }
  
  

}
