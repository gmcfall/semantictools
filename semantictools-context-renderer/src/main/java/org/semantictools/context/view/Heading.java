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
package org.semantictools.context.view;

import java.util.ArrayList;
import java.util.List;


public class Heading {
    private Level level;
    private String headingNumber;
    private String headingText;
    private String headingId;
    private String className;
    private List<Heading> children;
    private Heading parent;
    private boolean showNumber=true;
    private boolean inToc=true;

    public Heading(Level level, String headingText, String headingId) {
      this.level = level;
      this.headingText = headingText;
      this.headingId = headingId;
    }
    
    /**
     * Returns true if the number should be displayed with this heading.
     */
    public boolean isShowNumber() {
      return showNumber;
    }

    /**
     * Sets the flag that controls whether the number should be displayed
     * with this heading.
     */
    public void setShowNumber(boolean showNumber) {
      this.showNumber = showNumber;
    }



    public void setHeadingNumber(String headingNumber) {
      this.headingNumber = headingNumber;
    }

    public String getHeadingNumber() {
      return headingNumber;
    }

    public String getHeadingText() {
      return headingText;
    }

    public Level getLevel() {
      return level;
    }

    public String getHeadingId() {
      return headingId;
    }

    public void add(Heading subheading) {
      if (children == null) {
        children = new ArrayList<Heading>();
      }
      children.add(subheading);
      subheading.parent = this;
    }

    public List<Heading> getChildren() {
      return children;
    }

    public Heading getParent() {
      return parent;
    }

    public void setParent(Heading parent) {
      this.parent = parent;
    }

    public String getClassName() {
      return className;
    }

    public void setClassName(String className) {
      this.className = className;
    }

    public boolean isInToc() {
      return inToc;
    }

    public void setInToc(boolean inToc) {
      this.inToc = inToc;
    }
    

  }
