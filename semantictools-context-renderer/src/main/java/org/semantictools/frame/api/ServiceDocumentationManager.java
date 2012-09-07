package org.semantictools.frame.api;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.semantictools.context.renderer.model.ContextProperties;
import org.semantictools.context.renderer.model.HttpMethod;
import org.semantictools.context.renderer.model.MethodDocumentation;
import org.semantictools.context.renderer.model.ResponseInfo;
import org.semantictools.context.renderer.model.ServiceDocumentation;

import com.ibm.icu.util.StringTokenizer;

public class ServiceDocumentationManager {

  private static final String MEDIATYPE = "mediaType";
  private static final String STATUS = "status";
  private static final String ABSTRACT = "abstract";
  private static final String EDITORS = "editors";
  private static final String AUTHORS = "authors";
  private static final String INTRODUCTION = "introduction";
  private static final String METHODS = "methods";
  private static final String GET_SUMMARY = "GET.summary";
  private static final String GET_REQUEST_HEADERS = "GET.requestHeaders";
  private static final String GET_REQUEST_BODY = "GET.requestBody";
  private static final String GET_RESPONSE = "GET.response";
  
  private static final String GET_REQUEST_BODY_DEFAULT = "The request body must be empty.";
  
  private Map<String, ServiceDocumentation> map = new HashMap<String, ServiceDocumentation>();
  private ContextManager contextManager;
  
  
  public ServiceDocumentationManager(ContextManager contextManager) {
    this.contextManager = contextManager;
  }
  
  /**
   * Scans the given directory (and child directories recursively) for
   * files named "service.properties". The files that are found are then loaded.
   */
  public void scan(File dir) throws ServiceDocumentationSyntaxError, IOException {
    File[] fileList = dir.listFiles();
    for (int i=0; i<fileList.length; i++) {
      File file = fileList[i];
      if (file.getName().equals("service.properties")) {
        load(file);
      }
      if (file.isDirectory()) {
        scan(file);
      }
    }
  }

  public void add(ServiceDocumentation doc) {
    addDefaults(doc);
    map.put(doc.getMediaType(), doc);
    
  }
  
  public void load(File propertiesFile) 
      throws IOException, ServiceDocumentationSyntaxError {
      Properties properties = new Properties();
      FileInputStream input = new FileInputStream(propertiesFile);
      try {
        properties.load(input);
        parseProperties(properties);
      } finally {
        input.close();
      }
    }
  

  private void parseProperties(Properties properties) {
    ServiceDocumentation sink = new ServiceDocumentation();
    
    for (Map.Entry<Object, Object> e : properties.entrySet()) {
      
      String key = e.getKey().toString();
      String value = e.getValue().toString();
      
      if (key.startsWith("[")) {
        sink.putReference(key, value);
        continue;
      }
      
      
      if (MEDIATYPE.equals(key)) {
        sink.setMediaType(value);
      } else if (STATUS.equals(key)) {
        sink.setStatus(value);
      } else if (ABSTRACT.equals(key)) {
        sink.setAbstactText(value);
      } else if (EDITORS.equals(key)) {
        setEditors(sink, value);
      } else if (AUTHORS.equals(key)) {
        setAuthors(sink, value);
      } else if (INTRODUCTION.equals(key)) {
        sink.setIntroduction(value);
      }  else if (METHODS.equals(key)) {
        setMethods(sink, value);
      } else if (GET_SUMMARY.equals(key)) {
        setGetSummary(sink, value);        
      } else if (GET_REQUEST_BODY.equals(key)) {
        setGetRequestBody(sink, value);        
      } else if (GET_REQUEST_HEADERS.equals(key)) {
        setGetRequestHeaders(sink, value);
      } else if (GET_RESPONSE.equals(key)) {
        setGetResponse(sink, value);
      }
    }
    validate(sink);
    
  }


  private void setGetResponse(ServiceDocumentation sink, String value) {

    MethodDocumentation method = sink.getGetDocumentation();
    if (method == null) {
      method = new MethodDocumentation();
      sink.setGetDocumentation(method);
    }
    if ("default".equals(value)) {
      setGetResponseDefault(sink);
    }
    
  }

  private void setGetRequestHeaders(ServiceDocumentation sink, String value) {

    MethodDocumentation method = sink.getGetDocumentation();
    if (method == null) {
      method = new MethodDocumentation();
      sink.setGetDocumentation(method);
    }
    if ("default".equals(value)) {
      setGetRequestHeadersDefault(sink);
    }
    
  }

