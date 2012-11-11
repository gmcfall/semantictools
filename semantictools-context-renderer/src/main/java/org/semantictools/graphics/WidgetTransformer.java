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

public class WidgetTransformer extends Transformer {
  
  Widget widget;
  
  public WidgetTransformer(Widget w) {
    super(w);
  }
  
  public void set(Widget w) {
    super.set(w);
    widget = w;
  }
  
  public int getLeft() {
    return widget.getBounds().getLeft() + getDx();
  }
  
  public int getRight() {
    return widget.getBounds().getRight() + getDx();
  }
  
  public int getTop() {
    return widget.getBounds().getTop() + getDy();
  }
  
  public int getBottom() {
    return widget.getBounds().getBottom() + getDy();
  }
  
  public int getWidth() {
    return widget.getBounds().getWidth();
  }
  
  public int getHeight() {
    return widget.getBounds().getHeight();
  }

}
