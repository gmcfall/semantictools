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

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonToken;
import org.semantictools.jsonld.LdContainerType;
import org.semantictools.jsonld.LdContext;
import org.semantictools.jsonld.LdContextParseException;
import org.semantictools.jsonld.LdField;
import org.semantictools.jsonld.LdLiteral;
import org.semantictools.jsonld.LdNode;
import org.semantictools.jsonld.LdObject;
import org.semantictools.jsonld.LdTerm;
import org.semantictools.jsonld.io.LdContextReader;
import org.semantictools.jsonld.io.LdParseException;
import org.semantictools.jsonld.io.LdParser;

public class LdParserImpl implements LdParser {

  private LdContextReader contextParser;

  private boolean streaming=false;
  /**
   * Create a non-streaming LdParserImpl with the given context parser.
   */
  public LdParserImpl(LdContextReader contextParser) {
    this(contextParser, false);
  }

  @Override
  public LdNode parse(InputStream input) throws LdParseException, IOException {
    // Use a delegate to ensure that we are threadsafe.
    Delegate delegate = new Delegate();
    return delegate.parse(input);
  }

  /**
   * Create a new LdParserImpl and specify whether or not it is a streaming
   * parser.
   */
  public LdParserImpl(LdContextReader contextParser, boolean streaming) {
    this.contextParser = contextParser;
    this.streaming = streaming;
  }
  

  @Override
  public boolean isStreaming() {
    return streaming;
  }

  @Override
  public void setStreaming(boolean streaming) {
    this.streaming = streaming;
  }

  
  static enum IteratorMode {
    /**
     * Iteration has not started yet.
     */
    BEGIN,
    
    /**
     * Iteration is occurring over the lookahead elements that are
     * held in the linked list structure.
     */
    LINKED_LIST,
    
    /**
     * Iteration is occurring by parsing fields directly from the stream.
     */
    PARSER,
    
    /**
     * Iteration has ended.
     */
    END
  }
  class Delegate {
   
    private JsonParser jsonParser;
    
  
    /**  
     * The "@type" property may appear in both extended literal values and object nodes.
     * If we encounter the "@type" property, we need to buffer it until we can disambiguate
     * between these two types of nodes.  The following field serves as that buffer.
     */
    private String typeBuffer;
    
   
  
    public LdNode parse(InputStream input) throws LdParseException, IOException {
  
      JsonFactory factory = new JsonFactory();
      factory.enable(JsonParser.Feature.ALLOW_COMMENTS);
      jsonParser = factory.createJsonParser(input);
      jsonParser.nextToken();
      try {
        LdNode result = parseNode(null, null, null);
        close(result, input);
        return result;
      } catch (JsonParseException e) {
        throw new LdParseException(e);
      }
    }
    
    private void close(LdNode topNode, InputStream input) throws IOException {
      if (streaming) {
        
        //  Store the input stream in the top node so that it can be closed
        //  when the application is done streaming the data.
        
        // TODO: We should consider adding a close() method to the LdObject and LdArray
        // so that the application can explicitly close the stream instead of
        // triggering the close when the last node is read from the stream.
        
        if (topNode instanceof LdObjectImpl) {
          LdObjectImpl object = (LdObjectImpl) topNode;
          StreamingFieldList fieldList = (StreamingFieldList) object.fieldList;
          fieldList.setStream(input);
          
        } else if (topNode instanceof StreamingLdContainer) {
          StreamingLdContainer container = (StreamingLdContainer) topNode;
          container.setStream(input);
          
        }
        
      } else {
        // Since we are not streaming, we can go ahead and close the stream now.
        
        input.close();
      }
      
    }
  
  
    /**
     * Parse and return the JSON-LD node pointed at by the current token of the jsonParser.
     * @param parent  The parent object that "owns" the new node.  The JSON-LD context is
     * defined by this parent object.
     * @throws LdContextParseException 
     */
    private LdNode parseNode(LdObjectImpl parent, LdTerm term, LdField owner) throws JsonParseException, IOException, LdContextParseException {
      LdNode result = null;
      JsonToken token = jsonParser.getCurrentToken();
      if (token == null) {
        throw new JsonParseException("JSON-LD node not found.", jsonParser.getCurrentLocation());
      }
      switch (token) {
      
      case START_OBJECT:
        result = startObject(parent, owner);
        break;
  
      case VALUE_FALSE :
      case VALUE_TRUE :      
      case VALUE_NUMBER_FLOAT:      
      case VALUE_NUMBER_INT: 
        LdLiteral literal = new LdLiteral();
        result = literal;
        parseValue(literal);
        break;
        
      case VALUE_STRING:
        result = parseStringNode(parent, term);
        break;
        
      case START_ARRAY :
        result = startArray(term, parent, owner);
        break;
        
      default:
        throw new JsonParseException(
           "Expected the start of an object, array, or a literal value", jsonParser.getCurrentLocation());
        
      }
      
      return result;
    }
  
