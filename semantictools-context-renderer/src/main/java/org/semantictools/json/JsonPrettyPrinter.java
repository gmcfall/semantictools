package org.semantictools.json;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.PrettyPrinter;


public class JsonPrettyPrinter implements PrettyPrinter {
  
  private static enum Container {
    OBJECT,
    ARRAY
  }
  
  private int indent=0;
  private int tabSize=2;
  private List<Container> stack = new ArrayList<Container>();
  
  
  private void pushIndent(Container container) {
    indent += tabSize;
    stack.add(container);
  }
  
  private void popIndent() {
    indent -= tabSize;
    stack.remove(stack.size()-1);
  }
  
  private Container getContainer() {
    
    return stack.size()>1 ? stack.get(stack.size()-2) : Container.OBJECT;
  }
  
  private void indent(JsonGenerator g) throws IOException {
    for (int i=0; i<indent; i++) {
      g.writeRaw(' ');
    }
  }

  @Override
  public void writeRootValueSeparator(JsonGenerator jg) throws IOException,
      JsonGenerationException {
    jg.writeRaw('\n');
    
  }

  @Override
  public void writeStartObject(JsonGenerator jg) throws IOException,
      JsonGenerationException {
    jg.writeRaw("{");
    pushIndent(Container.OBJECT);
    
  }

  @Override
  public void writeEndObject(JsonGenerator jg, int nrOfEntries)
      throws IOException, JsonGenerationException {
    jg.writeRaw("\n");
    popIndent();
    indent(jg);
    jg.writeRaw("}");
    
  }

  @Override
  public void writeObjectEntrySeparator(JsonGenerator jg) throws IOException,
      JsonGenerationException {
    jg.writeRaw(",\n");
    indent(jg);
  }

  @Override
  public void writeObjectFieldValueSeparator(JsonGenerator jg)
      throws IOException, JsonGenerationException {
    jg.writeRaw(" : ");
    
  }

  @Override
  public void writeStartArray(JsonGenerator jg) throws IOException,
      JsonGenerationException {
    jg.writeRaw("[");
    pushIndent(Container.ARRAY);
    
  }

  @Override
  public void writeEndArray(JsonGenerator jg, int nrOfValues)
      throws IOException, JsonGenerationException {
    jg.writeRaw('\n');
    popIndent();
    indent(jg);
    jg.writeRaw("]");
    
  }

  @Override
  public void writeArrayValueSeparator(JsonGenerator jg) throws IOException,
      JsonGenerationException {
   
    jg.writeRaw(",\n");
    indent(jg);
    
  }

  @Override
  public void beforeArrayValues(JsonGenerator jg) throws IOException,
      JsonGenerationException {
   
    jg.writeRaw(" \n");
    indent(jg);
    
  }

  @Override
  public void beforeObjectEntries(JsonGenerator jg) throws IOException,
      JsonGenerationException {
    switch (getContainer()) {
    case OBJECT :
      jg.writeRaw('\n');
      indent(jg);
      break;
      
    case ARRAY :
      jg.writeRaw(' ');
      break;
      
    }
    
  }
  
}

