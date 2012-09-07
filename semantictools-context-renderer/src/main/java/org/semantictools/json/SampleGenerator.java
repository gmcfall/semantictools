package org.semantictools.json;

import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.semantictools.frame.model.Datatype;
import org.semantictools.frame.model.Field;
import org.semantictools.frame.model.Frame;
import org.semantictools.frame.model.ListType;
import org.semantictools.frame.model.RdfType;

import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntResource;
import com.hp.hpl.jena.rdf.model.AnonId;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.RDFList;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.XSD;

/**
 * Generates a sample RDF resource of a given type.
 * @author Greg McFall
 *
 */
public class SampleGenerator {
  
  private Random random;
  private int maxRepeat = 2;
  private int maxInstances = 3;
  private int sequence = 0;
  

  private Model model;
  
  public SampleGenerator(Model model) {
    this.model = model;
    random = new Random(new Date().getTime());
  }

  public Resource generateSample(Frame frame) {
    
    Resource root = createInstance(frame);
    addFields(root, frame);
    
    return root;
  }
  
  private Resource createInstance(Frame frame) {
    OntClass type = frame.getType();
    String typeName =  type.getLocalName();
    
    Resource resource = null;
    
    switch (frame.getCategory()) {

    
    case EMBEDDABLE: 
      AnonId id = new AnonId(typeName + (sequence++));
      resource = model.createResource(id);
      break;
      
    case ENUMERABLE :
      Resource value = selectInstance(type);
      if (value != null) {
        resource = model.getResource(value.getURI());
        if (resource != null) return resource;
        
        resource = model.createResource(value.getURI());
        break;
      }
      // if (value != null), then fall through to the default case.
      
    default:

      String instanceURI =        
          "http://server.example.com/resources/" + typeName + "/" + random.nextInt(100000);
      resource =  model.createResource(instanceURI);
      break;
      
    }
    
    resource.addProperty(RDF.type, type);
    return resource;
  }


  private Resource selectInstance(OntClass type) {
    List<Resource> list = new ArrayList<Resource>();
    Iterator<? extends OntResource> sequence = type.listInstances(false);
    while (sequence.hasNext()) {
      list.add(sequence.next());
    }
    return list.get(random.nextInt(list.size()));
  }

  private void addFields(Resource parent, Frame frame) {
    
    for (Field field : frame.listAllFields()) {
      addField(parent, field);
    }
    
  }

  private void addField(Resource parent, Field field) {
    
    
    RdfType type = field.getRdfType();
    
    if (type.canAsDatatype()) {
      addDatatype(parent, field);
      
    } else if (type.canAsListType()) {
      addList(parent, field);
      
    } else if (type.canAsFrame()) {
      addFrame(parent, field);
    }
    
    
    
  }


  private void addList(Resource parent, Field field) {
    
    ListType listType = field.getRdfType().asListType();
    
    RdfType type = listType.getElementType();
    
    RDFList list = model.createList();
    
    if (type.canAsDatatype()) {
      list = addDatatypes(list, type.asDatatype());
      
    } else if (type.canAsFrame()) {
      
      list = addFrames(list, type.asFrame());
    }

    parent.addProperty(field.getProperty(), list);
    
  }
  

  static class ListResourceConsumer implements ResourceConsumer {

    private RDFList list;
    
    public ListResourceConsumer(RDFList list) {
      this.list = list;
    }

    @Override
    public void consume(Resource value) {
      list = list.with(value);
    }

    public RDFList getList() {
      return list;
    }
    
  }

  private RDFList addFrames(RDFList list, Frame frame) {
    ListResourceConsumer consumer = new ListResourceConsumer(list);
    addFrame(frame, maxRepeat, consumer);
    
    return consumer.getList();
    
  }

  private RDFList addDatatypes(RDFList list, Datatype datatype) {
    for (int i=0; i<maxRepeat; i++) {
      RDFNode node = createDatatype(datatype);
      list = list.with(node);
    }
    return list;
  }

  private void addDatatype(Resource parent, Field field) {
    Datatype type = field.getRdfType().asDatatype();
    
    int max = field.getMaxCardinality();
    max = max<0 ? maxRepeat : Math.min(max, maxRepeat);
    for (int i=0; i<max; i++) {
      RDFNode node = createDatatype(type);
      parent.addProperty(field.getProperty(), node);
    }
    
  }
  
