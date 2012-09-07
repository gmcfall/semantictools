package org.semantictools.context.renderer;

import java.util.Comparator;

import org.semantictools.context.renderer.model.TreeNode;

public interface NodeComparatorFactory {
  
  /**
   * Return a comparator that can be used to sort the nodes representing fields of
   * the specified class.
   * @param frameURI  The URI for the RDF class whose fields are to be sorted.
   */
  public Comparator<TreeNode> getComparator(String frameURI);

}