  private void setGetRequestBody(ServiceDocumentation sink, String value) {
    MethodDocumentation method = sink.getGetDocumentation();
    if (method == null) {
      method = new MethodDocumentation();
      sink.setGetDocumentation(method);
    }
    if ("default".equals(value)) {
      method.setRequestBodyRequirement(GET_REQUEST_BODY_DEFAULT);
      
    } else {
      method.setRequestBodyRequirement(value);
    }
    
  }

  private void setGetSummary(ServiceDocumentation sink, String value) {
    MethodDocumentation method = sink.getGetDocumentation();
    if (method == null) {
      method = new MethodDocumentation();
      sink.setGetDocumentation(method);
    }

    method.setSummary(value);
    
  }

  private void setMethods(ServiceDocumentation sink, String value) {
    String[] array = value.split("\\s+");
    for (int i=0; i<array.length; i++) {
      String term = array[i];
      HttpMethod method = HttpMethod.getByName(term);
      if (method != null) {
        sink.add(method);
      }
    }
    
  }

  private void validate(ServiceDocumentation sink) {
    if (sink.getMediaType()== null) {
      throw new ServiceDocumentationSyntaxError("'mediaType property' is missing");
    }
    String mediaType = sink.getMediaType();
    ContextProperties properties = contextManager.getContextPropertiesByMediaType(mediaType);
    sink.setContextProperties(properties);
    
    add(sink);
    
  }

  private void setEditors(ServiceDocumentation sink, String value) {
    StringTokenizer tokens = new StringTokenizer(value, "\n");
    while (tokens.hasMoreTokens()) {
      String text = tokens.nextToken().trim();
      if (text.length()>0) {
        sink.getEditors().add(text);
      }
      
    }
    
  }

  private void setAuthors(ServiceDocumentation sink, String value) {
    StringTokenizer tokens = new StringTokenizer(value, "\n");
    while (tokens.hasMoreTokens()) {
      String text = tokens.nextToken().trim();
      if (text.length()>0) {
        sink.getAuthors().add(text);
      }
      
    }
    
  }

  private String getLocalName(String uri) {

    int hash = uri.lastIndexOf('#');
    int slash = uri.lastIndexOf('/');
    int delim = Math.max(hash, slash);
    
    String localName = uri.substring(delim+1);
    return localName;
  }
  
  private void addDefaults(ServiceDocumentation doc) {
    ContextProperties context = doc.getContextProperties();
    if (context == null) return;
    
    String typeName = getLocalName(context.getRdfTypeURI());
    String typeRef = context.getRdfTypeRef();
    
    typeRef = context.getRdfTypeRef()==null ? "" : " " + context.getRdfTypeRef();
    
    setMediaTypeRef(doc, typeName);
    setMethods(doc);
    setPostResponseMediaType(doc, context.getMediaType());
    setPostResponseMediaTypeRef(doc, typeName);
    setTitle(doc, typeName);
    setAbstractText(doc, typeName, typeRef);
    setIntroduction(doc, typeName, typeRef);
    setAuthors(doc, context.getAuthors());
    setEditors(doc, context.getEditors());
    setRepresentationHeading(doc, typeName);
    setRepresentationText(doc, typeName);
    setPostDoc(doc, typeName);
    setGetDoc(doc, typeName);
    setPutDoc(doc, typeName);
    setDeleteDoc(doc, typeName);
    
    addMediaTypeCitation(doc, context);
  }
  

  private void setMethods(ServiceDocumentation doc) {
    if (doc.getMethodList().isEmpty()) {
      doc.add(HttpMethod.POST);
      doc.add(HttpMethod.GET);
      doc.add(HttpMethod.PUT);
      doc.add(HttpMethod.DELETE);
    }
    
  }

  private void addMediaTypeCitation(ServiceDocumentation doc,  ContextProperties context) {
    
    String key = doc.getMediaTypeRef();
    String ref = doc.getReference(key);
    
    if (ref == null) {
      doc.putReference(key, createMediaTypeCitation(context));
    }
    
  }

  private String createMediaTypeCitation(ContextProperties context) {
    StringBuilder builder = new StringBuilder();
    addAuthors(builder, context);
    addTitle(builder, context);
    addStatus(builder, context);
    addURL(builder, context);
    
    return builder.toString();
  }

