package org.semantictools.plugin;

import java.io.File;
import java.io.PrintStream;
import java.util.Collections;
import java.util.List;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.semantictools.frame.api.TypeManager;
import org.semantictools.gwt.generator.DefaultGwtTypeGeneratorListener;
import org.semantictools.gwt.generator.FileWriterFactory;
import org.semantictools.gwt.generator.GwtTypeConfig;
import org.semantictools.gwt.generator.GwtTypeGenerator;

/**
 * Invokes the semantictools GWT Type Generator.
 * 
 * @goal generate
 * 
 * @phase generate-sources
 * @author Greg McFall
 *
 */
public class GwtGeneratorPlugin extends AbstractMojo {
  
  /**
   * Location of the directory that contains the RDF source files and the
   * configuration properties files that drive the documentation process.
   * <p>
   * The default location for this directory is
   * </p>
   * <pre>
   *   src/main/rdf
   * </pre>
   * @parameter expression="${basedir}/src/main/rdf"
   */
  private File rdfDir;
  
  /**
   * The directory in which the output source code will be stored locally.
   * <p>
   * The default location for this directory is
   * </p>
   * <pre>
   *   target/generated-sources/java
   * </pre>
   * @parameter expression="${basedir}/target/generated-sources/java"
   */
  private File outputDir;
  
  /**
   * A flag which indicates whether the list of ignored classes should be
   * printed.
   * 
   * @parameter expression="false"
   */
  private boolean reportIgnoredClasses;
  

  public void execute() throws MojoExecutionException, MojoFailureException {
    try {
      TypeManager typeManager = new TypeManager();
      typeManager.loadDir(rdfDir);
      
      FileWriterFactory factory = new FileWriterFactory(outputDir);
      GwtTypeConfig config = new GwtTypeConfig();
      config.scan(rdfDir);
      GwtTypeGenerator generator = new GwtTypeGenerator(config, typeManager, factory);
      
      DefaultGwtTypeGeneratorListener listener = new DefaultGwtTypeGeneratorListener();
      generator.setListener(listener);
      
      generator.generateAll();
      
      if (reportIgnoredClasses) {
        reportIgnoredClasses(listener.getIgnoreList());
      }
      
    } catch (Throwable oops) {
      throw new MojoExecutionException("Failed to generate GWT classes", oops);
    }
  }

  private void reportIgnoredClasses(List<String> ignoreList) {
    Collections.sort(ignoreList);
    PrintStream out = System.out;
    out.println("Ignored Classes:");
    for (int i=0; i<ignoreList.size(); i++) {
      String uri = ignoreList.get(i);
      out.print("  ");
      out.println(uri);
    }
  }

}
