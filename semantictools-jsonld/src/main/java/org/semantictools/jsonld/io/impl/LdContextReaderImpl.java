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
package org.semantictools.jsonld.io.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonToken;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.ObjectNode;
import org.semantictools.jsonld.LdClass;
import org.semantictools.jsonld.LdContainerType;
import org.semantictools.jsonld.LdContext;
import org.semantictools.jsonld.LdContextManager;
import org.semantictools.jsonld.LdContextParseException;
import org.semantictools.jsonld.LdDatatype;
import org.semantictools.jsonld.LdDatatypeManager;
import org.semantictools.jsonld.LdProperty;
import org.semantictools.jsonld.LdQualifiedRestriction;
import org.semantictools.jsonld.LdRestriction;
import org.semantictools.jsonld.LdTerm;
import org.semantictools.jsonld.Whitespace;
import org.semantictools.jsonld.io.ErrorHandler;
import org.semantictools.jsonld.io.LdContextReader;

public class LdContextReaderImpl implements LdContextReader {
  
  private LdContextManager manager;
  private ErrorHandler errorHandler;
  

  public LdContextReaderImpl(LdContextManager manager) {
    this.manager = manager;
  }

  /**
   * Returns the LdContextManager that is used to resolve external contexts.
   */
  public LdContextManager getManager() {
    return manager;
  }

  /**
   * Sets the LdContextManager that is used to resolve external contexts.
   */
  public void setManager(LdContextManager manager) {
    this.manager = manager;
  }

  private LdContext parseContext(JsonParser parser) throws JsonParseException, IOException {
    LdContext context = new LdContext();
    parseContext(parser, context);
    return context;
  }

  /**
   * Perform the work of parsing a JSON stream into the given LdContext.
   */
  private void parseContext(JsonParser parser, LdContext context) throws JsonParseException, IOException {
    
    JsonToken token = null;
    while ( (token=parser.nextToken()) != null) {
      if (token == JsonToken.END_OBJECT) break;
      
      if (token != JsonToken.FIELD_NAME) {
        throw new JsonParseException("Expected a field name", parser.getCurrentLocation());
      }
      
      String fieldName = parser.getCurrentName();
      parseField(parser, context, fieldName);
      
    }
    
  }

  /**
   * For the given fieldName, parse an LdTerm and add it to the given context.
   */
  private void parseField(JsonParser parser, LdContext context, String fieldName) throws JsonParseException, IOException {
    LdTerm term = new LdTerm();
    term.setShortName(fieldName);
    context.add(term);
    
    JsonToken token = parser.nextToken();
    switch (token) {
    case VALUE_STRING :
      term.setRawIRI(parser.getText());
      break;
      
    case START_OBJECT:
      parseTermFields(parser, term);
      break;
      
    default:
      throw new JsonParseException("Expected IRI value or object definition of term", parser.getCurrentLocation());
    }
    
  }

  /**
   * Parse the fields within a JSON-LD term from the given parser into the given LdTerm object.
   */
  private void parseTermFields(JsonParser parser, LdTerm term) throws JsonParseException, IOException {
  
    JsonToken token = null;
    while ( (token=parser.nextToken()) != JsonToken.END_OBJECT) {
      switch (token) {
      case FIELD_NAME :
        String fieldName = parser.getCurrentName();
        if ("@id".equals(fieldName)) {
          term.setRawIRI(readString(parser));
          
        } else if ("@type".equals(fieldName)) {
          term.setRawTypeIRI(readString(parser));
          
        } else if ("@language".equals(fieldName)) {
          term.setLanguage(readString(parser));
          
        } else if ("@container".equals(fieldName)) {
          term.setContainerType(readContainerType(parser));
          
        } else if ("@minCardinality".equals(fieldName)) {
          term.setMinCardinality(Integer.parseInt(readString(parser)));
          
        } else if ("datatype".equals(fieldName)) {
          parseDatatype(parser, term);
          
        } else if ("class".equals(fieldName)) {
          parseClass(parser, term);
          
        } else if("property".equals(fieldName)) {
          parseProperty(parser, term);
          
        } else {
          skipValue(parser);
        }
        break;
        
      default:
        throw new JsonParseException("Expected field name", parser.getCurrentLocation());
      }
      
      
    }
    
  }

