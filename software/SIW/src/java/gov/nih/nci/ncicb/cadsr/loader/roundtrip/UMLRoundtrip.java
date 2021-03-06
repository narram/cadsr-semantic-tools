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
package gov.nih.nci.ncicb.cadsr.loader.roundtrip;

import gov.nih.nci.ncicb.cadsr.domain.*;
import gov.nih.nci.ncicb.cadsr.loader.ElementsLists;

import gov.nih.nci.ncicb.cadsr.loader.event.ProgressListener;
import gov.nih.nci.ncicb.cadsr.loader.event.ProgressEvent;


import gov.nih.nci.ncicb.cadsr.loader.defaults.UMLDefaults;
import gov.nih.nci.ncicb.cadsr.loader.util.*;
import gov.nih.nci.ncicb.cadsr.loader.ext.*;
import gov.nih.nci.ncicb.cadsr.loader.UserSelections;
import gov.nih.nci.ncicb.cadsr.loader.persister.OCRDefinitionBuilder;

import org.apache.log4j.Logger;

import java.util.*;


/**
 * Roundtrip implementation for UML Models
 *
 * @author <a href="mailto:chris.ludet@oracle.com">Christophe Ludet</a>
 */
public class UMLRoundtrip implements Roundtrip, CadsrModuleListener {

  private static Logger logger = Logger.getLogger(UMLRoundtrip.class.getName());

  protected ElementsLists elements = ElementsLists.getInstance();

//  private Map<String, ValueDomain> valueDomains = new HashMap<String, ValueDomain>();

  protected UMLDefaults defaults = UMLDefaults.getInstance();

  private ClassificationScheme projectCs = null;
  private ProgressListener progressListener = null;

  private CadsrModule cadsrModule;

  public UMLRoundtrip() {
  }

  public void setProgressListener(ProgressListener l) {
    progressListener = l;
  }

