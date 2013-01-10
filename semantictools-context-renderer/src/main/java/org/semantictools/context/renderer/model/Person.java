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
package org.semantictools.context.renderer.model;

public class Person {
  private String personName;
  private String orgName;
  public String getPersonName() {
    return personName;
  }
  public void setPersonName(String personName) {
    this.personName = personName;
  }
  public String getOrgName() {
    return orgName;
  }
  public void setOrgName(String orgName) {
    this.orgName = orgName;
  }
  
  public int hashCode() {
    return personName.hashCode();
  }
  
  public boolean equals(Object object) {
    if (object instanceof Person) {
      Person person = (Person) object;
      return person.getPersonName().equals(personName);
    }
    return false;
  }
  
  

}
