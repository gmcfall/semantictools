package org.semantictools.context.renderer.model;


public class CreateDiagramRequest  {
  private JsonContext context;
  private TreeNode root;
  private String imagePath;
  
  
  public CreateDiagramRequest() {}
  
  
  public CreateDiagramRequest(JsonContext context, TreeNode root, String imagePath) {
    
    this.context = context;
    this.root = root;
    this.imagePath = imagePath;
  }
  

  public TreeNode getRoot() {
    return root;
  }


  public void setRoot(TreeNode root) {
    this.root = root;
  }



  public String getImagePath() {
    return imagePath;
  }



  public void setImagePath(String imagePath) {
    this.imagePath = imagePath;
  }



  public JsonContext getContext() {
    return context;
  }
  public void setContext(JsonContext context) {
    this.context = context;
  }
  



  
  
  

}
