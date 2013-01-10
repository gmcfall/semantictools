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

import java.io.PrintWriter;
import java.util.List;
import java.util.regex.Pattern;

import org.semantictools.jsonld.LdClass;
import org.semantictools.jsonld.LdContext;
import org.semantictools.jsonld.LdDatatype;
import org.semantictools.jsonld.LdProperty;
import org.semantictools.jsonld.LdQualifiedRestriction;
import org.semantictools.jsonld.LdRestriction;
import org.semantictools.jsonld.LdTerm;
import org.semantictools.jsonld.io.LdContextWriter;

public class LdContextWriterImpl implements LdContextWriter {
  

  @Override
  public void write(LdContext context, PrintWriter out) {
    Delegate delegate = new Delegate(out);
    delegate.write(context);
  }
  
  static class Delegate {
  
    private int indentSize=2;
    private int indent;
    private PrintWriter writer;
    
    public Delegate(PrintWriter writer) {
      this.writer = writer;
    }
  
    private Delegate pushIndent() {
      indent++;
      return this;
    }
    
    private Delegate popIndent() {
      indent--;
      return this;
    }
    
    private Delegate indent() {
      for (int i=0; i<indent*indentSize; i++) {
        writer.print(' ');
      }
      return this;
    }
    
    private Delegate print(String text) {
      writer.print(text);
      return this;
    }
    
    private Delegate println(String text) {
      writer.println(text);
      return this;
    }
    
    private Delegate println() {
      writer.println();
      return this;
    }
    
    public void write(LdContext context) {
      
      beginObject();
      println().indent();
      beginField("@context");
        beginObject();
        printTerms(context);
        endObject();
      endObject();
    }
  
    private void printTerms(LdContext context) {
  
      String comma = "";
      List<LdTerm> termList = context.listTerms();
      for (LdTerm term : termList) {
        printTerm(comma, term);
        comma = ",";
      }
      
      
    }
  
    private void printTerm(String comma, LdTerm term) {
      
      String id = term.getRawIRI();
      String type = term.getRawTypeIRI();
      LdProperty property = term.getProperty();
      LdClass rdfClass = term.getRdfClass();
      LdDatatype datatype = term.getDatatype();
      
      if (type==null && property != null) {
        type = term.getTypeIRI();
      }
      
      println(comma);
      comma = "";
      indent().quote(term.getShortName()).print(" : ");
      if (
        (type != null) ||
        (property != null) ||
        (rdfClass != null) ||
        (datatype != null)
      ) {
        beginObject();
        comma = writeField(comma, "@id", id);
        comma = writeField(comma, "@type", type);
        comma = writeClass(comma, rdfClass);
        comma = writeDatatype(comma, datatype);
        comma = writeProperty(comma, property);
        
        endObject();
        
      } else {
        quote(id);
        
      }
      
      
      
    }
  
    private String writeDatatype(String comma, LdDatatype datatype) {
      if (datatype == null) return comma;
      
      LdDatatype base = datatype.getBase();
      String baseURI = (base==null) ? null : base.getUri();
      
      println(comma);
      comma = "";
      beginField("datatype");
      beginObject();
      comma = writeField(comma, "base", baseURI);
      comma = writeField(comma, "fractionDigits", datatype.getFractionDigits());
      comma = writeField(comma, "length", datatype.getLength());
      comma = writeField(comma, "maxExclusive", datatype.getMaxExclusive());
      comma = writeField(comma, "minExclusive", datatype.getMinExclusive());
      comma = writeField(comma, "maxInclusive", datatype.getMaxInclusive());
      comma = writeField(comma, "minInclusive", datatype.getMinInclusive());
      comma = writeField(comma, "maxLength", datatype.getMaxLength());
      comma = writeField(comma, "minLength", datatype.getMinLength());
      comma = writeField(comma, "pattern", datatype.getPattern());
      endObject();
      
      return ",";
    }
  
    private String writeField(String comma, String fieldName, Pattern pattern) {
      if (pattern == null) return comma;    
      return writeField(comma, fieldName, pattern.pattern());
    }
  
    private String writeField(String comma, String fieldName, String value) {
      if (value == null) return comma;
      writer.println(comma);
      writeField(fieldName, value);
      return ",";
    }
  
    private String writeField(String comma, String fieldName, Number number) {
      if (number != null) {
        println(comma);
        beginField(fieldName);
        writer.append(number.toString());
        comma = ",";
      }
      return comma;
      
    }
  
