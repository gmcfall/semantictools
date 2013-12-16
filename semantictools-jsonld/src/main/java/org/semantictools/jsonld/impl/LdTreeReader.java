package org.semantictools.jsonld.impl;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Map.Entry;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.JsonParser.Feature;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.ObjectNode;
import org.semantictools.jsonld.LdContainerType;
import org.semantictools.jsonld.LdContext;
import org.semantictools.jsonld.LdField;
import org.semantictools.jsonld.LdLiteral;
import org.semantictools.jsonld.LdNode;
import org.semantictools.jsonld.LdObject;
import org.semantictools.jsonld.LdTerm;
import org.semantictools.jsonld.io.LdContextReader;
import org.semantictools.jsonld.io.LdParseException;
import org.semantictools.jsonld.io.LdParser;

public class LdTreeReader implements LdParser {
  private LdContextReader contextReader;

  public LdTreeReader(LdContextReader contextReader) {
    this.contextReader = contextReader;
  }

  @Override
  public LdNode parse(InputStream input) throws LdParseException, IOException {
    
    ObjectMapper mapper = new ObjectMapper();
    mapper.configure(Feature.ALLOW_COMMENTS, true);
    JsonNode node = mapper.readTree(input);
    if (! (node instanceof ObjectNode)) {
      throw new LdParseException("JSON-LD document must have an object as the root element");
    }
    
    LdNode result = parseNode(node, null, null, null);
    
    return result;
  }
  
  LdNode parseNode(JsonNode node, LdObjectImpl parent, LdTerm term, LdField owner) throws JsonParseException, IOException, LdParseException {
    LdNode result = null;
    
    if ( node instanceof ObjectNode) {
      result = parseObject((ObjectNode) node, parent, owner);
    } else if (node.isBoolean()) {
      LdLiteral literal = new LdLiteral();
      literal.setBooleanValue(node.getBooleanValue());
      result = literal;
    } else if (node.isFloatingPointNumber()) {
      LdLiteral literal = new LdLiteral();
      literal.setDoubleValue(node.getDoubleValue());
      result = literal;
    } else if (node.isLong() || node.isInt()) {
      LdLiteral literal = new LdLiteral();
      literal.setLongValue(node.getLongValue());
      result = literal;
    } else if (node.isTextual()) {
      result = parseStringNode(node.getTextValue(), parent, term);
    } else if (node.isArray()) {
      result = parseArray((ArrayNode)node, parent, term, owner);
    }
     
    return result;
  }
  
  private LdNode parseStringNode(String text, LdObject parent, LdTerm term) throws JsonParseException, IOException {
    LdNode result = null;
    String type = (term==null) ? null : term.getRawTypeIRI();
    if ("@id".equals(type)) {
      LdContext context = (parent==null) ? null : parent.getContext();
      LdObjectImpl object = new LdObjectImpl(context);
      object.setId(text);
      result = object;
    } else {

      LdLiteral literal = new LdLiteral();
      literal.setStringValue(text);
      result = literal;
      
    }
    return result;
  }

  private LdNode parseArray(ArrayNode array, LdObjectImpl parent, LdTerm term, LdField owner) throws JsonParseException, IOException, LdParseException {
    LdList list = new LdList(LdContainerType.SET);
    parseElements(array, term, parent, list, owner);
    
    return list;
  }

  private void parseElements(ArrayNode array, LdTerm term, LdObjectImpl parent, LdList list,
      LdField field) throws JsonParseException, IOException, LdParseException {
    
    for (int i=0; i<array.size(); i++) {
        JsonNode node = array.get(i);
        list.add(parseNode(node, parent, term, field));
    }
  }
    

  private LdNode parseObject(ObjectNode node, LdObjectImpl parent, LdField owner) throws LdParseException, JsonParseException, IOException {
    JsonNode value = node.get("@value");
    if (value != null) {
      return parseExtendedValue(node, parent, owner);
    }
    
    JsonNode contextNode = node.get("@context");
    LdContext context = null;
    LdContext parentContext = (parent==null) ? null : parent.getContext();
    if (contextNode != null) {
      try {
        context = contextReader.parseContext(contextNode);
        context.setParentContext(parentContext);
        
      } catch (Exception e) {
        throw new LdParseException(e);
      } 
    } else {
      context = parentContext;
    }
    if (context == null) {
      context = new LdContext();
    }
    LdObjectImpl object = new LdObjectImpl(context);
    object.setOwner(owner);
    parseFields(node, object, (ObjectNode)node, parent);
    return object;
  }

