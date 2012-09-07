package org.semantictools.uml.graphics;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.List;

import org.semantictools.frame.model.Encapsulation;
import org.semantictools.frame.model.Field;
import org.semantictools.graphics.Arc;
import org.semantictools.graphics.ArcEnd;
import org.semantictools.graphics.GraphicsUtil;
import org.semantictools.graphics.HorizontalPanel;
import org.semantictools.graphics.Label;
import org.semantictools.graphics.Rect;
import org.semantictools.graphics.Style;
import org.semantictools.graphics.Transformer;
import org.semantictools.graphics.VerticalPanel;
import org.semantictools.graphics.Widget;
import org.semantictools.graphics.WidgetTransformer;
import org.semantictools.uml.model.UmlAssociation;
import org.semantictools.uml.model.UmlAssociationEnd;
import org.semantictools.uml.model.UmlClass;

public class ClassDiagram {
  
  private Style classStyle;
  private Style arcStyle;
  private Style classNameStyle;
  private Style fieldStyle;
  private Style classBodyStyle;
  private Style imageStyle;
  
  private UmlClass umlClass;
  private ClassWidget classWidget;
  private VerticalPanel childrenPanel;
  private VerticalPanel parentPanel;
  private HorizontalPanel superTypePanel;
  private HorizontalPanel subtypePanel;
  private BufferedImage image;
  private int width;
  private int height;
  private int dx;
  private int dy;
  
  private int maxChildArcWidth;
  private int maxParentArcWidth;
  private int generalizationArrowLength = 50;
  
  
  
