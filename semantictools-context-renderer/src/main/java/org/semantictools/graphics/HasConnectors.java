package org.semantictools.graphics;

import java.util.List;

public interface HasConnectors {
  
  List<ArcEnd> listLeftArcs();
  List<ArcEnd> listTopArcs();
  List<ArcEnd> listRightArcs();
  List<ArcEnd> listBottomArcs();
  
  void addLeftArc(ArcEnd end);
  void addRightArc(ArcEnd end);
  void addTopArc(ArcEnd end);
  void addBottomArc(ArcEnd end);

}
