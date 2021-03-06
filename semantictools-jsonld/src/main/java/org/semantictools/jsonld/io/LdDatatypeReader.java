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
package org.semantictools.jsonld.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.semantictools.jsonld.LdContext;
import org.semantictools.jsonld.LdDatatype;
import org.semantictools.jsonld.LdDatatypeManager;
import org.semantictools.jsonld.LdTerm;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import com.hp.hpl.jena.vocabulary.XSD;

public class LdDatatypeReader {
  
  private LdContext context;
  
  public LdDatatypeReader(LdContext context) {
    this.context = context;
  }
  
  public void read(Reader dataSource) throws ParserConfigurationException, SAXException, IOException {

    SAXParserFactory factory = SAXParserFactory.newInstance();
    SAXParser parser = factory.newSAXParser();
    XMLReader reader = parser.getXMLReader();
    reader.setFeature("http://xml.org/sax/features/namespaces", true);
    XmlHandler handler = new XmlHandler();
    reader.setContentHandler(handler);
    InputSource source = new InputSource(dataSource);
    
    parser.parse(source, handler);
  }
  
  public void read(String data) throws ParserConfigurationException, SAXException, IOException {
    StringReader dataSource = new StringReader(data);
    read(dataSource);
  }

  public void read(InputStream input) throws IOException, ParserConfigurationException, SAXException {
    SAXParserFactory factory = SAXParserFactory.newInstance();
    SAXParser parser = factory.newSAXParser();
    XMLReader reader = parser.getXMLReader();
    reader.setFeature("http://xml.org/sax/features/namespaces", true);
    XmlHandler handler = new XmlHandler();
    reader.setContentHandler(handler);
    
    parser.parse(input, handler);
    
    
  }
  
  static enum NumberType {
    FLOAT,
    DOUBLE,
    INT,
    LONG,
    SHORT,
    BYTE,
    NONE
  }
  
  class XmlHandler extends DefaultHandler {
    
    private Map<String, String> namespaceMap = new HashMap<String, String>();
    private String targetNamespace;
    
    private String shortName;
    private String typeURI;
    private String baseURI;
    private Integer length;
    private Integer minLength;
    private Integer maxLength;
    private String pattern;
    private Number maxInclusive;
    private Number minInclusive;
    private Number maxExclusive;
    private Number minExclusive;
    private Integer totalDigits;
    private Integer fractionDigits;
    private NumberType numberType = NumberType.NONE;
    private StringBuilder characters;
    
    
    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
      
      if ("schema".equals(localName)) {
        targetNamespace = toRdfNamespace(attributes.getValue("targetNamespace"));
        
        parseNamespaces(attributes);
        
      } else if ("simpleType".equals(localName)) {
        shortName = attributes.getValue("name");
        typeURI = toURI(shortName);
        
      } else if ("restriction".equals(localName)) {
        baseURI = toURI(attributes.getValue("base"));
        setNumberType();
        
      } else if ("length".equals(localName)) {
        
        length = Integer.valueOf(attributes.getValue("value"));
        
      } else if ("maxExclusive".equals(localName)) {
        maxExclusive = getNumber(attributes.getValue("value"));
        
      } else if ("minExclusive".equals(localName)) {
        minExclusive = getNumber(attributes.getValue("value"));
        
      } else if ("maxInclusive".equals(localName)) {
        maxInclusive = getNumber(attributes.getValue("value"));
        
      } else if ("minInclusive".equals(localName)) {
        minInclusive = getNumber(attributes.getValue("value"));
        
      } else if ("factionDigits".equals(localName)) {
        fractionDigits = Integer.valueOf(attributes.getValue("value"));
        
      } else if ("maxLength".equals(localName)) {
        maxLength = Integer.valueOf(attributes.getValue("value"));
        
      } else if ("minLength".equals(localName)) {
        minLength = Integer.valueOf(attributes.getValue("value"));
        
      } else if ("totalDigits".equals(localName)) {
        totalDigits = Integer.valueOf(attributes.getValue("value"));
        
      } else if ("pattern".equals(localName)) {
        pattern = attributes.getValue("value");
        
      } else if ("label".equals(localName) || "prefix".equals(localName)) {
        characters = new StringBuilder();
      }
      
    }
    
    @Override
    public void characters(char[] ch, int start, int length) {
      if (characters != null) {
        characters.append(ch, start, length);
      }
    }
    
