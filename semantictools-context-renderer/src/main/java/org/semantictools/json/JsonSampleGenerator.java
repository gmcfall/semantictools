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
package org.semantictools.json;

import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.JsonNodeFactory;
import org.codehaus.jackson.node.ObjectNode;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.semantictools.context.renderer.model.ContextProperties;
import org.semantictools.context.renderer.model.FrameConstraints;
import org.semantictools.context.renderer.model.JsonContext;
import org.semantictools.context.renderer.model.TermInfo;
import org.semantictools.frame.api.FrameNotFoundException;
import org.semantictools.frame.api.TypeManager;
import org.semantictools.frame.model.Datatype;
import org.semantictools.frame.model.Field;
import org.semantictools.frame.model.Frame;
import org.semantictools.frame.model.ListType;
import org.semantictools.frame.model.RdfType;
import org.semantictools.frame.model.RestCategory;

import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntResource;
import com.hp.hpl.jena.vocabulary.XSD;

public class JsonSampleGenerator {
  
  private TypeManager typeManager;
  private JsonContext context;
  private ContextProperties contextProperties;
  private JsonNodeFactory factory;
  private Random random;
  
  private int maxCyclicDepth = 2;
  private int maxRepeat = 2;

  public JsonSampleGenerator(TypeManager typeManager) {
    this.typeManager = typeManager;
    factory = JsonNodeFactory.instance;
    random = new Random(new Date().getTime());
  }
  
  public ObjectNode generateSample(JsonContext context, ContextProperties properties) {

    ObjectNode node = factory.objectNode();
    
    if (context == null) {
      return node;
    }
    
    this.contextProperties = properties;
    this.context = context;
    addContextProperty(node);
    
    List<String> graphTypes = properties.getGraphTypes();
    if (graphTypes.isEmpty()) {
      Frame frame = typeManager.getFrameByUri(context.getRootType());
      Branch branch = new Branch(null, null, node, frame);
      
      addTypeProperty(node, frame);
      addProperties(branch);
      
    } else {
      buildGraph(node, graphTypes);
    }
    
    
    return node;
  }

  private void buildGraph(ObjectNode node, List<String> graphTypes) {
   
    ArrayNode array = factory.arrayNode();
    node.put("@graph", array);
    for (String typeURI : graphTypes) {
      Frame frame = typeManager.getFrameByUri(typeURI);
      if (frame == null) {
        throw new FrameNotFoundException(typeURI);
      }
      ObjectNode obj = factory.objectNode();
      array.add(obj);
      addTypeProperty(obj, frame);

      Branch branch = new Branch(null, null, obj, frame);
      addProperties(branch);
    }
    
  }

  private void addProperties(Branch branch) {
    
    addConditionalTypeProperty(branch);
    addIdProperty(branch);
    
    List<Field> fieldList = branch.getFrame().listAllFields();
    
    for (Field field : fieldList) {
      if (!isIncluded(field, contextProperties, branch.getFrame())) continue;
      addField(branch, field, null);
    }
    
    
  }

  private boolean isIncluded(Field field, ContextProperties properties, Frame declaringFrame) {
    
    String fieldType = field.getRdfType().getUri();
    if (properties.getExcludedTypes().contains(fieldType)) return false;
    
    FrameConstraints constraints = properties.getFrameConstraints(declaringFrame.getLocalName());
   
    return (constraints == null) || constraints.isIncludedProperty(field.getURI());
  }

  
  private void addField(Branch branch, Field field, String fieldName) {
    
    RdfType type = field.getRdfType();
    
    if (fieldName == null && contextProperties.isSetProperty(field.getURI())) {
      addSetProperty(branch, field, type);
      
    } else if (type.canAsDatatype()) {
      addDatatype(branch, field, fieldName, type.asDatatype());
      
    } else if (type.canAsListType()) {
      addList(branch, field, type.asListType());
      
    } else if (type.canAsFrame() && !shortCircuit(branch, field, fieldName, type.asFrame())) {
      
      addFrame(branch, field, fieldName, type.asFrame());
    }
    
  }
  
  