  private void parseProperty(JsonParser parser, LdTerm term) throws JsonParseException, IOException {
    
    LdProperty property = new LdProperty();
    term.setProperty(property);
    
    assertToken(parser, JsonToken.START_OBJECT);
    
    while ( parser.nextToken() == JsonToken.FIELD_NAME) {
      String fieldName = parser.getCurrentName();
      if ("domain".equals(fieldName)) {
        parseDomain(parser, property);
      }
    }
    
    assertCurrentToken(parser, JsonToken.END_OBJECT);
    
  }

  private void parseDomain(JsonParser parser, LdProperty property) throws JsonParseException, IOException {

    assertToken(parser, JsonToken.START_ARRAY);
    
    while ( parser.nextToken() == JsonToken.VALUE_STRING) {
      
      String domainValue = parser.getText();
      property.addDomain(domainValue);
    }
    
    assertCurrentToken(parser, JsonToken.END_ARRAY);
    
  }

  private void assertCurrentToken(JsonParser parser, JsonToken expected) throws JsonParseException {
    JsonToken actual = parser.getCurrentToken();
    if (expected != actual) {
      throw new JsonParseException("Expected " + expected + ", but found " + actual, parser.getCurrentLocation());
    }
    
  }

  private void parseClass(JsonParser parser, LdTerm term) throws JsonParseException, IOException {
    
    LdClass rdfClass = new LdClass(null);
    term.setRdfClass(rdfClass);
    assertToken(parser, JsonToken.START_OBJECT);
    
    while ( parser.nextToken() == JsonToken.FIELD_NAME) {
      String fieldName = parser.getCurrentName();
      if ("supertype".equals(fieldName))  {
        parserSupertype(parser, rdfClass);
        
      } else if ("restriction".equals(fieldName)) {
        parseRestrictionArray(parser, rdfClass);
        
      } else {
        skipValue(parser);
      }
      
    }
  }

  private void parseRestrictionArray(JsonParser parser, LdClass rdfClass) throws JsonParseException, IOException {
    
    
    assertToken(parser, JsonToken.START_ARRAY);
    while (parser.nextToken() == JsonToken.START_OBJECT) {
      parseRestriction(parser, rdfClass);
    }
    
    assertCurrentToken(parser, JsonToken.END_ARRAY);
    
  }

  private void parseRestriction(JsonParser parser, LdClass rdfClass) throws JsonParseException, IOException {


    LdRestriction restriction = new LdRestriction();
    
    while (parser.nextToken() == JsonToken.FIELD_NAME) {
      String fieldName = parser.getCurrentName();
      if ("onProperty".equals(fieldName)) {
        restriction.setPropertyURI(readString(parser));
        
      } else if ("maxCardinality".equals(fieldName)) {
        restriction.setMaxCardinality(readInt(parser));
        
      } else if ("minCardinality".equals(fieldName)) {
        restriction.setMinCardinality(readInt(parser));
        
      } else if ("qualifiedRestriction".equals(fieldName)) {
        parseQualifiedRestrictionArray(parser, restriction);
        
      } else if ("allValuesFrom".equals(fieldName)) {
        
        restriction.setAllValuesFrom(readString(parser));
        
      } else {
        skipValue(parser);
      }
      
    }
    
    assertCurrentToken(parser, JsonToken.END_OBJECT);

    rdfClass.add(restriction);
    
  }

  private void parseQualifiedRestrictionArray(
      JsonParser parser, LdRestriction restriction) throws JsonParseException, IOException {
    
    assertToken(parser, JsonToken.START_ARRAY);
    
    while (parser.nextToken() == JsonToken.START_OBJECT) {
      parseQualifiedRestriction(parser, restriction);
    }
    
    assertCurrentToken(parser, JsonToken.END_ARRAY);
    
  }

