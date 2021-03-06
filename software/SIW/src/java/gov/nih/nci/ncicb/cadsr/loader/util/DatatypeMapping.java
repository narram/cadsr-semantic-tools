/*L
 * Copyright Oracle Inc, SAIC, SAIC-F
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/cadsr-semantic-tools/LICENSE.txt for details.
 */

/*
 * Copyright 2000-2003 Oracle, Inc. This software was developed in conjunction with the National Cancer Institute, and so to the extent government employees are co-authors, any rights in such works shall be subject to Title 17 of the United States Code, section 105.
 *
 Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this list of conditions and the disclaimer of Article 3, below. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 *
 * 2. The end-user documentation included with the redistribution, if any, must include the following acknowledgment:
 *
 * "This product includes software developed by Oracle, Inc. and the National Cancer Institute."
 *
 * If no such end-user documentation is to be included, this acknowledgment shall appear in the software itself, wherever such third-party acknowledgments normally appear.
 *
 * 3. The names "The National Cancer Institute", "NCI" and "Oracle" must not be used to endorse or promote products derived from this software.
 *
 * 4. This license does not authorize the incorporation of this software into any proprietary programs. This license does not authorize the recipient to use any trademarks owned by either NCI or Oracle, Inc.
 *
 * 5. THIS SOFTWARE IS PROVIDED "AS IS," AND ANY EXPRESSED OR IMPLIED WARRANTIES, (INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE) ARE DISCLAIMED. IN NO EVENT SHALL THE NATIONAL CANCER INSTITUTE, ORACLE, OR THEIR AFFILIATES BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE 
 */
package gov.nih.nci.ncicb.cadsr.loader.util;

import gov.nih.nci.ncicb.cadsr.loader.UserSelections;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Properties;
import java.util.Set;
import java.util.Collection;
import java.util.Map;
import java.util.HashMap;
import org.apache.log4j.Logger;

public class DatatypeMapping {
  
  private static Map<String, String> vdMapping = new HashMap<String, String>();

  public static Map<String, String> getMapping() { 
    return vdMapping;
  }

  public static Set<String> getKeys() {
    return vdMapping.keySet();
  }

  public static Collection<String> getValues() {
    return vdMapping.values();
  }
   
  private static Map<String, String> systemMapping = new HashMap<String, String>();
  
  public static Map<String, String> getSystemMapping() { return systemMapping; }
  
  public static Set<String> getSystemKeys() { return systemMapping.keySet(); }
  
  public static Collection<String> getSystemValues() { return systemMapping.values(); }
    
  
  private static Map<String, String> userMapping = new HashMap<String, String>();
  
  public static Map<String, String> getUserMapping() { return userMapping; }
  
  public static Set<String> getUserKeys() { return userMapping.keySet(); }
  
  public static Collection<String> getUserValues() { return userMapping.values(); }
  

  private static Logger logger = Logger.getLogger(DatatypeMapping.class.getName());

  private static String userFilename = "user-datatype-mapping.xml";

  private static DatatypeMapping instance = new DatatypeMapping();

  private DatatypeMapping() 
  {}

  public static DatatypeMapping getInstance() {
    return instance;
  }

  public void setMappingURL(String mappingURL) 
  {
    // create url from xml file
    String systemFilename = mappingURL;
    
    URL url = null;     
    
    try {
      //        url = Thread.currentThread().getContextClassLoader().getResource(systemFilename);
      
      url = new URL(systemFilename);
      systemMapping = DatatypeMappingXMLUtil.readMapping(url);
      vdMapping.putAll(systemMapping);
      
    } catch (Exception e){
      logger.fatal("Resource Properties could not be loaded (" + systemFilename + "). Exiting now.");
      logger.fatal(e.getMessage());
      System.exit(1);
    } // end of try-catch
  }

  public void addUserMapping() {
    try{
      UserSelections selections = UserSelections.getInstance();
      String name = (String) selections.getProperty("FILENAME");
      
      if (name != null) {
        File userFile = new File(name);
        userFile = new File(userFile.getParent()+ "/" + userFilename);
	
        if(userFile.exists()) {
          URL url = new URL("file://" + userFile.toString());
          userMapping = DatatypeMappingXMLUtil.readMapping(url);
        } else 
          userMapping = new HashMap();
        
        updateVdMapping();
      } 
    } catch (Exception e){
      logger.fatal(e.getMessage());
      System.exit(1);
    } // end of try-catch
    
  }
  
  public static void writeUserMapping(Map<String, String> datatypes) 
  {
    UserSelections selections = UserSelections.getInstance();
    String filename =  (String) selections.getProperty("FILENAME");
    File f = new File(filename);
    f = new File(f.getParent()+ "/"+ userFilename);
    
    DatatypeMappingXMLUtil.writeMapping(f.toString(), datatypes);

    userMapping = datatypes;
    updateVdMapping();
  }

  private static void updateVdMapping() 
  {
    vdMapping = new HashMap<String, String>();
    vdMapping.putAll(systemMapping);
    vdMapping.putAll(userMapping); 
  }

}