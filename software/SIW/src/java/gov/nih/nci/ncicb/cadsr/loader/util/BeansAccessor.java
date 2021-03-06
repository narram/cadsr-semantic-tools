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

import gov.nih.nci.ncicb.cadsr.loader.CadsrLoader;
import gov.nih.nci.ncicb.cadsr.loader.UMLLoaderGUI;
import gov.nih.nci.ncicb.cadsr.loader.Unclassifier;
import gov.nih.nci.ncicb.cadsr.loader.UserSelections;
import gov.nih.nci.ncicb.cadsr.loader.ext.CadsrPublicApiModule;
import gov.nih.nci.ncicb.cadsr.loader.parser.ElementWriter;
import gov.nih.nci.ncicb.cadsr.loader.ui.CadsrDialog;
import gov.nih.nci.ncicb.cadsr.loader.ui.DEReuseDialog;
import gov.nih.nci.ncicb.cadsr.loader.ui.WizardController;
import gov.nih.nci.ncicb.cadsr.loader.validator.UMLValidator;
import gov.nih.nci.ncicb.cadsr.semconn.SemanticConnector;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.support.*;

public class BeansAccessor {
  
  private static Logger logger = Logger.getLogger(BeansAccessor.class.getName());

  private static BeanFactory factory = null;

  public static ElementWriter getWriter() {
    RunMode mode = (RunMode)UserSelections.getInstance().getProperty("MODE");
    if(mode.equals(RunMode.Reviewer)) {
      return (ElementWriter)getFactory().getBean("xmiWriter");
    } else if(mode.equals(RunMode.Curator)) {
      return (ElementWriter)getFactory().getBean("csvWriter");
    }
    else return null;
  }

  public static SemanticConnector getSemanticConnector() {
    return (SemanticConnector)getFactory().getBean("semanticConnector");
  }
  
  public static CadsrPublicApiModule getCadsrPublicApiModule() {
    return (CadsrPublicApiModule)getFactory().getBean("cadsrPublicApiModule");
  }

  public static UMLLoaderGUI getSiw() {
    return (UMLLoaderGUI)getFactoryForSiw().getBean("siw");
  }

  public static WizardController getWizardController() {
    return (WizardController)getFactory().getBean("wizardController");
  }
  
  public static CadsrLoader getUmlLoader() {
    return (CadsrLoader)getFactory().getBean("umlLoader");
  }

  public static CadsrLoader getIsoLoader() {
    return (CadsrLoader)getFactory().getBean("isoLoader");
  }
  
  public static Unclassifier getUnclassifier() {
    return (Unclassifier)getFactory().getBean("unclassifier");
  }

  public static UMLValidator getSiwValidator() {
    return (UMLValidator)getFactory().getBean("siwValidator");
  }

//   public static RoundtripAction getRoundtripAction() {
//     return (RoundtripAction)getFactory().getBean("roundtripAction");
//   }

  public static CadsrDialog getCadsrDEDialog() {
    return (CadsrDialog)getFactory().getBean("cadsrDEDialog");
  }

  public static CadsrDialog getCadsrVDDialog() {
    return (CadsrDialog)getFactory().getBean("cadsrVDDialog");
  }

  public static DEReuseDialog getDEReuseDialog() {
    return (DEReuseDialog)getFactory().getBean("deReuseDialog");
  }

  public static Object getBeanByName(String name) {
    return getFactory().getBean(name);
  }

  private static BeanFactory getFactoryForSiw()
  {
    if (factory == null)
    {
        try
	{
	    factory = new ClassPathXmlApplicationContext(new String[]{"beans.xml"});
        }
        catch (Throwable e)
        {
            logger.error(e.getMessage());
            throw new RuntimeException(e);
        }
    }
       
    return factory;
  }



  private static BeanFactory getFactory() {
    try {
      if(factory != null) {
        return factory;
      }
      ClassPathXmlApplicationContext cpCtx = new ClassPathXmlApplicationContext(new String[]{"beans.xml", "spring-datasources.xml", "loader-spring.xml"});

//       GenericApplicationContext genCtx = new GenericApplicationContext();
//       XmlBeanDefinitionReader xmlReader = new XmlBeanDefinitionReader(genCtx);

//       cpCtx.initBeanDefinitionReader(xmlReader);

//       cpCtx.refresh();
      
      factory = cpCtx;


//       factory = new XmlBeanFactory(new InputStreamResource(Thread.currentThread().getContextClassLoader().getResourceAsStream("beans.xml")));

      return factory;
    } catch (Exception e){
      logger.error(e.getMessage());
    } // end of try-catch
    return null;
  }
 
  
 
}