  private boolean shortCircuit(Branch branch, Field field, String fieldName, Frame frame) {
    List<Frame> frameList = frame.listAllSubtypes();
    List<Datatype> typeList = frame.getSubdatatypeList();
    
    if ((frameList.size()+typeList.size()) != 1) return false;
    
    if (frameList.isEmpty()) {
      addDatatype(branch, field, fieldName, typeList.get(0));
      
    } else {
      addFrame(branch, field, fieldName, frameList.get(0));
    }
    
    
    return true;
  }

  
  private void addFrame(final Branch branch, Field field, String fieldNameOverride, Frame frame) {
    
    int maxCount = field.getMaxCardinality();
    if (maxCount < 0 || maxCount>maxRepeat) {
      maxCount = maxRepeat;
    }
    
    String uri = field.getURI();
    TermInfo term = context.getTermInfoByURI(uri);
    if (term == null) {
      return;
    }
    final String fieldName = (fieldNameOverride==null) ? term.getTermName() : fieldNameOverride;
    
    NodeConsumer callback = null;
    
    if (maxCount == 1) {
      callback = new NodeConsumer() {
        
        @Override
        public void consume(JsonNode node) {
          branch.getNode().put(fieldName, node);
          
        }
      };
      
    } else {
      ArrayNode array = factory.arrayNode();
      branch.getNode().put(fieldName, array);
      
      callback = new ArrayNodeConsumer(array);
      
    }
    for (int i=0; i<maxCount; i++) {
      createFrame(branch, term, field, frame, callback);
    }
    
    
  }
  
  static class ArrayNodeConsumer implements NodeConsumer {

    private ArrayNode array;
    
    public ArrayNodeConsumer(ArrayNode array) {
      this.array = array;
    }

    @Override
    public void consume(JsonNode node) {
      array.add(node);      
    }
    
  }
  interface NodeConsumer {
    void consume(JsonNode node);
  }
  
  private void createFrame(Branch branch, TermInfo term, Field field, Frame frame, NodeConsumer callback) {

    boolean iriReference = term!=null && term.isCoercedAsIriRef();
    
    if (iriReference && frame.getCategory() == RestCategory.ENUMERABLE) {
      createEnumReference(branch, frame, callback);
      
    } else if (iriReference) {
      String typeName = frame.getLocalName();
      int id = random.nextInt(100000);
      callback.consume(factory.textNode("http://server.example.com/resources/" + typeName + "/" + id));
      
    } else {

      ObjectNode child = factory.objectNode();

      
      if (exceedsMaxCyclicDepth(branch, frame)) {
        return;
      }
      
      Frame childFrame = selectType(frame);
      Branch childBranch = new Branch(branch, field, child, childFrame);
      
      addProperties(childBranch);
      callback.consume(child);
    }
    
    
  }

  private void createEnumReference(Branch branch, Frame frame, NodeConsumer callback) {
    // TODO: it is expensive to compute the list of instances each time this method is called.
    // We might want to cache instance lists, so that they get generated only one.
    OntClass type = frame.getType();
    List<? extends OntResource> list = type.listInstances(false).toList();
    
    OntResource target = list.isEmpty() ? null : list.get(random.nextInt(list.size()));
    
    if (target == null) {
      String typeName = frame.getLocalName();
      int id = random.nextInt(100000);
      callback.consume(factory.textNode("http://server.example.com/resources/" + typeName + "/" + id));
      
    } else {
      String name = getSimpleName(target.getURI());
      callback.consume(factory.textNode(name));
    }
    
    
    
      
  }

//  private void printDebug(Branch branch, Field field) {
//    List<String> list = new ArrayList<String>();
//    list.add(field.getLocalName());
//    while (branch != null) {
//      field = branch.getField();
//      if (field != null) {
//        list.add(0, field.getLocalName());
//      }
//      branch = branch.getParent();
//    }
//    StringBuilder builder = new StringBuilder();
//    for (int i=0; i<list.size(); i++) {
//      if (i>0) {
//        builder.append(".");
//      }
//      builder.append(list.get(i));
//    }
//    
//    System.out.println(builder.toString());
//    
//    
//  }

