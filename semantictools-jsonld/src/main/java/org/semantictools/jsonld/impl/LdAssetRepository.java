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
package org.semantictools.jsonld.impl;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.Properties;

import org.semantictools.jsonld.LdAsset;
import org.semantictools.jsonld.LdPublishException;
import org.semantictools.jsonld.LdPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An LdAssetManager that maintains a local cache of LdAssets on the file system.
 * 
 * @author Greg McFall
 *
 */
public class LdAssetRepository extends LdAssetManagerImpl implements LdPublisher {
  private static final Logger logger = LoggerFactory.getLogger(LdAssetRepository.class);
  private static final String PROPERTIES_FILENAME = "asset.properties";
  private static final String URI = "uri";
  private static final String DEFAULT = "default";
  
  private File root;
  
  /**
   * Create a new LdAssetRepository.
   * @param root  The root directory under which assets will be stored.
   * @throws IOException
   */
  public LdAssetRepository(File root)  {
    this.root = root;
  }
  
  /**
   * Scan the specified directory for assets, and copy them into
   * this repository.
   * 
   * @param file
   */
  public void scan(File file) {
    if (file.isDirectory()) {
      File[] array = file.listFiles();
      for (int i=0; i<array.length; i++) {
        scan(array[i]);
      }
    } else {
      LdContentType format = LdContentType.guessContentType(file.getName());
      if (format != LdContentType.UNKNOWN) {
        try {
          URL location = file.toURI().toURL();
          LdAsset asset = readAsset(null, location);
          if ((asset.getURI()!=null) && (asset.getFormat() != LdContentType.UNKNOWN)) {
            publish(asset);
          }
          
        } catch (Throwable oops) {
          logger.warn("failed to store asset: " + file.getPath());
        }
      }
    }
  }
  
  /**
   * Copy the given asset into this repository
   */
  public void publish(LdAsset asset) throws LdPublishException {
    try {
      File assetDir = assetDir(asset.getURI());
      assetDir.mkdirs();
      
      LdContentType format = asset.getFormat();
      
      String formatName = format.name();
      
      String fileName = formatName + '.' + format.getExtension();
      
      Properties p = getAssetProperties(assetDir);
      p.put(formatName, fileName);
      p.put(URI, asset.getURI());
      
      if (format.isDefaultType() || p.get(DEFAULT)==null) {
        p.put(DEFAULT, formatName);
      }
      
      File propertiesFile = new File(assetDir, PROPERTIES_FILENAME);
      FileWriter writer = new FileWriter(propertiesFile);
      try {
        p.store(writer, null);
      } finally {
        safeClose(writer);
      }
      
      File contentFile = new File(assetDir, fileName);
      try {
        writer = new FileWriter(contentFile);
        writer.write(asset.getContent());      
      } finally {
        safeClose(writer);
      }
    } catch (IOException oops) {
      throw new LdPublishException(asset.getURI(), oops);
    }
    
    
  }

  private void safeClose(FileWriter writer) {
    try {
      writer.close();
    } catch (Throwable oops) {
      logger.warn("failed to close writer", oops);
    }
    
  }
  
  public LdAsset findAsset(String assetURI) {
    return findAsset(assetURI, null);
  }

  @Override
  public LdAsset findAsset(String assetURI, LdContentType format) {
    File assetDir = assetDir(assetURI);
    LdAsset asset = null;
    if (assetDir.exists()) {
      try {
        Properties properties = getAssetProperties(assetDir);
       
        String formatName = null;
        String fileName = null;
        if (format != null) {
          formatName = format.name();
          fileName = properties.getProperty(format.name());
          
        } else {
          // If there is an XSD file, use it by default.
          formatName = LdContentType.XSD.name();
          fileName = properties.getProperty(formatName);
          if (fileName == null) {
            // There is no XSD file, so use the declared default format.
            formatName = properties.getProperty(DEFAULT);
            fileName = properties.getProperty(formatName);
          }
        }
           
        File contentFile = new File(assetDir, fileName);
        URL location = contentFile.toURI().toURL();
        
        if (format == null) {
          format = LdContentType.valueOf(LdContentType.class, formatName);
        }
        
        asset = new LdAsset(assetURI, format, location);
        if (isEagerLoading()) {
          asset.loadContent();
        }
        
      } catch (Throwable oops) {
        logger.warn("Failed to load asset from repository: " + assetURI, oops);
      }
      
    } else {
      asset = loadAsset(assetURI, format);
    }
    return asset;
  }
  
  /**
   * Returns the properties of the asset whose representations are stored
   * in the given directory.
   * This file contains properties as described below:
   * <DL>
   *   <DT>uri</DT><DD>The URI of the asset</DD>
   *   <DT>default</DT><DD>The name of the default content type for the asset</DD>
   *   <DT>{format}</DT><DD>The name of the file containing the representation of the asset in the specified format.
   *   In this case, the property name matches the exact name of an element from the {@link LdContentType} enumeration.
   *   </DD>
   * </DL>
   */
  private Properties getAssetProperties(File assetDir) throws IOException {
    Properties p = new Properties();
    
    File propertiesFile = new File(assetDir, PROPERTIES_FILENAME);
    if (!propertiesFile.exists()) return p;
    
    FileReader reader = new FileReader(propertiesFile);
    try {
      p.load(reader);
    } finally {
      safeClose(reader);
    }
    
    return p;
  }
  
  private File assetDir(String assetURI) {
    try {
      URI uri = new URI(assetURI);
      String path = uri.getAuthority() + "/" + uri.getPath();
      
      return new File(root, path);
    } catch (Throwable oops) {
      return null;
    }
  }
  
  
  
  

}
