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
package org.semantictools.context.renderer;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.PrettyPrinter;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.ObjectWriter;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.JsonNodeFactory;
import org.codehaus.jackson.node.ObjectNode;
import org.semantictools.frame.api.TypeManager;
import org.semantictools.frame.model.OntologyInfo;

import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFList;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.vocabulary.RDF;

public class SampleWriter {
  
  private TypeManager typeManager;
  private JsonNodeFactory factory = JsonNodeFactory.instance;
  private Map<String, OntologyInfo> uri2OntologyInfo = new HashMap<String, OntologyInfo>();
  private Map<String, ObjectNode> uri2Resource = new HashMap<String, ObjectNode>();
  
  public SampleWriter(TypeManager typeManager) {
    this.typeManager = typeManager;
  }

  public void add(Resource resource) {
    
    String id = getIdentifier(resource);
    ObjectNode node = uri2Resource.get(id);
    if (node == null) {
      node = factory.objectNode();
      uri2Resource.put(id, node);
      node.put("@id", id);
      addType(node, resource);
      addProperties(node, resource);
      
    }
  }
  
  public void write(File file) throws IOException {
    ObjectNode universe = createUniverse();
    
    ObjectMapper mapper = new ObjectMapper();    
    mapper.configure(SerializationConfig.Feature.INDENT_OUTPUT, true);
    mapper.writeValue(file, universe);
  }
  
  public void write(OutputStream output) throws IOException {
    ObjectNode universe = createUniverse();
    ObjectMapper mapper = new ObjectMapper();  
  
    ObjectWriter writer = mapper.writer(new MyPrettyPrinter());
    mapper.configure(SerializationConfig.Feature.INDENT_OUTPUT, true);
    writer.writeValue(output, universe);
    
    
  }
  static class MyPrettyPrinter implements PrettyPrinter {
    
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
  
  private ObjectNode createUniverse() {
    ObjectNode universe = factory.objectNode();
    universe.put("@context", createContext());
    universe.put("database", createDatabase());
    return universe;
  }

  private ArrayNode createDatabase() {
    ArrayNode array = factory.arrayNode();
    List<Map.Entry<String, ObjectNode>> list = new ArrayList<Map.Entry<String,ObjectNode>>(uri2Resource.entrySet());
    
    Collections.sort(list, new Comparator<Map.Entry<String,ObjectNode>>() {

      @Override
      public int compare(Entry<String, ObjectNode> a,  Entry<String, ObjectNode> b) {
        return a.getKey().compareTo(b.getKey());
      }
    });
    
    for (Entry<String, ObjectNode> entry : list) {
      array.add(entry.getValue());
    }
    
    return array;
  }

  private ObjectNode createContext() {
    ObjectNode node = factory.objectNode();
    List<OntologyInfo> list = new ArrayList<OntologyInfo>(uri2OntologyInfo.values());
    Collections.sort(list, new Comparator<OntologyInfo>() {
      @Override
      public int compare(OntologyInfo a, OntologyInfo b) {
        return a.getPrefix().compareTo(b.getPrefix());
      }
    });
    for (OntologyInfo info : list) {
      node.put(info.getPrefix(), info.getNamespaceUri());
    }
    return node;
  }

  private String getIdentifier(Resource resource) {
    String result = 
      resource.isURIResource() ? resource.getURI()  :
      resource.isAnon() ? "_:" + resource.getId().getLabelString() :
      null;
      
    if (result == null) {
      throw new IllegalArgumentException("Resource does not have an identifier");
    }
    return result;
  }



