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
  public static TreeNode createDefaultTypeNode(TypeManager typeManager, JsonContext context, String typeURI) {

    if (typeManager.isStandardLiteralType(typeURI)) {
      // For abstract data types (rdfs:Literal, rdfs:Datatype, rdf:XMLLiteral)
      // use xsd:string for the data type in bindings.
      typeURI = XSD.xstring.getURI();
    }
    
    String namespaceURI = TypeManager.getNamespace(typeURI);
    TermInfo namespaceTerm = context.getTermInfoByURI(namespaceURI);
    if (namespaceTerm == null) {
      throw new TermNotFoundException(namespaceURI);          
    }

    TreeNode node = new TreeNode();
    
    
    
    String localName = TypeManager.getLocalName(typeURI);
    
    String prefix = namespaceTerm.getTermName();
    String typeName = 
        (XSD.getURI().equals(namespaceURI) || OWL.getURI().equals(namespaceURI)) ?
        prefix + ":" + localName : localName;
    String typeHref = typeManager.isStandardDatatype(namespaceURI) ? null : "#" + prefix + "." + localName;

    node.setTypeName(typeName);
    node.setTypeHref(typeHref);
    node.setTypeURI(typeURI);
    return node;
  }
}
