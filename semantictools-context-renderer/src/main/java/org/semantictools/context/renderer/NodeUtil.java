package org.semantictools.context.renderer;

import org.semantictools.context.renderer.model.JsonContext;
import org.semantictools.context.renderer.model.TermInfo;
import org.semantictools.context.renderer.model.TreeNode;
import org.semantictools.frame.api.TypeManager;

import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.XSD;

public class NodeUtil {

  /**
   * Returns a TreeNode for a datatype that is not declared in the specified JsonContext
   */
  public static TreeNode createDefaultTypeNode(JsonContext context, String typeURI) {

    String namespaceURI = TypeManager.getNamespace(typeURI);
    TermInfo namespaceTerm = context.getTermInfoByURI(namespaceURI);
    if (namespaceTerm == null) {
      throw new TermNotFoundException(namespaceURI);          
    }
    
    String localName = TypeManager.getLocalName(typeURI);
    
    String prefix = namespaceTerm.getTermName();
    String typeName = 
        (XSD.getURI().equals(namespaceURI) || OWL.getURI().equals(namespaceURI)) ?
        prefix + ":" + localName : localName;
    String typeHref = "#" + prefix + "." + localName;

    TreeNode node = new TreeNode();
    node.setTypeName(typeName);
    node.setTypeHref(typeHref);
    node.setTypeURI(typeURI);
    return node;
  }
}