  private void parseQualifiedRestriction(JsonParser parser,
      LdRestriction restriction) throws JsonParseException, IOException {
    
    LdQualifiedRestriction qr = new LdQualifiedRestriction();
    restriction.add(qr);
    
    while (parser.nextToken() == JsonToken.FIELD_NAME) {
      String fieldName = parser.getCurrentName();
      if ("onClass".equals(fieldName)) {
        qr.setRangeURI(readString(parser));
        
      } else if ("maxCardinality".equals(fieldName)) {
        qr.setMaxCardinality(readInt(parser));
        
      } else if ("minCardinality".equals(fieldName)) {
        qr.setMinCardinality(readInt(parser));
        
      }
    }
    
    assertCurrentToken(parser, JsonToken.END_OBJECT);
    
  }

  private void parserSupertype(JsonParser parser, LdClass rdfClass) throws JsonParseException, IOException {
    
    assertToken(parser, JsonToken.START_ARRAY);
    
    while ( parser.nextToken() == JsonToken.VALUE_STRING) {
      String supertypeURI = parser.getText();
      LdClass superClass = new LdClass(supertypeURI);
      rdfClass.addSupertype(superClass);
    }
    
  }

  private void parseDatatype(JsonParser parser, LdTerm term) throws JsonParseException, IOException {
    
    LdDatatype datatype = new LdDatatype();
    term.setDatatype(datatype);
    
    
    assertToken(parser, JsonToken.START_OBJECT);

    while ( parser.nextToken() == JsonToken.FIELD_NAME) {
      String fieldName = parser.getCurrentName();
      
      if (fieldName.equals("base")) {
        String baseURI = readString(parser);
        LdDatatype base = new LdDatatype();
        base.setURI(baseURI);
        datatype.setBase(base);
        
      } else if ("length".equals(fieldName)) {
        datatype.setLength(readInt(parser));
        
      } else if ("minLength".equals(fieldName)) {
        datatype.setMinLength(readInt(parser));
        
      } else if ("maxLength".equals(fieldName)) {
        datatype.setMaxLength(readInt(parser));
        
      } else if ("pattern".equals(fieldName)) {
        datatype.setPattern(Pattern.compile(readString(parser)));
        
      } else if ("whitespace".equals(fieldName)) {
        String name = readString(parser);
        
        Whitespace ws = 
            "collapse".equals(name) ? Whitespace.COLLAPSE :
            "replace".equals(name) ? Whitespace.REPLACE :
            "preserve".equals(name) ? Whitespace.PRESERVE :
            null;
        
        if (ws == null) {
          throw new JsonParseException("Unrecognized whitespace: " + name, parser.getCurrentLocation());
        }
        datatype.setWhitespace(ws);
        
      } else if ("maxInclusive".equals(fieldName)) {
        datatype.setMaxInclusive(readNumber(parser));
        
      } else if ("minInclusive".equals(fieldName)) {
        datatype.setMinInclusive(readNumber(parser));
        
      } else if ("maxExclusive".equals(fieldName)) {
        datatype.setMaxExclusive(readNumber(parser));
        
      } else if ("minExclusive".equals(fieldName)) {
        datatype.setMinExclusive(readNumber(parser));
        
      } else if ("totalDigits".equals(fieldName)) {
        datatype.setTotalDigits(readInt(parser));
        
      } else if ("fractionDigits".equals(fieldName)) {
        datatype.setFractionDigits(readInt(parser));
        
      } else {
        skipValue(parser);
      }
      
    }
    
    
    
    
    
  }

  private Number readNumber(JsonParser parser) throws JsonParseException, IOException {
    JsonToken token = parser.nextToken();
    switch (token) {
    case VALUE_NUMBER_INT: 
      return new Long(parser.getLongValue());
      
    case VALUE_NUMBER_FLOAT:
      return new Double(parser.getDoubleValue());
      
      default:
        throw new JsonParseException(
            "Expected int or float value, but found " + parser.getCurrentToken(), parser.getCurrentLocation());
    }
  }

