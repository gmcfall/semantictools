package org.semantictools.web.upload;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Checksum {
  
  public static String sha1(File file) throws IOException, NoSuchAlgorithmException {
    
    MessageDigest md = MessageDigest.getInstance("SHA1");
    FileInputStream fis = new FileInputStream(file);
    byte[] dataBytes = new byte[1024];
 
    int nread = 0; 
 
    while ((nread = fis.read(dataBytes)) != -1) {
      md.update(dataBytes, 0, nread);
    };
 
    byte[] mdbytes = md.digest();
 
    //convert the byte to hex format
    StringBuffer sb = new StringBuffer("");
    for (int i = 0; i < mdbytes.length; i++) {
        sb.append(Integer.toString((mdbytes[i] & 0xff) + 0x100, 16).substring(1));
    }
 
    return sb.toString();
  }

}