    private LdNode parseStringNode(LdObject parent, LdTerm term) throws JsonParseException, IOException {
      LdNode result = null;
      String type = (term==null) ? null : term.getRawTypeIRI();
      if ("@id".equals(type)) {
        LdContext context = (parent==null) ? null : parent.getContext();
        LdObjectImpl object = new LdObjectImpl(context);
        object.setId(jsonParser.getText());
        result = object;
      } else {
  
        LdLiteral literal = new LdLiteral();
        literal.setStringValue(jsonParser.getText());
        result = literal;
        
      }
      return result;
    }
  
    /**
     * Return a new LdContainer that is either a streaming or non-streaming container
     * depending on how this parser is configured.
     * If the parser is non-streaming, then all elements of the container will be 
     * parsed eagerly.
     * @throws LdContextParseException 
     */
    private LdNode startArray(LdTerm term, LdObjectImpl parent, LdField field) throws JsonParseException, IOException, LdContextParseException {
      if (streaming) {
        return new StreamingLdContainer(LdContainerType.SET);
      }
      
      LdList list = new LdList(LdContainerType.SET);
      parseElements(term, parent, list, field);
      
      return list;
    }
  
    private void parseElements(LdTerm term, LdObjectImpl parent, LdList list, LdField field) throws JsonParseException, IOException, LdContextParseException {
      
      JsonToken token = null;
      
      while ( (token=jsonParser.nextToken()) != null ) {
        switch (token) {
        case END_ARRAY :
          return;
          
        default :
          list.add(parseNode(parent, term, field));
          
        }
      }
      
      
    }
  
    /**
     * Handle the condition where the jsonParser has encountered the opening 
     * brace for an object or extended value.
     * @throws LdContextParseException 
     */
    private LdNode startObject(LdObjectImpl parent, LdField owner) throws JsonParseException, IOException, LdContextParseException {
      typeBuffer = null;
      JsonToken token = null;
      while ( (token=jsonParser.nextToken()) != null) {
  
        switch (token) {
        case FIELD_NAME:
          String fieldName = jsonParser.getCurrentName();
          LdNode node = createNode(parent, fieldName, owner);
          if (node != null) {
            return node;
          }
          break;
          
        case END_OBJECT:
          
          LdObjectImpl object = new LdObjectImpl(getContext(parent));
          object.setFieldList(new FieldList());
          object.setRawType(typeBuffer);
          typeBuffer = null;
          return object;
          
        default:
          throw new JsonParseException("Expected a field or object start", jsonParser.getCurrentLocation());
        }
      }
      return null;
      
    }
    
    /**
     * Based on the name for a field, create an appropriate type of node (LdObject, LdLiteral or LdContainer),
     * and consume the field. If the fieldName is &#064;type, then we won't be able to disambiguate
     * between LdObject and LdLiteral, but otherwise the node type is determined by the fieldName.
     * @throws LdContextParseException 
     */
    private LdNode createNode(LdObjectImpl parent, String fieldName, LdField owner) throws JsonParseException, IOException, LdContextParseException {
      
      LdNode node = null;
      if ("@context".equals(fieldName) || "@id".equals(fieldName) || !fieldName.startsWith("@")) {
        node = createObject(parent, fieldName, owner);
        
      } else if ("@type".equals(fieldName)) {
        // The node is either an object or an extended literal, but we
        // cannot disambiguate between the two options, so buffer the type value.
        
        typeBuffer = readString();
        
      } else if ("@value".equals(fieldName) || "@language".equals(fieldName)) {
        node = createLiteral(fieldName);
        
      } 
         
      
      return node;
    }
  
  
    private LdNode createLiteral(String fieldName) throws JsonParseException, IOException {
      LdLiteral literal = new LdLiteral();
      literal.setType(typeBuffer);
      typeBuffer = null;
      
      addLiteralField(literal, fieldName);
      
      return null;
    }
  
    private void addLiteralField(LdLiteral literal, String fieldName) throws JsonParseException, IOException {
      
      if ("@type".equals(fieldName)) {
        literal.setType(readString());
        
      } else if ("@language".equals(fieldName)) {
        literal.setLanguage(readString());
        
      } else if ("@value".equals(fieldName)) {
        jsonParser.nextToken();
        parseValue(literal);
        
      }
      
    }
  
