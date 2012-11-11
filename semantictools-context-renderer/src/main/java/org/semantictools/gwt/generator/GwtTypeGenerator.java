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
package org.semantictools.gwt.generator;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import org.semantictools.frame.api.DatatypeUtil;
import org.semantictools.frame.api.TypeManager;
import org.semantictools.frame.model.BindVocabulary;
import org.semantictools.frame.model.Datatype;
import org.semantictools.frame.model.Enumeration;
import org.semantictools.frame.model.Field;
import org.semantictools.frame.model.Frame;
import org.semantictools.frame.model.NamedIndividual;
import org.semantictools.frame.model.OntologyInfo;
import org.semantictools.frame.model.RdfType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.vocabulary.OWL;

public class GwtTypeGenerator {
  private static Logger logger = LoggerFactory.getLogger(GwtTypeGenerator.class);

  private TypeManager typeManager;
  private WriterFactory writerFactory;
  private Map<String, String> uri2JavaName = new HashMap<String, String>();
  private Map<String, ClassInfo> classInfoMap = new HashMap<String, ClassInfo>();
  private Map<String, ModuleInfo> moduleInfoMap = new HashMap<String, ModuleInfo>();
  
  private GwtTypeGeneratorListener listener;
  private GwtTypeConfig config;
  
  public GwtTypeGenerator(GwtTypeConfig config, TypeManager typeManager, WriterFactory writerFactory) {
    this.config = config;
    this.typeManager = typeManager;
    this.writerFactory = writerFactory;
  }
  
  

  public GwtTypeGeneratorListener getListener() {
    return listener;
  }

  public void setListener(GwtTypeGeneratorListener listener) {
    this.listener = listener;
  }

  public void generateAll() throws IOException {
    for (Frame frame : typeManager.listFrames()) {
      String uri = frame.getUri();
      if (!config.includeType(uri)) {
        if (listener != null) {
          listener.ignoreType(uri);
        }
        continue;
      }
      if (listener != null) {
        listener.beginType(uri);
      }
      
      if (frame.canAsEnumeration()) {
        generateEnum(frame.asEnumeration());
      } else {
        generateFrame(frame);
      }
    }
    generateModules();
  }

  private void generateModules() throws IOException {
    
    for (ModuleInfo module : moduleInfoMap.values()) {
      generateModule(module);
    }
    
  }



  private void generateModule(ModuleInfo module) throws IOException {
    
    String path = module.getModuleName().replace(".", "/") + ".gwt.xml";
    PrintWriter writer = writerFactory.getPrintWriter(path);
    try {
      ModuleWriter worker = new ModuleWriter(writer, module);
      worker.printModule();
    } finally {
      writer.close();
    }
    
  }



  private void generateEnum(Enumeration type) throws IOException {

    logger.debug("Generate... " + type.getUri());
    ClassInfo info = getClassInfo(type);

    String filePath = info.getJavaName().replace('.', '/')+".java";
    
    PrintWriter out = writerFactory.getPrintWriter(filePath);
    try {
      ClassWriter writer = new ClassWriter(out, info);
      writer.printEnum();
      
    } finally {
      out.close();
    }
    
  }

  private void generateFrame(Frame frame) throws IOException {
   
    logger.debug("Generate... " + frame.getUri());
    ClassInfo info = getClassInfo(frame);
    collectImports(info);
    
    String filePath = info.getJavaName().replace('.', '/')+".java";
    
    PrintWriter out = writerFactory.getPrintWriter(filePath);
    try {
      ClassWriter writer = new ClassWriter(out, info);
      writer.printClass();
      
    } finally {
      out.close();
    }
    
    
  }

