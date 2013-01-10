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
package org.semantictools.jsonld.impl;

import java.io.File;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import org.semantictools.jsonld.LdAsset;

public class LdAssetRepositoryTest {
  
  private File source = new File("src/test/resources/gdx");
  private File root = new File("target/LdAssetRepositoryTest");
  
  private LdAssetRepository repository;
  
  @Before
  public void setUp() {
    delete(root);
    repository = new LdAssetRepository(root);
  }

  /**
   * Delete the given file or directory recursively
   */
  private void delete(File file) {
    if (file.isDirectory()) {
      File[] array = file.listFiles();
      for (int i=0; i<array.length; i++) {
        delete(array[i]);
      }
    }
    if (file.exists()) {
      file.delete();
    }
    
  }

  @Test
  public void test() throws Exception {
    
    repository.scan(source);
    verifyContext();
    verifyTurtle();
    
    
  }

  private void verifyTurtle() {
    LdAsset asset = repository.findAsset("http://purl.org/pearson/core/v1/vocab/outcomes#");
    assertTrue(asset != null);
    assertEquals(LdContentType.TURTLE, asset.getFormat());
    assertEquals("http://purl.org/pearson/core/v1/vocab/outcomes#", asset.getURI());
    
  }

  private void verifyContext() {

    LdAsset asset = repository.findAsset("http://purl.org/pearson/core/v1/ctx/outcomes/GradebookItemEvent");
    assertTrue(asset != null);
    assertEquals(LdContentType.JSON_LD_CONTEXT, asset.getFormat());
    assertEquals("http://purl.org/pearson/core/v1/ctx/outcomes/GradebookItemEvent", asset.getURI());
    
  }
  
}