    /**
     * Examine the current token, and based on that token, read a value from the jsonParser
     * and record the value in the given literal.
     */
    private void parseValue(LdLiteral literal) throws JsonParseException, IOException {
      
      JsonToken token = jsonParser.getCurrentToken();
      switch (token) {
      
      case VALUE_FALSE :
      case VALUE_TRUE :
        literal.setBooleanValue(jsonParser.getBooleanValue());
        break;
        
      case VALUE_NUMBER_FLOAT:
        literal.setDoubleValue(jsonParser.getDoubleValue());
        break;
        
      case VALUE_NUMBER_INT:
        literal.setLongValue(jsonParser.getLongValue());
        break;
        
      case VALUE_STRING:
        literal.setStringValue(jsonParser.getText());
        break;
      }
      
    }
    
    private LdContext getContext(LdObject object) {
      return object==null ? null : object.getContext();
    }
  
    /**
     * Create a new LdObject and process the fields on that object.
     * @param fieldName  The name of the first field on the object.  The jsonParser is positioned immediately after
     * the name of this field.
     * @throws LdContextParseException 
     */
    private LdObjectImpl createObject(LdObjectImpl parent, String fieldName, LdField owner) throws IOException, LdContextParseException {
      LdObjectImpl object = new LdObjectImpl(getContext(parent));
      object.setOwner(owner);
  
      FieldList list = streaming ? new StreamingFieldList(object) : new FieldList();
      object.setFieldList(list);
      
      if (typeBuffer != null) {
        object.setRawType(typeBuffer);
        typeBuffer = null;
      }
      addField(object, fieldName);    
      
      // For a non-streaming parser, we need to parse all fields on the object.
      // For a streaming parser, we only need to parse the JSON-LD keyword fields, which
      // are assumed to appear at the beginning of the object.  Thus, for a streaming
      // parser, we only need to parse more fields as long as the current field starts with '@'
      //
      if (!streaming || fieldName.startsWith("@")) {
        parseFields(object);
      }
      
      
      return object;
    }
  
    /**
     * Parse fields for the given object.
     * 
     * For a non-streaming parser, we need to parse all fields on the object.
     * For a streaming parser, we only need to parse the JSON-LD keyword fields, which
     * are assumed to appear at the beginning of the object.  Thus, for a streaming
     * parser, we only need to parse more fields until we encounter a field that is not a 
     * JSON-LD keyword.
     * @throws LdContextParseException 
     */
    private void parseFields(LdObjectImpl object) throws JsonParseException, IOException, LdContextParseException {
      
      JsonToken token = null;
      while ( (token=jsonParser.nextToken()) != null) {
        switch (token) {
        case FIELD_NAME:
          String fieldName = jsonParser.getCurrentName();
          addField(object, fieldName);
          if (streaming && !fieldName.startsWith("@")) {
            // This is a streaming parser, so we won't parse any more fields eagerly.
            // The remaining fields will be parsed by the StreamingFieldIterator.
            return;
          }
          break;
          
        case END_OBJECT:
          return;
          
        default:
          throw new JsonParseException(
              "Unexpected token while parsing fields: " + token, jsonParser.getCurrentLocation());
        }
      }
      
    }
    
    
  
    /**
     * Add a field with the given name to the specified object, parsing the value from the stream.
     * @throws LdContextParseException 
     */
    private LinkedLdField addField(LdObjectImpl object, String fieldName) throws IOException, LdContextParseException {
      LinkedLdField field = null;
      if ("@context".equals(fieldName)) {
        LdContext context = contextParser.parseContextField(jsonParser);
        object.setContext(context);
        
      } else if ("@id".equals(fieldName)) {
        object.setId(readString());
        
      } else if ("@type".equals(fieldName)) {
        object.setRawType(readString());
        
      } else {
        LdContext context = object.getContext();
        LdTerm term = (context==null) ? null : context.getTerm(fieldName);
        field = new LinkedLdField(object);
  
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
        
        
        jsonParser.nextToken();
        LdNode value = parseNode(object, term, field);
        field.setValue(value);
        setValueOwner(field);
  
        setType(field, fieldName, context);
        object.fieldList.add(field);
      }
      return field;
      
    }
  
