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
package org.semantictools.jsonld.impl;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.semantictools.jsonld.AmbiguousRestrictionException;
import org.semantictools.jsonld.LdClass;
import org.semantictools.jsonld.LdContainer;
import org.semantictools.jsonld.LdContext;
import org.semantictools.jsonld.LdDatatype;
import org.semantictools.jsonld.LdField;
import org.semantictools.jsonld.LdLiteral;
import org.semantictools.jsonld.LdNode;
import org.semantictools.jsonld.LdObject;
import org.semantictools.jsonld.LdProperty;
import org.semantictools.jsonld.LdQualifiedRestriction;
import org.semantictools.jsonld.LdRestriction;
import org.semantictools.jsonld.LdTerm;
import org.semantictools.jsonld.LdValidationMessage;
import org.semantictools.jsonld.LdValidationReport;
import org.semantictools.jsonld.LdValidationResult;
import org.semantictools.jsonld.LdValidationService;

public class LdValidationServiceImpl implements LdValidationService {
  
  public LdValidationServiceImpl() {
  }

  @Override
  public LdValidationReport validate(LdNode node) {
    // To ensure that this method is threadsafe, use a delegate.
    Delegate delegate = new Delegate();
    return delegate.validate(node);
  }
  

  /**
   * A non-threadsafe implementation.
   *
   * @author Greg McFall
   *
   */
  static class Delegate implements LdValidationService {

  private LdValidationReport report;
  @Override
  public LdValidationReport validate(LdNode node) {
    report = new LdValidationReport();
    if (node.isObject()) {
      validateObject("", node.asObject());
    }
    return report;
  }


  private void validateObject(String path, LdObject obj) {
    String objectType = obj.getTypeIRI();
    if (objectType == null) {
      objectType = inferQualifiedType(path, obj);
      obj.setTypeIRI(objectType);
    }
    RandomAccessObject object = new RandomAccessObject(obj);
    LdContext context = obj.getContext();
    
    // TODO: Do we need to check restrictions on superclasses in the type hierarchy?

    if (path.length()>0) {
      path = path + ".";
    }
    LdClass dr = context.getClass(objectType);
    if (dr != null) {
      validateObject(path, object, dr);
    }
    validateFields(path, object);
    
  }
  
  


  private String inferQualifiedType(String path, LdObject obj) {
    LdField field = obj.owner();
    if (field == null) return null;
    
    LdObject ownerObject = field.getOwner();
    String ownerType = ownerObject.getTypeIRI();
    if (ownerType == null) return null;
    
    LdContext context = obj.getContext();
    LdTerm term = context.getTerm(field.getLocalName());
    
    if (term == null) return null;
    
    LdProperty property = term.getProperty();
    if (property == null) return null;
    
    
    LdClass ownerClass = context.getClass(ownerType);
    if (ownerClass == null) return null;

    String result = null;
    
    try {
      result = ownerClass.inferQualifiedPropertyType(field.getPropertyURI());
    } catch (AmbiguousRestrictionException e) {
      // TODO: construct better error message.
      error(path, "The type of this property is ambiguous");
    }
    return result;
  }
  
  private void error(String path, String message) {
    report(LdValidationResult.ERROR, path, message);
    
  }

  private void warn(String path, String message) {
    report(LdValidationResult.WARNING, path, message);
    
  }

  private void validateFields(String path, RandomAccessObject obj) {
    Iterator<LdField> sequence = obj.getNode().fields();
    if (sequence == null) return;
    while (sequence.hasNext()) {
      LdField field = sequence.next();
      String fieldName = field.getLocalName();
      String fieldPath = path + fieldName;
      validateField(fieldPath, field);
    }
    
  }

  private void validateObject(String path, RandomAccessObject object, LdClass dr) {
    if (dr.listRestrictions()==null) return;
    
    for (LdRestriction restriction : dr.listRestrictions()) {
      String propertyURI = restriction.getPropertyURI();
      LdField field = object.getField(propertyURI);
      String fieldName = getFieldName(object, field, propertyURI);
      
      String fieldPath = path + fieldName;
      Integer minCardinality = restriction.getMinCardinality();
      Integer maxCardinality = restriction.getMaxCardinality();
      
      int cardinality = getCardinality(field);
      
      if (minCardinality != null) {
        validateMinCardinality(fieldPath, minCardinality, cardinality);
      }
      
      if (maxCardinality != null) {
        validateMaxCardinality(fieldPath, maxCardinality, cardinality);
      }

      validateQualifiedRestrictions(fieldPath, restriction, field, cardinality);
      
    }

    validateSuperDomains(path, object, dr);
    
  }
  