  public ClassDiagram(UmlClass type) {
    umlClass = type;
    createStyles();
    
    Label classLabel = new Label(type.getLocalName(), classNameStyle);
    classWidget = new ClassWidget(classLabel, classStyle);
    childrenPanel = new VerticalPanel();
    
    dx = imageStyle.getMarginLeft();
    dy = imageStyle.getMarginTop();
    addFields();
    addChildren();
    addParents();
    addSupertypes();
    addSubtypes();
    layout();


    image = new BufferedImage(width+1, height+1, BufferedImage.TYPE_INT_ARGB);
    Graphics2D g = image.createGraphics();
    g.setRenderingHint(
        RenderingHints.KEY_TEXT_ANTIALIASING,
        RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
    paint(g);

  }
  
  private void addFields() {
    List<Field> list = umlClass.getFieldList();
    for (Field field : list) {
      String name = field.getLocalName();
      String type = field.getType().getLocalName();
      String multiplicity = field.getMultiplicity();
      String text = name + ": " + type + "[" + multiplicity + "]";
      Label label = new Label(text, fieldStyle);
      classWidget.addField(label);
    }
    
    if (!list.isEmpty()) {
      classWidget.getBody().setStyle(classBodyStyle);
    }
    
  }

  private void addSubtypes() {

    List<UmlClass> list = umlClass.getSubtypeList();
    
    
    if (list.isEmpty()) return;
    
    subtypePanel = new HorizontalPanel();
    
    for (UmlClass subtype : list) {
      
      Label label = new Label(subtype.getLocalName(), classNameStyle);
      ClassWidget subWidget = new ClassWidget(label, classStyle);
      
      subtypePanel.add(subWidget);
      
      
      
      ArcEnd selfEnd = new ArcEnd(null, null, arcStyle);
      
      ArcEnd otherEnd = new ArcEnd(null, null, arcStyle);
      selfEnd.setShape(ArcEnd.TRIANGLE);
      
      new Arc(selfEnd, otherEnd);
      
      classWidget.addBottomArc(selfEnd);
      subWidget.addTopArc(otherEnd);
      
      
    }
  }
  
  
  private void addSupertypes() {

    List<UmlClass> list = umlClass.getSupertypeList();
    
    
    if (list.isEmpty()) return;
    
    superTypePanel = new HorizontalPanel();
    
    for (UmlClass supertype : list) {
      
      Label label = new Label(supertype.getLocalName(), classNameStyle);
      ClassWidget superWidget = new ClassWidget(label, classStyle);
      
      superTypePanel.add(superWidget);
      
      
      
      ArcEnd selfEnd = new ArcEnd(null, null, arcStyle);
      
      ArcEnd otherEnd = new ArcEnd(null, null, arcStyle);
      otherEnd.setShape(ArcEnd.TRIANGLE);
      
      new Arc(selfEnd, otherEnd);
      
      classWidget.addTopArc(selfEnd);
      superWidget.addBottomArc(otherEnd);
      
      
    }
    
  }

  public BufferedImage getImage() {
    return image;
  }


  private void layout() {
    performInitialLayout();
    performFinalLayout();
    setEndWidths();
    performFinalLayout();
    setDimensions();
    
  }
  
  private void setDimensions() {
    
    width = 0;
    height = 0;
    
    updateDimensions(classWidget);
    updateDimensions(childrenPanel);
    updateDimensions(subtypePanel);
    updateDimensions(parentPanel);
    updateDimensions(superTypePanel);
    
    width = imageStyle.getMarginLeft() + width + imageStyle.getMarginRight() + 1;
    height = imageStyle.getMarginTop() + height + imageStyle.getMarginBottom() + 1;
    
  }

  private void updateDimensions(Widget widget) {
    if (widget == null) return;
    width = Math.max(width, widget.getBounds().getRight());
    height = Math.max(height, widget.getBounds().getBottom());
    
  }

  private void setEndWidths() {
    classWidget.setEndWidths(classWidget.listLeftArcs());
    classWidget.setEndWidths(classWidget.listRightArcs());
  }

  static class Bounds {
    int top=0;
    int left=0;
    int right=0;
    int bottom=0;
    
    void apply(Widget widget) {
      if (widget != null) {
        Rect r = widget.getBounds();
        top = Math.min(top, r.getTop());
        left = Math.min(left, r.getLeft());
        right = Math.max(right, r.getRight());
        bottom = Math.max(bottom, r.getBottom());
      }
    }
    
    
  }
  
  
  private void performFinalLayout() {
    
    dx = 0;
    dy = 0;

    maxChildArcWidth = 0;
    maxParentArcWidth = 0;
    
    placeClassWidget();
    placeChildrenPanel();
    placeParentPanel();
    placeSuperTypePanel();
    placeSubtypePanel();
    
    Bounds bounds = new Bounds();
    bounds.apply(classWidget);
    bounds.apply(superTypePanel);
    bounds.apply(childrenPanel);
    bounds.apply(parentPanel);
    bounds.apply(subtypePanel);
    
    width = imageStyle.getMarginLeft() + bounds.right - bounds.left + imageStyle.getMarginRight() + 1;
    height = imageStyle.getMarginTop() +  bounds.bottom - bounds.top + imageStyle.getMarginBottom() + 1;
    
    if (bounds.left<0) {
      dx = -bounds.left;
    }
    if (bounds.top<0) {
      dy = -bounds.top;
    }
    
    dx += imageStyle.getMarginLeft();
    dy += imageStyle.getMarginTop();
    
    maxChildArcWidth = 0;
    maxParentArcWidth = 0;
    
    placeClassWidget();
    placeChildrenPanel();
    placeParentPanel();
    placeSuperTypePanel();
    placeSubtypePanel();
    
    
  }

  private int getMaxChildArcWidth() {

    if (maxChildArcWidth == 0) {
      int leftWidth = 0;
      int rightWidth = 0;
      
      List<ArcEnd> childList = classWidget.listRightArcs();
      if (childList != null) {
        for (ArcEnd end : childList) {
          Arc arc = end.getArc();
          int a = arc.getEnds()[0].getBounds().getWidth();
          leftWidth = Math.max(leftWidth, a);
          
          int b = arc.getEnds()[1].getBounds().getWidth();
          rightWidth = Math.max(rightWidth, b);
          
        }
        maxChildArcWidth = leftWidth + rightWidth;
      }
    }
    
    return maxChildArcWidth;
    
  }

  private int getMaxParentArcWidth() {

    if (maxParentArcWidth == 0) {
      int leftWidth = 0;
      int rightWidth = 0;
      
      List<ArcEnd> parentList = classWidget.listLeftArcs();
      if (parentList != null) {
        for (ArcEnd end : parentList) {
          Arc arc = end.getArc();
          int a = arc.getEnds()[0].getBounds().getWidth();
          leftWidth = Math.max(leftWidth, a);
          
          int b = arc.getEnds()[1].getBounds().getWidth();
          rightWidth = Math.max(rightWidth, b);
          
        }
        maxParentArcWidth = leftWidth + rightWidth;
      }
    }
    
    return maxParentArcWidth;
    
  }

  private void performInitialLayout() {

    int marginLeft = imageStyle.getMarginLeft();
    int marginTop = imageStyle.getMarginTop();
    int marginRight = imageStyle.getMarginRight();
    int marginBottom = imageStyle.getMarginBottom();
    

    int dy=marginTop;
    classWidget.setPosition(marginLeft, marginTop);
    classWidget.layout();
    
    if (superTypePanel != null) {
      
      superTypePanel.setPosition(marginLeft, marginTop);
      superTypePanel.layout();
      
      WidgetTransformer t = new WidgetTransformer(superTypePanel);
      classWidget.setTop(dy = t.getBottom() + generalizationArrowLength);
      
    }
    
    if (parentPanel != null) {
      
      parentPanel.setPosition(marginLeft, dy);
      parentPanel.layout();
      classWidget.setLeft(600); 
      height = Math.max(height, parentPanel.getBottom());
    }
    
    
    width = classWidget.getRight();
    height = classWidget.getBottom();
    
    if (childrenPanel != null) {
      WidgetTransformer t = new WidgetTransformer(classWidget);
      
      childrenPanel.setPosition(t.getRight() + 100, marginTop);
      childrenPanel.layout();
      
      t.set(childrenPanel);
      
      
      width = t.getWidth();
      
      height = Math.max(height, t.getBottom());
    }
    
    if (subtypePanel != null) {
      int top = height + generalizationArrowLength;
      int left = classWidget.getLeft();
      
      subtypePanel.setPosition(left, top);
      subtypePanel.layout();
      height = subtypePanel.getBottom();
    }
    
    width += marginLeft + marginRight;
    height += marginTop + marginBottom;
    
  }

  private void paint(Graphics2D g) {
    
    GraphicsUtil.paint(g, classWidget);
    GraphicsUtil.paint(g, superTypePanel);
    GraphicsUtil.paint(g, childrenPanel);
    GraphicsUtil.paint(g, parentPanel);
    GraphicsUtil.paint(g,  subtypePanel);
    classWidget.paintArcs(g, classWidget.listRightArcs());
    classWidget.paintArcs(g, classWidget.listTopArcs());
    classWidget.paintArcs(g, classWidget.listLeftArcs());
    classWidget.paintArcs(g, classWidget.listBottomArcs());
    
  }

  private void addParents() {

    List<UmlAssociation> list = umlClass.getParentList();
    
    if (list.isEmpty()) return;
    
    parentPanel = new VerticalPanel();
    
    UmlClass priorParent = null;
    ClassWidget priorWidget = null;
    
    for (UmlAssociation a : list) {
     
      UmlClass peer = a.getOtherEnd(umlClass).getParticipant();
      Label label = new Label(peer.getLocalName(), classNameStyle);
      
      ClassWidget otherWidget = (peer == priorParent) ? priorWidget : new ClassWidget(label, classStyle);
      
      if (otherWidget != priorWidget) {
        parentPanel.add(otherWidget);
      }
      priorParent = peer;
      priorWidget = otherWidget;
      
      UmlAssociationEnd myEnd = a.getSelfEnd(umlClass);
      UmlAssociationEnd parentEnd = a.getOtherEnd(umlClass);
      
      ArcEnd selfEnd = new ArcEnd(myEnd.getLocalName(), myEnd.getMultiplicity(), arcStyle);
      setEndShape(selfEnd, myEnd.getEncapsulation());
      
      ArcEnd otherEnd = new ArcEnd(parentEnd.getLocalName(), parentEnd.getMultiplicity(), arcStyle);
      setEndShape(otherEnd, parentEnd.getEncapsulation());
      new Arc(selfEnd, otherEnd);
      
      classWidget.addLeftArc(selfEnd);
      otherWidget.addRightArc(otherEnd);
      
      
    }
  }
  
  private void addChildren() {
   
    List<UmlAssociation> list = umlClass.getChildren();
    
    if (list.isEmpty()) return;
    
    childrenPanel = new VerticalPanel();
    
    UmlClass priorClass = null;
    ClassWidget priorWidget = null;
    for (UmlAssociation a : list) {

      UmlAssociationEnd classEnd = a.getSelfEnd(umlClass);
      UmlAssociationEnd fieldEnd = a.getOtherEnd(umlClass);
      
      UmlClass fieldClass = a.getOtherEnd(umlClass).getParticipant();
      
      UmlClass peer = fieldEnd.getParticipant();
      Label label = new Label(peer.getLocalName(), classNameStyle);
      
      ClassWidget otherWidget = (priorClass == fieldClass) ? priorWidget :   new ClassWidget(label, classStyle);
      
      
      if (otherWidget != priorWidget) {
        childrenPanel.add(otherWidget);
      }

      priorClass = fieldClass;
      priorWidget = otherWidget;
      
      ArcEnd selfEnd = new ArcEnd(classEnd.getLocalName(), classEnd.getMultiplicity(), arcStyle);
      setEndShape(selfEnd, classEnd.getEncapsulation());
      
      ArcEnd otherEnd = new ArcEnd(fieldEnd.getLocalName(), fieldEnd.getMultiplicity(), arcStyle);
      setEndShape(otherEnd, fieldEnd.getEncapsulation());
      
      new Arc(selfEnd, otherEnd);
      
      classWidget.addRightArc(selfEnd);
      otherWidget.addLeftArc(otherEnd);
      
      
    }
    
    
    
  }


  private void setEndShape(ArcEnd end, Encapsulation e) {
    switch (e) {
    case AGGREGATION :
      end.setShape(ArcEnd.DIAMOND);
      break;
      
    case COMPOSITION :
      end.setShape(ArcEnd.DIAMOND);
      end.setFilled(true);
      break;
    }
    
  }

  private void createStyles() {
    
    if (imageStyle == null) {
      imageStyle = new Style();
      imageStyle.setMarginTop(5);
      imageStyle.setMarginBottom(5);
      imageStyle.setMarginLeft(5);
      imageStyle.setMarginRight(5);
    }
    if (classStyle == null) {
      
      classStyle = new Style();
      classStyle.setBgColor(new Color(1f, 1f, 0.7226562f));
      classStyle.setBorderColor(new Color(0.5f, 0.0f, 0.0f));
      classStyle.setMarginTop(15);
      classStyle.setMarginBottom(15);
      classStyle.setMarginLeft(15);
      classStyle.setMarginRight(15);
      
    }
    if (classBodyStyle == null) {
      classBodyStyle = new Style();
      classBodyStyle.setPadBottom(3);
    }
    
    if (fieldStyle == null) {
      fieldStyle = new Style();
      fieldStyle.setFont(new Font("Arial", Font.PLAIN, 11));
      fieldStyle.setPadBottom(1);
      fieldStyle.setPadTop(1);
      fieldStyle.setPadLeft(5);
      fieldStyle.setPadRight(5);
    }
    
    if (arcStyle == null) {
      arcStyle = new Style();
      arcStyle.setBorderColor(new Color(0.5f, 0.0f, 0.0f));
      arcStyle.setColor(Color.black);
      arcStyle.setFont(new Font("Arial", Font.PLAIN, 11));
      arcStyle.setPadBottom(3);
      arcStyle.setPadTop(3);
      arcStyle.setPadLeft(5);
      arcStyle.setPadRight(5);
      arcStyle.setMarginTop(10);
      arcStyle.setMarginBottom(10);
      
    }
    
    if (classNameStyle == null) {
      classNameStyle = new Style();
      classNameStyle.setColor(Color.black);
      classNameStyle.setFont(new Font("Arial", Font.BOLD, 12));
      classNameStyle.setPadTop(3);
      classNameStyle.setPadBottom(3);
      classNameStyle.setPadLeft(5);
      classNameStyle.setPadRight(5);
    }

    BufferedImage image = new BufferedImage(10, 10, BufferedImage.TYPE_INT_ARGB);
    Graphics2D g = image.createGraphics();
    setMetrics(arcStyle, g);
    setMetrics(classNameStyle, g);
    setMetrics(fieldStyle, g);
  }


  private void setMetrics(Style style, Graphics2D g) {
    if (style.getMetrics() != null || style.getFont()==null) return;
    
    
    Font font = style.getFont();
    FontMetrics metrics = g.getFontMetrics(font);
    style.setMetrics(metrics);
    
  }
  
  
  private void placeClassWidget() {
    classWidget.setPosition(dx, dy);
  }
  
  private void placeParentPanel() {
    if (parentPanel == null) return;
    
    int middle = classWidget.getTop() + classWidget.getHeight()/2;
    int left = classWidget.getLeft() - getMaxParentArcWidth() - parentPanel.getWidth();
    int top = middle - parentPanel.getHeight()/2;
    
    parentPanel.setPosition(left, top);
    
  }
  
  private void placeChildrenPanel() {
    if (childrenPanel == null) return;
    int dx = getMaxChildArcWidth();
    int middle = classWidget.getTop() + classWidget.getHeight()/2;
    int left = classWidget.getRight() + dx;
    int top = middle - childrenPanel.getHeight()/2;
    
    childrenPanel.setPosition(left, top);
    
  }
  
  private void placeSuperTypePanel() {
    if (superTypePanel == null) return;
    
    int center = classWidget.getLeft() + classWidget.getWidth()/2;
    
    int left = center - superTypePanel.getWidth()/2;
    int top = classWidget.getTop() - generalizationArrowLength - superTypePanel.getHeight();
    
    superTypePanel.setPosition(left, top);
  }
  



  private void placeSubtypePanel() {
    if (subtypePanel == null) return;
    
    int center = classWidget.getLeft() + classWidget.getWidth()/2;
    int left = center - subtypePanel.getWidth()/2;
    int top = classWidget.getBottom() + generalizationArrowLength;
    
    subtypePanel.setPosition(left, top);
    
  }
  

}
