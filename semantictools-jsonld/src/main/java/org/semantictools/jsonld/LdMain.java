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

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class LdMain {
  private static final String DEFAULT_REPO = "repo";
  private static final String REPO = "-repo";
  private static final String SRC = "-src";
  
  private List<File> fileList = new ArrayList<File>();
  private File sourceDir;
  private File repoDir;

  /**
   * @param args
   */
  public static void main(String[] args) {

    LdMain main = new LdMain(args);
    main.run();

  }
  
  public LdMain(String[] args) {
    for (int i=0; i<args.length; i++) {
      String value = args[i];
      if (REPO.equals(value)) {
        repoDir = new File(args[++i]);
        
      } else if (SRC.equals(value)) {
        sourceDir = new File(args[++i]);
        
      } else {
        addFile(new File(value));
      }
    }
  }
  
  public void run() {
    LdProcessor processor = new LdProcessor(sourceDir, getRepoDir(), false);
    for (File file : fileList) {
      URL url;
      try {
        System.out.print("Validating... ");
        System.out.println(file);
        url = file.toURI().toURL();
        LdValidationReport report = processor.validate(url);
        System.out.print(report);
      } catch (Throwable oops) {
        oops.printStackTrace(System.out);
      }
    }
  }

  private File getRepoDir() {
    if (repoDir == null) {
      repoDir = new File(DEFAULT_REPO);
    }
    return repoDir;
  }

  /**
   * Add the file for a JSON document that is to be validated.
   */
  public void addFile(File file) {
    fileList.add(file);
  }

  
    
    
}