    private void beginField(String fieldName) {
      indent().quote(fieldName).print(" : ");
    }
  
    private String writeProperty(String comma, LdProperty property) {
      if (property == null) return comma;
      println(comma);
      beginField("property");
      beginObject();
        beginField("domain");
        beginArray();
          printDomain(property);    
        endArray();    
      endObject();
      return ",";
    }
  
    private void printDomain(LdProperty property) {
      
      String comma = "";
      List<String> array = property.getDomain();
      for (String value : array) {
        println(comma);
        indent().quoteValue(value);
        comma = ",";
      }
      
    }
  
    private void endArray() {
     
      println();
      popIndent();
      indent().print("]");
      
    }
  
    private void beginArray() {
      print("[");
      pushIndent();
      
    }
  
    private String writeClass(String comma, LdClass rdfClass) {
      if (rdfClass == null) return comma;
      
      println(comma);
      comma = "";
      
      
      beginField("class");
      beginObject();
      comma = writeSuperTypes(comma, rdfClass);
      comma = writeRestrictions(comma, rdfClass);
      
      endObject(comma);
      
      return ",";
    }
  
    private void endObject(String comma) {
      if (comma.length()==0) {
        popIndent().println("}");
      } else {
        endObject();
      }
      
    }
  
    private String writeRestrictions(String comma, LdClass rdfClass) {
      
      List<LdRestriction> list = rdfClass.listRestrictions();
      if (list == null || list.isEmpty()) return comma;
      println(comma);
      beginField("restriction");
      beginArray();
      comma = "";
      for (LdRestriction r : list) {
        comma = printRestriction(comma, r);
      }
      
      endArray();
      
      return ",";
    }
  
    private String printRestriction(String comma, LdRestriction r) {
      println(comma).indent();
      
      comma = "";
      beginObject();
        comma = writeField(comma, "onProperty", r.getPropertyURI());
        comma = writeField(comma, "maxCardinality", r.getMaxCardinality());
        comma = writeField(comma, "minCardinality", r.getMinCardinality());
        comma = writeQualifiedRestrictions(comma, r);
      endObject();
      return ",";
    }
  
    private String writeQualifiedRestrictions(String comma, LdRestriction r) {
      List<LdQualifiedRestriction> qlist = r.listQualifiedRestrictions();
      if (qlist == null || qlist.isEmpty()) return comma;
      
      println(comma);
      beginField("qualifiedRestriction");
      beginArray();
        comma = "";
        for (LdQualifiedRestriction qr : qlist) {
          comma = writeQualifiedRestriction(comma, qr);
        }
      
      endArray();
      return ",";
    }
  
    private String writeQualifiedRestriction(String comma, LdQualifiedRestriction qr) {
      println(comma).indent();
      comma = "";
      beginObject();
      comma = writeField(comma, "onClass", qr.getRangeURI());
      comma = writeField(comma, "maxCardinality", qr.getMaxCardinality());
      comma = writeField(comma, "minCardinality", qr.getMinCardinality());
      endObject();
      return ",";
    }
  
    private String writeSuperTypes(String comma, LdClass rdfClass) {
     
      List<LdClass> superList = rdfClass.listSupertypes();
      if (superList==null || superList.isEmpty()) return comma;
      println(comma);
      beginField("supertype");
      beginArray();
      comma = "";
      for (LdClass superType : superList) {
        comma = printValue(comma, superType.getURI());
      }
      endArray();
      return ",";
    }
  
    private String printValue(String comma, String value) {
      println(comma);
      indent().quote(value);
      return ",";
    }
  
    private void writeField(String fieldName, String value) {
      indent().quote(fieldName).print(" : ").quoteValue(value);
      
    }
  
    private Delegate quoteValue(String value) {
  
      if (value == null) {
        writer.print("null");
        return this;
      }
      value = value.replace("\\", "\\\\");
      writer.print('"');
      writer.print(value);
      writer.print('"');
      return this;
      
    }
  
    private Delegate quote(String value) {
      if (value == null) {
        writer.print("null");
        return this;
      }
      writer.print('"');
      writer.print(value);
      writer.print('"');
      return this;
      
    }
  
    private Delegate endObject() {
      
      return popIndent().println().indent().print("}");
      
    }
  
    private Delegate beginObject() {
     
      return print("{").pushIndent();
      
    }
  }
  
  

}
