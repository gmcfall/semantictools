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

public enum XsdType {

  ANYURI("anyURI"),
  BASE64BINARY("base64Binary"),
  BOOLEAN("boolean"),
  BYTE("byte"),
  DATE("date"),
  DATETIME("dateTime"),
  DECIMAL("decimal"),
  DOUBLE("double"),
  DURATION("duration"),
  FLOAT("float"),
  GDAY("gDay"),
  GMONTH("gMonth"),
  GMONTHDAY("gMonthDay"),
  GYEAR("gYear"),
  GYEARMONTH("gYearMonth"),
  HEXBINARY("hexBinary"),
  INT("int"),
  INTEGER("integer"),
  LANGUAGE("language"),
  LONG("long"),
  NAME("Name"),
  NCNAME("NCName"),
  NEGATIVEINTEGER("negativeInteger"),
  NMTOKEN("NMTOKEN"),
  NONNEGATIVEINTEGER("nonNegativeInteger"),
  NONPOSITIVEINTEGER("nonPositiveInteger"),
  NORMALIZEDSTRING("normalizedString"),
  POSTIVEINTEGER("positiveInteger"),
  SHORT("short"),
  STRING("string"),
  TIME("time"),
  TOKEN("token"),
  UNSIGNEDBYTE("unsignedByte"),
  UNSIGNEDINT("unsignedInt"),
  UNSIGNEDLONG("unsignedLong"),
  UNSIGNEDSHORT("unsignedShort"),
  
  UNDEFINED("undefined");
  
  public static final String URI = "http://www.w3.org/2001/XMLSchema#";
  
  private String localName;
  private XsdType(String localName) {
    this.localName = localName;
  }
  public String getLocalName() {
    return localName;
  }
}
