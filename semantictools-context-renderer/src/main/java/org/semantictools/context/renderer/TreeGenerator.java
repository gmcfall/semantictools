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

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.semantictools.context.renderer.model.BranchStyle;
import org.semantictools.context.renderer.model.ContextProperties;
import org.semantictools.context.renderer.model.FrameConstraints;
import org.semantictools.context.renderer.model.JsonContext;
import org.semantictools.context.renderer.model.ObjectPresentation;
import org.semantictools.context.renderer.model.TermInfo;
import org.semantictools.context.renderer.model.TreeNode;
import org.semantictools.context.renderer.model.TreeNode.Kind;
import org.semantictools.frame.api.FrameNotFoundException;
import org.semantictools.frame.api.TypeManager;
import org.semantictools.frame.model.Datatype;
import org.semantictools.frame.model.Field;
import org.semantictools.frame.model.Frame;
import org.semantictools.frame.model.RdfType;
import org.semantictools.frame.model.RestCategory;

import com.hp.hpl.jena.vocabulary.OWL;

public class TreeGenerator {
  private static final String XMLSCHEMA_URI = "http://www.w3.org/2001/XMLSchema#";
  private static final String pageOf = "http://www.w3.org/ns/ldp#pageOf";
  private JsonContext context;
  private ContextProperties contextProperties;
  private int maxDepth;
  private Set<String> memory;
  private TypeManager typeManager;
  
  
  public TreeGenerator(TypeManager typeManager, JsonContext context, ContextProperties properties) {
    this.typeManager = typeManager;
    this.context = context;
    contextProperties = properties;
  }
  
  public TreeNode generateRoot(Frame frame, String propertyName, int depth) {
    if (propertyName == null) {
      return generateRoot(frame, depth);
    }
    
    TermInfo term = context.getTermInfoByShortName(propertyName);
    if (term == null) {
      throw new TermNotFoundException(propertyName);
    }

    Field field = getField(frame, propertyName);
    if (field == null) {
      throw new FieldNotFoundException(frame.getLocalName(), propertyName);
    }
    
    
    RdfType type = field.getRdfType();
    if ((type.canAsDatatype() || type.canAsEnumeration()) && field.getMaxCardinality()==1) {
      throw new RuntimeException(
        "Cannot define a JSON-LD representation where the top node is a literal or an enumeration as required by " +
        frame.getLocalName() + "." + propertyName);
    }
    
    if (type.canAsFrame() && field.getMaxCardinality()==1) {
      return generateRoot(type.asFrame(), depth);
    }
    
    
    TreeNode root = new TreeNode();
    root.setTypeName("");
    addContextNode(root);
    TreeNode graph = new TreeNode();
    root.add(graph);
    graph.setLocalName("@graph");
    graph.setMaxCardinality(-1);
    
    setType(graph, field);
    if (type.canAsFrame()) {
      memory = new HashSet<String>();
      addProperties(graph, type.asFrame(), depth);
      memory = null;
    }
    
    
    return root;
  }

  private Field getField(Frame frame, String propertyName) {
    List<Field> list = frame.listAllFields();
    for (Field field : list) {
      if (field.getLocalName().equals(propertyName)) {
        return field;
      }
    }
    return null;
  }

  public TreeNode generateRoot(Frame frame, int depth) {
    
    memory = new HashSet<String>();
    this.maxDepth = depth;
    TreeNode root = null;
    
    if (frame.getSubtypeList().isEmpty() || depth==1) {
      root = createBasicFrameNode(frame);
      addContextNode(root);
      addTypeNode(root, frame, 1);
      
      addProperties(root, frame, 0);
    } else {
      root = generateRootSubtypes(frame, depth);
    }
    memory = null;
    return root;
  }
  
  private TreeNode generateRootSubtypes(Frame frame, int depth) {
    
    TreeNode root = new TreeNode();
    root.setKind(Kind.FRAME);
    root.setDescription("");
    root.setTypeName("");
    root.setLocalName("");
    
    
    addSubtypes(root, frame, true, depth);
    
    return root;
  }
  
