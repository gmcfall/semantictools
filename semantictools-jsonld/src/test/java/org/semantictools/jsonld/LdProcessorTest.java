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

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.net.URL;

import org.junit.Before;
import org.junit.Test;
import org.semantictools.jsonld.impl.LdValidationServiceImplTest;

public class LdProcessorTest {
  
  private File sourceDir = new File("src/test/resources/gdx");
  private File repositoryDir = new File("target/LdEnhancerAppTest");
  private String contextURL = "http://purl.org/pearson/core/v1/ctx/outcomes/GradebookItemEvent";
  private File outFile = new File("target/LdEnhancerAppTest/purl.org/pearson/core/v1/ctx/outcomes/GradebookItemEvent/ENHANCED_CONTEXT.json");
  private String jsonDocumentPath = "GradebookItemEventInvalid.json";
  
  private URL jsonDocumentURL;
  
  @Before
  public void setUp() {
    jsonDocumentURL = getClass().getClassLoader().getResource(jsonDocumentPath);
  }
  
  @Test
  public void test() throws Exception {
    
    FileUtil.delete(repositoryDir);
    LdProcessor app = new LdProcessor(sourceDir, repositoryDir, false);
    app.publishEnhancedContext(contextURL);
    assertTrue(outFile.exists());
    
    LdValidationReport report = app.validate(jsonDocumentURL);
    LdValidationServiceImplTest.validateReport(report);
    
  }

}
