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

import java.awt.Graphics2D;


public interface Widget {
  
  /**
   * Initialize the layout of this widget at the given position
   */
  void layout();
  
  void setPosition(int left, int top);
  void paint(Graphics2D graphics);
  Rect getBounds();
  
  Style getStyle();
  void setStyle(Style style);
  
  Widget getParent();
  void setParent(Widget parent);

}