  private Frame selectType(Frame frame) {
   
    if (frame.getSubtypeList().isEmpty()) return frame;
    
    List<Frame> list = frame.listAllSubtypes();
    if (!frame.isAbstract()) {
      list.add(frame);
    }
    Iterator<Frame> sequence = list.iterator();
    while (sequence.hasNext()) {
      Frame type = sequence.next();
      if (type.isAbstract()) sequence.remove();
    }
    
    if (list.isEmpty()) return frame;
    
    return list.get(random.nextInt(list.size()));
  }
  

  private boolean exceedsMaxCyclicDepth(Branch branch, Frame frame) {
    int count = 0;
    while (branch != null) {
      RdfType type = (branch.getField()==null) ? branch.getFrame() : branch.getField().getRdfType();
      if (type.canAsFrame() && type.asFrame() == frame) {
        count++;
        if (count >= maxCyclicDepth) return true;
      }
      branch = branch.getParent();
    }
    
    return false;
  }

  private void addList(Branch branch, Field field, ListType listType) {
    
    String fieldName = getSimpleName(field.getURI());
    if (fieldName == null) return;
    RdfType elementType = listType.getElementType();
    
    ArrayNode array = factory.arrayNode();
    
    branch.getNode().put(fieldName, array);
    
    if (elementType.canAsDatatype()) {
      addDatatypesToArray(array, elementType.asDatatype());
      
    } else if (elementType.canAsFrame()) {
      addFramesToArray(branch, field, array, elementType.asFrame());
    }
    
  }

  private void addFramesToArray(Branch branch, Field field, ArrayNode array, Frame frame) {
    
    ArrayNodeConsumer callback = new ArrayNodeConsumer(array);
    TermInfo term = context.getTermInfoByURI(field.getURI());
    
    for (int i=0; i<maxRepeat; i++) {
      createFrame(branch, term, field, frame, callback);
    }
    
  }

  private void addDatatypesToArray(ArrayNode array, Datatype datatype) {
    
    for (int i=0; i<maxRepeat; i++) {
      JsonNode value = createDatatype(datatype);
      array.add(value);
    }
    
  }


  private void addSetProperty(Branch branch, Field field, RdfType type) {
    String fieldName = getSimpleName(field.getURI());
    
    
    ObjectNode setContainer = factory.objectNode();
    branch.getNode().put(fieldName, setContainer);
    
    
    TermInfo term = context.getTermInfoByURI(branch.getFrame().getUri());
    String typeName = (term==null) ? branch.getFrame().getLocalName() : term.getTermName();
    String value = "http://server.example.com/resources/" + typeName + "/" + random.nextInt(100000) + "/" + fieldName;
    setContainer.put("@id", value);
    Branch setBranch = new Branch(branch, field, setContainer, branch.getFrame());
    addField(setBranch, field, "@set");
    
    
    
  }

  private void addDatatype(Branch branch, Field field, String fieldName, Datatype type) {
    
    int maxCount = field.getMaxCardinality();
    if (maxCount<0 || maxCount>maxRepeat) {
      maxCount = maxRepeat;
    }
    if (fieldName == null) {
      fieldName = getSimpleName(field.getURI());
    }
    if (fieldName == null) return;
    
    if (maxCount == 1) {
      JsonNode value = createDatatype(type);
      branch.getNode().put(fieldName, value);
      return;
    }
    
    ArrayNode array = factory.arrayNode();
    branch.getNode().put(fieldName, array);
    for (int i=0; i<maxCount; i++) {
      JsonNode value = createDatatype(type);
      array.add(value);
    }
    
  }