  private void collectImports(ClassInfo info) {
    info.addImport("com.google.gwt.core.client.JavaScriptObject");
    
    if (info.hasSingleSupertype()) {
      RdfType superType = info.getSingleSupertype();
      ClassInfo superInfo = getClassInfo(superType);
      info.addImport(superInfo);
    }
    
    Frame frame = info.getRdfType().asFrame();
    
    String frameURI = frame.getUri();
    List<Field> fieldList = frame.getDeclaredFields();
    for (Field field : fieldList) {
      RdfType type = field.getRdfType();
      String fieldName = field.getLocalName();
      if (field.getMaxCardinality() != 1 && !config.excludeProperty(frameURI, fieldName)) {
        info.addImport("com.google.gwt.core.client.JsArray");
      }
      if (type.canAsFrame()) {
        String typeURI = type.getUri();
        if (
          !config.includeType(typeURI) ||
          config.useJavaScriptObject(frameURI, fieldName)
        ) {
          continue;
        }
        ClassInfo fieldInfo = getClassInfo(type);
        info.addImport(fieldInfo);
      }
    }
    
  }

  private ClassInfo getClassInfo(RdfType frame) {
    ClassInfo info = classInfoMap.get(frame.getUri());
    if (info == null) {
      ModuleInfo moduleInfo = getModuleInfo(frame.getNamespace());
      
      String packageName = moduleInfo.getPackageName() + ".client";
      info = new ClassInfo(moduleInfo, packageName, frame);
      classInfoMap.put(frame.getUri(), info);
      
      
    }
    return info;
  }

  

  private ModuleInfo getModuleInfo(String namespaceURI) {
    ModuleInfo info = moduleInfoMap.get(namespaceURI);
    if (info == null) {

      String modulePackage = toJavaName(namespaceURI);
      
      info = new ModuleInfo(modulePackage, "DataModel");
      moduleInfoMap.put(namespaceURI, info);
    }
    return info;
  }



  private String toJavaName(String uri) {
    String javaName = uri2JavaName.get(uri);
    if (javaName == null) {
      OntModel model = typeManager.getOntModel();
      Resource resource = model.getResource(uri);
      if (resource != null) {
        Statement statement = resource.getProperty(BindVocabulary.javaName);
        if (statement != null && statement.getObject().isLiteral()) {
          javaName = statement.getString();
        }
      }
      if (javaName == null) {
        javaName = defaultJavaName(uri);
      }
      uri2JavaName.put(uri, javaName);
    }
    return javaName;
  }

  private List<String> listParts(String uri) {
    
    List<String> list = new ArrayList<String>();
    StringTokenizer tokenizer = new StringTokenizer(uri, "/#");
    
    String protocol = tokenizer.nextToken();
    if (!protocol.endsWith(":")) {
      list.add(protocol);
    }
    if (tokenizer.hasMoreTokens()) {
      String domain = tokenizer.nextToken();
      String[] array = domain.split("\\.");
      for (int i=array.length-1; i>=0; i--) {
        list.add(array[i]);
      }
      
    }
    while (tokenizer.hasMoreTokens()) {
      list.add(tokenizer.nextToken());
    }
    return list;
  }
  
  String defaultJavaName(String namespace) {
    StringBuilder builder = new StringBuilder();
    List<String> list = listParts(namespace);
    for (String part : list) {
      if (builder.length()>0) {
        builder.append('.');
      }
      builder.append(part);
    }
    return builder.toString();
  }
  
  
  class BaseWriter {
    private PrintWriter out;
    private int indent = 0;
    
    public BaseWriter(PrintWriter out) {
      this.out = out;
    }
    

    protected void pushIndent() {
      indent++;
    }
    
    protected void popIndent() {
      indent--;
    }
    
    protected BaseWriter print(String text) {
      out.print(text);
      return this;
    }
    
    protected void println() {
      out.println();
    }
    
    protected void println(String text) {
      out.println(text);
    }
    
    protected BaseWriter indent() {
      for (int i=0; i<2*indent; i++) {
        out.print(' ');
      }
      return this;
    }
    
    protected BaseWriter indent(String text) {
      indent();
      return print(text);
    }
    
    
    
  }
  
  class ModuleWriter extends BaseWriter {
    private ModuleInfo moduleInfo;
    
    public ModuleWriter(PrintWriter out, ModuleInfo info) {
      super(out);
      this.moduleInfo = info;
    }
    
