package org.semantictools.context.renderer;

import java.io.IOException;

import org.semantictools.context.renderer.model.CreateDiagramRequest;

public interface DiagramGenerator {

  public void generateDiagram(CreateDiagramRequest request) throws IOException;
  
  /**
   * Creates a diagram that shows a single node with arrows pointing
   * to a the property name and property type in the node.
   */
  public void generateNotationDiagram(CreateDiagramRequest request) throws IOException;
  
}