  private void addURL(StringBuilder builder, ContextProperties context) {
    String url = context.getMediaTypeURI();
    if (url != null) {
      builder.append("URL: ");
      builder.append(url);
    }
    
  }

  private void addStatus(StringBuilder builder, ContextProperties context) {
    String status = context.getStatus();
    if (status != null) {
      builder.append(status);
      builder.append("|");
    }
    
  }

  private void addTitle(StringBuilder builder, ContextProperties context) {
    String title = context.getTitle().replaceAll("<BR/?>", " ");
    builder.append(title);
    builder.append("|");
    
    
  }

  private void addAuthors(StringBuilder builder, ContextProperties context) {
    String delim = "";
    for (String text : context.getAuthors()) {
      builder.append(delim);
      int comma = text.indexOf(',');
      if (comma>0) {
        text = text.substring(0, comma).trim();
      }
      if (text.length()>0) {
        builder.append(text);
        delim = ", ";
      }
    }
    builder.append("|");
    
  }

  private void setPostResponseMediaTypeRef(ServiceDocumentation doc, String typeName) {
    if (doc.getPostResponseMediaTypeRef() == null) {
      String value = contextManager.createIdMediaTypeRef(typeName);
      doc.setPostResponseTypeRef(value);
    }
    
    
  }

  private void setPostResponseMediaType(ServiceDocumentation doc, String mediaType) {
    if (doc.getPostResponseMediaType() == null && mediaType.endsWith("+json")) {
      String value = contextManager.createIdMediaTypeName(mediaType);
      doc.setPostResponseMediaType(value);
    }
    
  }

  private void setGetDoc(ServiceDocumentation doc, String typeName) {

    if (doc.getGetDocumentation()==null) {
     
      MethodDocumentation method = new MethodDocumentation();
      doc.setGetDocumentation(method);

      String pattern =
          "To get a representation of a particular {0} instance, the client submits an HTTP GET request to the item''s " +
          "REST endpoint, in accordance with the following rules:";
      method.setSummary(format(pattern, typeName));   
      setGetRequestHeadersDefault(doc);
      method.setRequestBodyRequirement(GET_REQUEST_BODY_DEFAULT);
      
      
      setGetResponseDefault(doc);
      
    }
    
  } 
  
  private void setGetResponseDefault(ServiceDocumentation doc) {
    MethodDocumentation method = doc.getGetDocumentation();
    if (!method.contains(ResponseInfo.OK)) {

      String mediaType = doc.getMediaType();
      String pattern = "The request was successful.  " +
              "<P>The body contains a JSON document in the format defined by the <code>{0}</code> media type.";
      ResponseInfo info = ResponseInfo.OK.copy(format(pattern, mediaType));
      
      method.add(info);
    }

    addResponse(method, ResponseInfo.UNAUTHORIZED);
    addResponse(method, ResponseInfo.MOVED_PERMANENTLY);
    addResponse(method, ResponseInfo.TEMPORARY_REDIRECT);
    addResponse(method, ResponseInfo.NOT_FOUND);
    addResponse(method, ResponseInfo.INTERNAL_SERVER_ERROR);
    
  }

  private void setGetRequestHeadersDefault(ServiceDocumentation doc) {
    MethodDocumentation method = doc.getGetDocumentation();
    method.addRequestHeader("AUTHORIZATION", "<em>Authorization parameters dictated by the OAuth Body Hash Protocol</em>");
  }
  