    public void printModule() {

      println("<module>");
      pushIndent();
      printInherits();
      indent().println("<source path=\"client\"/>");
      popIndent();
      println("</module>");
      
    }

    private void printInherits() {
      List<String> list = new ArrayList<String>( moduleInfo.getInheritSet());
      Collections.sort(list);
      for (String name : list) {
        indent("<inherits name=\"").print(name).println("\"/>");
      }
      
    }

    
  }
  
  class ClassWriter extends BaseWriter {
    ClassInfo info;
    
    
    
    public ClassWriter(PrintWriter out, ClassInfo info) {
      super(out);
      this.info = info;
    }
    
    public void printEnum() {

      print("package ").print(info.getPackageName()).println(";");

      Enumeration type = info.getRdfType().asEnumeration();
      println();
      print("public enum ").print(type.getLocalName()).println(" {");
      pushIndent();
      List<NamedIndividual> list = type.getIndividualList();
      for (int i=0; i<list.size()-1; i++) {
        indent(list.get(i).getLocalName()).println(",");
      }
      if (!list.isEmpty()) {
        indent().println(list.get(list.size()-1).getLocalName());
      }
      
      popIndent();
      println("}");
      
    }

    public void printClass() {
      print("package ").print(info.getPackageName()).println(";");
      println();
      printImports();
      beginClass();
      pushIndent();
      printConstructor();
      printCreateMethod();
      printFields();
      
      popIndent();
      endClass();
    }

    private void printFields() {
      String frameType = info.getRdfType().asFrame().getUri();
      
      if (!info.hasSingleSupertype()) {
        printLdContextField();
        printRdfTypeField();
        printResourceUriField();
      }
      
      List<Field> list = !info.hasMultipleSupertypes() ? 
          info.getRdfType().asFrame().getDeclaredFields() :
          info.getRdfType().asFrame().listAllFields();
        
      for (Field field : list) {
        if (config.excludeProperty(frameType, field.getLocalName())) {
          continue;
        }
        printField(field);
      }
      
    }
    private void printResourceUriField() {
      printResourceUriGetter();
      printResourceUriSetter();
    }

    private void printResourceUriSetter() {
      println();
      indent().println("public final native void setResourceUri(String uri) /*-{");
      indent().println("  this[\"@id\"] = uri;");
      indent().println("}-*/;");
    }

    private void printResourceUriGetter() {
      println();
      indent().println("public final native String getResourceUri() /*-{");
      indent().println("  return this[\"@id\"];");
      indent().println("}-*/;");
      
    }

    private void printRdfTypeField() {
      printRdfTypeGetter();
      printRdfTypeSetter();
      
    }

    private void printRdfTypeSetter() {
      println();
      indent().println("public final native void setRdfType(String type) /*-{");
      indent().println("  this[\"@type\"]=type;");
      indent().println("}-*/;");
      
    }

    private void printRdfTypeGetter() {
      println();
      indent().println("public final native String getRdfType() /*-{");
      pushIndent();
      indent().println("return this[\"@type\"]");
      popIndent();
      indent().println("}-*/;");
    }

    private void printLdContextField() {
      printLdContextGetter();
      printLdContextSetter();
      
    }

    private void printLdContextSetter() {
      println();
      indent().println("public final native void setLdContextUri(String uri) /*-{");
      pushIndent();
      indent().println("this[\"@context\"]=uri;");
      popIndent();
      indent().println("}-*/;");
      
    }

    /**
  public final native String getLdContextUri() {
    if (typeof this["@context"] == "string") {
      return this["@context"];
    }
    return null;
  }
     */
    private void printLdContextGetter() {
      println();
      indent().println("public final native String getLdContextUri() /*-{");
      pushIndent();
      indent().println("if (typeof this[\"@context\"] == \"string\") {");
      indent().println("  return this[\"@context\"];");
      indent().println("}");
      indent().println("return null;");
      popIndent();
      indent().println("}-*/;");
      
    }

