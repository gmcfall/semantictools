package org.semantictools.json;

import java.io.File;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.ObjectWriter;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.node.ObjectNode;
import org.junit.Test;
import org.semantictools.context.renderer.model.ContextProperties;
import org.semantictools.context.renderer.model.JsonContext;
import org.semantictools.frame.api.ContextBuilder;
import org.semantictools.frame.api.ContextManager;
import org.semantictools.frame.api.TypeManager;

public class JsonSampleGeneratorTest {

  @Test
  public void test() throws Exception {

    String typeURI = "http://purl.org/pearson/xl/v1/vocab/knewton#KnewtonRecommendations";
    File rdfDir = new File("src/test/resources/knewton/rdf");
    
    TypeManager typeManager = new TypeManager();
    typeManager.loadDir(rdfDir);
    typeManager.processOntologies();


    File propertiesFile = new File("src/test/resources/knewton/rdf/context.properties");
    
    ContextManager contextManager = new ContextManager();
    contextManager.loadContextProperties(propertiesFile);
    
    ContextProperties properties = contextManager.getContextPropertiesByMediaType("application/vnd.pearson.xl.KnewtonRecommendations+json");
    
    ContextBuilder contextBuilder = new ContextBuilder(typeManager);
    
    JsonContext context = contextBuilder.createContext(properties);
   
    JsonSampleGenerator generator = new JsonSampleGenerator(typeManager);
    
    ObjectNode root = generator.generateSample(context);
    

    ObjectMapper mapper = new ObjectMapper();  
  
    ObjectWriter writer = mapper.writer(new JsonPrettyPrinter());
    mapper.configure(SerializationConfig.Feature.INDENT_OUTPUT, true);
    writer.writeValue(System.out, root);
    
  }

}
