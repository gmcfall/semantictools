package org.semantictools.gwt.generator;

import java.util.HashSet;
import java.util.Set;

public class DomainUtil {
  private static final Set<String> domainSet = new HashSet<String>();
  
  static {
    domainSet.add("aero");
    domainSet.add("asia");
    domainSet.add("biz");
    domainSet.add("cat");
    domainSet.add("com");
    domainSet.add("coop");
    domainSet.add("info");
    domainSet.add("int");
    domainSet.add("jobs");
    domainSet.add("mobi");
    domainSet.add("museum");
    domainSet.add("name");
    domainSet.add("net");
    domainSet.add("org");
    domainSet.add("pro");
    domainSet.add("tel");
    domainSet.add("travel");
    domainSet.add("xxx");
    domainSet.add("edu");
    domainSet.add("gov");
    domainSet.add("mil");
    
    // TODO: Add country code top-level domains
  }
  
  public static boolean isTopLevelDomain(String value) {
    return domainSet.contains(value);
  }

}