  public TreeNode generateGraph(List<Frame> frameList, int maxDepth) {
    memory = new HashSet<String>();
    this.maxDepth = maxDepth;
    TreeNode root = new TreeNode();
    root.setDescription("");
    root.setKind(Kind.FRAME);
    root.setLocalName("");
    root.setTypeName("");
    
    addContextNode(root);
    TreeNode graph = new TreeNode();
    root.add(graph);
    graph.setMaxCardinality(-1);
    graph.setKind(Kind.PROPERTY);
    graph.setLocalName("@graph");
    
    if (frameList.size() == 1) {
      Frame frame = frameList.get(0);
      graph.setTypeName(frame.getLocalName());
      addProperties(graph, frame, 0);
      
    } else {
      graph.setTypeName("");
      graph.setBranchStyle(BranchStyle.OBLIQUE);

      for (Frame frame : frameList) {
        TreeNode node = createBasicFrameNode(frame);
        graph.add(node);
        addTypeNode(node, frame, 1);
        addProperties(node, frame, 0);
      }
    }
    
    memory = null;
    
    return root;
  }
  
  public TreeNode generateNode(Frame frame, int depth) {
    memory = new HashSet<String>();
    maxDepth = depth;
    TreeNode root = createBasicFrameNode(frame);    
    if (frame.hasFields()) {
      addProperties(root, frame, 0);
    } 
    memory = null;
    
    return root;
  }
  
  private void addProperties(TreeNode parent, Frame frame, int depth) {
   if (maxDepth>=0 && depth >= maxDepth) return;
      
    String frameURI = frame.getUri();
    if (memory.contains(frameURI)) {
      return;
    }
    memory.add(frameURI);
   
    
    depth++;
    
    addIdProperty(parent, frame);
    List<Field> list = frame.listAllFields();
    for (Field field : list) {
      addField(parent, frame, field, depth);
    }
    
  }
  
  private void addIdProperty(TreeNode parent, Frame frame) {
    if (frame.getCategory() == RestCategory.ADDRESSABLE) {
      String parentType = parent.getTypeName();
      
      TreeNode node = new TreeNode();
      node.setKind(Kind.PROPERTY);
      node.setDescription("The URI that identifies this <code>" + parentType + "</code> instance.");
      node.setLocalName("@id");
      node.setMaxCardinality(1);
      if (contextProperties.requiresId(frame.getUri())) {
        node.setMinCardinality(1);
      } else {
        node.setMinCardinality(0);
      } 
      node.setTypeName("xs:anyURI");
      
      parent.add(node);
    }
  }


  public TreeNode generateNode(Field field) {
    memory = new HashSet<String>();
    TreeNode result = doGenerateNode(field);
    memory = null;
    return result;
  }

  private TreeNode doGenerateNode(Field field) {

    String uri = field.getURI();
    TermInfo term = context.getTermInfoByURI(uri);
    if (term == null) return null;
    

    String frameURI = field.getDeclaringFrame().getUri();
    
    FrameConstraints constraints = contextProperties.getFrameConstraints(frameURI);
    if (constraints != null && !constraints.isIncludedProperty(field.getURI())) {
      return null;
    }

    Frame frame = getFieldTypeAsFrame(field);
    
    
    TreeNode node = new TreeNode();
    TreeNode setContainer = null;
    int max = field.getMaxCardinality();
    if (max > 0) {
      node.setMaxCardinality(max);
    }
    boolean isList = field.getRdfType().canAsListType();
    if (max < 0  && !isList && contextProperties.isSetProperty(field.getURI())) {
      node.setLocalName("@set");
      TreeNode id = new TreeNode();
      id.setLocalName("@id");
      id.setMaxCardinality(1);
      id.setMinCardinality(1);
      id.setTypeName("xs:anyURI");
      
      setContainer = new TreeNode();
      setContainer.add(id);
      setContainer.add(node);
      setContainer.setLocalName(context.rewrite(uri));
      setContainer.setMinCardinality(field.getMinCardinality());
      setContainer.setMaxCardinality(1);
      setContainer.setTypeName("");
      setContainer.setKind(Kind.PROPERTY);
    } else {
      String localName = context.rewrite(uri);
      if (contextProperties.usePrefix(uri)) {
        String namespace = typeManager.getNamespace(uri);
        String prefix = context.rewrite(namespace);
        node.setLocalName(prefix + ":" + localName);
      } else {
        node.setLocalName(localName);
      }
    }
    
    boolean readOnly = 
        field.getRdfType().canAsFrame() &&
        (field.getRdfType().asFrame().getContainerRestriction() != null);
    
    int maxCardinality = field.getMaxCardinality();
    if (contextProperties.getOptionalProperties().contains(uri)) {
      maxCardinality = 0;
    }
    
    node.setReadOnly(readOnly);
    node.setKind(Kind.PROPERTY);
    node.setMinCardinality(field.getMinCardinality());
    node.setMaxCardinality(maxCardinality);
    setDescription(node, field);
    setType(node, field);
    
    if (isList) {
      node.setMaxCardinality(-1);
      node.setSequential(true);
    }
    
    
    
    if (contextProperties.isMixed(uri)) {
      node.setObjectPresentation(ObjectPresentation.MIXED_VALUE);
      
    } else if (
        term.hasObjectValue() &&
        "@id".equals(term.getObjectValue().getType()) &&
        frame != null &&
        frame.getCategory() != RestCategory.ENUMERABLE
    ) {
      node.setObjectPresentation(ObjectPresentation.URI_REFERENCE);
      node.setTypeHref(null);
      
    
    } else if (
        frame != null &&
        frame.getCategory() == RestCategory.ENUMERABLE
    ) {
      node.setObjectPresentation(ObjectPresentation.SIMPLE_NAME);
      
    } 
    
    if (contextProperties.isSimpleName(uri)) {
      node.setObjectPresentation(ObjectPresentation.SIMPLE_NAME);
    }
    
    checkExpandedValue(node, field, term);
    
    return setContainer == null ? node : setContainer;
    
  }
  