  private Integer readInt(JsonParser parser) throws JsonParseException, IOException {
    assertToken(parser, JsonToken.VALUE_NUMBER_INT);
    return new Integer(parser.getIntValue());
  }

  private void assertToken(JsonParser parser, JsonToken expected) throws JsonParseException, IOException {
    if (parser.nextToken() != expected) {
      throw new JsonParseException("Expected " + expected, parser.getCurrentLocation());
    }
    
  }

  private void skipValue(JsonParser parser) throws JsonParseException, IOException {
    int objectDepth=0;
    int arrayDepth=0;
    JsonToken token = null;
    while ( (token = parser.nextToken()) != null) {
      switch (token) {
      case END_ARRAY :
        arrayDepth--;
        break;
        
      case END_OBJECT :
        objectDepth--;
        break;
        
      case FIELD_NAME :
        continue;
        
      case START_ARRAY :
        arrayDepth++;
        break;
        
      case START_OBJECT :
        objectDepth++;
        break;
        
      case VALUE_FALSE :
      case VALUE_NULL :
      case VALUE_NUMBER_FLOAT :
      case VALUE_NUMBER_INT :
      case VALUE_STRING :
      case VALUE_TRUE :
        // Do nothing
        break;
      }
      if (objectDepth == 0 && arrayDepth==0) {
        break;
      }
    }
    
  }

  private LdContainerType readContainerType(JsonParser parser) throws JsonParseException, IOException {
    String text = readString(parser);
    
    return 
      "@set".equals(text) ? LdContainerType.SET :
      "@list".equals(text) ? LdContainerType.LIST :
      LdContainerType.UNDEFINED;
  }

  private String readString(JsonParser parser) throws JsonParseException, IOException {
    
    JsonToken token = parser.nextToken();
    if (token != JsonToken.VALUE_STRING) {
      throw new JsonParseException("Expected string value", parser.getCurrentLocation());
    }
    
    return parser.getText();
  }
  
  /**
   * Parse the JSON-LD context from the given stream, associate it with the given contextURI,
   * and store the result in the internal LdContextManager.
   * @throws LdContextParseException 
   */
  public LdContext parseExternalContext(String contextURI, InputStream stream) throws 
  IOException, LdContextParseException {
    LdContext context = parseExternalContext(stream);
    context.setContextURI(contextURI);
    
    return context;
  }


  @Override
  public LdContext parserExternalContext(Reader reader)
      throws IOException, LdContextParseException {
    

    LdContext context = null;
    JsonFactory factory = new JsonFactory();
    factory.enable(JsonParser.Feature.ALLOW_COMMENTS);
    JsonParser jsonParser = factory.createJsonParser(reader);
    JsonToken token=null;
    while ( (token = jsonParser.nextToken()) != null) {
     
      if (token == JsonToken.FIELD_NAME) {
        String fieldName = jsonParser.getCurrentName();
        if ("@context".equals(fieldName)) {
          context = parseContextField(jsonParser);
        }
      }
    }
    return context;
  }

  @Override
  public LdContext parseExternalContext(InputStream stream) throws JsonParseException,
      IOException, LdContextParseException {

    LdContext context = null;
    JsonFactory factory = new JsonFactory();
    factory.enable(JsonParser.Feature.ALLOW_COMMENTS);
    JsonParser jsonParser = factory.createJsonParser(stream);
    JsonToken token=null;
    while ( (token = jsonParser.nextToken()) != null) {
     
      if (token == JsonToken.FIELD_NAME) {
        String fieldName = jsonParser.getCurrentName();
        if ("@context".equals(fieldName)) {
          context = parseContextField(jsonParser);
        }
      }
    }
    return context;
  }

  @Override
  public LdContext parseContextField(JsonParser jsonParser) throws IOException, LdContextParseException {
    LdContext context = null;
    JsonToken token = jsonParser.nextToken();
    boolean external = false;
    switch (token) {
    case VALUE_STRING:
      context = loadExternalContext(jsonParser.getText());
      external = true;
      break;
      
    case START_ARRAY:
      context = new LdContext();
      parseContextArray(jsonParser, context);
      break;
      
    case START_OBJECT:
      context = parseContext(jsonParser);
      break;
    }
    
    if (context != null) {
      context.close();
      prepareOwlClasses(context);
      
      // We don't need to resolve references if the context is external
      // and obtained from the LdContextManager.
      //
      if (!external && context.isEnhanced()) {
        resolveRefereces(context);
      }
    }
    
    return context;
  }