  private void addProperties(ObjectNode node, Resource resource) {
   
    Iterator<Statement> sequence = resource.listProperties();
    while (sequence.hasNext()) {
      Statement s = sequence.next();
      Property p = s.getPredicate();
      if (p.equals(RDF.type)) continue;
      
      RDFNode object = s.getObject();
      
      String fieldName = getCurie(p);
      JsonNode fieldValue = node.get(fieldName);
      if (fieldValue == null) {
        addField(node, fieldName, object);
        
      } else if (fieldValue.isArray()) {
        // We already have more than one value for the given
        // field name, so add the new value to the existing array.
        
        addElement((ArrayNode) fieldValue, object);
        
      } else {
        
        // The object already has exactly one value for the given
        // field name.  Replace that value with an array that
        // contains the previous value plus the new value.
        
        ArrayNode array = factory.arrayNode();
        node.remove(fieldName);
        node.put(fieldName, array);
        array.add(fieldValue);
        addElement(array, object);
        
      }
    }
    
  }

  private void addElement(ArrayNode array, RDFNode value) {

    if (value.isLiteral()) {
      JsonNode jsonValue = toJsonNode(value.asLiteral());
      array.add(jsonValue);
      
    } else if (value.isResource()){
      Resource resource = value.asResource();
      String id = getIdentifier(resource);
      array.add(id);
      add(resource);
      
    } else {
      throw new IllegalArgumentException("The elements in an array must be a literal or a resource");
    }
    
  }

  private void addField(ObjectNode node, String fieldName, RDFNode object) {
    if (object.isLiteral()) {
      addLiteral(node, fieldName, object.asLiteral());
      
    } else if (object.isResource()) {
      addResource(node, fieldName, object.asResource());
    }
    
  }
  
  private void addResource(ObjectNode node, String fieldName,  Resource resource) {
    
    
    if (resource.canAs(RDFList.class)) {
      addList(node, fieldName, resource.as(RDFList.class));
      
    } else {
      
      node.put(fieldName, getIdentifier(resource));
      add(resource);
      
    }
    
  }

  private void addList(ObjectNode node, String fieldName, RDFList list) {
    ArrayNode array = factory.arrayNode();
    node.put(fieldName, array);
    
    List<RDFNode> valueList = list.asJavaList();
    for (RDFNode value : valueList) {
      addElement(array, value);
    }
    
  }

  private JsonNode toJsonNode(Literal literal) {
    JsonNode node = null;
    Object value = literal.getValue();
    
    if (value instanceof Boolean) {
      node = factory.booleanNode(literal.getBoolean());
      
    } else if (value instanceof Byte) {
      node = factory.numberNode(literal.getByte());
      
    } else if (value instanceof Double) {
      node = factory.numberNode(literal.getDouble());
      
    } else if (value instanceof Float) {
      node = factory.numberNode(literal.getFloat());
      
    } else if (value instanceof Integer) {
      node = factory.numberNode(literal.getInt());
    
    } else if (value instanceof Long) {
      node = factory.numberNode(literal.getLong());
      
    } else if (value instanceof Short) {
      node = factory.numberNode(literal.getShort());
      
    } else {
      node = factory.textNode(value.toString());
    }
    return node;
  }
  
  private void addLiteral(ObjectNode node, String fieldName, Literal literal) {
   
    JsonNode value = toJsonNode(literal);
    node.put(fieldName, value);
  }

  private void addType(ObjectNode node, Resource resource) {

    List<Statement> list = resource.listProperties(RDF.type).toList();
    
    if (list.size() == 1) {
      String value = getCurie(list.get(0).getResource());
      node.put("@type", value);
      
      
    } else if (list.size()>1) {
      
      ArrayNode array = factory.arrayNode();
      node.put("@type", array);
      for (Statement s : list) {
        Resource type = s.getResource();
        String curie = getCurie(type);
        array.add(curie);
      }
      
    }
    
    
  }

  private String getCurie(Resource resource) {
    String namespace = resource.getNameSpace();
    OntologyInfo info = getOntologyInfo(namespace);
    if (info != null) {
      return info.getPrefix() + ":" + resource.getLocalName();
    }
    return resource.getURI();
  }

  private OntologyInfo getOntologyInfo(String namespace) {
   OntologyInfo info = uri2OntologyInfo.get(namespace);
   if (info == null) {
     info = typeManager.getOntologyByNamespaceUri(namespace);
     if (info != null) {
       uri2OntologyInfo.put(namespace, info);
     }
   }
   return info;
  }


}
