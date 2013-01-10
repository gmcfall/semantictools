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
package org.semantictools.jsonld;

import java.util.HashMap;
import java.util.Map;


public class LdDatatypeManager {

  public static final String XPATH_DATATYPES_URI = "http://www.w3.org/2004/10/xpath-datatypes#";
  private static Map<String, LdDatatype> map = new HashMap<String, LdDatatype>();
  
  public static final LdDatatype LD_ANYURI = createDatatype(XsdType.ANYURI);
  public static final LdDatatype LD_BASE64BINARY = createDatatype(XsdType.BASE64BINARY);
  public static final LdDatatype LD_BOOLEAN = createDatatype(XsdType.BOOLEAN);
  public static final LdDatatype LD_BYTE = createDatatype(XsdType.BYTE);
  public static final LdDatatype LD_DATE = createDatatype(XsdType.DATE);
  public static final LdDatatype LD_DATETIME = createDatatype(XsdType.DATETIME);
  public static final LdDatatype LD_DECIMAL = createDatatype(XsdType.DECIMAL);
  public static final LdDatatype LD_DOUBLE = createDatatype(XsdType.DOUBLE);
  public static final LdDatatype LD_DURATION = createDatatype(XsdType.DURATION);
  public static final LdDatatype LD_FLOAT = createDatatype(XsdType.FLOAT);
  public static final LdDatatype LD_GDAY = createDatatype(XsdType.GDAY);
  public static final LdDatatype LD_GMONTH = createDatatype(XsdType.GMONTH);
  public static final LdDatatype LD_GMONTHDAY = createDatatype(XsdType.GMONTHDAY);
  public static final LdDatatype LD_GYEAR = createDatatype(XsdType.GYEAR);
  public static final LdDatatype LD_GYEARMONTH = createDatatype(XsdType.GYEARMONTH);
  public static final LdDatatype LD_HEXBINARY = createDatatype(XsdType.HEXBINARY);
  public static final LdDatatype LD_INT = createDatatype(XsdType.INT);
  public static final LdDatatype LD_INTEGER = createDatatype(XsdType.INTEGER);
  public static final LdDatatype LD_LANGUAGE = createDatatype(XsdType.LANGUAGE);
  public static final LdDatatype LD_LONG = createDatatype(XsdType.LONG);
  public static final LdDatatype LD_NAME = createDatatype(XsdType.NAME);
  public static final LdDatatype LD_NCNAME = createDatatype(XsdType.NCNAME);
  public static final LdDatatype LD_NEGATIVEINTEGER = createDatatype(XsdType.NEGATIVEINTEGER);
  public static final LdDatatype LD_NMTOKEN = createDatatype(XsdType.NMTOKEN);
  public static final LdDatatype LD_NONNEGATIVEINTEGER = createDatatype(XsdType.NONNEGATIVEINTEGER);
  public static final LdDatatype LD_NONPOSITIVEINTEGER = createDatatype(XsdType.NONPOSITIVEINTEGER);
  public static final LdDatatype LD_NORMALIZEDSTRING = createDatatype(XsdType.NORMALIZEDSTRING);
  public static final LdDatatype LD_POSTIVEINTEGER = createDatatype(XsdType.POSTIVEINTEGER);
  public static final LdDatatype LD_SHORT = createDatatype(XsdType.SHORT);
  public static final LdDatatype LD_STRING = createDatatype(XsdType.STRING);
  public static final LdDatatype LD_TIME = createDatatype(XsdType.TIME);
  public static final LdDatatype LD_TOKEN = createDatatype(XsdType.TOKEN);
  public static final LdDatatype LD_UNSIGNEDBYTE = createDatatype(XsdType.UNSIGNEDBYTE);
  public static final LdDatatype LD_UNSIGNEDINT = createDatatype(XsdType.UNSIGNEDINT);
  public static final LdDatatype LD_UNSIGNEDLONG = createDatatype(XsdType.UNSIGNEDLONG);
  public static final LdDatatype LD_UNSIGNEDSHORT = createDatatype(XsdType.UNSIGNEDSHORT);
  
  public static final LdDatatype LD_DAYTIMEDURATION = new LdDatatype();
  
  static {
    LD_DAYTIMEDURATION.setBase(LD_DURATION);

    LD_DAYTIMEDURATION.setNamespace("http://www.w3.org/2004/10/xpath-datatypes#");
    LD_DAYTIMEDURATION.setUri("http://www.w3.org/2004/10/xpath-datatypes#dayTimeDuration");
    LD_DAYTIMEDURATION.setLocalName("dayTimeDuration");
    LD_DAYTIMEDURATION.setXsdType(XsdType.DURATION);
    map.put(LD_DAYTIMEDURATION.getUri(), LD_DAYTIMEDURATION);
    
  }
  

  private static LdDatatype createDatatype(XsdType xsdType) {
    LdDatatype datatype = new LdDatatype();
    datatype.setNamespace(XsdType.URI);
    datatype.setLocalName(xsdType.getLocalName());
    datatype.setUri(XsdType.URI + xsdType.getLocalName());
    datatype.setXsdType(xsdType);
    
    map.put(datatype.getUri(), datatype);
    
    return datatype;
  }
  
  public static LdDatatype getXsdTypeByURI(String uri) {
    return map.get(uri);
  }

}