    private void setValueOwner(LinkedLdField field) {
      
      LdNode value = field.getValue();
      
      if (value instanceof LdObjectImpl) {
        LdObjectImpl object = (LdObjectImpl) value;
        object.setOwner(field);
      } else if (value instanceof LdContainerImpl) {
        LdContainerImpl container = (LdContainerImpl) value;
        container.setOwner(field);
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
  
    private String readString() throws JsonParseException, IOException {
      if (jsonParser.nextToken() != JsonToken.VALUE_STRING) {
        throw new JsonParseException("Expected String value", jsonParser.getCurrentLocation());
      }
      
      return jsonParser.getText();
    }
    
    /**
     * This LdContainer implementation uses a specialized iterator that streams
     * elements of the container directly via the jsonParser.
     *
     */
    class StreamingLdContainer extends LdContainerImpl {
  
      private InputStream stream;
      
      public StreamingLdContainer(LdContainerType type) {
        super(type);
      }
      
      
  
      void setStream(InputStream stream) {
        this.stream = stream;
      }
  
      @Override
      public Iterator<LdNode> iterator() {
        LdField ownerField = owner();
        
        LdObjectImpl parent = ownerField==null ? null : (LdObjectImpl) ownerField.getOwner();
        return new ElementIterator(parent);
      }
  
      class ElementIterator implements Iterator<LdNode> {
  
        LdTerm term;
        LdObjectImpl parent;
        
        ElementIterator(LdObjectImpl parent) {
          this.parent = parent;
          LdField field = parent.owner();
          LdObject object = (field==null) ? null : field.getOwner();
          LdContext context = (object==null) ? null : object.getContext();
          term = (context==null) ? null : context.getTerm(field.getLocalName());
        }
  
        @Override
        public boolean hasNext() {
          try {
            JsonToken token = jsonParser.nextToken();
            switch (token) {
            case VALUE_STRING:
            case VALUE_NUMBER_FLOAT:
            case VALUE_NUMBER_INT:
            case VALUE_FALSE :
            case VALUE_TRUE:
            case START_OBJECT:
              return true;
              
            default:
              if (stream != null) {
                stream.close();
                stream = null;
              }
              return false;
              
            }
            
          } catch (Throwable e) {
            throw new RuntimeException(e);
          }
        }
  
        @Override
        public LdNode next() {
          
          try {
            return parseNode(parent, term, null);
          } catch (Exception e) {
            throw new RuntimeException(e);
          }
        }
  
        @Override
        public void remove() {
          throw new UnsupportedOperationException("Cannot remove nodes from a streaming container");
          
        }
        
      }
  
      @Override
      public int size() {
        throw new UnsupportedOperationException("The size() method is not supported when using a streaming parser.");
      }
      
      
    }
    
  
    /**
     * This list implementation attempts to provide a streaming interface to the fields.
     * Some fields may be held indefinitely in a linked list structure due to the parser's need
     * to lookahead while determining the type of node (object versus extended literal).
     * However, all other fields will be read directly via the parser upon request.  This
     * means that a client must get the iterator exactly once and must traverse all fields
     * in the list in order to continue parsing.
     * The specialized iterator operates in one of four different modes defined by the IteratorMode enum.
     * @author Greg McFall
     *
     */
    class StreamingFieldList extends FieldList {
  
      private LdObjectImpl object;
      private IteratorMode mode = IteratorMode.BEGIN;
      private InputStream stream;
      
      public StreamingFieldList(LdObjectImpl object) {
        this.object = object;
      }
      
      void setStream(InputStream stream) {
        this.stream = stream;
      }
  
  
  
      public Iterator<LdField> iterator() {
        if (mode != IteratorMode.BEGIN) {
          throw new IllegalStateException("The iterator for a StreamingFieldList may be accessed only once");
        }
        mode = IteratorMode.LINKED_LIST;
        return new StreamingFieldIterator(object);
      }
      
      /**
       * A specialized iterator that first scans the linked list structure for fields, and
       * then uses the parser to stream the remaining fields.
       * @author Greg McFall
       *
       */
      class StreamingFieldIterator extends FieldIterator {
        
        LdObjectImpl object;
        public StreamingFieldIterator(LdObjectImpl object) {
          super(object.fieldList);
        }
  
  
        @Override
        public boolean hasNext() {
          boolean more = false;
          
          switch (mode) {
          
          case LINKED_LIST :
            more = super.hasNext();
            if (more) break;
            
            // There are no more elements in the linked list,
            // so switch modes, and fall through to the next case
            
            mode = IteratorMode.PARSER;
            
          case PARSER :
            
            try{
              
              more = jsonParser.nextToken()==JsonToken.FIELD_NAME;
  
              if (!more) {
                mode = IteratorMode.END;
                if (stream != null) {
                  
                  // Since the input stream is defined on the enclosing FieldList,
                  // this must be the collection of fields in the top-level object.
                  // Since we have reached the end of the list of fields, we
                  // must close the stream now.
                  //
                  stream.close();
                  stream = null;
                }
              }
              
            } catch (Throwable oops) {
              throw new RuntimeException(oops);
            }
            break;
          }
          
          return more;
        }
        
        
  
        @Override
        public LdField next() {
          LdField field = null;
          switch (mode) {
          
          case LINKED_LIST :
            field = super.next();
            break;
            
          case PARSER :
            try {
              field = addField(object, jsonParser.getCurrentName());
            } catch (Exception e) {
              throw new RuntimeException(e);
            }
            break;
            
          }
          
          return field;
        }
      }
    }
  }

}