  private void checkExpandedValue(TreeNode node, Field field, TermInfo term) {
   
    if (term.hasObjectValue()) return;
    String typeURI = node.getTypeURI();
    TypeManager manager = field.getDeclaringFrame().getTypeManager();
    
    if (manager.getDatatypeByUri(typeURI)==null) return;
    
    node.setObjectPresentation(ObjectPresentation.EXPANDED_VALUE);
    
    
  }


  private void setDescription(TreeNode node, Field field) {
    
    String comment = field.getComment();
    if (comment == null || comment.length()==0) {
      
      if (OWL.sameAs.getURI().equals(field.getURI())) {
        
        Frame parent = field.getDeclaringFrame();
        TermInfo term = context.getTermInfoByURI(parent.getUri());
        String typeName = (term==null) ? parent.getLocalName() : term.getTermName();
        
        String text = "Specifies an alternative representation of this <code>{0}</code>.";
        
        comment = text.replace("{0}", typeName);
        
        
      }
    }
    
    node.setDescription(comment);
    
  }

  private Frame getFieldTypeAsFrame(Field field) {

    RdfType rdfType = field.getRdfType();
    if (rdfType.canAsListType()) {
      rdfType = rdfType.asListType().getElementType();
    }
    
    Frame frame = rdfType.canAsFrame() ? rdfType.asFrame() : null;
    return frame;
  }

  private void addField(TreeNode parent, Frame parentFrame, Field field, int depth) {
    
    
    TreeNode node = doGenerateNode(field);
    if (node == null) {
      return;
    }

    
    parent.add(node);
    if (contextProperties.isSetProperty(field.getURI())) {
      node = node.getChildren().get(1);
    }
    
    
    
    Frame frame = getFieldTypeAsFrame(field);
    if (frame == null || isCyclic(node)) return;

    if (field.getURI().equals(pageOf)) {
      // Special handling for LDP pageOf property
      
      String containerType = contextProperties.getRdfTypeURI();
      frame = typeManager.getFrameByUri(containerType);
      if (frame == null) {
        throw new FrameNotFoundException(containerType);
      }
      
      node.setTypeURI(containerType);
      node.setTypeName(frame.getLocalName());
     
    }
    
    if (node.getObjectPresentation()==ObjectPresentation.NONE ) {
      
      List<Frame> subtypeList = frame.getSubtypeList();
      boolean excludeSubtypes = excludeSubtypes(parentFrame, field);
      if (subtypeList.isEmpty() || excludeSubtypes) {      
        addProperties(node, frame, depth);
        
      } else {
        node.setBranchStyle(BranchStyle.OBLIQUE);
        addSubtypes(node, frame, false, depth);
      }
     
    }
    
  }
 
  private boolean excludeSubtypes(Frame frame, Field field) {
    FrameConstraints constraints = contextProperties.getFrameConstraints(frame.getUri());
    return constraints != null && constraints.isExcludesSubtypes(field.getURI());
  }