  private LdNode parseExtendedValue(ObjectNode object, LdObjectImpl parent,
      LdField owner) {
    LdLiteral literal = new LdLiteral();
    
    Iterator<Entry<String,JsonNode>> sequence = object.getFields();
    while (sequence.hasNext()) {
      Entry<String,JsonNode> entry = sequence.next();
      String fieldName = entry.getKey();
      JsonNode node = entry.getValue();
      
      if (fieldName.equals("@language")) {
        literal.setLanguage(node.getTextValue());
      } else if (fieldName.equals("@type")) {
        literal.setType(node.getTextValue());
      } else if ("@value".equals(fieldName)) {
        if (node.isBoolean()) {
          literal.setBooleanValue(node.getBooleanValue());
        } else if (node.isFloatingPointNumber()) {
          literal.setDoubleValue(node.getDoubleValue());
        } else if (node.isLong()) {
          literal.setLongValue(literal.getLongValue());
        } else if (node.isTextual()) {
          literal.setStringValue(node.getTextValue());
        }
      }
    }
    
    return literal;
  }

  private void parseFields(ObjectNode json, LdObjectImpl object, ObjectNode node,
      LdObjectImpl parent) throws JsonParseException, IOException, LdParseException {
    
    FieldList fieldList = new FieldList();
    object.setFieldList(fieldList);
    LdContext context = object.getContext();
    
    Iterator<Entry<String,JsonNode>> sequence = json.getFields();
    while (sequence.hasNext()) {
      Entry<String,JsonNode> entry = sequence.next();
      String fieldName = entry.getKey();
      JsonNode value = entry.getValue();
      
      if ("@context".equals(fieldName)) {
        continue;
      } else if ("@id".equals(fieldName)) {
        object.setId(value.getTextValue());
      } else if ("@type".equals(fieldName)) {
        object.setRawType(value.getTextValue());
      } else {

        LdTerm term = (context==null) ? null : context.getTerm(fieldName);
        LinkedLdField field = new LinkedLdField(object);
  
        // If the field name contains a '#', '/', or ':'
        // then the simple name is substring after that delimiter.
        
        int delim = fieldName.lastIndexOf('#');
        if (delim < 0) {
          delim = fieldName.lastIndexOf('/');
        }
        if (delim < 0) {
          delim = fieldName.lastIndexOf(':');
        }
        if (delim >= 0) {
          field.setLocalName(fieldName.substring(delim+1));
        } else {
          field.setLocalName(fieldName);
        }
        
        
        String propertyIRI = (context == null) ? fieldName : context.expand(fieldName);
        field.setPropertyURI(propertyIRI);
        
        LdNode v = parseNode(value, object, term, field);
        field.setValue(v);
        setValueOwner(field);
  
        setType(field, fieldName, context);
        object.fieldList.add(field);
      }
      
     
      
    }
    
  }
  
  /**
   * Set the type of the given field based on the given context, but only if the 
   * field does not already have the type declared.
   */
  private void setType(LinkedLdField field, String fieldName, LdContext context) {
    if (context == null) return;
    
    LdNode value = field.getValue();
    if (value == null) return;
    
    LdTerm term = context.getTerm(fieldName);
    if (term == null) return;
    
    if (value instanceof LdObjectImpl) {
      LdObjectImpl object = (LdObjectImpl) value;
      if (object.getTypeIRI() != null) return;
      object.setTypeIRI(term.getTypeIRI());
      
    } else if (value instanceof LdLiteral) {
      LdLiteral literal = (LdLiteral) value;
      if (literal.getType() == null) {
        literal.setType(term.getTypeIRI());
      }
    }
    
  }

  private void setValueOwner(LdField field) {
    
    LdNode value = field.getValue();
    
    if (value instanceof LdObjectImpl) {
      LdObjectImpl object = (LdObjectImpl) value;
      object.setOwner(field);
    } else if (value instanceof LdContainerImpl) {
      LdContainerImpl container = (LdContainerImpl) value;
      container.setOwner(field);
    }
    
  }

  @Override
  public void setStreaming(boolean streaming) {

  }

  @Override
  public boolean isStreaming() {
    return false;
  }

}
