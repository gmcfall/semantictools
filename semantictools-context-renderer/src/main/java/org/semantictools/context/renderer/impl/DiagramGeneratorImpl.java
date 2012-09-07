package org.semantictools.context.renderer.impl;

import java.io.IOException;
import java.util.List;

import org.semantictools.context.renderer.ContextRenderer;
import org.semantictools.context.renderer.DiagramGenerator;
import org.semantictools.context.renderer.StreamFactory;
import org.semantictools.context.renderer.model.CreateDiagramRequest;
import org.semantictools.context.renderer.model.DiagramSpec;
import org.semantictools.context.renderer.model.JsonContext;
import org.semantictools.context.renderer.model.Modifier;
import org.semantictools.context.renderer.model.Node;
import org.semantictools.context.renderer.model.TreeNode;

public class DiagramGeneratorImpl implements DiagramGenerator {
  private StreamFactory streamFactory;
  
  public DiagramGeneratorImpl(StreamFactory streamFactory) {
    this.streamFactory = streamFactory;
  }
  



  @Override
  public void generateNotationDiagram(CreateDiagramRequest request)  throws IOException {
    
    DiagramSpec spec = createDiagramSpec(request);
    
    ContextRenderer renderer = new ContextRenderer(streamFactory);
    renderer.renderGraphicalNotationFigure(spec);
    
  }
  

  
  @Override
  public void generateDiagram(CreateDiagramRequest request) throws IOException {

    DiagramSpec spec = createDiagramSpec(request);
    
    ContextRenderer renderer = new ContextRenderer(streamFactory);
    renderer.render(spec);
  }
  
  
  private DiagramSpec createDiagramSpec(CreateDiagramRequest request) {

    JsonContext context = request.getContext();
    
    NodeGenerator generator = new NodeGenerator(context);
    Node rootNode = generator.createTree(null, request.getRoot());
        
    DiagramSpec spec = new DiagramSpec();
    spec.setImagePath(request.getImagePath());
    spec.setRoot(rootNode);
    
    return spec;
  }
  
  class NodeGenerator {
    JsonContext context;

    NodeGenerator(JsonContext context) {
      this.context = context;
    }
    
    Node createTree(Node parent, TreeNode source) {
      Node peer = new Node();
      peer.setNameText(source.getLocalName());
      peer.setTypeText(source.getTypeName());
      peer.setBranchStyle(source.getBranchStyle());
      setModifier(peer, source);
      setObjectPresentation(peer, source);
      
      if (parent != null) {
        parent.add(peer);
      }
      List<TreeNode> kids = source.getChildren();
      if (kids != null) {
        for (TreeNode child : kids) {
          createTree(peer, child);
        }
      }
      return peer;
      
    }

    private void setObjectPresentation(Node sink, TreeNode source) {
      switch (source.getObjectPresentation()) {
      case SIMPLE_NAME :
        sink.applyNameRef();
        break;
        
      case URI_REFERENCE :
        sink.applyIriRef();
        break;
        
      case EXPANDED_VALUE :
        sink.applyExpandedValue();
        break;
      }
      
    }

    private void setModifier(Node sink, TreeNode source) {
      
      int min = source.getMinCardinality();
      int max = source.getMaxCardinality();
      
      Modifier modifier =
          (min==0 && max==1) ? Modifier.OPTIONAL :
          (max<0 || max>1) ? Modifier.REPEATABLE :
          Modifier.NONE;
      
      sink.setModifier(modifier);
      
    }
    
  }


}
