package org.semantictools.frame.api;

import static org.junit.Assert.*;

import org.junit.Test;

public class LinkManagerTest {
  LinkManager manager = new LinkManager();

  @Test
  public void testRelativeAnchor() {
    String baseURI = "http://example.org/resources/lti/all.html";
    String target  = "http://example.org/resources/lti/all.html#ToolProxy";
    manager.setBaseURI(baseURI);
    
    String relative = manager.relativize(target);
    
    
    assertEquals("#ToolProxy", relative);
    
  }
  
  @Test
  public void testRelativeUp() {

    String baseURI = "http://example.org/resources/lti/v2/all.html";
    String target  = "http://example.org/resources/var/v1/all.html#Variable";

    manager.setBaseURI(baseURI);
    String relative = manager.relativize(target);
    assertEquals("../../../var/v1/all.html#Variable", relative);
  }
  
  @Test
  public void testRelativeDown() {
    String baseURI = "target/pub/";
    String target = "target/pub/uml/www.imsglobal.org/imspurl/lti/v2/capability/index.html";
    
    manager.setBaseURI(baseURI);
    String relative = manager.relativize(target);
    assertEquals("uml/www.imsglobal.org/imspurl/lti/v2/capability/index.html", relative);
    
  }
  
  @Test
  public void testFolderBase() {
    String baseURI = "target/gdx/uml/purl.org/pearson/core/v1/vocab/outcomes/";
    String target = "target/gdx/mediatype/application/vnd/pearson/core/v1/AssignmentResultDeletedEvent+json/index.html";

    manager.setBaseURI(baseURI);
    String relative = manager.relativize(target);
    assertEquals("../../../../../../../mediatype/application/vnd/pearson/core/v1/AssignmentResultDeletedEvent+json/index.html", relative);
  }

}