  private void addSubtypes(TreeNode node, Frame frame, boolean hasContext, int depth) {
    
    List<Frame> list = frame.listAllSubtypes();
    filterSubtypes(list);

    
    Collections.sort(list);
    if (frame.isAbstract() && list.size()==1) {
      //  There is only one known subtype of an abstract base type.
      //  So instead of offering a choice, just require that this subtype 
      //  be used.
      
      Frame subtype = list.get(0);
      TermInfo info = context.getTermInfoByURI(subtype.getUri());
      if (info == null) {
        throw new TermNotFoundException(subtype.getUri());
      }
      String typeName = info.getTermName();
      String href = "#" + typeName;
      node.setTypeHref(href);
      node.setTypeName(typeName);
      node.setTypeURI(subtype.getUri());
      node.setBranchStyle(BranchStyle.RECTILINEAR);
      addConcreteTypeNode(node, subtype, hasContext, depth);
      addProperties(node, subtype, depth);
      
      return;
      
    }
    if (maxDepth>=0 && depth>=maxDepth) return;
    
    if (!frame.isAbstract())  list.add(0, frame);

    node.setBranchStyle(BranchStyle.OBLIQUE);
    for (Frame sub : list) {
      if (contextProperties.getExcludedTypes().contains(sub.getUri())) {
        continue;
      }
        
      TreeNode child = createBasicFrameNode(sub);
      node.add(child);
      

      TermInfo info = context.getTermInfoByURI(sub.getUri());
      if (info == null) {
        throw new TermNotFoundException(contextProperties.getMediaType(), sub.getUri());
      }
      String typeName = info.getTermName();
      String href = "#" + typeName;
      child.setTypeHref(href);
      addConcreteTypeNode(child, sub, hasContext, depth+1);
      addProperties(child, sub, depth+1);
    }
    
    
    
    
  }

  private void addConcreteTypeNode(TreeNode node, Frame subtype, boolean hasContext, int depth) {

    if (maxDepth>=0 && depth >= maxDepth) return;
    
    if (hasContext) {
      addContextNode(node);
    }
    
    TreeNode typeNode = new TreeNode();
    node.add(typeNode);
    typeNode.setLocalName("@type");
    typeNode.setTypeName("owl:Class");
    typeNode.setKind(Kind.PROPERTY);
    typeNode.setDescription(
        "A simple name that identifies the type of this resource.  The value should be <code>" +
        subtype.getLocalName() + "</code>."
    );
    typeNode.setMaxCardinality(1);
    typeNode.setMinCardinality(1);
    typeNode.setObjectPresentation(ObjectPresentation.SIMPLE_NAME);
  }

  private void filterSubtypes(List<Frame> list) {
    Iterator<Frame> sequence = list.iterator();
    while (sequence.hasNext()) {
      Frame frame = sequence.next();
      if (frame.isAbstract()) {
        sequence.remove();
      }
      // Don't include subtypes that are not referenced by the JSON-LD context.
      // 
      if (context.getTermInfoByURI(frame.getUri())==null) {
        sequence.remove();
      }
    }
    
  }

  private boolean isCyclic(TreeNode node) {
    String typeName = node.getTypeName();
    if (typeName.length()==0) return false;
    
    while ((node=node.getParent()) != null) {
      if (typeName.equals(node.getTypeName())) {
        return true;
      }
    }
    return false;
  }

  private void setType(TreeNode node, Field field) {
    RdfType type = field.getRdfType();
    
    String propertyURI = field.getURI();
   
    if (!contextProperties.isIdRef(propertyURI) && shortCircuitType(node, field)) {
      return;
    }
    
    boolean isList = type.canAsListType();
    
    String typeURI = isList ? 
        type.asListType().getElementType().getUri() :         
        field.getType().getURI();
    
    TermInfo term = context.getTermInfoByURI(typeURI);
    
    String typeName = null;
    String typeHref = null;
    
    if (typeURI.startsWith(XMLSCHEMA_URI)) {
      typeName = "xs:" + field.getType().getLocalName();
      
    } else {
    
      if (term == null) {
        
       TreeNode tmp = NodeUtil.createDefaultTypeNode(typeManager, context, typeURI);
       typeName = tmp.getTypeName();
       typeHref = tmp.getTypeHref();
        
      } else {
      
      
        typeName = term.getTermName();
        if (!field.getRdfType().canAsDatatype()) {
          typeHref = "#" + typeName;
        }
      }
      
    }

    node.setTypeName(typeName);
    node.setTypeURI(typeURI);
    node.setTypeHref(typeHref);
    if (isList) {
      node.setSequential(true);
    }
    
    
    
    
    
  }
  
  private boolean shortCircuitType(TreeNode node, Field field) {
    RdfType rdfType = field.getRdfType();
    if (!rdfType.canAsFrame()) return false;
    
    Frame frame = rdfType.asFrame();
    if (!frame.isAbstract()) return false;
    
    List<Frame> subtypeList = frame.listAllSubtypes();
    List<Datatype> datatypeList = frame.getSubdatatypeList();
    
    if ((subtypeList.size()+datatypeList.size()) != 1) return false;
    RdfType type = subtypeList.isEmpty() ? datatypeList.get(0) : subtypeList.get(0);

    String typeURI = type.getUri();
    
    TermInfo term = context.getTermInfoByURI(typeURI);
    
    String typeName = null;
    String typeHref = null;
    
    if (typeURI.startsWith(XMLSCHEMA_URI)) {
      typeName = "xs:" + type.getLocalName();
      
    } else {
    
      if (term == null) {
        throw new TermNotFoundException(typeURI);
      }
      
      
      typeName = term.getTermName();
      typeHref = "#" + typeName;
      
    }

    node.setTypeName(typeName);
    node.setTypeURI(typeURI);
    node.setTypeHref(typeHref);
//    if (isList) {
//      node.setSequential(true);
//    }
    
    
    
    return true;
  }