  private void validateQualifiedRestrictions(
      String fieldPath, LdRestriction restriction, LdField field, int cardinality) {
   
    List<LdQualifiedRestriction> list = restriction.listQualifiedRestrictions();
    if (list == null) return;
    
    for (LdQualifiedRestriction qr : list) {

      Integer minCardinality = qr.getMinCardinality();
      Integer maxCardinality = qr.getMaxCardinality();

      if (minCardinality != null) {
        validateMinCardinality(fieldPath, minCardinality, cardinality);
      }
      
      if (maxCardinality != null) {
        validateMaxCardinality(fieldPath, maxCardinality, cardinality);
      }
    }
    
  }

  private void validateSuperDomains(String path, RandomAccessObject object, LdClass dr) {
    
    List<LdClass> superList = dr.listSupertypes();
    if (superList == null) return;
    
    for (LdClass superDomain : superList) {
      validateObject(path, object, superDomain);
    }
    
  }

  private void validateField(String path, LdField field) {
   
    if (field == null) return;
    
    LdNode value = field.getValue();

    validateDomain(path, field);
    
    if (value.isObject()) {    
      validateObject(path, value.asObject());
      
    } else if (value.isContainer()) {
      validateContainer(path, value.asContainer());
      
    } else if (value.isLiteral()) {
      validateLiteral(path, field, value.asLiteral());
    }
    
    
    
  }


  private void validateLiteral(String path, LdField field, LdLiteral value) {
    
    // TODO: need to handle qualified restrictions.
    
    LdObject owner = field.getOwner();
    LdContext context = (owner==null) ? null : owner.getContext();
    
    if (context == null) {
      String msg =
          "Cannot validate this property because the JSON-LD context is not defined.";
      warn(path, msg);
      return;
    }
    
    LdTerm fieldTerm = context.getTerm(field.getLocalName());
    if (fieldTerm == null) {
      String msg  = "No term is defined for this property";
      error(path, msg);
      return;
    }
    
    String typeIRI = fieldTerm.getTypeIRI();
    
    LdDatatype datatype = context.findDatatypeByURI(typeIRI);
    
    if (datatype == null) {
      
      // Don't warn about rdfs:label
      
      if (!"http://www.w3.org/2000/01/rdf-schema#label".equals(fieldTerm.getIRI())) {
        // It is possible that the field is supposed to be an embedded object
        // but was given an IRI reference as a string value instead.
        // Let's test that hypothesis.
        
        LdTerm typeTerm = context.getTerm(typeIRI);
        LdClass rdfClass = typeTerm.getRdfClass();
        if (rdfClass != null) {
          String msg = "Expected an embedded object but found an IRI reference";
          warn(path, msg);
        } else {
          String msg = "Cannot validate this property because the datatype is not known.";
          warn(path, msg);
        }
      }
      return;
    }
    
    validateLiteral(path, value, datatype);
    
    
  }

  private void validateLiteral(String path, LdLiteral value, LdDatatype datatype) {
    // TODO: validate dateTime and duration syntax
    
    // String validation
    
    if (
        (datatype.getMaxLength() != null) && 
        (value.getStringValue().length()>datatype.getMaxLength())
    ) {
      String msg =
          "Should have maxLength=" + datatype.getMaxLength() + ", but found length=" +
           value.getStringValue().length();
      
      error(path, msg);
      
    }
    
    if (
        (datatype.getPattern() != null) &&
        (!datatype.getPattern().matcher(value.getStringValue()).matches()) 
    ) {
      String msg =
          "Value does not match the " + datatype.getLocalName() + " pattern: " + datatype.getPattern().pattern();
      error(path, msg);
    }
    // TODO: perform other string validation
    
  }

