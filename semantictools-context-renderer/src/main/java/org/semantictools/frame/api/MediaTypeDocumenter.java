package org.semantictools.frame.api;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.List;
import java.util.Properties;

import javax.xml.parsers.ParserConfigurationException;

import org.semantictools.context.renderer.ContextHtmlPrinter;
import org.semantictools.context.renderer.MediaTypeFileManager;
import org.semantictools.context.renderer.MediaTypeIndexPrinter;
import org.semantictools.context.renderer.ServiceDocumentationPrinter;
import org.semantictools.context.renderer.StreamFactory;
import org.semantictools.context.renderer.URLRewriter;
import org.semantictools.context.renderer.impl.DiagramGeneratorImpl;
import org.semantictools.context.renderer.impl.FileStreamFactory;
import org.semantictools.context.renderer.model.ContextProperties;
import org.semantictools.context.renderer.model.JsonContext;
import org.semantictools.context.renderer.model.ServiceDocumentation;
import org.semantictools.web.upload.AppspotUploadClient;
import org.xml.sax.SAXException;

/**
 * Produces HTML documentation for a media type
 * @author Greg McFall
 *
 */
public class MediaTypeDocumenter {
  private TypeManager typeManager;
  private ContextManager contextManager;
  private MediaTypeFileManager fileManager;
  private ServiceDocumentationManager serviceDocManager;
  private ServiceDocumentationPrinter serviceDocPrinter;
  private GeneratorProperties generatorProperties;
  private ContextProperties currentContext;
  private AppspotUploadClient uploadClient;
  private boolean publish = false;
  
  
  public MediaTypeDocumenter() {
    typeManager = new TypeManager();
    contextManager = new ContextManager();
    fileManager = new MediaTypeFileManager();
    serviceDocManager = new ServiceDocumentationManager(contextManager);
    serviceDocPrinter = new ServiceDocumentationPrinter(new MyURLRewriter());
    uploadClient = new AppspotUploadClient();
  }
  
  public AppspotUploadClient getUploadClient() {
    return uploadClient;
  }
  
  public ServiceDocumentationManager getServiceDocumentManager() {
    return serviceDocManager;
  }
  
  public ContextManager getContextManager() {
    return contextManager;
  }



  /**
   * Returns true if this MediaTypeDocumenter is configured to publish
   * the documentation that was produced to appspot.
   */
  public boolean isPublish() {
    return publish;
  }

  /**
   * Specifies whether this MediaTypeDocumenter will publish
   * the documentation that is produced to appspot.
   */
  public void setPublish(boolean publish) {
    this.publish = publish;
  }



  class MyURLRewriter implements URLRewriter {

    @Override
    public String rewrite(String url) {
      if (generatorProperties == null || fileManager == null || currentContext==null) {
        return url;
      }
      String baseURL = generatorProperties.getBaseURL();
      String mediaType = currentContext.getMediaType();
      
      return fileManager.toRelativeURL(url, baseURL, mediaType);
    }
    
  }
  
  public void loadAll(File directory) throws IOException, ParserConfigurationException, SAXException, ContextPropertiesSyntaxException {
    loadSchemas(directory);
   
    scan(directory);
    serviceDocManager.scan(directory);
  }
  
  private void scan(File directory) throws ContextPropertiesSyntaxException, IOException {

    File[] fileList = directory.listFiles();
    for (int i=0; i<fileList.length; i++) {
      File file = fileList[i];
      if (file.getName().equals("context.properties")) {
        loadContextProperties(file);
        
      }  else if (file.getName().equals("generator.properties")) {
        createGeneratorProperties(file);
      }
      
      if (file.isDirectory()) {
        scan(file);
      }
    }
    
    
  }

  private void createGeneratorProperties(File file) throws IOException {
    Properties p = new Properties();
    FileInputStream input = new FileInputStream(file);
    try {
      p.load(input);
      generatorProperties = new GeneratorProperties(p);
    } finally {
      input.close();
    }
    
  }

  private void loadSchemas(File directory) throws IOException, ParserConfigurationException, SAXException {
    typeManager.loadDir(directory);
  }
  
