package org.semantictools.context.renderer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;

import org.semantictools.context.renderer.model.GlobalProperties;
import org.semantictools.context.renderer.model.Person;
import org.semantictools.frame.api.OntologyManager;

public class GlobalPropertiesReader {
  private static final String IGNORE = "ignore";
  private static final String UPLOAD_SCHEMA_SERVICE_URI = "uploadSchemaServiceURI";
  private static final String UPLOAD_SCHEMA_LIST = "uploadSchemaList";
  private static final String LOCAL_REPO = "localRepo";
  private static final String LOGO = "logo";
  private static final String SUBTITLE = "subtitle";
  private static final String TEMPLATE = "template";
  private static final String LATEST_VERSION = "latestVersion";
  private static final String LEGAL_NOTICE = "legalNotice";
  private static final String STATUS = "status";
  private static final String DATE = "date";
  private static final String COCHAIRS = "co-chairs";
  private static final String EDITORS = "editors";
  private static final String AUTHORS = "authors";
  private static final String VERSION = "version";
  private static final String RELEASE = "release";
  private static final String PURPOSE = "purpose";
  private static final String FOOTER = "footer";
  private static final String DOCUMENT_LOCATION = "documentLocation";
  
  private OntologyManager ontologyManager;
  
  
  
  public GlobalPropertiesReader(OntologyManager ontologyManager) {
    this.ontologyManager = ontologyManager;
  }

  public GlobalProperties scan(File source) throws IOException {
    if ("global.properties".equals(source.getName())) {
      return parseProperties(source);
    }
    if (source.isDirectory()) {
      for (File child : source.listFiles()) {
        GlobalProperties result = scan(child);
        if (result != null) return result;
      }
    }
    return null;
  }

  private GlobalProperties parseProperties(File source) throws IOException {
    GlobalProperties global = new GlobalProperties();
    Properties properties = new Properties();
    FileReader reader = new FileReader(source);
    properties.load(reader);

    for (Map.Entry<Object, Object> e : properties.entrySet()) {
      
      String key = e.getKey().toString();
      String value = e.getValue().toString();
      if (IGNORE.equals(key)) {
        setIgnoredOntologies(global, value);
      } else if (UPLOAD_SCHEMA_SERVICE_URI.equals(key)) {
        ontologyManager.setOntologyServiceURI(value);
      } else if (LOCAL_REPO.equals(key)) {
        ontologyManager.setLocalRepository(new File(value));
      } else if (UPLOAD_SCHEMA_LIST.equals(key)) {
        setUploadSchemaList(value);
      } else if (LOGO.equals(key)) {
        global.setLogo(value);
      } else if (SUBTITLE.equals(key)) {
        global.setSubtitle(value);
      } else if (TEMPLATE.equals(key)) {
        global.setTemplateName(value);
      } else if (STATUS.equals(key)) {
        global.setStatus(value);
      } else if (FOOTER.equals(key)) {
        global.setFooter(value);
      } else if (DATE.equals(key)) {
        global.setDate(value);
      } else if (LATEST_VERSION.equals(key)) {
        global.setLatestVersionURI(value);
      } else if (COCHAIRS.equals(key)) {
        setCoChairs(global, value);
      } else if (AUTHORS.equals(key)) {
        setAuthors(global, value);
      } else if (EDITORS.equals(key)) {
        setEditors(global, value);
      } else if (LEGAL_NOTICE.equals(key)) {
        global.setLegalNotice(value);
      } else if (PURPOSE.equals(key)) {
        global.setPurpose(value);
      } else if (VERSION.equals(key)) {
        global.setVersion(value);
      } else if (DOCUMENT_LOCATION.equals(key)) {
        global.setDocumentLocation(value);
      } else if (RELEASE.equals(key)) {
        global.setRelease(value);
      }
    }
    return global;
  }

  private void setAuthors(GlobalProperties global, String value) {
    StringTokenizer tokens = new StringTokenizer(value, "\n");
    while (tokens.hasMoreTokens()) {
      String line = tokens.nextToken().trim();
      if (line.length()==0) continue;

      Person person = parsePerson(line);
      global.addAuthor(person);
    }
    
  }

  private void setEditors(GlobalProperties global, String value) {
    StringTokenizer tokens = new StringTokenizer(value, "\n");
    while (tokens.hasMoreTokens()) {
      String line = tokens.nextToken().trim();
      if (line.length()==0) continue;

      Person person = parsePerson(line);
      global.addEditor(person);
    }
    
  }
  private void setCoChairs(GlobalProperties global, String value) {
    StringTokenizer tokens = new StringTokenizer(value, "\n");
    while (tokens.hasMoreTokens()) {
      String line = tokens.nextToken().trim();
      if (line.length()==0) continue;

      Person person = parsePerson(line);
      global.addCoChair(person);
    }
    
  }

  private Person parsePerson(String line) {
    String personName = line;
    String orgName = null;
    int comma = line.indexOf(',');
    if (comma > 0) {
      personName = line.substring(0, comma).trim();
      orgName = line.substring(comma+1).trim();
    }
    Person person = new Person();
    person.setPersonName(personName);
    person.setOrgName(orgName);
    return person;
  }

  private void setUploadSchemaList(String value) {

    StringTokenizer tokenizer = new StringTokenizer(value, " \t\r\n");
    while (tokenizer.hasMoreTokens()) {
      String uri = tokenizer.nextToken();
      if (uri.length()>0) {
        ontologyManager.getUploadList().add(uri);
      }
    }
  }

  private void setIgnoredOntologies(GlobalProperties global, String value) {
    
    StringTokenizer tokenizer = new StringTokenizer(value, " \t\r\n");
    while (tokenizer.hasMoreTokens()) {
      global.addIgnoredOntology(tokenizer.nextToken());
    }
    
  }

}