    private void printField(Field field) {
      RdfType type = field.getRdfType();
      
        
      
      if (type.canAsEnumeration()) {
        printEnumField(field);
      } else if (type.canAsFrame()) {
        int maxCardinality = field.getMaxCardinality();
        if (maxCardinality == 1) {
          printEntityField(field);
        } else {
          printCollectionField(field);
        }
      } else if (type.canAsDatatype()) {
        printDatatypeField(field);
      } 
    }

    private void printEnumField(Field field) {
      printEnumGetter(field);
      printEnumSetter(field);
      printEnumNameGetter(field); 
      printEnumNameSetter(field);
      
    }

    private void printEnumSetter(Field field) {

      String fieldName = field.getLocalName();
      String fieldType = field.getRdfType().getLocalName();
      String setter = StringUtil.setter(fieldName);
      String nameSetter = setter + "Name";
      
      println();
      indent("public final void ").print(setter).print("(").print(fieldType);
      println(" value) {");
      pushIndent();
      indent(nameSetter).println("(value.name());");
      popIndent();
      indent().println("}");
      
    }

    private void printEnumGetter(Field field) {
      String fieldName = field.getLocalName();
      String fieldType = field.getRdfType().getLocalName();
      String getter = StringUtil.getter(fieldName);
      String nameGetter = getter + "Name";
      
      println();
      indent("public final ").print(fieldType).print(" ").print(getter).println("() {");
      pushIndent();
      indent("return ").print(fieldType).print(".valueOf(").print(fieldType).print(".class, ");
      print(nameGetter).println("());");
      popIndent();
      indent().println("};");
      
    }

    private void printEnumNameSetter(Field field) {
      String fieldName = field.getLocalName();
      String setter = StringUtil.setter(fieldName) + "Name";
      
      println();
      indent("private final native void ").print(setter).println("(String value) /*-{");
      pushIndent();
      indent("this.").print(fieldName).println(" = value;");
      popIndent();
      indent().println("}-*/;");
      
    }

    private void printEnumNameGetter(Field field) {
     
      String fieldName = field.getLocalName();
      String getter = StringUtil.getter(fieldName) + "Name";
      
      println();
      indent("private final native String ").print(getter).println("() /*-{");
      pushIndent();
      indent("return this.").print(fieldName).println(";");
      popIndent();
      indent().println("}-*/;");
      
    }

    private void printDatatypeField(Field field) {
      printDatatypeGetter(field);
      printDatatypeSetter(field);
    }
    
    private void printDatatypeSetter(Field field) {
      Datatype type = field.getRdfType().asDatatype();
      String typeName = DatatypeUtil.toGwtType(type);

      String fieldName = field.getLocalName();
      String setter = StringUtil.setter(fieldName);
      
      println();
      indent("public final native void ").print(setter).print("(");
      print(typeName).println(" value) /*-{");
      pushIndent();
      indent("this.").print(fieldName).println(" = value;");
      popIndent();
      indent().println("}-*/;");
      
    }

    private void printDatatypeGetter(Field field) {
      Datatype type = field.getRdfType().asDatatype();
      String typeName = DatatypeUtil.toGwtType(type);

      String fieldName = field.getLocalName();
      String getter = StringUtil.getter(fieldName);
      
      println();
      indent("public final native ").print(typeName).print(" ").print(getter);
      println("() /*-{");
      pushIndent();
      indent("return this.").print(fieldName).println(";");
      popIndent();
      indent().println("}-*/;");
    }

    /*
public final native JsArray<Phone> getPhone() {
  if (typeof this.phone == "object") {
    return new Array(this.phone);
  } else if (typeof this.phone == "string") {
    return { "@id" : this.phone};
  }
  return this.phone;
}
     */
    private void printCollectionField(Field field) {

      String fieldType =  null;
      
      if (OWL.Thing.getURI().equals(field.getRdfType().getUri())) {
        fieldType = "JavaScriptObject";
      } else {
        fieldType = field.getRdfType().getLocalName();
      }
      
      printCollectionGetter(field, fieldType);
      printCollectionSetter(field, fieldType);
    }
    
