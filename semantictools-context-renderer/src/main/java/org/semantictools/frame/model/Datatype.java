package org.semantictools.frame.model;

import com.hp.hpl.jena.ontology.OntClass;

public class Datatype implements RdfType {
  
  private String localName;
  private String uri;
  private Datatype base;
  private String namespace;
  private OntClass rdfNode;
  private String label;
  
  private Integer length;
  private Integer minLength;
  private Integer maxLength;
  private String pattern;
  private Whitespace whitespace;
  
  private Number maxInclusive;
  private Number maxExclusive;
  private Number minInclusive;
  private Number minExclusive;
  private Integer totalDigits;
  private Integer fractionDigits;
  
  
  
  public Datatype() {
    
  }
  
  
  public OntClass getRdfNode() {
    return rdfNode;
  }

  public void setRdfNode(OntClass rdfNode) {
    this.rdfNode = rdfNode;
  }

  public String getLocalName() {
    return localName;
  }
  public void setLocalName(String localName) {
    this.localName = localName;
  }
  public String getUri() {
    return uri;
  }
  public void setUri(String uri) {
    this.uri = uri;
  }
  public Datatype getBase() {
    return base;
  }
  public void setBase(Datatype base) {
    this.base = base;
  }
  
  public String toString() {
    return localName;
  }



  @Override
  public boolean canAsOntClass() {
    return rdfNode != null;
  }


  @Override
  public boolean canAsFrame() {
    return false;
  }


  @Override
  public boolean canAsDatatype() {
    return true;
  }


  @Override
  public OntClass asOntClass() {
    return null;
  }


  @Override
  public Frame asFrame() {
    return null;
  }


  @Override
  public Datatype asDatatype() {
    return this;
  }


  @Override
  public boolean canAsListType() {
    return false;
  }


  @Override
  public ListType asListType() {
    return null;
  }


  public Integer getLength() {
    return length;
  }


  public void setLength(Integer length) {
    this.length = length;
  }


  public Integer getMinLength() {
    return minLength;
  }


  public void setMinLength(Integer minLength) {
    this.minLength = minLength;
  }


  public Integer getMaxLength() {
    return maxLength;
  }


  public void setMaxLength(Integer maxLength) {
    this.maxLength = maxLength;
  }


  public String getPattern() {
    return pattern;
  }


  public void setPattern(String pattern) {
    this.pattern = pattern;
  }


  public Whitespace getWhitespace() {
    return whitespace;
  }


  public void setWhitespace(Whitespace whitespace) {
    this.whitespace = whitespace;
  }


  public Number getMaxInclusive() {
    return maxInclusive;
  }


  public void setMaxInclusive(Number maxInclusive) {
    this.maxInclusive = maxInclusive;
  }


  public Number getMaxExclusive() {
    return maxExclusive;
  }


  public void setMaxExclusive(Number maxExclusive) {
    this.maxExclusive = maxExclusive;
  }


  public Number getMinInclusive() {
    return minInclusive;
  }


  public void setMinInclusive(Number minInclusive) {
    this.minInclusive = minInclusive;
  }


  public Number getMinExclusive() {
    return minExclusive;
  }


  public void setMinExclusive(Number minExclusive) {
    this.minExclusive = minExclusive;
  }


  public Integer getTotalDigits() {
    return totalDigits;
  }


  public void setTotalDigits(Integer totalDigits) {
    this.totalDigits = totalDigits;
  }


  public Integer getFractionDigits() {
    return fractionDigits;
  }


  public void setFractionDigits(Integer fractionDigits) {
    this.fractionDigits = fractionDigits;
  }


  @Override
  public String getNamespace() {
    if (namespace == null) {
      int hash = uri.lastIndexOf('#');
      int slash = uri.lastIndexOf('/');
      int delim = Math.max(hash, slash);
      namespace = uri.substring(0, delim+1);
    }
    return namespace;
  }


  @Override
  public boolean canAsEnumeration() {
    return false;
  }


  @Override
  public Enumeration asEnumeration() {
    return null;
  }


  public String getLabel() {
    return label;
  }


  public void setLabel(String label) {
    this.label = label;
  }

}
