package org.semantictools.context.renderer;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.junit.Test;
import org.semantictools.frame.api.TypeManager;
import org.semantictools.frame.model.Frame;
import org.semantictools.json.SampleGenerator;
import org.xml.sax.SAXException;

import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;

public class SampleWriterTest {

  @Test
  public void test() throws IOException, ParserConfigurationException, SAXException {

    String typeURI = "http://purl.org/pearson/xl/v1/vocab/knewton#KnewtonRecommendations";
    File rdfDir = new File("src/test/resources/knewton/rdf");
    
    TypeManager typeManager = new TypeManager();
    typeManager.loadDir(rdfDir);
    typeManager.processOntologies();
    Frame frame = typeManager.getFrameByUri(typeURI);
   
    SampleGenerator generator = new SampleGenerator(ModelFactory.createDefaultModel());
    
    Resource resource = generator.generateSample(frame);
    
    SampleWriter writer = new SampleWriter(typeManager);
    writer.add(resource);
    
    writer.write(System.out);
    
    
    
  }

}