  public void start() throws RoundtripException {
    List<DataElement> des = elements.getElements(DomainObjectFactory.newDataElement());
    List<ObjectClass> ocs = elements.getElements(DomainObjectFactory.newObjectClass());
    List<ObjectClassRelationship> ocrs = elements.getElements(DomainObjectFactory.newObjectClassRelationship());
//    List<ValueDomain> vds = elements.getElements(DomainObjectFactory.newValueDomain());
    List<ClassificationSchemeItem> packages = elements.getElements(DomainObjectFactory.newClassificationSchemeItem());


    ProgressEvent pEvt = new ProgressEvent();
    pEvt.setGoal(des.size() + ocs.size() + ocrs.size() + packages.size() + 2);
    pEvt.setMessage("Looking up Project");
    if(progressListener != null) 
      progressListener.newProgressEvent(pEvt);

    pEvt = new ProgressEvent();
    pEvt.setStatus(1);
    pEvt.setMessage("Looking up CDEs");
    if(progressListener != null) 
      progressListener.newProgressEvent(pEvt);

    
    // cache package / csCsi
    Map<String, ClassSchemeClassSchemeItem> csCsiCache = 
      new HashMap<String, ClassSchemeClassSchemeItem>();

    for(DataElement de : des) {
      ObjectClass oc = de.getDataElementConcept().getObjectClass();

      pEvt.setMessage("Looking up CDEs");
      pEvt.setStatus(pEvt.getStatus() + 1);
      if(progressListener != null) 
        progressListener.newProgressEvent(pEvt);

      String className = LookupUtil.lookupFullName(oc);

      int ind = className.lastIndexOf(".");
      if(ind < 0)
        continue;
      String packageName = className.substring(0, ind);
      className = className.substring(ind + 1);

      ClassSchemeClassSchemeItem csCsi = csCsiCache.get(packageName);
      if(csCsi == null) {
        csCsi = lookupCsCsi(packageName);
      }
      
      if(csCsi != null) {  
        AlternateName altName = DomainObjectFactory.newAlternateName();
        altName.setName(de.getDataElementConcept().getLongName());
        altName.setType(AlternateName.TYPE_UML_DE);
        
        try {
          Collection<DataElement> l = cadsrModule.findDEByClassifiedAltName(altName, csCsi);

          
          DataElement newDe = null;
          if(l.size() > 0) 
            newDe = l.iterator().next();
          
          if(newDe != null) {
            logger.debug("Found Matching DE " + altName.getName());
            de.setPublicId(newDe.getPublicId());
            de.setVersion(newDe.getVersion());

            AlternateName gmeAn = null;
            // find GME alt Name
            List<AlternateName> _ans = cadsrModule.getAlternateNames(newDe);
            loopAn:
            for(AlternateName _an : _ans) {
              if(_an.getType().equals(AlternateName.TYPE_GME_XML_LOC_REF)) {
                for(ClassSchemeClassSchemeItem _csCsi : _an.getCsCsis()) {
                  if(_csCsi.getId().equals(csCsi.getId())) {
                    gmeAn = _an;
                    break loopAn;
                  }
                }
              }
            }

            if(gmeAn != null)
              de.addAlternateName(gmeAn);

          } else
            logger.debug("NO DE MATCH " + altName.getName());
        } catch (Exception e){
          e.printStackTrace();
          logger.error("Cannot connect to Cadsr Public API: " + e.getMessage());
        } // end of try-catch
      }
    }

    boolean excludeNamespaces = (Boolean)UserSelections.getInstance().getProperty("ROUNDTRIP_EXCLUDE_NAMESPACES");

    for(ObjectClass oc : ocs) {
      pEvt.setMessage("Looking up Object Classes");
      pEvt.setStatus(pEvt.getStatus() + 1);
      if(progressListener != null) 
        progressListener.newProgressEvent(pEvt);

      String className = LookupUtil.lookupFullName(oc);

      int ind = className.lastIndexOf(".");
      if(ind < 0)
        continue;
      String packageName = className.substring(0, ind);
//       className = className.substring(ind + 1);

      ClassSchemeClassSchemeItem csCsi = csCsiCache.get(packageName);
      if(csCsi == null) {
        csCsi = lookupCsCsi(packageName);
      }
      
      if(csCsi != null) {  
        AlternateName altName = DomainObjectFactory.newAlternateName();
        altName.setName(className);
        altName.setType(AlternateName.TYPE_CLASS_FULL_NAME);
        
        try {
          Collection<ObjectClass> l = cadsrModule.findOCByClassifiedAltName(altName, csCsi);
          
          ObjectClass newOc = null;
          if(l.size() > 0) 
            newOc = l.iterator().next();
          
          if(newOc != null) {
            logger.debug("Found Matching OC " + altName.getName());
            oc.setPublicId(newOc.getPublicId());
            oc.setVersion(newOc.getVersion());
            oc.setId(newOc.getId());

            AlternateName gmeNsAn = null, gmeEltAn = null;
            // find GME alt Name
            List<AlternateName> _ans = cadsrModule.getAlternateNames(newOc);
            for(AlternateName _an : _ans) {
              if (gmeNsAn == null || gmeEltAn == null)
                if(_an.getType().equals(AlternateName.TYPE_GME_NAMESPACE)) {
                  for(ClassSchemeClassSchemeItem _csCsi : _an.getCsCsis()) {
                    if(_csCsi.getId().equals(csCsi.getId())) {
                      gmeNsAn = _an;
                    }
                  }
                }
                else if(_an.getType().equals(AlternateName.TYPE_GME_XML_ELEMENT)) {
                  for(ClassSchemeClassSchemeItem _csCsi : _an.getCsCsis()) {
                    if(_csCsi.getId().equals(csCsi.getId())) {
                      gmeEltAn = _an;
                    }
                  }
                }
              }
            
            if(!excludeNamespaces) {
              if(gmeNsAn != null)
                oc.addAlternateName(gmeNsAn);
            }
            
            if(gmeEltAn != null)
              oc.addAlternateName(gmeEltAn);
            
          } else
            logger.debug("NO OC MATCH " + className);
        } catch (Exception e){
          e.printStackTrace();
          logger.error("Cannot connect to Cadsr Public API: " + e.getMessage());
        } // end of try-catch
      }
    }

    if (ocrs != null) {
      for (ObjectClassRelationship ocr : ocrs) {
        pEvt.setMessage("Looking up Object Classes Relationship");
        pEvt.setStatus(pEvt.getStatus() + 1);
        if(progressListener != null) 
          progressListener.newProgressEvent(pEvt);

        if(ocr.getType().equals(ObjectClassRelationship.TYPE_HAS)) {
          
          String sourcePackage = LookupUtil.getPackageName(ocr.getSource());
          //        String targetPackage = LookupUtil.getPackageName(ocr.getTarget());
          
          ClassSchemeClassSchemeItem csCsi = csCsiCache.get(sourcePackage);
          if(csCsi == null) {
            csCsi = lookupCsCsi(sourcePackage);
          }
          
          ocr.setPreferredDefinition(new OCRDefinitionBuilder().buildDefinition(ocr));
          
          if ((ocr.getLongName() == null) ||
              (ocr.getLongName().length() == 0)) {
            logger.debug("No Role name for association. Generating one");
            ocr.setLongName(new OCRRoleNameBuilder().buildRoleName(ocr));
          }
          
          ObjectClass sOcr = ocr.getSource();
          socr:
          for(AlternateName an : sOcr.getAlternateNames()) {
            if(an.getType().equals(AlternateName.TYPE_CLASS_FULL_NAME)) {
              ocr.setSource(LookupUtil.lookupObjectClass(an));
              break socr;
            }
          }
          ObjectClass tOcr = ocr.getTarget();
          tocr:
          for(AlternateName an : tOcr.getAlternateNames()) {
            if(an.getType().equals(AlternateName.TYPE_CLASS_FULL_NAME)) {
              ocr.setTarget(LookupUtil.lookupObjectClass(an));
              break tocr;
            }
          }
          
          // check if association already exists
          ObjectClassRelationship ocr2 = DomainObjectFactory.newObjectClassRelationship();
          
          Map<String, Object> queryFields = new HashMap<String, Object>();
          //         queryFields.put();
          
          ocr2.setSource(ocr.getSource());
          ocr2.setSourceRole(ocr.getSourceRole());
          ocr2.setTarget(ocr.getTarget());
          ocr2.setTargetRole(ocr.getTargetRole());
          ocr2.setDirection(ocr.getDirection());
          ocr2.setSourceLowCardinality(ocr.getSourceLowCardinality());
          ocr2.setSourceHighCardinality(ocr.getSourceHighCardinality());
          ocr2.setTargetLowCardinality(ocr.getTargetLowCardinality());
          ocr2.setTargetHighCardinality(ocr.getTargetHighCardinality());
          
          List<ObjectClassRelationship> l = cadsrModule.findOCR(ocr2);
          
          if (l.size() > 0) {
            ocr2 = l.get(0);
            logger.debug("Found Matching OCR " + ocr2.getLongName());
            ocr.setPublicId(ocr2.getPublicId());
            ocr.setVersion(ocr2.getVersion());
            
            AlternateName gmeSrcAn = null, gmeTgtAn = null;
            // find GME alt Name
            List<AlternateName> _ans = cadsrModule.getAlternateNames(ocr2);
              for(AlternateName _an : _ans) {
                if (gmeSrcAn == null || gmeTgtAn == null)
                  if(_an.getType().equals(AlternateName.TYPE_GME_SRC_XML_LOC_REF)) {
                    for(ClassSchemeClassSchemeItem _csCsi : _an.getCsCsis()) {
                      if(_csCsi.getId().equals(csCsi.getId())) {
                        gmeSrcAn = _an;
                      }
                    }
                  }
                  else if(_an.getType().equals(AlternateName.TYPE_GME_TARGET_XML_LOC_REF)) {
                    for(ClassSchemeClassSchemeItem _csCsi : _an.getCsCsis()) {
                      if(_csCsi.getId().equals(csCsi.getId())) {
                        gmeTgtAn = _an;
                      }
                    }
                  }
              }
            
            if(gmeSrcAn != null)
              ocr.addAlternateName(gmeSrcAn);
            
            if(gmeTgtAn != null)
              ocr.addAlternateName(gmeTgtAn);
            
            
          } else {
            logger.debug("NO OCR MATCH " + ocr2.getLongName());
          }
        }
      }

    }

    if(!excludeNamespaces) {
      for(ClassificationSchemeItem csi : packages) {
        pEvt.setMessage("Looking up CSIs");
        pEvt.setStatus(pEvt.getStatus() + 1);
        if(progressListener != null) 
          progressListener.newProgressEvent(pEvt);
        
        String packageName = csi.getLongName();
        ClassSchemeClassSchemeItem csCsi = csCsiCache.get(packageName);
        if(csCsi == null) {
          csCsi = lookupCsCsi(packageName);
        }
        
        AlternateName gmeNsAn = null;
        // find GME alt Name
        List<AlternateName> _ans = cadsrModule.getAlternateNames(csCsi.getCsi());
        for(AlternateName _an : _ans) {
          if (gmeNsAn == null)
            if(_an.getType().equals(AlternateName.TYPE_GME_NAMESPACE)) {
              for(ClassSchemeClassSchemeItem _csCsi : _an.getCsCsis()) {
                if(_csCsi.getId().equals(csCsi.getId())) {
                  gmeNsAn = _an;
                }
              }
            }
        }
        
        if(gmeNsAn != null) {
          logger.debug("Found Matching CSI's gme Name " + packageName);
          csi.addAlternateName(gmeNsAn);
        } else {
          logger.debug("CSI -- No Match " + packageName);
        }
      }
    }
  }

  private ClassSchemeClassSchemeItem lookupCsCsi(String packageName) {
    List<ClassSchemeClassSchemeItem> csCsis = projectCs.getCsCsis();
    ClassSchemeClassSchemeItem packageCsCsi = null;

    for(ClassSchemeClassSchemeItem csCsi : csCsis) {
      try {
        if(csCsi.getCsi().getLongName().equals(packageName)
           || (csCsi.getCsi().getComments() != null && csCsi.getCsi().getComments().equals(packageName))
           )
          packageCsCsi = csCsi;
      } catch (NullPointerException e){
        e.printStackTrace();
      } // end of try-catch
    }
    
    return packageCsCsi;

  }

  public void setClassificationScheme(ClassificationScheme cs) {
    this.projectCs = cs;
  }

  /**
   * IoC setter
   */
  public void setCadsrModule(CadsrModule module) {
    cadsrModule = module;
  }

}