  private void addTypeNode(TreeNode parent, Frame frame, int minCardinality) {
    if (frame.isAbstract()) minCardinality = 1;
    Set<String> set = getTypesForFrame(frame);
    
    if (minCardinality == 0 && set.isEmpty()) return;
    
    
    TreeNode child = new TreeNode();
    child.setLocalName("@type");
    child.setTypeName("owl:Class");
    child.setTypeURI(OWL.Class.getURI());
    child.setObjectPresentation(ObjectPresentation.SIMPLE_NAME);
    child.setMinCardinality(minCardinality);
    child.setMaxCardinality(1);
    
    StringBuilder text = new StringBuilder(
        "A simple name identifying the object's type.  ");
    
    if (!frame.isAbstract()) {
      set.add(parent.getTypeName());
    }
    text.append("The standard context " + contextProperties.getContextRef() + 
        " defines the following simple names that are applicable: <UL>\n");
    for (String name : set) {
      text.append("  <LI><CODE>");
      text.append(name);
      text.append("</CODE></LI>");
    }
    
    text.append("</UL>");
    TermInfo info = context.getTermInfoByURI(frame.getUri());
    if (info == null) {
      throw new TermNotFoundException(context.getMediaType(), frame.getUri());
    }
    String termName = info.getTermName();
    text.append(
        "<P>Implementations may use a custom JSON-LD context which defines simple names for additional types " +
        "that are subtypes of <code>" + termName + ".</code></P>"
    );
    
    if (minCardinality == 0) {
      text.append(
          "<P>The default value of the @type property is <code>" + termName + "</code>. " +
          "The @type property may be omitted if the object's type is <code>" + termName + "</code>, " +
          "but this property is required otherwise."
      );
    }
    
    child.setDescription(text.toString());
    
    
    parent.add(child);
  }


  private Set<String> getTypesForFrame(Frame frame) {
    Set<String> set = new HashSet<String>();
    
    List<Frame> list = frame.listAllSubtypes();
    Iterator<Frame> sequence = list.iterator();
    while (sequence.hasNext()) {
      Frame subtype = sequence.next();
      if (!subtype.isAbstract()) {
        String uri = subtype.getUri();
        TermInfo term = context.getTermInfoByURI(uri);
        if (term == null) {
        	continue; // The class must have been excluded
        }
        set.add(term.getTermName());
      }
    }
    return set;
    
  }

  private void addContextNode(TreeNode parent) {
    TreeNode node = new TreeNode();
    node.setKind(Kind.PROPERTY);
    node.setLocalName("@context");
    node.setTypeName("JSON-LD Context");
    node.setMinCardinality(1);
    node.setMaxCardinality(-1);
    addContextDescription(node);
    
    
    parent.add(node);    
  }

  private void addContextDescription(TreeNode node) {
    if (contextProperties == null) {
      node.setDescription(
          "This value specifies one or more JSON-LD contexts, either by reference or by value."
      );
      return;
    }
    
    String text =
        "<p>This value specifies one or more JSON-LD contexts, either by reference or by value.\n" +
        "When multiple contexts are specified, they must be encapsulated within an array.</p>\n" +
        "<p>For most implementations, the value will be the single URI for the standard context associated " +
        "with the <code>{0}</code> media type.  In this case, the value will be</p>\n" +
        "<blockquote><code>\"{1}\"</code></blockquote>";
    
    String mediaType = contextProperties.getMediaType();
    String contextURI = contextProperties.getContextURI();
    
    text = text.replace("{0}", mediaType).replace("{1}", contextURI);
    
    node.setDescription(text);
    
    
    
    
  }

  private TreeNode createBasicFrameNode(Frame frame) {

    String localName = context.rewrite(frame.getUri());
    
    TreeNode node = new TreeNode();
    node.setDescription(frame.getComment());
    node.setKind(Kind.FRAME);
    node.setLocalName("");
    node.setTypeName(localName);
    node.setTypeURI(frame.getUri());
    
    return node;
  }
  
  
  
  
  
  

}