  private JsonNode createDatatype(Datatype datatype) {
    JsonNode node = null;

    String uri = datatype.getUri();
    String baseURI = typeManager.getXsdBaseURI(datatype);
    
    if (baseURI == null) {
      throw new UnsupportedDatatypeException(uri);
    }
    
    if (XSD.anyURI.getURI().equals(uri)) {
      node = factory.textNode("http://www.example.com/sampleURI");
      
    } else if (XSD.date.getURI().equals(baseURI)) {
      
      DateTime now = DateTime.now();
      DateTimeFormatter formatter = DateTimeFormat.forPattern("YYYY-MM-ddZZ");
      node = factory.textNode(formatter.print(now));
      
    } else if (XSD.dateTime.getURI().equals(baseURI)) {

      DateTime now = DateTime.now();
      DateTimeFormatter formatter = DateTimeFormat.forPattern("YYYY-MM-dd'T'HH:mm:ssZZ");
      node = factory.textNode(formatter.print(now));
      
    } else if (XSD.xboolean.getURI().equals(baseURI)) {
      boolean value[] = new boolean[] {true, false};
      node = factory.booleanNode(value[random.nextInt(2)]);
      
    } else if (
        XSD.xbyte.getURI().equals(baseURI) ||
        XSD.unsignedByte.getURI().equals(baseURI)
    ) {
      byte value = (byte) random.nextInt(8);
      node = factory.numberNode(value);
      
    } else if (
      XSD.decimal.getURI().equals(baseURI) ||
      XSD.xdouble.getURI().equals(baseURI) ||
      XSD.xfloat.getURI().equals(baseURI)
    ) {
      
      String text = Double.toString(random.nextInt(1000) * random.nextDouble());
      if (text.length() > 5) {
        text = text.substring(0, 5) + "000000000";
      }
      double value = Double.parseDouble(text);
      node = factory.numberNode(value);
      
    } else if (
        XSD.duration.getURI().equals(baseURI) ||
        "http://www.w3.org/2004/10/xpath-datatypes#dayTimeDuration".equals(baseURI)
    ) {
      int hour = random.nextInt(24);
      int min = random.nextInt(60);
      node = factory.textNode("PT" + hour + "H" + min + "M");
      
    } else if ("http://www.w3.org/2004/10/xpath-datatypes#yearMonthDuration".equals(baseURI)) {
      int year = GregorianCalendar.getInstance().get(GregorianCalendar.YEAR);
      int month = random.nextInt(12)+1;
      node = factory.textNode("P" + year + "Y" + month + "M");
      
    } else if (XSD.gDay.getURI().equals(baseURI)) {
     
      String text = "---" + zeroPad(random.nextInt(30), 2);
      node = factory.textNode(text);
      
    } else if (XSD.gMonth.getURI().equals(baseURI)) {
      String text = "--" +zeroPad(random.nextInt(12)+1, 2);
      node = factory.textNode(text);
      
    } else if (XSD.gMonthDay.getURI().equals(baseURI)) {
      String text = "--" +zeroPad(random.nextInt(12)+1, 2) + "-" + zeroPad(random.nextInt(30), 2);
      node = factory.textNode(text);
      
    } else if (XSD.gYear.getURI().equals(baseURI)) {
      String text = Integer.toString( GregorianCalendar.getInstance().get(GregorianCalendar.YEAR) );
      node = factory.textNode(text);
      
    } else if (XSD.gYearMonth.getURI().equals(baseURI)) {
      int year = GregorianCalendar.getInstance().get(GregorianCalendar.YEAR);
      String month = zeroPad(random.nextInt(12) + 1, 2);
      node = factory.textNode(year + "-" + month);
      
    } else if (
        XSD.ID.getURI().equals(baseURI) ||
        XSD.IDREF.getURI().equals(baseURI)
    ) {
      node = factory.textNode("x" + random.nextInt(10000));
      
      
    } else if (
      XSD.xint.getURI().equals(baseURI) ||
      XSD.integer.getURI().equals(baseURI) ||
      XSD.nonNegativeInteger.getURI().equals(baseURI) ||
      XSD.positiveInteger.getURI().equals(baseURI) ||
      XSD.xlong.getURI().equals(baseURI) ||
      XSD.unsignedInt.getURI().equals(baseURI) ||
      XSD.unsignedLong.getURI().equals(baseURI)
    ) {
      node = factory.numberNode(random.nextInt(10000));
      
    } else if (XSD.language.getURI().equals(baseURI)) {
      
      String[] languageList = new String[] {
        "ar", "en", "en-us", "fr", "de", "it", "ja", "pl", "ru", "es", "sv", "zh"
      };
      String text = languageList[random.nextInt(languageList.length)];
      node = factory.textNode(text);
      
      
    } else if (
        XSD.Name.getURI().equals(baseURI) ||
        XSD.NCName.getURI().equals(baseURI) ||
        XSD.token.getURI().equals(baseURI) ||
        XSD.normalizedString.getURI().equals(baseURI) ||
        XSD.xstring.getURI().equals(baseURI)
    ) {
      String name[] = new String[] {
        "alpha", "beta", "gamma", "delta", "epsilon", "zeta",
        "eta", "theta", "iota", "kappa", "lambda", "mu", "nu",
        "xi", "omicron", "pi", "rho", "sigma", "tau", "upsilon",
        "phi", "chi", "psi", "omega"
      };
      node = factory.textNode(name[random.nextInt(name.length)]);
      
    } else if (XSD.negativeInteger.getURI().equals(baseURI)) {
      int value = -random.nextInt(10000);
      node = factory.numberNode(value);
      
    } else if (
        XSD.xshort.getURI().equals(baseURI) ||
        XSD.unsignedShort.getURI().equals(baseURI)
    ) {
      
      short value = (short) random.nextInt(100);
      node = factory.numberNode(value);
      
    } else if (XSD.time.getURI().equals(baseURI)){
      DateTime now = DateTime.now();
      DateTimeFormatter formatter = DateTimeFormat.forPattern("HH:mm:ss.SSS");
      String text = formatter.print(now);
      node = factory.textNode(text);
      
    } else {
      throw new UnsupportedDatatypeException(uri);
    }
    
    return node;
  }