  /** Add RDF class for owl:Thing if it is referenced explicitly.
   * This method is a bit of a hack.
   * TODO: Find a more elegant solution.
   * @param context
   */
  private void prepareOwlClasses(LdContext context) {
    LdTerm term = context.getTerm("http://www.w3.org/2002/07/owl#Thing");
    if (term == null) return;
    
    LdClass type = term.getRdfClass();
    if (type == null) {
      type = new LdClass(term.getIRI());
      term.setRdfClass(type);
    }
    
  }

  private void resolveRefereces(LdContext context) {
    
    List<LdTerm> termList = context.listTerms();
    if (termList == null) return;
    for (LdTerm term : termList) {
      resolve(context, term);
    }
    
  }

  private void resolve(LdContext context, LdTerm term) {
    resolveDatatype(context, term);
    resolveClass(context, term);
    
  }

  private void resolveClass(LdContext context, LdTerm term) {
    LdClass rdfClass = term.getRdfClass();
    if (rdfClass != null) {
      String uri = term.getIRI();
      rdfClass.setURI(uri);
      resolveSupertypes(context, rdfClass);
    }
    
  }

  private void resolveSupertypes(LdContext context, LdClass rdfClass) {
    List<LdClass> superList = rdfClass.listSupertypes();
    if (superList != null && !superList.isEmpty()) {
      List<LdClass> newList = new ArrayList<LdClass>();
      for (LdClass original : superList) {
        LdClass newClass = context.getClass(original.getURI());
        if (newClass == null) {
          newClass = original;
        }
        newList.add(newClass);
      }
      rdfClass.setSupertypes(newList);
    }
    
  }

  private void resolveDatatype(LdContext context, LdTerm term) {
    LdDatatype datatype = term.getDatatype();
    if (datatype != null) {

      String uri = term.getIRI();
      int mark = uri.lastIndexOf('#');
      if (mark < 0) {
        mark = uri.lastIndexOf('/');
      }
      String localName = uri.substring(mark+1);
      
      datatype.setLocalName(localName);
      datatype.setNamespace(uri.substring(0, mark));
      datatype.setURI(uri);
      
      
      LdDatatype base = datatype.getBase();
      if (base != null) {
        String baseURI = base.getURI();
        LdDatatype newBase = LdDatatypeManager.getXsdTypeByURI(baseURI);
        if (newBase == null) {
          LdTerm baseTerm = context.getTerm(baseURI);
          newBase = baseTerm == null ? null : baseTerm.getDatatype();
        }
        if (newBase != null) {
          datatype.setBase(newBase);
        }
        
      }
      
    }
    
  }

  private void parseContextArray(JsonParser jsonParser, LdContext context) throws IOException, LdContextParseException {
    
    JsonToken token = null;
    LdContext child = null;
    while ( (token=jsonParser.nextToken()) != null) {
      switch (token) {
      case END_ARRAY: return;
      
      case VALUE_STRING:
        try {
          child = loadExternalContext(jsonParser.getText());
          context.add(child);
        } catch (IOException e) {
          if (errorHandler == null) {
            throw e;
          } else {
            errorHandler.handleError(e);
          }
        } catch (LdContextParseException e) {
          if (errorHandler == null) {
            throw e;
          } else {
            errorHandler.handleError(e);
          }
          
        }
        break;
        
      case START_OBJECT :
        child = parseContext(jsonParser);
        context.add(child);
        break;
      }
    }
    
  }

  private LdContext loadExternalContext(String contextURI) throws IOException, LdContextParseException {
    if (manager == null) {
      String msg = "Cannot load external contexts";
      handleError( new LdContextParseException(msg) );
    }
    LdContext context = manager.findContext(contextURI);
    if (context == null) {
      handleError( new LdContextParseException("JSON-LD context not found: " + contextURI));
    }
    return context;
  }
  
