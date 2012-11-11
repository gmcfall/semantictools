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
package org.semantictools.graphics;

public class Padding {
  private int padTop;
  private int padBottom;
  private int padLeft;
  private int padRight;
  private int maxDescent;
  
  
  public Padding(int top, int left, int bottom, int right) {
    padTop = top;
    padBottom = bottom;
    padLeft = left;
    padRight = right;
  }
  
  public Padding() {
    
  }
  
  public int getPadTop() {
    return padTop;
  }
  public void setPadTop(int padTop) {
    this.padTop = padTop;
  }
  public int getPadBottom() {
    return padBottom;
  }
  public void setPadBottom(int padBottom) {
    this.padBottom = padBottom;
  }
  public int getPadLeft() {
    return padLeft;
  }
  public void setPadLeft(int padLeft) {
    this.padLeft = padLeft;
  }
  public int getPadRight() {
    return padRight;
  }
  public void setPadRight(int padRight) {
    this.padRight = padRight;
  }

  @Deprecated
  public int getMaxDescent() {
    return maxDescent;
  }

  @Deprecated
  public void setMaxDescent(int maxDescent) {
    this.maxDescent = maxDescent;
  }
  
  

}
