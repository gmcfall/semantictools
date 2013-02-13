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
package org.semantictools.frame.api;

import java.io.PrintWriter;
import java.util.Collections;
import java.util.List;

import org.semantictools.context.renderer.model.Container;
import org.semantictools.context.renderer.model.JsonContext;
import org.semantictools.context.renderer.model.TermInfo;
import org.semantictools.context.renderer.model.TermValue;

public class ContextWriter {
  
  private int tabSize = 2;
  
  public void writeContext(PrintWriter writer, JsonContext context) {
    
    writer.println("{");
    int indent = tabSize;
    beginObject(writer, indent, "@context");
    indent += tabSize;
    writeTerms(writer, indent, context);
    
    writer.println();
    indent -= tabSize;
    endObject(writer, indent);
    writer.println(",");
    writer.print("  \"@id\" : \"");
    writer.print(context.getContextURI());
    writer.println("\"");
    writer.println("}");
    
    
  }
  

  


  


  private void writeTerms(PrintWriter writer, int indent, JsonContext context) {
    List<TermInfo> list = context.getTerms();
    Collections.sort(list);
    
    String newline = "\n";
    
    for (TermInfo term : list) {
      writer.print(newline);
      String name = term.getTermName();
      
      if (!term.hasObjectValue()) {
        writeProperty(writer, indent, name, term.getIri());
        
      } else {
        beginObject(writer, indent, name);
        indent += tabSize;
        TermValue value = term.getObjectValue();
        writer.println();
        
        writeProperty(writer, indent, "@id", value.getId());
        if (value.getType() != null) {
          writer.println(",");
          writeProperty(writer, indent, "@type", value.getType());
        }
        if (value.getContainer() == Container.LIST) {
          writer.println(",");
          writeProperty(writer, indent, "@container", "@list");
        }
        Integer minCardinality = value.getMinCardinality();
        if (minCardinality != null) {
          writer.println(",");
          writeProperty(writer, indent, "@minCardinality", minCardinality.toString());
        }
        indent -= tabSize;
        writer.println();
        endObject(writer, indent);
      }
      newline = ",\n";
      
    }
    
  }

  private void beginObject(PrintWriter writer, int indent, String name) {

    for (int i=0; i<indent; i++) {
      writer.print(' ');
    }
    writer.print('"');
    writer.print(name);
    writer.print("\" : {");
    
  }

  private void endObject(PrintWriter writer, int indent) {
    for (int i=0; i<indent; i++) {
      writer.print(' ');
    }
    writer.print("}");
    
  }

  private void writeProperty(PrintWriter writer, int indent, String name, String value) {
    for (int i=0; i<indent; i++) {
      writer.print(' ');
    }
    writer.print('"');
    writer.print(name);
    writer.print("\" : \"");
    writer.print(value);
    writer.print('"');
  }

}