  private void setPutDoc(ServiceDocumentation doc, String typeName) {

    if (doc.getPutDocumentation()==null) {
     
      MethodDocumentation method = new MethodDocumentation();
      doc.setPutDocumentation(method);

      String pattern =
          "To update a particular {0} instance, the client submits an HTTP PUT request to the item''s " +
          "REST endpoint in accordance with the following rules:";
      
      method.setSummary(format(pattern, typeName));      
      method.addRequestHeader("CONTENT-TYPE", "<code>" + doc.getMediaType() + "</code>");
      method.addRequestHeader("AUTHORIZATION", "<em>Authorization parameters dictated by the OAuth Body Hash Protocol</em>");
      
      method.setRequestBodyRequirement(
          format(
            "The request body must contain a JSON document in the format defined by the <code>{0}</code> " +
            "media type.", doc.getMediaType()));
      
      if (!method.contains(ResponseInfo.OK)) {
        
        ResponseInfo info = ResponseInfo.OK.copy("The request was successful.");
        
        method.add(info);
      }

      addResponse(method, ResponseInfo.UNAUTHORIZED);
      addResponse(method, ResponseInfo.NOT_FOUND);
      addResponse(method, ResponseInfo.INTERNAL_SERVER_ERROR);
      
      
      
    }
    
  }private void setDeleteDoc(ServiceDocumentation doc, String typeName) {

    if (doc.getDeleteDocumentation()==null) {
     
      MethodDocumentation method = new MethodDocumentation();
      doc.setDeleteDocumentation(method);

      String pattern =
          "To delete a particular {0} instance, the client submits an HTTP DELETE request to the item''s " +
          "REST endpoint in accordance with the following rules:";
      
      method.setSummary(format(pattern, typeName));    
      method.addRequestHeader("AUTHORIZATION", "<em>Authorization parameters dictated by the OAuth Body Hash Protocol</em>");
      
      method.setRequestBodyRequirement("The request body must be empty.");
      
      if (!method.contains(ResponseInfo.OK)) {
        
        ResponseInfo info = ResponseInfo.OK.copy("The request was successful and the resource has been deleted.");
        
        method.add(info);
      }

      addResponse(method, ResponseInfo.UNAUTHORIZED);
      addResponse(method, ResponseInfo.NOT_FOUND);
      addResponse(method, ResponseInfo.INTERNAL_SERVER_ERROR);
      
      
      
    }
    
  }
  
  private void addResponse(MethodDocumentation method, ResponseInfo response) {
    if (!method.contains(response)) {
      method.add(response);
    }
  }

  private void setPostDoc(ServiceDocumentation doc, String typeName) {
    if (doc.getPostDocumentation()==null) {
      String pattern =
          "To create a new {0} instance within the server, a client submits an HTTP POST request to the server''s " +
          "{0} collection endpoint in accordance with the following rules: ";
     
      MethodDocumentation method = new MethodDocumentation();
      doc.setPostDocumentation(method);
      
      method.setSummary(format(pattern, typeName));      
      method.addRequestHeader("CONTENT-TYPE", "<code>" + doc.getMediaType() + "</code>");
      method.addRequestHeader("AUTHORIZATION", "<em>Authorization parameters dictated by the OAuth Body Hash Protocol</em>");
      
      pattern =
          "The request body MUST be a JSON document that conforms to the <code>{0}</code> media type. ";
      method.setRequestBodyRequirement(format(pattern, doc.getMediaType()));
      
      String okDescription =
          "The request has succeeded.\n" +
          "<p>The body must contain a small JSON document that provides the endpoint URI for the newly created " +
          "<code>{0}</code> resource.  This JSON document must conform to the <code>{1}</code> format.  " +
          "For details about this format, see {2}.  The <code>Content-Type</code> header of the response must be set to the " +
          "aforementioned media type.";
      String idMediaType = doc.getPostResponseMediaType();
      String mediaTypeRef = doc.getPostResponseMediaTypeRef();
      
      addResponse(method, ResponseInfo.OK.copy(format(okDescription, typeName, idMediaType, mediaTypeRef)));
      addResponse(method, ResponseInfo.BAD_REQUEST);
      addResponse(method, ResponseInfo.UNAUTHORIZED);
      addResponse(method, ResponseInfo.INTERNAL_SERVER_ERROR);
      
      
    }
    
  }
  
  

  private void setMediaTypeRef(ServiceDocumentation doc, String typeName) {
    if (doc.getMediaTypeRef() == null) {
      doc.setMediaTypeRef(format("[{0}-media-type]", typeName));
    }
    
  }

  private void setRepresentationText(ServiceDocumentation doc, String typeName) {
    if (doc.getRepresentationText() == null) {
      String pattern =
          "<code>{0}</code> resources manipulated via this REST API are represented as JSON documents in " +
          "the <code>{1}</code> format.  For detailed information about this media type, see {2}.";
      
      doc.setRepresentationText(format(pattern, typeName, doc.getMediaType(), doc.getMediaTypeRef()));
      
    }
    
  }

  private void setRepresentationHeading(ServiceDocumentation doc,
      String typeName) {
    
    if (doc.getRepresentationHeading() == null) {
      String pattern = "{0} Representations";
      doc.setRepresentationHeading(format(pattern, typeName));
    }
    
  }

