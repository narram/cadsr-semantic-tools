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
package gov.nih.nci.ncicb.cadsr.loader.validator;

import java.util.*;

import gov.nih.nci.ncicb.cadsr.domain.*;

import gov.nih.nci.ncicb.cadsr.loader.ElementsLists;
import gov.nih.nci.ncicb.cadsr.loader.event.ProgressListener;

import gov.nih.nci.ncicb.cadsr.loader.util.*;

public class ConceptCodeValidator implements Validator {

  private ElementsLists elements = ElementsLists.getInstance();

  private ValidationItems items = ValidationItems.getInstance();

  private InheritedAttributeList inheritedList = InheritedAttributeList.getInstance();

  public ConceptCodeValidator() {
  }

  public void addProgressListener(ProgressListener l) {

  }


  /**
   * returns a list of Validation errors.
   */
  public ValidationItems validate() {
    List<ObjectClass> ocs = elements.getElements(DomainObjectFactory.newObjectClass());
    if(ocs != null)
      for(ObjectClass o : ocs) {
        if(StringUtil.isEmpty(o.getPublicId()) || o.getVersion() == null) {
          if(StringUtil.isEmpty(o.getPreferredName()))
            items.addItem(new ValidationConceptError("Class: " + o.getLongName() + " has no concept code.", o));
          else {
            checkConcepts(o);
          }
        }
      }    


    List<DataElement> des = elements.getElements(DomainObjectFactory.newDataElement());
    if(des != null)
      for(DataElement de : des ) {
        if(!inheritedList.isInherited(de)) {
          if(StringUtil.isEmpty(de.getPublicId()) || de.getVersion() == null) {
            // no existing DE mapping -- check for concepts
            Property prop = de.getDataElementConcept().getProperty();
            if(StringUtil.isEmpty(prop.getPublicId()) || prop.getVersion() == null) {
              if(StringUtil.isEmpty(prop.getPreferredName()))
                items.addItem(new ValidationConceptError("Attribute: " + prop.getLongName() + " has no concept code.", prop));
              else {
                checkConcepts(prop);
              }
            }
          }
        }
      }

    List<ValueDomain> vds = elements.getElements(DomainObjectFactory.newValueDomain());
    if(vds != null)
      for(ValueDomain vd: vds) {
        if(vd.getConceptDerivationRule() != null)
          if(vd.getConceptDerivationRule().getComponentConcepts().size() == 0)
            items.addItem
              (new ValidationConceptWarning
               (PropertyAccessor.getProperty
                ("vd.missing.concept", vd.getLongName()), vd));
          else {
            checkConcepts(vd);
          }
      }


    List<ValueMeaning> vms = elements.getElements(DomainObjectFactory.newValueMeaning());
    if(vms != null)
      for(ValueMeaning vm : vms) {
        if(vm.getConceptDerivationRule().getComponentConcepts().size() == 0)
          items.addItem
            (new ValidationConceptWarning
             (PropertyAccessor.getProperty
              ("vm.missing.concept", vm.getLongName()), vm));
        else {
          checkConcepts(vm);
        }
      }


    List<ObjectClassRelationship> ocrs = elements.getElements(DomainObjectFactory.newObjectClassRelationship());
    if(ocrs != null)
      for(ObjectClassRelationship ocr : ocrs) {
        checkConcepts(ocr, ocr.getConceptDerivationRule());
        checkConcepts(ocr, ocr.getSourceRoleConceptDerivationRule());
        checkConcepts(ocr, ocr.getTargetRoleConceptDerivationRule());
      }

    return items;

  }

  private void checkConcepts(AdminComponent ac) {
    String[] conStr = ac.getPreferredName().split(":");
    for(String s : conStr) {
      Concept con = LookupUtil.lookupConcept(s);
      if(StringUtil.isEmpty(con.getLongName()))
        items.addItem(new ValidationConceptError(PropertyAccessor.getProperty("validation.concept.missing.longName", con.getPreferredName()), ac));
      if(StringUtil.isEmpty(con.getPreferredDefinition())) {
        items.addItem(new ValidationConceptError(PropertyAccessor.getProperty("validation.concept.missing.definition", con.getPreferredName()), ac));
      } else if (con.getPreferredDefinition().length() > 2000){
        items.addItem(new ValidationError(PropertyAccessor.getProperty("validation.concept.definition.too.long", con.getPreferredName()), ac));
        con.setPreferredDefinition(con.getPreferredDefinition().substring(0, 2000));
      }
      if(StringUtil.isEmpty(con.getDefinitionSource()))
        items.addItem(new ValidationConceptError(PropertyAccessor.getProperty("validation.concept.missing.source", con.getPreferredName()), ac));
    }
  }

  private void checkConcepts(ValueMeaning vm) {
    checkConcepts(vm, vm.getConceptDerivationRule());
  }

  private void checkConcepts(ValueDomain vd) {
    checkConcepts(vd, vd.getConceptDerivationRule());
  }

  private void checkConcepts(AdminComponent ac, ConceptDerivationRule condr) {
    if(condr == null)
      return;
    if(condr.getComponentConcepts() == null)
      return;
                                                
    for(ComponentConcept compCon : condr.getComponentConcepts()) {
      Concept con = compCon.getConcept();
      if(StringUtil.isEmpty(con.getLongName()))
        items.addItem(new ValidationConceptError(PropertyAccessor.getProperty("validation.concept.missing.longName", con.getPreferredName()), ac));
      if(StringUtil.isEmpty(con.getPreferredDefinition())) {
        items.addItem(new ValidationConceptError(PropertyAccessor.getProperty("validation.concept.missing.definition", con.getPreferredName()), ac));
      } else if (con.getPreferredDefinition().length() > 2000){
        items.addItem(new ValidationError(PropertyAccessor.getProperty("validation.concept.definition.too.long", con.getPreferredName()), ac));
        con.setPreferredDefinition(con.getPreferredDefinition().substring(0, 2000));
      }
      if(StringUtil.isEmpty(con.getDefinitionSource()))
        items.addItem(new ValidationConceptError(PropertyAccessor.getProperty("validation.concept.missing.source", con.getPreferredName()), ac));
    }
  }


}