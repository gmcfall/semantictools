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
public class SchemaReference {
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

}
