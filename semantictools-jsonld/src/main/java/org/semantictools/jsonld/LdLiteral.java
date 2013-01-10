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

import java.util.Calendar;

import org.joda.time.DateTime;
import org.semantictools.util.DurationUtil;


/**
 * LdLiteral represents a JSON-LD node that contains a literal value, i.e. a primitive data value.
 * @author Greg McFall
 *
 */
public class LdLiteral implements LdNode {
  
  private String stringValue;
  private Long longValue;
  private Double doubleValue;
  private Boolean booleanValue;
  private String type;
  private String language;
  
  
  /**
   * Returns the String representation of this literal.
   */
  public String getStringValue() {
    return 
        stringValue != null ? stringValue :
        longValue != null ? longValue.toString() :
        doubleValue != null ? doubleValue.toString() :
        booleanValue != null ? booleanValue.toString() :
        null;
  }
  
  /**
   * Sets the String representation of this literal.
   * Setting a Long or Double value automatically makes a String value
   * accessible via getStringValue.  Thus, if you call setLongValue or setDoubleValue,
   * it is not necessary to also call setStringValue.
   */
  public void setStringValue(String stringValue) {
    this.stringValue = stringValue;
  }
  
  /**
   * Returns the value as a duration in milliseconds.
   * The underlying value must be a string in ISO 8601 format for a duration.
   */
  public long getDurationValue() {
    return DurationUtil.parseDuration(stringValue);
  }
  
  /**
   * Returns a Calendar representation of this literal.
   * The underlying value must be a string in ISO 8601 format.
   */
  public Calendar getCalendarValue() {

    Calendar result = null;
    String text = getStringValue();
    try {
      DateTime datetime = new DateTime(text);
      result = datetime.toGregorianCalendar();
    } catch (Exception oops) {
      throw new RuntimeException(oops);
    }
    return result;
  }
  
  /**
   * Returns a Long representation of this literal.
   */
  public Long getLongValue() {
    return 
      (longValue != null) ? longValue :
      (stringValue != null) ? Long.parseLong(stringValue) : 
      (doubleValue != null) ? doubleValue.longValue() :   
      null;
  }
  
  /**
   * Sets the Long representation of this literal.
   */
  public void setLongValue(Long longValue) {
    this.longValue = longValue;
  }
  
  /**
   * Returns a Double representation of this literal.
   * 
   */
  public Double getDoubleValue() {
    return 
      (doubleValue!=null) ? doubleValue :
      (stringValue!=null) ? Double.parseDouble(stringValue) : 
      (longValue != null) ? longValue.doubleValue() :
      null;
  }
  
  /**
   * Sets the Double representation of this literal.
   */
  public void setDoubleValue(Double doubleValue) {
    this.doubleValue = doubleValue;
  }
  
  
  public Boolean getBooleanValue() {
    return booleanValue;
  }

  public void setBooleanValue(Boolean booleanValue) {
    this.booleanValue = booleanValue;
  }

  /**
   * Returns the URI of the datatype of this literal value.
   * May be null if the type is not declared in the JSON-LD context.
   */
  public String getType() {
    return type;
  }
  
  /**
   * Sets the URI of the datatype of this literal value.
   */
  public void setType(String type) {
    this.type = type;
  }

  /**
   * Returns the language of the value, or null if the language is not specified.
   */
  public String getLanguage() {
    return language;
  }

  /**
   * Sets the language of the value, or null if the language is not specified.
   */
  public void setLanguage(String language) {
    this.language = language;
  }

  @Override
  public boolean isObject() {
    return false;
  }

  @Override
  public boolean isContainer() {
    return false;
  }

  @Override
  public boolean isLiteral() {
    return true;
  }

  @Override
  public LdLiteral asLiteral() throws ClassCastException {
    return this;
  }

  @Override
  public LdContainer asContainer() throws ClassCastException {
    throw new ClassCastException("Cannot cast as LdContainer: type is LdLiteral");
  }

  @Override
  public LdObject asObject() throws ClassCastException {
    throw new ClassCastException("Cannot cast as LdObject: type is LdLiteral");
  }

  @Override
  public boolean isIRI() {
    return false;
  }

  @Override
  public boolean isBlankNode() {
    return false;
  }

  @Override
  public LdIRI asIRI() throws ClassCastException {
    throw new ClassCastException("Cannot cast as LdIRI: type is LdLiteral");
  }

  @Override
  public LdBlankNode asBlankNode() throws ClassCastException {
    throw new ClassCastException("Cannot cast as LdBlankNode: type is LdLiteral");
  }
  
  public boolean equals(Object obj) {
    if (obj instanceof LdNode) {
      LdNode node = (LdNode) obj;
      if (node.isLiteral()) {
        LdLiteral other = node.asLiteral();
        return 
            (getStringValue().equals(other.getStringValue())) &&
            (type==null || other.getType()==null || type.equals(other.getType()));
      }
    }
    
    return false;
  }
  

}
