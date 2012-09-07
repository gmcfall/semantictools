package org.semantictools.web.upload;

import java.util.ArrayList;
import java.util.List;

public class URLMapper {
  private List<RewriteRule> rewriteRules = new ArrayList<URLMapper.RewriteRule>();
  
  public void addRewriteRule(String fromURL, String toURL) {
    rewriteRules.add(new RewriteRule(fromURL, toURL));
  }
  
  /**
   * Rewrites the give URL (which may be relative URL)
   * @param url
   * @return
   */
  public String rewrite(String url) {
    for (RewriteRule rule : rewriteRules) {
      if (url.startsWith(rule.getFromURL())) {
        
        String suffix = url.substring(rule.getFromURL().length());
        String result = rule.getToURL() + suffix;
        
        return result;
        
      }
    }
    
    return null;
  }
  
  
  private static class RewriteRule {
    private String fromURL;
    private String toURL;
    private RewriteRule(String fromURL, String toURL) {
      this.fromURL = fromURL;
      this.toURL = toURL;
    }
    public String getFromURL() {
      return fromURL;
    }
    public String getToURL() {
      return toURL;
    }
    
    
  }

}