    private void setNumberType() {
      if (
          XSD.decimal.getURI().equals(baseURI) ||
          XSD.xdouble.getURI().equals(baseURI)
      ) {
        numberType = NumberType.DOUBLE;
        
      } else if (XSD.xfloat.getURI().equals(baseURI)) {
        numberType = NumberType.FLOAT;
        
      } else if (
        XSD.integer.getURI().equals(baseURI) ||
        XSD.nonPositiveInteger.getURI().equals(baseURI) ||
        XSD.nonNegativeInteger.getURI().equals(baseURI) ||
        XSD.unsignedInt.getURI().equals(baseURI)
      ) {
        numberType = NumberType.INT;
        
      } else if (
         XSD.xlong.getURI().equals(baseURI) ||
         XSD.unsignedLong.getURI().equals(baseURI) 
      ) {
        numberType = NumberType.LONG;
        
      } else if (
          XSD.xshort.getURI().equals(baseURI) ||
          XSD.unsignedShort.getURI().equals(baseURI)
      ) {
        numberType = NumberType.SHORT;
        
      } else if (
          XSD.xbyte.getURI().equals(baseURI) ||
          XSD.unsignedByte.getURI().equals(baseURI)
      ) {
        numberType = NumberType.BYTE;
      }
      
    }

    private Number getNumber(String value) {
     
      Number number = null;
      switch (numberType) {
      case BYTE : number = Byte.valueOf(value); break;
      case DOUBLE : number = Double.valueOf(value); break;
      case FLOAT : number = Float.valueOf(value); break;
      case INT : number = Integer.valueOf(value); break;
      case LONG : number = Long.valueOf(value); break;
      case SHORT : number = Short.valueOf(value); break;
      }
      return number;
    }

    @Override
    public void endElement(String uri, String localName, String qName) {
      
      if (typeURI != null && "simpleType".equals(localName)) {
        LdTerm term = context.getTerm(typeURI);
        if (term != null) {
          // We only keep datatypes that are referenced in the context.
          
          LdDatatype type = new LdDatatype();
          term.setDatatype(type);
          term.setIRI(typeURI);
          
          type.setLocalName(this.shortName);
          type.setURI(typeURI);
          type.setLength(length);
          type.setFractionDigits(fractionDigits);
          type.setMaxExclusive(maxExclusive);
          type.setMaxInclusive(maxInclusive);
          type.setMinExclusive(minExclusive);
          type.setMinInclusive(minInclusive);
          type.setMinLength(minLength);
          type.setMaxLength(maxLength);
          type.setPattern(pattern==null ? null : Pattern.compile(pattern));
          type.setTotalDigits(totalDigits);
          
          if (baseURI != null) {
            
            LdDatatype base = LdDatatypeManager.getXsdTypeByURI(baseURI);
            
            if (base == null) {
              /**
               * TODO: we should lookup the base datatype from
               * the context and if not found, create a new term.
               */
              base = new LdDatatype();
              String baseName = getLocalName(baseURI);
              base = new LdDatatype();
              base.setLocalName(baseName);
              base.setURI(baseURI);
            }
            type.setBase(base);
            
          }
          
        }
        typeURI = baseURI = shortName = pattern = null;
        length = fractionDigits = maxLength = minLength = totalDigits = null;
        maxExclusive = minExclusive = maxInclusive = minInclusive = null;
        numberType = NumberType.NONE;
      }
      
      characters = null;
      
    }
    
    @Override
    public void endDocument() {
    }
    
    private String getLocalName(String uri) {

      int hash = uri.lastIndexOf('#');
      int slash = uri.lastIndexOf('/');
      int delim = Math.max(hash, slash);
      return uri.substring(delim+1);
    }
    
    private String toURI(String value) {
      int colon = value.indexOf(':');
      return (colon < 0) ? targetNamespace + value : 
        namespaceMap.get(value.substring(0, colon)) + value.substring(colon+1);
    }
    
    private String toRdfNamespace(String uri) {
      return (uri.endsWith("/") || uri.endsWith("#")) ? uri : uri + "#";
    }


    private void parseNamespaces(Attributes attributes) {
      for (int i=0; i<attributes.getLength(); i++) {
        String name = attributes.getQName(i);
        if (name.startsWith("xmlns:")) {
          String prefix = name.substring(6);
          String value = toRdfNamespace(attributes.getValue(i));
          namespaceMap.put(prefix, value);
          
        }
      }
      
      
    }
    
  }
  
}