  protected void handleError(Throwable oops) throws IOException, LdContextParseException {
    if (errorHandler==null) {
      if (oops instanceof IOException) throw (IOException) oops;
      if (oops instanceof LdContextParseException) throw (LdContextParseException) oops;
      throw new LdContextParseException(oops);
    } else {
      errorHandler.handleError(oops);
    }
  }

  @Override
  public void setErrorHandler(ErrorHandler handler) {
    errorHandler = handler;
  }

  @Override
  public ErrorHandler getErrorHandler() {
    return errorHandler;
  }

  @Override
  public LdContext parseContext(JsonNode node) throws LdContextParseException, IOException {
    LdContext context = null;
    boolean external = false;
    
    if (node.isTextual()) {
      context = loadExternalContext(node.asText());
      external = true;
      
    } else if (node instanceof ObjectNode) {
      context = new LdContext();
      parseContextObject((ObjectNode) node, context);
    } else if (node instanceof ArrayNode) {
      context = new LdContext();
      parseContextArray((ArrayNode)node, context);
    }
    
    if (context != null) {
      context.close();
      prepareOwlClasses(context);
      
      // We don't need to resolve references if the context is external
      // and obtained from the LdContextManager.
      //
      if (!external && context.isEnhanced()) {
        resolveRefereces(context);
      }
    }
    
    return context;
  }

  private void parseContextArray(ArrayNode array, LdContext context) throws LdContextParseException, IOException {
    
    for (int i=0; i<array.size(); i++) {
      JsonNode node = array.get(i);
      if (node.isTextual()) {
          LdContext child;
          try {
            child = loadExternalContext(node.getTextValue());
            if (child != null) {
              context.add(child);
            }
          } catch (LdContextParseException e) {
            if (errorHandler != null) {
              errorHandler.handleError(e);
            } else {
              throw e;
            }
          } catch (IOException e) {
            if (errorHandler != null) {
              errorHandler.handleError(e);
            } else {
              throw e;
            }
          }
      } else if (node instanceof ObjectNode) {
        LdContext child = new LdContext();
        try {
          parseContextObject((ObjectNode)node, child);
          context.add(child);
        } catch (JsonParseException e) {
          if (errorHandler != null) {
            errorHandler.handleError(e);
          } else {
            throw e;
          }
        }
      }
    }
  }

  private void parseContextObject(ObjectNode node, LdContext context) throws JsonParseException, LdContextParseException {
   
    Iterator<Entry<String, JsonNode>> sequence = node.getFields();
    while (sequence.hasNext()) {
      Entry<String, JsonNode> entry = sequence.next();
      parseField(context, entry.getKey(), entry.getValue());
    }
  }

  private void parseField(LdContext context, String fieldName, JsonNode value) throws JsonParseException, LdContextParseException {
    LdTerm term = new LdTerm();
    term.setShortName(fieldName);
    context.add(term);
    
    if (value.isTextual()) {
      term.setRawIRI(value.getTextValue());
      
    } else if (value instanceof ObjectNode) {
      parseTermFields((ObjectNode)value, term);
      
    } else {
      throw new JsonParseException("Expected IRI value or object definition of term " + fieldName, null);
    }
    
  }

  private void parseTermFields(ObjectNode json, LdTerm term) throws LdContextParseException {
    JsonToken token = null;
    
    Iterator<Entry<String,JsonNode>> sequence = json.getFields();
    while (sequence.hasNext()) {
      Entry<String,JsonNode> entry = sequence.next();
      String fieldName = entry.getKey();
      JsonNode value = entry.getValue();
   
      if ("@id".equals(fieldName)) {
        term.setRawIRI(value.getTextValue());
        
      } else if ("@type".equals(fieldName)) {
        term.setRawTypeIRI(value.getTextValue());
        
      } else if ("@language".equals(fieldName)) {
        term.setLanguage(value.getTextValue());
        
      } else if ("@container".equals(fieldName)) {
        term.setContainerType(readContainerType(value));
        
      }  else if ("@minCardinality".equals(fieldName)) {
        term.setMinCardinality(value.getIntValue());
        
      }  else if ("datatype".equals(fieldName)) {
        parseDatatype(value, term);
        
      } else if ("class".equals(fieldName)) {
        parseClass(value, term);
        
      } else if("property".equals(fieldName)) {
        parseProperty(value, term);
        
      } 
    }
    
    
  }