  private void validateDomain(String path, LdField field) {
    LdObject owner = field.getOwner();
    LdContext context = owner.getContext();
    String fieldName = field.getLocalName();
    LdTerm term = context.getTerm(fieldName);
    if (term == null) {
      String msg = 
          "Cannot expand property name to a URI.  The term '" + fieldName + "' is not defined.";
      report(LdValidationResult.ERROR, path, msg);
      return;
    }
    LdProperty property = term.getProperty();
    if (property == null) return;
    
    List<String> domainList = property.getDomain();
    if (domainList == null) {
      if (!"http://www.w3.org/2000/01/rdf-schema#label".equals(term.getIRI())) {
        // Special handling for rdfs:label
        String msg =
            "The domain for the property '" + fieldName + "' is not known.";
        report(LdValidationResult.WARNING, path, msg);
      }
      return;
    }
    
    String ownerType = owner.getTypeIRI();
    if (ownerType == null) {
      String msg =
          "Cannot evaluate the domain of this property because the type of the parent object is not known.";
      report(LdValidationResult.WARNING, path, msg);
      return;
    }
    
   if (!isMemberOf(context, ownerType, domainList)) {
     String msg = "Invalid domain for this property";
     report(LdValidationResult.ERROR, path, msg);
   }
  }


  /**
   * Returns true if the type named by the typeURI parameter is one of the
   * classes in the given uriList, or a subclass of one of those classes.
   */
  private boolean isMemberOf(
      LdContext context, String typeURI, List<String> uriList
  ) {
    if (uriList.contains(typeURI)) return true;
    
    LdClass dr = context.getClass(typeURI);
    return isSuperMemberOf(context, uriList, dr);
  }

  private boolean isSuperMemberOf(LdContext context, List<String> uriList, LdClass dr) {
    
    if (dr == null) return false;
    
    List<LdClass> list = dr.listSupertypes();
    if (list != null) {
      for (LdClass superDR : list) {
        String domainURI = superDR.getURI();
        if (uriList.contains(domainURI)) return true;
        if (isSuperMemberOf(context, uriList, superDR)) return true;
      }
    }
    
    return false;
  }

  private void validateContainer(String path, LdContainer container) {

    int index = 0;
    Iterator<LdNode> sequence = container.iterator();
    LdField field = container.owner();
    while (sequence.hasNext()) {
      LdNode node = sequence.next();
      String elemPath = path + "[" + index + "]";
      
      if (node.isObject()) {
        validateObject(elemPath, node.asObject());
        
      } if (node.isLiteral()) {
        validateLiteral(elemPath, field, node.asLiteral());
      }
      
      
      index++;
    }
    
  }


  private void validateMaxCardinality(
      String fieldPath, int maxCardinality, int cardinality) {
    
    if (cardinality > maxCardinality) {
      String message =
          "Expected maxCardinality=" + maxCardinality + ", but found cardinality=" + cardinality;
      
      report(LdValidationResult.ERROR, fieldPath, message);
      
    } 
    
  }


  private String getFieldName(RandomAccessObject object, LdField field, String propertyURI) {
    if (field != null) {
      return field.getLocalName();
    }
    LdContext context = object.getNode().getContext();
    if (context == null) {
      return getLocalName(propertyURI);
    }
    LdTerm term = context.getTerm(propertyURI);
    
    return term==null ? getLocalName(propertyURI) : term.getShortName();
  }

  
  private String getLocalName(String propertyURI) {
    int delim = propertyURI.lastIndexOf('#');
    if (delim < 0) {
      delim = propertyURI.lastIndexOf('/');
    }
    if (delim < 0) {
      return propertyURI;
    }
    return propertyURI.substring(delim+1);
  }


  private void validateMinCardinality(
      String fieldPath, int minCardinality, int cardinality) {
    
    if (cardinality < minCardinality) {
      String message =
          "Expected minCardinality=" + minCardinality + ", but found cardinality=" + cardinality;
      
      report(LdValidationResult.ERROR, fieldPath, message);
      
    }
    
  }
  
  
  private void report(LdValidationResult result, String path, String message) {
    
    report.add(new LdValidationMessage(result, path, message));
    
  }


  private int getCardinality(LdField field) {
    if (field == null) return 0;
    LdNode node = field.getValue();
    if (!node.isContainer()) return 1;
    
    return node.asContainer().size();
    
  }







  static class RandomAccessObject {
    private Map<String, LdField> fieldMap;
    private LdObject object;
    
    public RandomAccessObject(LdObject object) {
      this.object = object;
      fieldMap = new HashMap<String, LdField>();
      Iterator<LdField> sequence = object.fields();
      if (sequence != null) {
        while (sequence.hasNext()) {
          LdField field = sequence.next();
          fieldMap.put(field.getPropertyURI(), field);
        }
      }
      
    }
    
    public LdObject getNode() {
      return object;
    }



    public LdField getField(String propertyURI) {
      return fieldMap.get(propertyURI);
    }

    public String toString() {
      return "RadnomAccessObject(" + object.getId() + ")";
    }
    
    }
  }
  

}
