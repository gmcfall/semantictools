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
package org.semantictools.index.model;

import java.io.File;

/**
 * SchemaReference encapsulates information about a schema (either an RDF Schema
 * or XML Schema) suitable for generating an index entry with links to the
 * documentation for the schema.
 * 
 * @author Greg McFall
 * 
 */
public class SchemaReference implements Comparable<SchemaReference>{
  private String schemaLabel;
  private File schemaDoc;

  public SchemaReference(String schemaLabel, File schemaDoc) {
    this.schemaLabel = schemaLabel;
    this.schemaDoc = schemaDoc;
  }

  public String getSchemaLabel() {
    return schemaLabel;
  }

  public File getSchemaDoc() {
    return schemaDoc;
  }

  public String getSchemaDocPath() {
    return schemaDoc.toString().replace("\\", "/");
  }

  @Override
  public int compareTo(SchemaReference other) {
    
    return schemaLabel.compareTo(other.schemaLabel);
  }

}