  private void parseProperty(JsonNode value, LdTerm term) {
    // TODO Auto-generated method stub
    
  }

  private void parseClass(JsonNode value, LdTerm term) throws LdContextParseException {
    if ( ! (value instanceof ObjectNode)) {
      throw new LdContextParseException("Expected class definition to be an object in term " + term.getRawIRI());
    }
    ObjectNode object = (ObjectNode) value;
    LdClass rdfClass = new LdClass(null);
    term.setRdfClass(rdfClass);
    
    Iterator<Entry<String,JsonNode>> sequence = object.getFields();
    
    while ( sequence.hasNext() ) {
      Entry<String,JsonNode> entry = sequence.next();
      String fieldName = entry.getKey();
      value = entry.getValue();
      
      if ("supertype".equals(fieldName))  {
        parserSupertype(value, rdfClass);
        
      } else if ("restriction".equals(fieldName)) {
        parseRestrictionArray(value, rdfClass);
        
      } 
      
    }
    
  }

  private void parseRestrictionArray(JsonNode value, LdClass rdfClass) throws LdContextParseException {

    if (! (value instanceof ArrayNode)) {
      throw new LdContextParseException("Expected restriction definition to be an array");
    }
    ArrayNode array = (ArrayNode) value;
    for (int i=0; i<array.size(); i++) {
      value = array.get(i);
      parseRestriction(value, rdfClass);
    }
    
    
  }

  private void parseRestriction(JsonNode value, LdClass rdfClass) throws LdContextParseException {

    if (!(value instanceof ObjectNode)) {
      throw new LdContextParseException("Expected restriction definition to be an object");
    }
    LdRestriction restriction = new LdRestriction();
    rdfClass.add(restriction);
    ObjectNode object = (ObjectNode) value;
    Iterator<Entry<String,JsonNode>> sequence = object.getFields();
    
    while (sequence.hasNext()) {
      Entry<String,JsonNode> entry = sequence.next();
      
      String fieldName = entry.getKey();
      value = entry.getValue();
      
      if ("onProperty".equals(fieldName)) {
        restriction.setPropertyURI(value.getTextValue());
        
      } else if ("maxCardinality".equals(fieldName)) {
        restriction.setMaxCardinality(value.getIntValue());
        
      } else if ("minCardinality".equals(fieldName)) {
        restriction.setMinCardinality(value.getIntValue());
        
      } else if ("qualifiedRestriction".equals(fieldName)) {
        parseQualifiedRestrictionArray(value, restriction);
        
      }
      
    }
    
  }

  private void parseQualifiedRestrictionArray(JsonNode value,
      LdRestriction restriction) throws LdContextParseException {
    
    if (!(value instanceof ArrayNode)) {
      throw new LdContextParseException("Expected qualified restriction definition to be an array");
    }
    ArrayNode array = (ArrayNode) value;
    
    for (int i=0; i<array.size(); i++) {
      parseQualifiedRestriction(value, restriction);
    }
    
  }

  private void parseQualifiedRestriction(JsonNode value, LdRestriction restriction) throws LdContextParseException {
    
    if (! (value instanceof ObjectNode)) {
      throw new LdContextParseException("Expected qualified restriction definition to be an object");
    }
    ObjectNode object = (ObjectNode) value;
    LdQualifiedRestriction qr = new LdQualifiedRestriction();
    restriction.add(qr);
    
    Iterator<Entry<String,JsonNode>> sequence = object.getFields();
    
    while (sequence.hasNext()) {
      Entry<String,JsonNode> entry = sequence.next();
      value = entry.getValue();
      String fieldName = entry.getKey();
      if ("onClass".equals(fieldName)) {
        qr.setRangeURI(value.getTextValue());
        
      } else if ("maxCardinality".equals(fieldName)) {
        qr.setMaxCardinality(value.getIntValue());
        
      } else if ("minCardinality".equals(fieldName)) {
        qr.setMinCardinality(value.getIntValue());
        
      }
    }
    
  }

