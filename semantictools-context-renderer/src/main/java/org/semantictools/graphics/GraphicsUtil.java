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
import java.awt.geom.AffineTransform;

public class GraphicsUtil {
  
  public static void paint(Graphics2D g, Widget w) {
    if (w == null) return;
    beginWidget(g, w);
    w.paint(g);
    endWidget(g, w);
  }
  public static void beginWidget(Graphics2D g, Widget w) {
    int x = w.getBounds().getLeft();
    int y = w.getBounds().getTop();
    g.translate(x, y);
  }
  
  public static void endWidget(Graphics2D g, Widget w) {
    int x = -w.getBounds().getLeft();
    int y = -w.getBounds().getTop();
    g.translate(x, y);
  }
  
  public static void setTransform(Graphics2D g, Widget w) {
    AffineTransform t = new AffineTransform();
    while (w != null) {
      int x = w.getBounds().getLeft();
      int y = w.getBounds().getTop();
      t.translate(x, y);
      w = w.getParent();
    }
    g.setTransform(t);
  }

}