  private static interface ResourceConsumer {
    void consume(Resource value);
  }

  private void addFrame(final Resource parent, final Field field) {
    final Frame frame = field.getRdfType().asFrame();

    int max = field.getMaxCardinality();
    max = max<0 ? maxRepeat : Math.min(max, maxRepeat);
    addFrame(frame, max, new ResourceConsumer(){

      @Override
      public void consume(Resource value) {
        parent.addProperty(field.getProperty(), value);
      }});
 
    
  }
  

  private void addFrame(Frame frame, int count, ResourceConsumer consumer) {
    
    Map<String, InstanceInfo> map = new HashMap<String, InstanceInfo>();
    

    for (int i=0; i<count; i++) {
      Resource value = null;
      Frame selectedType = selectType(frame);
      InstanceInfo info = map.get(selectedType.getUri());
    
      
      if (info == null) {
        info = new InstanceInfo(listInstancesOfType(selectedType.getType()));
        map.put(selectedType.getUri(), info);
      }
      
      value = info.get();
      if (value == null) {
        value = createInstance(selectedType);
        addFields(value, selectedType);
        info.count++;
      }
      consumer.consume(value);
    }
    
  }
  
  /**
   * A structure that keeps track of the set of instances of a given type that have
   * already been created.
   * 
   * @author Greg McFall
   *
   */
  private class InstanceInfo {
    List<Resource> list;
    int count;
    
    /**
     * Initialize this InstanceInfo object with the list of resources of a given
     * type that already exist.
     * @param list
     */
    InstanceInfo(List<Resource> list) {
      this.list = list;
      count = list.size();
    }
    
    private Resource get() {
      if (count >= maxInstances && !list.isEmpty()) {
        return list.remove(random.nextInt(list.size()));
      }
      return null;
    }
    
    
    
    
  }

  private List<Resource> listInstancesOfType(OntClass type) {
    List<Resource> list = new ArrayList<Resource>();
    Iterator<Resource> sequence = model.listResourcesWithProperty(RDF.type, type);
    while (sequence.hasNext()) {
      list.add(sequence.next());
    }
    
    return list;
  }

