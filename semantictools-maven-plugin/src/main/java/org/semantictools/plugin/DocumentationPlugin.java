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
package org.semantictools.plugin;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.semantictools.publish.DocumentationGenerator;

/**
 * Invokes the semantictools DocunentationGenerator.
 * 
 * @goal generate
 * 
 * @phase generate-sources
 * @author Greg McFall 
 *
 */
public class DocumentationPlugin extends AbstractMojo {


  /**
   * Location of the directory that contains the RDF source files and the
   * configuration properties files that drive the documentation process.
   * <p>
   * The default location for this directory is
   * </p>
   * <pre>
   *   src/main/resources/rdf
   * </pre>
   * @parameter expression="${basedir}/src/main/resources/rdf"
   */
  private File rdfDir;
  
  /**
   * The directory in which the output artifacts will be stored locally.
   * <p>
   * The default location for this directory is
   * </p>
   * <pre>
   *   target/generated-sources/rdf
   * </pre>
   * @parameter expression="${basedir}/target/generated-sources/rdf"
   */
  private File outputDir;
  
  /**
   * The directory to use as the local RDF repository.
   * <p>The default location for this directory is</p>
   * <pre>
   *   target/repo
   * </pre>
   * @parameter expression="${basedir}/target/repo"
   */
  private File repoDir;
  
  /**
   * A flag that specifies whether or not the generated artifacts should
   * be published to semantictools.appspot.com
   * @parameter expression="false"
   */
  private boolean publish;
  
  /**
   * The endpoint to which documentation will be published.  This parameter
   * is meaningful only if publish=true.
   * 
   * @parameter expression="http://semantic-tools.appspot.com/admin/upload.do"
   */
  private String publishEndpoint;
  
  /**
   * The name of the top-level index file.  The default value is "index.html".
   * @parameter expression="index.html"
   */
  private String indexFileName;
  
  /**
   * The version identifier for resources that are published.  This should be a
   * date in the format "yyyy-MM-dd".  By default, the current date is used.
   * 
   * @parameter expression="yyyy-MM-dd"
   */
  private String version;
  
  /**
   * A flag which controls whether a new set of documentation should be generated.
   * When generate=true, the plugin will generate a new set of documentation,
   * otherwise it will proceed directly to the publish phase.  This value is true by
   * default.
   * 
   * @parameter expression="true"
   */
  private boolean generate;
  
  public void execute() throws MojoExecutionException, MojoFailureException {

    if ("yyyy-MM-dd".equals(version)) {
      SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
      version = dateFormat.format(new Date());
    }
    
    Logger logger = Logger.getLogger("mavenLogger");
    logger.setUseParentHandlers(false);
    logger.addHandler(new MavenLogHandler(getLog()));
    
    DocumentationGenerator generator = new DocumentationGenerator(rdfDir, outputDir, repoDir, publish);
    generator.setLogger(logger);
    
    generator.setUploadEndpoint(publishEndpoint);
    generator.setVersion(version);
    generator.setGenerate(generate);
    generator.setIndexFileName(indexFileName);
    
    
    try {
      generator.run();
    } catch (Exception e) {
      throw new MojoExecutionException("Failed to generate documentation", e);
    }

  }
  
  
  static class MavenLogHandler extends Handler {
    private boolean severe = false;
    private Log log;

    public MavenLogHandler(Log log) {
      this.log = log;
    }

    @Override
    public void publish(LogRecord record) {
      
      Level level = record.getLevel();
      if (level == Level.SEVERE) {
        log.error(record.getMessage(), record.getThrown());
        severe = true;
      } else {
        log.warn(record.getMessage());
      }
      
    }

    @Override
    public void flush() {
    }

    @Override
    public void close() throws SecurityException {
    }
    
  }

}