  private void setIntroduction(ServiceDocumentation doc, String typeName,
      String typeRef) {
    
    if (doc.getIntroduction() == null) {
      String mediaType = doc.getMediaType();
      
      String value = null;
      if (doc.getMethodList().size()==1) {
        
        String pattern = 
            "<P>This specification defines a REST API for " + actionList(doc) +
            "<code>{0}</code> resources{1} via an HTTP " + doc.getMethodList().get(0).getName() + " request.</P>";
        
        value = format(pattern, typeName, typeRef);
        
        
      } else {
      
        String pattern =
            "<P>This specification defines a REST API for " + actionList(doc) +
            "<code>{0}</code> resources{1}.  Following common conventions, the API " +
            "uses a different HTTP verb for each type of operation: " + methodUsage(doc) +
            "</P>\n" +
            "<P>Implementations of this REST API may be incomplete; a given server " +
            "might support only a subset of the HTTP verbs. A server that supports the complete API will \n" +
            "expose two different kinds of endpoints: a <em>collection</em> endpoint for receiving POST \n" +
            "requests and <em>item</em> endpoints for manipulating individual instances.  This specification \n" +
            "document does not prescribe a method for discovering the endpoint URLs.</P>"
            ;
        
        value = format(pattern, typeName, typeRef, mediaType);
        
      }
           
      doc.setIntroduction(value);
      
    }
    
  }
  
  private String methodUsage(ServiceDocumentation doc) {
    StringBuilder builder = new StringBuilder();
    List<HttpMethod> list = doc.getMethodList();
    for (int i=0; i<list.size(); i++) {
      HttpMethod method = list.get(i);
      if (i>0 && i==list.size()-1) {
        builder.append(" and ");
      } else if (i>0) {
        builder.append(", ");
      }
      if (method == HttpMethod.POST) {
        builder.append("<code>POST</code> for create");
      } else if (method == HttpMethod.GET) {
        builder.append("<code>GET</code> for read");      
      } else if (method == HttpMethod.PUT) {
        builder.append("<code>PUT</code> for update");  
      } else if (method == HttpMethod.DELETE) {
        builder.append("<code>DELETE</code> for delete");            
      }
    }
    builder.append(' ');
    return builder.toString();
  }

  private void setTitle(ServiceDocumentation doc, String typeName) {
    if (doc.getTitle() == null) {
      doc.setTitle(format("A REST API for {0} Resources<br/>" +
      		"in the <code>{1}</code> Format", typeName, doc.getMediaType()));
    }
    
//    if (doc.getSubtitle() == null) {
//      doc.setSubtitle(format("in a format defined by the <code>{0}</code> Media Type", doc.getMediaType()));
//    }
    
  }

  private void setEditors(ServiceDocumentation doc, List<String> editors) {
    if (doc.getEditors().isEmpty() && !editors.isEmpty()) {
      doc.getEditors().addAll(editors);
    }
    
  }

  private void setAuthors(ServiceDocumentation doc, List<String> authors) {
    if (!authors.isEmpty() && doc.getAuthors().isEmpty()) {
      doc.getAuthors().addAll(authors);
    }
    
  }

  private void setAbstractText(ServiceDocumentation doc, String typeName,  String typeRef) {
    
    if (doc.getAbstactText()==null) {
      
      String pattern =
          "This specification defines a REST API for " + actionList(doc) +
          "<code>{0}</code> resources{1}.";
      
      doc.setAbstactText(format(pattern, typeName, typeRef));
     
      
    }
    
  }
  
  private String actionList(ServiceDocumentation doc) {
    StringBuilder builder = new StringBuilder();
    
    List<HttpMethod> list = doc.getMethodList();
    for (int i=0; i<list.size(); i++) {
      HttpMethod method = list.get(i);
      
      if (i>0 && i==list.size()-1) {
        builder.append(" and ");
      } else if (i>0) {
        builder.append(", ");
      }
      builder.append(method.getGerund());
      
    }
    builder.append(' ');
    
    return builder.toString();
  }

  private String format(String pattern, Object... arg) {
    return MessageFormat.format(pattern, arg);
  }

  public ServiceDocumentation getServiceDocumentationByMediaType(String mediaType) {
    return map.get(mediaType);
  }

}
