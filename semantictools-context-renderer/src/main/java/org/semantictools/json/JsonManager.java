package org.semantictools.json;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.ObjectWriter;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.JsonNodeFactory;
import org.codehaus.jackson.node.ObjectNode;
import org.semantictools.context.renderer.model.JsonContext;
import org.semantictools.frame.api.TypeManager;
import org.semantictools.frame.model.Field;
import org.semantictools.frame.model.Frame;
import org.semantictools.frame.model.RdfType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class JsonManager {
  private static final Logger logger = LoggerFactory.getLogger(JsonManager.class); 
  
  private Map<String, ObjectNode> typeURI2Node = new HashMap<String, ObjectNode>();
  private Map<String, ObjectNode> id2Node = new HashMap<String, ObjectNode>();
  
  private JsonContext context;
  private TypeManager typeManager;
  private JsonNodeFactory factory = JsonNodeFactory.instance;

  public JsonManager(TypeManager typeManager, JsonContext context) {
    this.context = context;
    this.typeManager = typeManager;
  }


  public void add(String jsonText) {
    ObjectMapper mapper = new ObjectMapper();
    StringReader reader = new StringReader(jsonText);
    try {
      JsonNode node = mapper.readValue(reader, JsonNode.class);
      if (node instanceof ObjectNode) {
        add((ObjectNode) node);
      }
    } catch (Throwable ignore) {
      logger.warn("Failed to parse json for " + context.getMediaType(), ignore);
    }
  }
  
  public void add(ObjectNode node) {
    String type = node.get("@type").asText();
    if (type != null) {
      type = context.rewrite(type);
      crawl(type, node);
    }
  }

  public ObjectNode getObjectNodeByTypeURI(String typeURI) {
    return typeURI2Node.get(typeURI);
  }

  private void crawl(String typeURI, ObjectNode node) {
    ObjectNode prior = getObjectNodeByTypeURI(typeURI);
    
    ObjectNode clone = factory.objectNode();
    if (prior == null) {
      typeURI2Node.put(typeURI, clone);
    }
    Iterator<Entry<String,JsonNode>> sequence = node.getFields();
    
    while (sequence.hasNext()) {
      
      Entry<String,JsonNode> entry = sequence.next();
      String fieldName = entry.getKey();
      JsonNode value = entry.getValue();
      
      
      if (value instanceof ObjectNode) {
       ObjectNode objectValue = (ObjectNode) value;
       String fieldTypeURI = getType(objectValue, typeURI, fieldName);
       
       if (fieldTypeURI != null) {
         crawl(fieldTypeURI, objectValue);
       }

       // Replace the value with an empty object node
       value = factory.objectNode();
        
      } else if (value instanceof ArrayNode) {
        ArrayNode array = (ArrayNode) value;
        crawlArray(typeURI, fieldName, array);
        
        // Replace the value with an empty array node
        value = factory.arrayNode();
        
      } else if (fieldName.equals("@id")) {
        id2Node.put(value.asText(), clone);
      }
      
      clone.put(fieldName, value);
      
    }
  }
  
  public String getJsonText(String typeURI) {
    ObjectNode node = typeURI2Node.get(typeURI);
    if (node == null) return null;
    

    ObjectMapper mapper = new ObjectMapper();  
  
    StringWriter buffer = new StringWriter();
    ObjectWriter writer = mapper.writer(new JsonPrettyPrinter());
    try {
      writer.writeValue(buffer, node);
    } catch (Throwable oops){
      return null;
    }
    buffer.flush();
    return buffer.toString();
  }
  
  private String getType(ObjectNode object, String frameTypeURI, String fieldName) {
    JsonNode type = object.get("@type");
    String fieldTypeURI = (type==null) ? null : context.rewrite( type.asText() );
    
    if (fieldTypeURI == null) {
      fieldTypeURI = inferFieldType(frameTypeURI, fieldName);
    }
    return fieldTypeURI;
  }

  private void crawlArray(String typeURI, String fieldName, ArrayNode array) {
   
    int count = array.size();
    for (int i=0; i<count; i++) {
      JsonNode value = array.get(i);
      if (value instanceof ObjectNode) {
        ObjectNode object = (ObjectNode) value;
        String objectTypeURI = getType(object, typeURI, fieldName);
        if (objectTypeURI != null) {
          crawl(objectTypeURI, object);
        }
      }
    }
  }


  private String inferFieldType(String typeURI, String fieldName) {
    String propertyURI = context.rewrite(fieldName);
    Frame frame = typeManager.getFrameByUri(typeURI);
    if (frame == null) return null;
    
    List<Field> list = frame.listAllFields();
    for (Field field : list) {
      
      RdfType fieldType = field.getRdfType();
      if (fieldType.canAsListType()) {
        fieldType = fieldType.asListType().getElementType();
      }
      
      if (field.getURI().equals(propertyURI)) {
        return fieldType.getUri();
      }
      
    }
    
    
    return null;
  }
 

}