  private String zeroPad(int value, int len) {
    String zero = "00000";
    String text = Integer.toString(value);
    
    if (text.length() < len) {
      text = zero.substring(0, len - text.length());
    } 
    if (text.length() > len) {
      text = text.substring(0, len);
    }
    return text;
    
  }

  private void addIdProperty(Branch branch) {
    
    if (branch.getFrame().getCategory() == RestCategory.ADDRESSABLE) {
      String typeName = simpleTypeName(branch.getFrame());
      String value = "http://server.example.com/resources/" + typeName + "/" + random.nextInt(100000);
      branch.getNode().put("@id", value);
    }
    
  }
  
  private String simpleTypeName(Frame frame) {
    TermInfo term = context.getTermInfoByURI(frame.getUri());
    return (term==null) ? frame.getLocalName() : term.getTermName();
    
  }

  private void addConditionalTypeProperty(Branch branch) {

    Field field = branch.getField();
    RdfType type = field==null ? null : field.getRdfType();
    Frame declaredFrame = 
      (type!=null && type.canAsListType()) ?  type.asListType().getElementType().asFrame() :
      (type != null) ? type.asFrame() : null;
      
    Frame actualFrame = branch.getFrame();
    
    if ((actualFrame==null) || (actualFrame == declaredFrame) || branch.getNode().has("@type")) return;
    
    addTypeProperty(branch.getNode(), actualFrame);
  }

  private void addTypeProperty(ObjectNode node, Frame frame) {
    node.put("@type", getSimpleName(frame.getUri()));
  }
  
  private String getSimpleName(String uri) {
    TermInfo term = context.getTermInfoByURI(uri);
    return term==null ? null : term.getTermName();
  }
  

  private void addContextProperty(ObjectNode node) {
    node.put("@context", context.getContextURI());
  }
  
  /**
   * A wrapper around an ObjectNode so that we can track associated objects
   * such as the parent within which the ObjectNode is placed, the Frame
   * that defines the properties of the ObjectNode, and the field through which
   * the ObjectNode is accessed.
   *
   */
  static class Branch {
    private Branch parent;
    private ObjectNode node;
    private Field field;
    private Frame frame;
    public Branch(Branch parent, Field field, ObjectNode node, Frame frame) {
      this.node = node;
      this.field = field;
      this.frame = frame;
      this.parent = parent;
    }
    public ObjectNode getNode() {
      return node;
    }
    public Frame getFrame() {
      return frame;
    }
    public Branch getParent() {
      return parent;
    }
    public Field getField() {
      return field;
    }
   
    
    
  }

}
