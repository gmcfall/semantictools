package org.semantictools.web.upload;

import java.util.HashMap;
import java.util.Map;

public class MimeTypes {
  
  private static Map<String, String> suffix2MediaType = new HashMap<String, String>();
  static {
    suffix2MediaType.put("json", "application/json");
    suffix2MediaType.put("html", "text/html");
    suffix2MediaType.put("png", "image/png");
  }

  
  public static String getMediaType(String suffix) {
    return suffix2MediaType.get(suffix);
  }

}
