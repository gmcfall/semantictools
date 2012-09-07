package org.semantictools.context.renderer.model;

public class HttpMethod {

  public static final HttpMethod POST = new HttpMethod("POST", "creating");
  public static final HttpMethod GET = new HttpMethod("GET", "reading");
  public static final HttpMethod PUT = new HttpMethod("PUT", "updating");
  public static final HttpMethod DELETE = new HttpMethod("DELETE", "deleting");
  
  private String name;
  private String gerund;
  
  
  private HttpMethod(String name, String gerund) {
    this.name = name;
    this.gerund = gerund;
  }

  public static HttpMethod getByName(String name) {
    if ("POST".equals(name)) return POST;
    if ("GET".equals(name)) return GET;
    if ("PUT".equals(name)) return PUT;
    if ("DELETE".equals(name)) return DELETE;
    return null;
  }

  public String getName() {
    return name;
  }


  public String getGerund() {
    return gerund;
  }
  
  

}