  private RDFNode createDatatype(Datatype datatype) {
    RDFNode node = null;

    String uri = datatype.getUri();
    String baseURI = getBaseURI(datatype);
    
    if (baseURI == null) {
      throw new UnsupportedDatatypeException(uri);
    }
    
    if (XSD.anyURI.getURI().equals(uri)) {
      node = model.createTypedLiteral("http://www.example.com/sampleURI", uri);
      
    } else if (XSD.date.getURI().equals(baseURI)) {
      
      DateTime now = DateTime.now();
      DateTimeFormatter formatter = DateTimeFormat.forPattern("YYYY-MM-ddZZ");
      node = model.createTypedLiteral(formatter.print(now), uri);
      
    } else if (XSD.dateTime.getURI().equals(baseURI)) {

      DateTime now = DateTime.now();
      DateTimeFormatter formatter = DateTimeFormat.forPattern("YYYY-MM-dd'T'HH:mm:ssZZ");
      node = model.createTypedLiteral(formatter.print(now), uri);
      
    } else if (XSD.xboolean.getURI().equals(baseURI)) {
      boolean value[] = new boolean[] {true, false};
      node = model.createTypedLiteral(value[random.nextInt(2)], uri);
      
    } else if (
        XSD.xbyte.getURI().equals(baseURI) ||
        XSD.unsignedByte.getURI().equals(baseURI)
    ) {
      byte value = (byte) random.nextInt(8);
      node = model.createTypedLiteral(value, uri);
      
    } else if (
      XSD.decimal.getURI().equals(baseURI) ||
      XSD.xdouble.getURI().equals(baseURI) ||
      XSD.xfloat.getURI().equals(baseURI)
    ) {
      
      String text = Double.toString(random.nextInt(1000) * random.nextDouble());
      if (text.length() > 5) {
        text = text.substring(0, 5);
      }
      node = model.createTypedLiteral(Float.parseFloat(text), uri);
      
    } else if (
        XSD.duration.getURI().equals(baseURI) ||
        "http://www.w3.org/2004/10/xpath-datatypes#dayTimeDuration".equals(baseURI)
    ) {
      int hour = random.nextInt(24);
      int min = random.nextInt(60);
      node = model.createTypedLiteral("PT" + hour + "H" + min + "M", uri);
      
    } else if ("http://www.w3.org/2004/10/xpath-datatypes#yearMonthDuration".equals(baseURI)) {
      int year = GregorianCalendar.getInstance().get(GregorianCalendar.YEAR);
      int month = random.nextInt(12)+1;
      node = model.createTypedLiteral("P" + year + "Y" + month + "M", uri);
      
    } else if (XSD.gDay.getURI().equals(baseURI)) {
     
      String text = "---" + zeroPad(random.nextInt(30), 2);
      node = model.createTypedLiteral(text, uri);
      
    } else if (XSD.gMonth.getURI().equals(baseURI)) {
      String text = "--" +zeroPad(random.nextInt(12)+1, 2);
      node = model.createTypedLiteral(text, uri);
      
    } else if (XSD.gMonthDay.getURI().equals(baseURI)) {
      String text = "--" +zeroPad(random.nextInt(12)+1, 2) + "-" + zeroPad(random.nextInt(30), 2);
      node = model.createTypedLiteral(text, uri);
      
    } else if (XSD.gYear.getURI().equals(baseURI)) {
      String text = Integer.toString( GregorianCalendar.getInstance().get(GregorianCalendar.YEAR) );
      node = model.createTypedLiteral(text, uri);
      
    } else if (XSD.gYearMonth.getURI().equals(baseURI)) {
      int year = GregorianCalendar.getInstance().get(GregorianCalendar.YEAR);
      String month = zeroPad(random.nextInt(12) + 1, 2);
      node = model.createTypedLiteral(year + "-" + month, uri);
      
    } else if (
        XSD.ID.getURI().equals(baseURI) ||
        XSD.IDREF.getURI().equals(baseURI)
    ) {
      node = model.createTypedLiteral("x" + random.nextInt(10000), uri);
      
      
    } else if (
      XSD.xint.getURI().equals(baseURI) ||
      XSD.integer.getURI().equals(baseURI) ||
      XSD.nonNegativeInteger.getURI().equals(baseURI) ||
      XSD.positiveInteger.getURI().equals(baseURI) ||
      XSD.xlong.getURI().equals(baseURI) ||
      XSD.unsignedInt.getURI().equals(baseURI) ||
      XSD.unsignedLong.getURI().equals(baseURI)
    ) {
      node = model.createTypedLiteral(random.nextInt(10000), uri);
      
    } else if (XSD.language.getURI().equals(baseURI)) {
      
      String[] languageList = new String[] {
        "ar", "en", "en-us", "fr", "de", "it", "ja", "pl", "ru", "es", "sv", "zh"
      };
      String text = languageList[random.nextInt(languageList.length)];
      node = model.createTypedLiteral(text, uri);
      
      
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
      node = model.createTypedLiteral(name[random.nextInt(name.length)], uri);
      
    } else if (XSD.negativeInteger.getURI().equals(baseURI)) {
      int value = -random.nextInt(10000);
      node = model.createTypedLiteral(value, uri);
      
    } else if (
        XSD.xshort.getURI().equals(baseURI) ||
        XSD.unsignedShort.getURI().equals(baseURI)
    ) {
      
      short value = (short) random.nextInt(100);
      node = model.createTypedLiteral(value, uri);
      
    } else if (XSD.time.getURI().equals(baseURI)){
      DateTime now = DateTime.now();
      DateTimeFormatter formatter = DateTimeFormat.forPattern("HH:mm:ss.SSS");
      String text = formatter.print(now);
      node = model.createTypedLiteral(text, uri);
      
    } else {
      throw new UnsupportedDatatypeException(uri);
    }
    
    return node;
  }


  private String getBaseURI(Datatype datatype) {
    String xsdURI = XSD.getURI();
    while (datatype != null) {
      if (datatype.getUri().startsWith(xsdURI)) {
        return datatype.getUri();
      }
      datatype = datatype.getBase();
    }
    return null;
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
  

  private Frame selectType(Frame frame) {
    // TODO: exclude abstract types from list
    if (frame.getSubtypeList().isEmpty()) return frame;
    
    List<Frame> list = frame.listAllSubtypes();
    list.add(frame);
    
    return list.get(random.nextInt(list.size()));
  }
  

}