  private void loadContextProperties(File contextProperties) throws ContextPropertiesSyntaxException, IOException {
    contextManager.loadContextProperties(contextProperties);
  }
  
  public void produceAllDocumentation(File outDir) throws IOException {
    List<ContextProperties> list = contextManager.listContextProperties();
    for (ContextProperties p : list) {
      produceDocumentation(p, outDir);
    }
    copyMediaTypeStylesheet(outDir);
    updateMediaTypeIndex(outDir);
  }

  private void updateMediaTypeIndex(File outDir) throws IOException {
    File indexFile = new File(outDir, "index.html");
    MediaTypeIndexPrinter printer = new MediaTypeIndexPrinter();
    printer.printIndex(indexFile);
    
  }

  private void copyMediaTypeStylesheet(File outDir) throws IOException {
    InputStream stream = getClass().getClassLoader().getResourceAsStream("mediaType.css");
    if (stream != null) {
      File cssFile = new File(outDir, "mediaType.css");
      copyFile(stream, cssFile);
      if (publish && uploadClient != null) {
        uploadClient.upload("text/css", "mediatype/mediaType.css", cssFile);
      }
    }
    
  }

  private void copyFile(InputStream stream, File cssFile) throws IOException  {
   
    FileOutputStream out = new FileOutputStream(cssFile);
    try {
      byte[] buffer = new byte[1024];
      
      int len;
      while ( (len = stream.read(buffer)) > 0) {
        out.write(buffer, 0, len);
      }
      
    } finally {
      out.close();
    }
    
  }

  public void produceDocumentation(String mediaType, File outDir) throws MediaTypeNotFoundException, IOException {
    ContextProperties properties = contextManager.getContextPropertiesByMediaType(mediaType);
    if (properties == null) {
      throw new MediaTypeNotFoundException(mediaType);
    }
    produceDocumentation(properties, outDir);
  }
    
  private void produceDocumentation(ContextProperties properties, File outDir) throws IOException {
    currentContext = properties;
    
    String mediaType = properties.getMediaType();
    
    File baseDir = new File(outDir, fileManager.pathToMediaTypeDir(mediaType));
    baseDir.mkdirs();
    
    ContextBuilder contextBuilder = new ContextBuilder(typeManager);
    JsonContext context = contextBuilder.createContext(properties);
    
    ContextWriter contextWriter = new ContextWriter();
    File contextFile = new File(baseDir, fileManager.getJsonContextFileName(context));
    PrintWriter printWriter = new PrintWriter(new FileWriter(contextFile));
    contextWriter.writeContext(printWriter, context);
    printWriter.close();
    


    File inputDir = properties.getSourceFile().getParentFile();
    
    StreamFactory streamFactory = new FileStreamFactory(inputDir, baseDir);
    DiagramGeneratorImpl diagramManager = new DiagramGeneratorImpl(streamFactory);
    ContextHtmlPrinter contextPrinter = new ContextHtmlPrinter(generatorProperties, typeManager, fileManager, streamFactory, diagramManager);
    contextPrinter.setIncludeOverviewDiagram(true);
    contextPrinter.setIncludeClassDiagrams(true);
    contextPrinter.printHtml(context, properties);
    
    printServiceDocumentation(streamFactory, properties);
    if (publish) {
      uploadClient.upload(baseDir, properties);
    }
    
    
  }

  private void printServiceDocumentation(StreamFactory streamFactory,
      ContextProperties properties) throws IOException {
    
    String mediaType = properties.getMediaType();
    ServiceDocumentation doc = serviceDocManager.getServiceDocumentationByMediaType(mediaType);
    if (doc == null) return;
    doc.setCssHref( fileManager.pathToStyleSheet(mediaType));
    String text = serviceDocPrinter.print(doc);
    
    OutputStream out = streamFactory.createOutputStream(fileManager.getServiceDocumentationFileName());
    OutputStreamWriter writer = new OutputStreamWriter(out);
    try {
      writer.write(text);
      writer.flush();
    } finally {
      writer.close();      
    }
  }
  
  

}