    private void printCollectionSetter(Field field, String fieldType) {

      String fieldName = field.getLocalName();
      String setter = StringUtil.setter(fieldName);
      
      println();
      indent("public final native void ").print(setter).print("(JsArray<").print(fieldType);
      println("> array) /*-{");
      pushIndent();
      indent("this.").print(fieldName).println(" = array;");
      popIndent();
      indent().println("}-*/;");
      
    }

    private void printCollectionGetter(Field field, String fieldType) {

      String fieldName = field.getLocalName();
      String getter = StringUtil.getter(fieldName);
      
      println();
      indent("public final native JsArray<");
      print(fieldType);
      print("> ");
      print(getter);
      println("() /*-{");
      pushIndent();
      indent("if (typeof this.").print(fieldName).println(" == \"object\") {");
      indent("  return [this.").print(fieldName).println("];");
      indent("} else if (typeof this.").print(fieldName).println(" == \"string\") {");
      indent("  return [{ \"@id\" : this.").print(fieldName).println("}];");
      indent().println("}");
      indent("return this.").print(fieldName).println(";");
      popIndent();
      indent().println("}-*/;");
    }

    private void printEntityField(Field field) {
      
      String ownerURI = field.getDeclaringFrame().getUri();
      RdfType type = field.getRdfType();
      String fieldType = type.getLocalName();
      String fieldName = field.getLocalName();
      if (
        !config.includeType(type.getUri()) || 
        config.useJavaScriptObject(ownerURI, fieldName)
      ) {
        fieldType = "JavaScriptObject";
      }
      
      printEntityGetter(field, fieldType);
      printEntitySetter(field, fieldType);
      printEntityUriGetter(field);
      printEntityUriSetter(field);
    }
    
    private void printEntityUriSetter(Field field) {

      String fieldName = field.getLocalName();
      String setter = StringUtil.setter(fieldName) + "Uri";
      
      println();
      indent().print("public final native void ").print(setter).println("(String uri) /*-{");
      pushIndent();
      indent("this.").print(fieldName).println(" = uri;");
      popIndent();
      indent().println("}-*/;");
      
    }

//      public final native String getPostalAddressUri() {
//        if ( typeof this.postalAddress == "string") {
//          return this.postalAddress;
//        }
//        if (typeof this.postalAddress == "object") {
//          return this.postalAddress["@id"];
//        }
//        return null;
//      }
    private void printEntityUriGetter(Field field) {

      String fieldName = field.getLocalName();
      String getter = StringUtil.getter(fieldName) + "Uri";
      
      println();
      indent("public final native String ").print(getter).println("() /*-{");
      pushIndent();
      indent("if (typeof this.").print(fieldName).println(" == \"string\") {");
      indent("  return this.").print(fieldName).println(";");
      indent().println("}");
      indent("if (typeof this.").print(fieldName).println(" == \"object\") {");
      indent(" return this.").print(fieldName).println("[\"@id\"];");
      indent().println("}");
      indent().println("return null;");
      popIndent();
      indent().println("}-*/;");
      
    }

    private void printEntitySetter(Field field, String typeName) {
      
      String fieldName = field.getLocalName();
      String setter = StringUtil.setter(fieldName);
      
      println();
      indent("public final native void ").print(setter).print("(");
      print(typeName).println(" value) /*-{");
      pushIndent();
      indent("this.").print(fieldName).println(" = value;");
      popIndent();
      indent().println("}-*/;");
      
    }

    private void printEntityGetter(Field field, String fieldType) {
      String fieldName = field.getLocalName();
      String getter = StringUtil.getter(fieldName);
      println();
      indent("public final native ");
      print(fieldType);
      print(" ");
      print(getter);
      println("() /*-{");
      pushIndent();
      indent("if (typeof this.").print(fieldName).println(" == \"string\") {");
      pushIndent();
      indent("return { \"@id\" : this.").print(fieldName).println("};");
      popIndent();
      indent().println("}");
      
      indent("return this.");
      print(fieldName);
      println(";");
      popIndent();
      indent().println("}-*/;");
    }

