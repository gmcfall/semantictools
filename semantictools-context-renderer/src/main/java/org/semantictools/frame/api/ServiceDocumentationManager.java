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
package org.semantictools.frame.api;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;

import org.semantictools.context.renderer.model.ContextProperties;
import org.semantictools.context.renderer.model.DocumentMetadata;
import org.semantictools.context.renderer.model.HttpMethod;
import org.semantictools.context.renderer.model.MethodDocumentation;
import org.semantictools.context.renderer.model.Person;
import org.semantictools.context.renderer.model.QueryParam;
import org.semantictools.context.renderer.model.ResponseInfo;
import org.semantictools.context.renderer.model.ServiceDocumentation;
import org.semantictools.context.renderer.model.ServiceFileManager;
import org.semantictools.context.view.ServiceDocumentationPrinter;
import org.semantictools.context.view.StringUtil;
import org.semantictools.frame.model.Frame;
import org.semantictools.frame.model.Uri;
import org.semantictools.index.model.ServiceDocumentationList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ServiceDocumentationManager {

  private static final Logger logger = LoggerFactory.getLogger(ServiceDocumentationManager.class);
  
  private static final String ABSTRACT = "abstract";
  private static final String AUTHORS = "authors";
  private static final String CONTAINER_RDFTYPE = "container.rdfType";
  private static final String CONTAINER_GET_MEDIATYPE = "container.GET.mediaType";
  private static final String CONTAINER_GET_PARAM = "container.GET?";
  private static final String CONTENT_NEGOTIATION = "contentNegotiation";
  private static final String DATE = "date";
  private static final String DEFAULT_MEDIA_TYPE = "GET.default.mediaType";
  private static final String EDITORS = "editors";
  private static final String ENABLE_VERSION_HISTORY = "enableVersionHistory";
  private static final String GET_INSTRUCTIONS = "GET.instructions";
  private static final String GET_REQUEST_BODY = "GET.requestBody";
  private static final String GET_REQUEST_BODY_DEFAULT = "The request body must be empty.";
  private static final String GET_REQUEST_HEADERS = "GET.requestHeaders";
  private static final String GET_STATUS = "GET.status.";
  private static final String GET_SUMMARY = "GET.summary";
  private static final String HTML_FORMAT_DOCUMENTATION = "htmlFormatDocumentation";
  private static final String INTRODUCTION = "introduction";
  private static final String MEDIATYPE = "mediaType";
  private static final String MEDIA_TYPE_URI_PREFIX = "mediaType.uri.";
  private static final String METHODS = "methods";
  private static final String POST_CREATED_DESCRIPTION = "POST.created.description";
  private static final String POST_PROCESSING_RULES = "POST.processing.rules";
  private static final String POST_REQUEST_MEDIATYPE = "POST.request.mediaType";
  private static final String POST_RESPONSE_MEDIATYPE = "POST.response.mediaType";
  private static final String PUT_INSTRUCTIONS = "PUT.instructions";
  private static final String PUT_RULES = "PUT.rules";
  private static final String QUERY_PARAM = "GET?";
  private static final String RDFTYPE = "rdfType";
  private static final String REPRESENTATIONS_HEADING = "representations.heading";
  private static final String REPRESENTATIONS_TEXT = "representations.text";
  private static final String STATUS = "status";
  private static final String SUBTITLE = "subtitle";
  private static final String TITLE = "title";
  private static final String URL_TEMPLATES = "urlTemplates";
  
  private Map<String, ServiceDocumentationList> map = new HashMap<String, ServiceDocumentationList>();
  private ContextManager contextManager;
  private TypeManager typeManager;
  private ServiceFileManager serviceFileManager;
  private ServiceDocumentationPrinter printer;
  private DocumentMetadata global;
  
  
  public ServiceDocumentationManager(
      TypeManager typeManager,
      DocumentMetadata global,
      ContextManager contextManager, 
      ServiceFileManager fileManager, 
      ServiceDocumentationPrinter printer
  ) {
    this.typeManager = typeManager;
    this.global = global;
    this.contextManager = contextManager;
    serviceFileManager = fileManager;
    this.printer = printer;
  }
  
  public List<ServiceDocumentationList> getServiceDocumentationLists() {
    return new ArrayList<ServiceDocumentationList>(map.values());
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
    ServiceDocumentation sink = new ServiceDocumentation(global);
    ServiceMethodInfo methodInfo = new ServiceMethodInfo();
    
    sink.setDefaultMediaType(properties.getProperty(DEFAULT_MEDIA_TYPE));
    
    for (Map.Entry<Object, Object> e : properties.entrySet()) {
      
      String key = e.getKey().toString();
      String value = e.getValue().toString();
      
      if (key.startsWith("[")) {
        sink.putReference(key, value);
        continue;
      }
      
      
      if (MEDIATYPE.equals(key)) {
        setMediaType(sink, value, properties);
      } else if (TITLE.equals(key)) {
        sink.setTitle(value);
      } else if (SUBTITLE.equals(key)) {
        sink.setSubtitle(value);
      } else if (RDFTYPE.equals(key)) {
        setRdfType(sink, value);
      } else if (CONTAINER_RDFTYPE.equals(key)) {
        sink.setContainerType(new Uri(value));
      } else if (CONTAINER_GET_MEDIATYPE.equals(key)) {
        setContainerGetMediaType(sink, value);
      } else if (key.startsWith(CONTAINER_GET_PARAM)) {
        addContainerGetParam(sink, key, value);
      } else if (CONTENT_NEGOTIATION.equals(key)) {
        sink.setContentNegotiation("true".equals(value));
      } else if (STATUS.equals(key)) {
        sink.setStatus(value);
      } else if (DATE.equals(key)) {
        sink.setDate(value);
      } else if (ABSTRACT.equals(key)) {
        sink.setAbstactText(value);
      } else if (EDITORS.equals(key)) {
        setEditors(sink, value);
      } else if (ENABLE_VERSION_HISTORY.equals(key)) {
        sink.setHistoryLink("true".equalsIgnoreCase(value));
      } else if (AUTHORS.equals(key)) {
        setAuthors(sink, value);
      } else if (INTRODUCTION.equals(key)) {
        sink.setIntroduction(value);
      }  else if (METHODS.equals(key)) {
        setMethods(sink, value);
      } else if (GET_SUMMARY.equals(key)) {
        setGetSummary(sink, value);     
      } else if (GET_INSTRUCTIONS.equals(key)) {
        sink.setGetInstructions(value);
      } else if (GET_REQUEST_BODY.equals(key)) {
        setGetRequestBody(sink, value);        
      } else if (GET_REQUEST_HEADERS.equals(key)) {
        setGetRequestHeaders(sink, value);
      } else if (key.startsWith(QUERY_PARAM)) {
        addQueryParam(sink, key, value);
      } else if (POST_REQUEST_MEDIATYPE.equals(key)) {
        setPostRequestMediaType(sink, value);
      } else if (POST_RESPONSE_MEDIATYPE.equals(key)) {
        setPostResponseMediaType(sink, value);
      } else if (URL_TEMPLATES.equals(key)) {
        sink.setUrlTemplateText(value);
      } else if (HTML_FORMAT_DOCUMENTATION.equals(key)) {
        sink.setHtmlFormatDocumentation(value);
      } else if (POST_PROCESSING_RULES.equals(key)) {
        sink.setPostProcessingRules(value);
      } else if (REPRESENTATIONS_HEADING.equals(key)) {
        sink.setRepresentationHeading(value);
      } else if (REPRESENTATIONS_TEXT.equals(key)) {
        sink.setRepresentationText(value);
      } else if (PUT_INSTRUCTIONS.equals(key)) {
        sink.setPutInstructions(value);
      } else if (PUT_RULES.equals(key)) {
        setPutRules(sink, value);
      } else if (POST_CREATED_DESCRIPTION.equals(key)) {
        methodInfo.setPostCreatedDescription(value);
      }
    }

    validate(sink);
    addDefaults(properties, sink, methodInfo);
    
    put(sink);
    
  }



  private void setContainerGetMediaType(ServiceDocumentation sink, String value) {
   
    MethodDocumentation method = sink.getContainerGetDocumentation();
    if (method == null) {
      sink.setContainerGetDocumentation(method = new MethodDocumentation());
    }
    List<String> list = method.getResponseMediaTypes();
    
    StringTokenizer tokens = new StringTokenizer(value);
    while (tokens.hasMoreTokens()) {
      list.add(tokens.nextToken());
    }
    
  }

  private void setPostRequestMediaType(ServiceDocumentation sink, String value) {

    MethodDocumentation method = sink.getPostDocumentation();
    if (method == null) {
      method = new MethodDocumentation();
      sink.setPostDocumentation(method);
    }
    List<String> list = method.getRequestContentTypes();
    StringTokenizer tokens = new StringTokenizer(value);
    while (tokens.hasMoreTokens()) {
      list.add(tokens.nextToken());
    }
  }

  private void setGetStatus(Properties properties, ServiceDocumentation sink) {
    
    MethodDocumentation method = sink.getGetDocumentation();
    for (ResponseInfo info : ResponseInfo.all) {
      StringBuilder builder = new StringBuilder();
      builder.append(GET_STATUS);
      builder.append(info.getStatusCode());
      String key = builder.toString();
      String description = properties.getProperty(key);
      if (description != null) {
        info = info.copy(description);
        method.add(info);
      }
    }
    
  }

  private void setPutRules(ServiceDocumentation sink, String value) {
    StringTokenizer tokens = new StringTokenizer(value, "\r\n");
    while (tokens.hasMoreTokens()) {
      String rule = tokens.nextToken().trim();
      sink.getPutRules().add(rule);
    }
    
  }


  private void addContainerGetParam(ServiceDocumentation sink, String key,
      String value) {
    
    MethodDocumentation method = sink.getContainerGetDocumentation();
    if (method == null) {
      method = new MethodDocumentation();
      sink.setContainerGetDocumentation(method);
    }
    
    int qmark = key.indexOf('?');
    String paramName = key.substring(qmark+1);
    QueryParam param = new QueryParam();
    param.setName(paramName);
    param.setDescription(value);
    
    method.add(param);
    
  }

  private void addQueryParam(ServiceDocumentation doc, String key, String value) {
    
    MethodDocumentation method = doc.getGetDocumentation();
    if (method == null) {
      method = new MethodDocumentation();
      doc.setGetDocumentation(method);
    }
    
    int qmark = key.indexOf('?');
    String paramName = key.substring(qmark+1);
    QueryParam param = new QueryParam();
    param.setName(paramName);
    param.setDescription(value);
    
    method.add(param);
    
    
  }

  private void setPostResponseMediaType(ServiceDocumentation sink, String value) {
    sink.setPostResponseMediaType(value);
    
  }

  private void setRdfType(ServiceDocumentation sink, String value) {
    sink.setRdfType(new Uri(value));
  }

  private void put(ServiceDocumentation sink) {
    ServiceDocumentationList list = map.get(sink.getRdfType().stringValue());
    if (list == null) {
      list = new ServiceDocumentationList(sink.getRdfType().stringValue());
      map.put(sink.getRdfType().stringValue(), list);
    }
    list.add(sink);
    
  }

  private void setMediaType(ServiceDocumentation sink, String value, Properties properties) {
    StringTokenizer tokenizer = new StringTokenizer(value, ",");
    while (tokenizer.hasMoreTokens()) {
      String mediaType = tokenizer.nextToken().trim();
      if (mediaType.length()==0) continue;
      
      if ("*/*".equals(mediaType)) {
        sink.setAllowArbitraryFormat(true);
      } else if ("text/html".equals(mediaType)) {
        sink.setAllowHtmlFormat(true);
      } else {
        ContextProperties context = contextManager.getContextPropertiesByMediaType(mediaType);
        if (context == null) {
          
          String key = MEDIA_TYPE_URI_PREFIX + mediaType;
          String uri = properties.getProperty(key);
          
          if (uri != null) {
            sink.getMediaTypeUriMap().put(mediaType, uri);
            continue;
          }
          
          throw new ServiceDocumentationSyntaxError("MediaType not found: " + mediaType);
        }
        sink.add(context);
      }
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
    if (sink.getRdfType() == null) {
      throw new ServiceDocumentationSyntaxError("rdfType is not not defined");
    }
    if (sink.listContextProperties().isEmpty()) {
      throw new ServiceDocumentationSyntaxError("'mediaType property' is missing");
    }
    
    Frame frame = typeManager.getFrameByUri(sink.getRdfType().stringValue());
    
    if (frame == null) {
      throw new RuntimeException("Frame not found: " + sink.getRdfType().stringValue());
    }
    
    sink.setFrame(frame);
    
  }

 
  private void setEditors(ServiceDocumentation sink, String value) {
    StringTokenizer tokens = new StringTokenizer(value, "\n");
    while (tokens.hasMoreTokens()) {
      String text = tokens.nextToken().trim();
      if (text.length()>0) {
        sink.getEditors().add(parsePerson(text));
      }
      
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

  
  private void setAuthors(ServiceDocumentation sink, String value) {
    StringTokenizer tokens = new StringTokenizer(value, "\n");
    while (tokens.hasMoreTokens()) {
      String text = tokens.nextToken().trim();
      if (text.length()>0) {
        sink.getEditors().add(parsePerson(text));
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
  
  private void addDefaults(Properties properties, ServiceDocumentation doc, ServiceMethodInfo methodInfo) {
    List<ContextProperties> contextList = doc.listContextProperties();
    if (contextList.isEmpty()) return;
    
    String typeName = doc.getRdfType().getLocalName();

    setServiceDocumentationFile(doc);
    setCssFile(doc);
    setMethods(doc);
    setPostResponseMediaTypeRef(doc, typeName);
    setTitle(doc, typeName);
    setAbstractText(doc, typeName);
    setIntroduction(doc, typeName);
    setRepresentationHeading(doc, typeName);
    setPostDoc(doc, methodInfo, typeName);
    setGetDoc(properties, doc, typeName);
    setContainerGetDoc(properties, doc, typeName);
    setPutDoc(doc, typeName);
    setDeleteDoc(doc, typeName);
    setRepresentationText(doc, typeName);
  }
  

  private void setContainerGetDoc(Properties properties,  ServiceDocumentation doc, String typeName) {
    
    
    Frame frame = doc.getFrame();
    
    List<Frame> containerList = frame.getContainerList();
    if (containerList == null || containerList.isEmpty()) {
      return;
    }
    
    MethodDocumentation method = doc.getContainerGetDocumentation();
    if (method == null) {
      method = new MethodDocumentation();
      doc.setContainerGetDocumentation(method);
    }
    Uri containerType = null;
    String containerURI = null;
    if (doc.getContainerType() != null) {
      containerType = doc.getContainerType();
    } else  {
      containerType = new Uri(containerList.get(0).getUri());
      doc.setContainerType(containerType);
    }
   
    // TODO: deal with the cases where there is more than one acceptable containerFormat or
    //       more than one container type.
    
    
    String summary = method.getSummary();
    if (summary == null) {
      
      summary = MessageFormat.format(
        "To get a paginated list of {0} resources from a {1}, the client submits an HTTP " +
        "GET request to the container endpoint in accordance with the following rules:", 
        typeName, containerType.getLocalName());
      method.setSummary(summary);
    }

    String containerFormat = null;
    List<String> list = method.getResponseMediaTypes();
    if (list.isEmpty()) {
      List<ContextProperties> contextList = 
          contextManager.listContextPropertiesForClass(containerType.stringValue());
      
      if (!contextList.isEmpty()) {
        ContextProperties p = contextList.get(0);
        containerFormat = p.getMediaType();
      }
    }
    if (containerFormat == null) {
      List<String> responseTypeList = method.getResponseMediaTypes();
      containerFormat = responseTypeList.isEmpty() ? "???" : responseTypeList.get(0);
    }
    if (list.isEmpty()) {
      list.add(containerFormat);
    }
    
    
    // Request Headers
    method.addRequestHeader("Authorization", "Authorization parameters dictated by the OAuth Body Hash Protocol");
    
    // Status Codes
    
    
    if (!method.contains(ResponseInfo.OK)) {

      
      String description = MessageFormat.format(
        "<p>The request was successful.</p>\n" +
        "<p>The response body contains the first <a href=\"http://www.w3.org/TR/ldp/#paging\">Page</a>\n" +
        "of {0} resources in the format defined by the <code>{1}</code> media type. This Page has its own URL\n" +
        "which is distinct from the URL of the <code>{2}</code>. The URL of the first Page is supplied in\n" +
        "the <code>Content-Location</code> header.</p>\n" +
        "<p>Clients should be aware that a server may send a 303 redirect to the first Page (see below)\n" +
        "instead of delivering this Page directly in the entity body. Likewise a server may send a 301\n" +
        "or 307 redirect.", 
        typeName, containerFormat, containerType.getLocalName());
      
      method.add(ResponseInfo.OK.copy(description));
    }
    
    method.add(ResponseInfo.MOVED_PERMANENTLY);
   
    if (!method.contains(ResponseInfo.SEE_OTHER)) {
      String description = MessageFormat.format(
        "This status code signals that the <code>{0}</code> itself cannot be delivered in the response.\n" +
        "Instead, the client may receive the first page from a paginated list of {1} resources at the\n" +
        "address specified by the <code>Location</code> header.", 
        containerType.getLocalName(), typeName);
      method.add(ResponseInfo.SEE_OTHER.copy(description));
    }
    method.add(ResponseInfo.TEMPORARY_REDIRECT);
    method.add(ResponseInfo.UNAUTHORIZED);
    method.add(ResponseInfo.NOT_FOUND);
    method.add(ResponseInfo.INTERNAL_SERVER_ERROR);
  }



  private void setCssFile(ServiceDocumentation doc) {

    File htmlFile = doc.getServiceDocumentationFile();
    String cssPath = serviceFileManager.getRelativeCssPath(htmlFile);
    
    doc.setCss(cssPath);
    
  }

  private void setServiceDocumentationFile(ServiceDocumentation doc) {
    doc.setServiceDocumentationFile(serviceFileManager.getServiceDocumentationFile(doc.getRdfType().stringValue()));
  }

  private void setMethods(ServiceDocumentation doc) {
    if (doc.getMethodList().isEmpty()) {
      doc.add(HttpMethod.POST);
      doc.add(HttpMethod.GET);
      doc.add(HttpMethod.PUT);
      doc.add(HttpMethod.DELETE);
    }
    
  }

  private void setPostResponseMediaTypeRef(ServiceDocumentation doc, String typeName) {
    if (doc.getPostResponseMediaTypeRef() == null) {
      String value = contextManager.createIdMediaTypeRef(typeName);
      doc.setPostResponseTypeRef(value);
    }
    
    
  }


  private void setGetDoc(Properties properties, ServiceDocumentation doc, String typeName) {

    if (doc.getGetDocumentation()==null) {
     
      MethodDocumentation method = new MethodDocumentation();
      doc.setGetDocumentation(method);

      String pattern = doc.getGetInstructions();
      if (pattern == null) {
        pattern =
          "To get a representation of a particular {0} instance, the client submits an HTTP GET request to the resource''s " +
          "REST endpoint, in accordance with the following rules:";
      }
      method.setSummary(format(pattern, typeName));   
      setGetRequestHeadersDefault(doc);
      method.setRequestBodyRequirement(GET_REQUEST_BODY_DEFAULT);
      
      
      
    }
    setGetStatus(properties, doc);
    setGetResponseDefault(doc);
    
  } 
  
  private void setGetResponseDefault(ServiceDocumentation doc) {
    MethodDocumentation method = doc.getGetDocumentation();
    if (!method.contains(ResponseInfo.OK)) {
      
      ResponseInfo okInfo = null;
      List<ContextProperties> list = doc.listContextProperties();
      if (list.size()==1) {
        String mediaType = list.get(0).getMediaType();
        String pattern = "The request was successful.  " +
                "<P>The response contains a JSON document in the format defined by the <code>{0}</code> media type.";

        okInfo = ResponseInfo.OK.copy(format(pattern, mediaType));
        
      } else {
        StringBuilder builder = new StringBuilder();
        builder.append("The request was successful.\n");
        builder.append("<p>The response contains a document in one of the formats specified by the Accept header.</p>");
       
        okInfo = ResponseInfo.OK.copy(builder.toString());
        
        addAcceptHeader(doc, method);
        
       
      }
      method.add(okInfo);

    }
    

    addResponse(method, ResponseInfo.UNAUTHORIZED);
    addResponse(method, ResponseInfo.MOVED_PERMANENTLY);
    addResponse(method, ResponseInfo.TEMPORARY_REDIRECT);
    addResponse(method, ResponseInfo.NOT_FOUND);
    
    if (doc.hasMultipleFormats()) {
      addResponse(method, ResponseInfo.NOT_ACCEPTABLE);
    }
    addResponse(method, ResponseInfo.INTERNAL_SERVER_ERROR);
    
  }

  private void addAcceptHeader(ServiceDocumentation doc, MethodDocumentation method) {
    List<ContextProperties> list = doc.listContextProperties();
    
    int count = list.size() + doc.getMediaTypeUriMap().size() + (doc.isAllowArbitraryFormat() ? 2 : 0) +
        (doc.isAllowHtmlFormat() ? 2 : 0);
    
    StringBuilder builder = new StringBuilder();
    if (count == 1 && list.size()==1) {
      builder.append(list.get(0).getMediaType());
    } else {
      builder.append("A comma-separated list containing at least one of the following media types: \n");
      builder.append("<UL>\n");
      LinkManager linkManager = new LinkManager(doc.getServiceDocumentationFile());
      for (ContextProperties context : list) {
        String mediaType = context.getMediaType();
        String href = linkManager.relativize(context.getMediaTypeDocFile());
        
        builder.append("  <LI><code><a href=\"");
        builder.append(href);
        builder.append("\">");
        builder.append(mediaType);
        builder.append("</a></code>\n");
      }
      for (Map.Entry<String, String> e : doc.getMediaTypeUriMap().entrySet()) {
        String mediaType = e.getKey();
        String href = e.getValue();
        builder.append("  <LI><code><a href=\"");
        builder.append(href);
        builder.append("\">");
        builder.append(mediaType);
        builder.append("</a></code>\n");
      }
      if (doc.isAllowHtmlFormat()) {
        builder.append("  <LI><code>text/html</code>\n");
      }
      if (doc.isAllowArbitraryFormat()) {
        String type = doc.getRdfType().getLocalName();
        builder.append("  <LI> ...any other media type for ");
        builder.append(article(type));
        builder.append(type);
        builder.append(" resource");
       
      }
      
      builder.append("</UL>\n");
      String defaultMediaType = doc.getDefaultMediaType();
      if (defaultMediaType != null) {
        builder.append("The Accept header is optional. If omitted, the <code>");
        builder.append(defaultMediaType);
        builder.append(" format is assumed by default.");
      }
    }
    
    
    method.addRequestHeader("Accept", builder.toString());
    
    
  }

  private void setGetRequestHeadersDefault(ServiceDocumentation doc) {
    MethodDocumentation method = doc.getGetDocumentation();
    method.addRequestHeader("Authorization", "<em>Authorization parameters dictated by the OAuth Body Hash Protocol</em>");
    if (doc.isContentNegotiation()) {
      addAcceptHeader(doc, method);
    }
  }
  
  private void setPutDoc(ServiceDocumentation doc, String typeName) {

    if (doc.getPutDocumentation()==null) {
     
      MethodDocumentation method = new MethodDocumentation();
      doc.setPutDocumentation(method);

      String pattern = doc.getPutInstructions();
      if (pattern == null) {
        pattern =
          "To update a particular {0} instance, the client submits an HTTP PUT request to the resource''s " +
          "REST endpoint in accordance with the following rules:";
      }
      
      method.setSummary(format(pattern, typeName));      
      addContentTypeHeader(doc, method);
      method.addRequestHeader("AUTHORIZATION", "<em>Authorization parameters dictated by the OAuth Body Hash Protocol</em>");
      
      method.setRequestBodyRequirement(
            "The request body must contain a JSON document in the format defined by the Content-Type request header." );
      
      if (!method.contains(ResponseInfo.OK)) {
        
        ResponseInfo info = ResponseInfo.OK.copy("The request was successful.");
        
        method.add(info);
      }

      addResponse(method, ResponseInfo.UNAUTHORIZED);
      addResponse(method, ResponseInfo.NOT_FOUND);
      addResponse(method, ResponseInfo.INTERNAL_SERVER_ERROR);
      
      
      
    }
    
  }private void addContentTypeHeader(ServiceDocumentation doc, MethodDocumentation method) {
    

    List<ContextProperties> list = doc.listContextProperties();
    if (list.size()==1) {
      String mediaType = list.get(0).getMediaType();
      method.addRequestHeader("Content-Type", "<code>" + mediaType + "</code>");
      
    } else {
      StringBuilder builder = new StringBuilder();
      builder.append("One of:\n");
      builder.append("<UL>\n");
      for (ContextProperties context : list) {
        builder.append("  <LI>");
        builder.append(context.getMediaType());
        builder.append("\n");
      }
      for (Map.Entry<String,String> e : doc.getMediaTypeUriMap().entrySet()) {
        String mediaType = e.getKey();
        builder.append("  <LI>");
        builder.append(mediaType);
        builder.append("\n");
      }
      builder.append("</UL>\n");
      if (doc.isAllowArbitraryFormat()) {
        String typeName = doc.getRdfType().getLocalName();
        builder.append("<p>Or any arbitrary media type for ");
        builder.append(article(typeName));
        builder.append(typeName);
        builder.append(".</p>");
        
      }
      method.addRequestHeader("Content-Type", builder.toString());
    }
    
    
  }

  private void setDeleteDoc(ServiceDocumentation doc, String typeName) {

    if (doc.getDeleteDocumentation()==null) {
     
      MethodDocumentation method = new MethodDocumentation();
      doc.setDeleteDocumentation(method);

      String pattern =
          "To delete a particular {0} instance, the client submits an HTTP DELETE request to the resource''s " +
          "REST endpoint in accordance with the following rules:";
      
      method.setSummary(format(pattern, typeName));    
      method.addRequestHeader("Authorization", "<em>Authorization parameters dictated by the OAuth Body Hash Protocol</em>");
      
      if (doc.hasMultipleFormats()) {
        StringBuilder builder = new StringBuilder();
        builder.append("The format for one specific representation of the ");
        builder.append(typeName);
        builder.append(" resource that is to be deleted. If the Content-Type header is not specified, then all ");
        builder.append("representations of the resource will be deleted.");
        
        method.addRequestHeader("Content-Type", builder.toString());
      }
      
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

  private void setPostDoc(ServiceDocumentation doc, ServiceMethodInfo methodInfo, String typeName) {
    List<HttpMethod> list = doc.getMethodList();
    if (!list.contains(HttpMethod.POST)) {
      return;
    }
    
    MethodDocumentation method=doc.getPostDocumentation();
    if (method == null) {
      method = new MethodDocumentation();
      doc.setPostDocumentation(method);
    }
    
    if (method.getSummary()==null) {
      String pattern =
          "To create a new {0} instance within the server, a client submits an HTTP POST request to the server''s " +
          "{0} container endpoint in accordance with the following rules: ";
     
      method.setSummary(format(pattern, typeName));   
    }
    
    if (method.getRequestBodyRequirement() == null) {
      method.setRequestBodyRequirement(
        "The request body MUST be a JSON document that conforms to the format specified by the Content-Type header.");
    }

    applyRequestMediaTypes(doc, method);

    String idMediaType = doc.getPostResponseMediaType();
    String createdDescription = null;
      
    // TODO: Support the option of multiple response types, in which case one of them
    //       will be a default, but others may be requested via the Accept header.
    
    if (idMediaType == null) {
      createdDescription = "The request has succeeded.\n" +
      "<p>The reponse will contain an empty body.</p>";
    } else {
      
      ContextProperties context = contextManager.getContextPropertiesByMediaType(idMediaType);
      if (context == null) {
        throw new ServiceDocumentationSyntaxError("Unknown media type: " + idMediaType);
      }
      LinkManager linkManager = new LinkManager(doc.getServiceDocumentationFile());
      String href = linkManager.relativize(context.getMediaTypeDocFile());
      StringBuilder anchor = new StringBuilder();
      appendAnchor(anchor, href, idMediaType);
      createdDescription = methodInfo.getPostCreatedDescription();
      if (createdDescription == null) {
          createdDescription = 
            "The request has succeeded.\n" +
            "<p>The response contains a small JSON document that provides the endpoint URI for the newly created " +
            "<code>{0}</code> resource.  This JSON document must conform to the <code>{1}</code> format.  " +
            "The <code>Content-Type</code> header of the response will be set to this media type.";
      }
      createdDescription = format(createdDescription, typeName, anchor.toString());
      
    }

    method.addRequestHeader("Authorization", "<em>Authorization parameters dictated by the OAuth Body Hash Protocol</em>");
    
    
    addResponse(method, ResponseInfo.CREATED.copy(createdDescription));
    addResponse(method, ResponseInfo.BAD_REQUEST);
    addResponse(method, ResponseInfo.UNAUTHORIZED);
    addResponse(method, ResponseInfo.INTERNAL_SERVER_ERROR);
   
    
  }
  
  


  private void applyRequestMediaTypes(ServiceDocumentation doc, MethodDocumentation method) {
    
    List<String> typeList = method.getRequestContentTypes();
    String value = null;
    if (!typeList.isEmpty()) {
      
      if (typeList.size() == 1) {
        value = typeList.get(0);
      } else {
        StringBuilder builder = new StringBuilder();
        builder.append("<em>One of</em>\n");
        builder.append("<ul>\n");
        for (String type : typeList) {
          builder.append("<li><code>");
          builder.append(type);
          builder.append("</code>\n");
        }
        builder.append("</ul>");
        value = builder.toString();
      }
    } else if (doc.isAllowArbitraryFormat()) {
      value = "<em>An arbitrary media type</em>";
    }
    
    if (value != null) {
      method.addRequestHeader("Content-Type", value);
    }
    
    
  }

  private void appendAnchor(StringBuilder builder, String href, String text) {
    builder.append("<a href=\"");
    builder.append(href);
    builder.append("\">");
    builder.append(text);
    builder.append("</a>");
  }

  private void setRepresentationText(ServiceDocumentation doc, String typeName) {
    
    
    if (doc.getRepresentationText() == null) {
      List<ContextProperties> list = doc.listContextProperties();
      if (list.size()==1) {
        String mediaType = list.get(0).getMediaType();
        String mediaTypeRef = list.get(0).getMediaTypeRef();
        String pattern =
            "<p><code>{0}</code> resources manipulated via this REST API are represented as JSON documents in " +
            "the <code>{1}</code> format.  For detailed information about this media type, see {2}.</p>";
        
        doc.setRepresentationText(format(pattern, typeName, mediaType, mediaTypeRef));
        
        
      } else {
        StringBuilder builder = new StringBuilder();
        
        
        
        builder.append("<code>{0}</code> resources accessed through this REST API are represented by documents in ");
        builder.append(" a variety of formats including");
        if (doc.isAllowArbitraryFormat()) {
          builder.append(" (but not limited to)");
        }
        builder.append(":\n");
        builder.append("<div class=\"mediatype\">\n");
        LinkManager linkManager = new LinkManager(doc.getServiceDocumentationFile());
        for (ContextProperties context : list) {
          String mediaType = context.getMediaType(); 
          
          File mediaTypeFile = context.getMediaTypeDocFile();
          String href = linkManager.relativize(mediaTypeFile);
          builder.append("    <div><a href=\"");
          builder.append(href);
          builder.append("\">");
          builder.append(mediaType);
          builder.append("</a></div>\n");
        }
        for (Map.Entry<String,String> e : doc.getMediaTypeUriMap().entrySet()) {
          String mediaType = e.getKey();
          String href = e.getValue();
          builder.append("    <div>");
          appendAnchor(builder, href, mediaType);
          builder.append("</div>\n");
        }
        if (doc.isAllowHtmlFormat()) {
          builder.append("    <div>text/html</div>");
        }
        builder.append("</div>\n");
        
        if (doc.isAllowArbitraryFormat()) {
          builder.append("<p>This list is not exhaustive; in general the REST API supports arbitrary\n");
          builder.append("content types for {0} resources. Thus, a client may POST ");
          builder.append(article(typeName));
          builder.append(typeName);
          builder.append(" representation in some arbitrary format and later GET that representation from the server.\n");
          builder.append("The API supports multiple representations of a given resource simultaneously.</p>");
        }
        
        
        builder.append("<p>{0} resources have ");
        appendAnchor(builder, "http://www.w3.org/Provider/Style/URI.html", "Cool URLs");
        builder.append(". This means that there is a single URL for each resource and the URL does not\n");
        builder.append("change for different representations (or versions) of the resource.  Each representation is\n");
        builder.append("defined by a different media type, and clients access specific representations\n");
        builder.append("through ");
        appendAnchor(builder, "http://www.w3.org/Protocols/rfc2616/rfc2616-sec12.html", "content negotiation");
        builder.append(".</p>");
        
        doc.setRepresentationText(format(builder.toString(), typeName));
        
      }
      
      MethodDocumentation method = doc.getContainerGetDocumentation();
      if (method != null) {
        
        String text = null;
        List<String> mediaTypeList = method.getResponseMediaTypes();
        
        
        if (mediaTypeList.size()==1 && doc.getContainerType()!=null) {
          String containerName = doc.getContainerType().getLocalName();
          String containerArticle = StringUtil.article(containerName);
          
          String containerMediaType = mediaTypeList.get(0);
          ContextProperties context = contextManager.getContextPropertiesByMediaType(containerMediaType);
          
          if (context == null) {
            logger.warn("ContextProperties not found: " + containerMediaType);
          } else {
            String containerFormat = context.getMediaTypeRef();
            String pattern = 
                "<p>It is also possible to obtain a paginated list of <code>{0}</code> resources from " +
                "{1} <code>{2}</code> in the <code>{3}</code> format.  For detailed information about this media type, " +
                "see {4}."
                ;
            
            text = MessageFormat.format(pattern, typeName, containerArticle, containerName, containerMediaType, containerFormat);
            
          }
              
        }
        
        // TODO: handle the case where mediaTypeList.size() > 1
        
        if (text != null) {
          StringBuilder builder = new StringBuilder(doc.getRepresentationText());
          builder.append("\n");
          builder.append(text);
          
          doc.setRepresentationText(builder.toString());
        }
      }
    }
    
  }

  private String article(String typeName) {
    
    char c = Character.toUpperCase(typeName.charAt(0));
    return (c=='A' || c=='E' || c=='I' || c=='O' || c=='U') ? "an " : "a ";
    
  }

  private void setRepresentationHeading(ServiceDocumentation doc,
      String typeName) {
    
    if (doc.getRepresentationHeading() == null) {
      String pattern = "{0} Representations";
      doc.setRepresentationHeading(format(pattern, typeName));
    }
    
  }

  private void setIntroduction(ServiceDocumentation doc, String typeName) {
    
    if (doc.getIntroduction() == null) {
      
      String value = null;
      if (doc.getMethodList().size()==1) {
        
        String pattern = 
            "<P>This specification defines a REST API for " + actionList(doc) +
            "<code>{0}</code> resources via an HTTP " + doc.getMethodList().get(0).getName() + " request.</P>";
        
        value = format(pattern, typeName);
        
        
      } else {
      
        String pattern =
            "<P>This specification defines a REST API for " + actionList(doc) +
            "<code>{0}</code> resources.  Following common conventions, the API " +
            "uses a different HTTP verb for each type of operation: " + methodUsage(doc) +
            "</P>\n" +
            "<P>Implementations of this REST API may be incomplete; a given server " +
            "might support only a subset of the HTTP verbs. A server that supports the complete API will \n" +
            "expose two different kinds of endpoints: a <em>container</em> endpoint for receiving POST \n" +
            "requests and <em>item</em> endpoints for manipulating individual instances.  This specification \n" +
            "document does not prescribe a method for discovering the endpoint URLs.</P>"
            ;
        
        value = format(pattern, typeName);
        
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
      
      List<ContextProperties> list = doc.listContextProperties();
      if (list.size() == 1) {
        String mediaType = list.get(0).getMediaType();
      
        doc.setTitle(format("A REST API for {0} Resources<br/>" +
        		"in the <code>{1}</code> Format", typeName, mediaType));
      } else {

        doc.setTitle(format("A REST API for {0} Resources in multiple formats", typeName));
      }
    }
    
//    if (doc.getSubtitle() == null) {
//      doc.setSubtitle(format("in a format defined by the <code>{0}</code> Media Type", doc.getMediaType()));
//    }
    
  }

  


  private void setAbstractText(ServiceDocumentation doc, String typeName) {
    
    if (doc.getAbstactText()==null) {
      
      String pattern =
          "This specification defines a REST API for " + actionList(doc) +
          "<code>{0}</code> resources.";
      
      doc.setAbstactText(format(pattern, typeName));
     
      
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

  public ServiceDocumentationList getServiceDocumentationByRdfType(String rdfTypeURI) {
    return map.get(rdfTypeURI);
  }
  
  public File getServiceDocumentationFile(String rdfTypeURI) {
    return serviceFileManager.getServiceDocumentationFile(rdfTypeURI);
  }
  
  public void writeAll() throws IOException {
    for (List<ServiceDocumentation> list : map.values()) {
      for (ServiceDocumentation doc : list) {
        write(doc);
      }
    }
  }

  private void write(ServiceDocumentation doc) throws IOException {
    if (doc == null) return;
    
    String text = printer.print(doc);
    
    File htmlFile = serviceFileManager.getServiceDocumentationFile(doc.getRdfType().stringValue());
    htmlFile.getParentFile().mkdirs();
    
    OutputStream out = new FileOutputStream(htmlFile);
    OutputStreamWriter writer = new OutputStreamWriter(out);
    try {
      writer.write(text);
      writer.flush();
    } finally {
      writer.close();      
    }
    
  }
  
  static class ServiceMethodInfo {
    private String postCreatedDescription;

    public String getPostCreatedDescription() {
      return postCreatedDescription;
    }

    public void setPostCreatedDescription(String postCreatedDescription) {
        this.postCreatedDescription = postCreatedDescription;
    }
    
    
    
  }

}