  private void parserSupertype(JsonNode value, LdClass rdfClass) throws LdContextParseException {

    if (! (value instanceof ArrayNode)) {
      throw new LdContextParseException("Expected supertype value to be an array");
    }
    ArrayNode array = (ArrayNode) value;
    for (int i=0; i<array.size(); i++) {
      value = array.get(i);
      String supertypeURI = value.getTextValue();
      LdClass superClass = new LdClass(supertypeURI);
      rdfClass.addSupertype(superClass);
    }
    
  }

  private void parseDatatype(JsonNode value, LdTerm term) throws LdContextParseException {
    LdDatatype datatype = new LdDatatype();
    term.setDatatype(datatype);
    
    if ( !(value instanceof ObjectNode)) {
      throw new LdContextParseException("Expected object for datatype description " + term.getRawIRI());
    }
    ObjectNode object = (ObjectNode) value;

    Iterator<Entry<String,JsonNode>> sequence = object.getFields();
    while (sequence.hasNext()) {
      Entry<String,JsonNode> entry = sequence.next();
      String fieldName = entry.getKey();
      value = entry.getValue();
      
      if (fieldName.equals("base")) {
        String baseURI = value.getTextValue();
        LdDatatype base = new LdDatatype();
        base.setURI(baseURI);
        datatype.setBase(base);
        
      } else if ("length".equals(fieldName)) {
        datatype.setLength(value.getIntValue());
        
      } else if ("minLength".equals(fieldName)) {
        datatype.setMinLength(value.getIntValue());
        
      } else if ("maxLength".equals(fieldName)) {
        datatype.setMaxLength(value.getIntValue());
        
      } else if ("pattern".equals(fieldName)) {
        datatype.setPattern(Pattern.compile(value.getTextValue()));
        
      } else if ("whitespace".equals(fieldName)) {
        String name = value.getTextValue();
        
        Whitespace ws = 
            "collapse".equals(name) ? Whitespace.COLLAPSE :
            "replace".equals(name) ? Whitespace.REPLACE :
            "preserve".equals(name) ? Whitespace.PRESERVE :
            null;
        
        if (ws == null) {
          throw new LdContextParseException("Unrecognized whitespace '" + name + "' in term " + term.getIRI());
        }
        datatype.setWhitespace(ws);
        
      } else if ("maxInclusive".equals(fieldName)) {
        datatype.setMaxInclusive(readNumber(value));
        
      } else if ("minInclusive".equals(fieldName)) {
        datatype.setMinInclusive(readNumber(value));
        
      } else if ("maxExclusive".equals(fieldName)) {
        datatype.setMaxExclusive(readNumber(value));
        
      } else if ("minExclusive".equals(fieldName)) {
        datatype.setMinExclusive(readNumber(value));
        
      } else if ("totalDigits".equals(fieldName)) {
        datatype.setTotalDigits(value.getIntValue());
        
      } else if ("fractionDigits".equals(fieldName)) {
        datatype.setFractionDigits(value.getIntValue());
        
      } 
      
    }
    
  }

  private Number readNumber(JsonNode value) throws LdContextParseException {
    if (value.isInt()) {
      return new Integer(value.getIntValue());
    }
    if (value.isLong()) {
      return new Long(value.getLongValue());
    }
    if (value.isDouble()) {
      return new Double(value.getDoubleValue());
    }
    throw new LdContextParseException("Expected a numeric value");
  }

  private LdContainerType readContainerType(JsonNode value) {
 String text = value.getTextValue();
    
    return 
      "@set".equals(text) ? LdContainerType.SET :
      "@list".equals(text) ? LdContainerType.LIST :
      LdContainerType.UNDEFINED;
  }


}