    private void printCreateMethod() {
      println();
      indent("public static ");
      print(info.getRdfType().getLocalName());
      println(" create() {");
      pushIndent();
      indent().println("return JavaScriptObject.createObject().cast();");
      popIndent();
      indent().println("}");
      
    }

    private void printConstructor() {
      println();
      indent("protected ");
      print(info.getRdfType().getLocalName());
      println("() {}");
      
    }


    private void beginClass() {
      println();
      print("public class ");
      print(info.getRdfType().getLocalName());
      
      
      if (
        info.hasSingleSupertype() && 
        config.includeType(info.getSingleSupertype().getUri())
      ) {

        print(" extends ");
        String superTypeName = info.getSingleSupertype().getLocalName();
        print(superTypeName);
        println(" {");
      } else {
        println(" extends JavaScriptObject {");
      }
      
    }

    private void endClass() {
      println("}");
      
    }

    private void printImports() {
     for (String pkg : info.listImports()) {
       print("import ").print(pkg).println(";");
     }
      
    }

    
  }
  
  static class ModuleInfo {
    private String packageName;
    private String localName;
    private String moduleName;
    private Set<String> inheritSet = new HashSet<String>();
    
    public ModuleInfo(String packageName, String localName) {
      this.packageName = packageName;
      this.localName = localName;
      this.moduleName = packageName + "." + localName;
      inheritSet.add("com.google.gwt.user.User");
      inheritSet.add("com.google.gwt.json.JSON");
    }

    public String getPackageName() {
      return packageName;
    }
    
    public String getLocalName() {
      return localName;
    }

    public String getModuleName() {
      return moduleName;
    }

    public void addInherits(ModuleInfo info) {
      if (info != this) {
        inheritSet.add(info.getModuleName());
      }
    }
    
    public Set<String> getInheritSet() {
      return inheritSet;
    }
    
  }
  
  static class ClassInfo {
    private ModuleInfo moduleInfo;
    private String packageName;
    private String javaName;
    private RdfType rdfType;
    private Set<String> imports = new HashSet<String>();
    private List<Frame> supertypeList = null;
    
    
    public ClassInfo(ModuleInfo moduleInfo, String packageName, RdfType rdfType) {
      this.moduleInfo = moduleInfo;
      this.packageName = packageName;
      this.rdfType = rdfType;
      javaName = packageName + "." + rdfType.getLocalName();
    }
    
    public ModuleInfo getModuleInfo() {
      return moduleInfo;
    }

    public List<String> listImports() {
      List<String> list = new ArrayList<String>(imports);
      Collections.sort(list);
      return list;
    }
    
    public List<Frame> listSupertypes() {
      if (supertypeList == null) {
        // Filter standard RDF types from the type hierarchy.
        List<Frame> sourceList = rdfType.asFrame().getSupertypeList();
        supertypeList = new ArrayList<Frame>();
        for (Frame frame : sourceList) {
          String uri = frame.getUri();
          if (!"http://www.w3.org/2000/01/rdf-schema#Resource".equals(uri)) {
            supertypeList.add(frame);
          }
        }
      }
      return supertypeList;
    }
    
    public boolean hasSingleSupertype() {
      return listSupertypes().size()==1;
    }
    public boolean hasMultipleSupertypes() {
      return listSupertypes().size()>1;
    }
    
    public Frame getSingleSupertype() {
      List<Frame> list = listSupertypes();
      return (list.size()>0) ? list.get(0) : null;
    }


    public String getPackageName() {
      return packageName;
    }

    public String getJavaName() {
      return javaName;
    }


    public RdfType getRdfType() {
      return rdfType;
    }


    public void addImport(String className) {
      if (className.startsWith(packageName)) {
        if (className.indexOf('.', packageName.length()) > 0) {
          return;
        }
      }
      imports.add(className);
    }
    
    public void addImport(ClassInfo importedClass) {
      if (importedClass.getModuleInfo() != moduleInfo) {
        String javaName = importedClass.getJavaName();
        imports.add(javaName);
        moduleInfo.addInherits(importedClass.getModuleInfo());
      }
    }
    
  }
  
  
}
