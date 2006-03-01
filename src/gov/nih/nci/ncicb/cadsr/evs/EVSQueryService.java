/*
 * Copyright 2000-2005 Oracle, Inc. This software was developed in conjunction with the National Cancer Institute, and so to the extent government employees are co-authors, any rights in such works shall be subject to Title 17 of the United States Code, section 105.
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
package gov.nih.nci.ncicb.cadsr.evs;

import gov.nih.nci.evs.domain.*;
import gov.nih.nci.evs.query.*;
import gov.nih.nci.ncicb.cadsr.evs.EVSConcept;
import gov.nih.nci.system.applicationservice.*;
import gov.nih.nci.system.applicationservice.ApplicationService;

import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Expression;

import java.io.IOException;

import java.util.*;


public class EVSQueryService {
  private static String NCI_THESAURUS_VOCAB_NAME = "NCI_Thesaurus";
  private static String SYNONYM_PROPERTY_NAME = "Synonym";
  private static String PREFERRED_NAME_PROP = "PREFERRED_NAME";
  private static String DEFINITION_PROPERTY_NAME = "DEFINITION";
  private ApplicationService evsService;
  private String cacoreServiceURL;

  public EVSQueryService(String cacoreServiceURL) {
    this.cacoreServiceURL = cacoreServiceURL;
    evsService = ApplicationService.getRemoteInstance(cacoreServiceURL);
  }

  public void setCacoreServiceURL(String cacoreServiceURL) {
    this.cacoreServiceURL = cacoreServiceURL;
  }


  /* CL: Redo. No time atm. */
  /**
   * returns by list of concepts by preferredName
   * <br> Usually, it means one concept, not always...
   */
  public List<EVSConcept> findConceptsByPreferredName(
    String searchTerm,
    boolean includeRetiredConcepts) throws Exception {
    
    // CL: this is bad. replace by true query. who guaranties the result is in the 1st 500 rows? 
    List<EVSConcept> consBySyn = findConceptsBySynonym(searchTerm, includeRetiredConcepts, 500);
    
    List<EVSConcept> results = new ArrayList<EVSConcept>();
    for(EVSConcept con : consBySyn) {
      if(con.getPreferredName().equalsIgnoreCase(searchTerm))
        results.add(con);
    }

    return results;
  }

  public List<EVSConcept> findConceptsBySynonym(
    String searchTerm,
    boolean includeRetiredConcepts,
    int rowCount) throws Exception {
    if (cacoreServiceURL == null) {
      throw new Exception("Please specify a valid caCORE Service URL");
    }

    EVSQuery query = new EVSQueryImpl();
    query.getConceptWithPropertyMatching(
      NCI_THESAURUS_VOCAB_NAME, SYNONYM_PROPERTY_NAME, searchTerm, rowCount);

    List conceptNames = evsService.evsSearch(query);

    return this.findConceptDetailsByName(conceptNames, includeRetiredConcepts);

  }

  public List<EVSConcept> findConceptDetailsByName(
    List<String> conceptNames,
    boolean includeRetiredConcepts) throws Exception {
    List<EVSConcept> results = new ArrayList<EVSConcept>();

    for (String conceptName : conceptNames) {
      DescLogicConcept concept =
        this.findDescLogicConceptByName(NCI_THESAURUS_VOCAB_NAME, conceptName);

      if (
        (includeRetiredConcepts) ||
            (!includeRetiredConcepts && !concept.isRetired().booleanValue())) {
        List synonyms = this.retrieveSynonyms(concept);
        List defs = this.retrieveDefinitions(concept);

        EVSConcept c = new EVSConcept();
        c.setCode(concept.getCode());
        c.setPreferredName(retrievePreferredName(concept));
        c.setName(conceptName);
        c.setDefinitions(defs);
        c.setSynonyms(synonyms);

        results.add(c);
      }
    }

    return results;
  }

  public List findConceptsByCode(
    String conceptCode,
    boolean includeRetiredConcepts,
    int rowCount) throws Exception {
    if (cacoreServiceURL == null) {
      throw new Exception("Please specify a valid caCORE Service URL");
    }

    List results = new ArrayList();
    EVSQuery query = new EVSQueryImpl();
    query.getConceptNameByCode(NCI_THESAURUS_VOCAB_NAME, conceptCode);
    List conceptNames = evsService.evsSearch(query);

    results =
      this.findConceptDetailsByName(conceptNames, includeRetiredConcepts);

    return results;
  }

  private DescLogicConcept findDescLogicConceptByName(
    String vocabName,
    String conceptName) throws Exception {
    EVSQuery query = new EVSQueryImpl();
    query.getConceptByName(vocabName, conceptName);

    List results = evsService.evsSearch(query);

    return (DescLogicConcept) results.get(0);
  }

  private List retrieveSynonyms(DescLogicConcept dlc) {
    List synonyms = new ArrayList();
    Vector propVect = dlc.getPropertyCollection();
    for (int x = 0; x < propVect.size(); x++) {
      Property p = (Property) propVect.get(x);
      if (p.getName().equalsIgnoreCase(SYNONYM_PROPERTY_NAME)) {
        synonyms.add(p.getValue());
      }
    }

    return synonyms;
  }

  private String retrievePreferredName(DescLogicConcept dlc) {
    Collection propVect = dlc.getPropertyCollection();
    for (Iterator it = propVect.iterator(); it.hasNext();) {
      Property p = (Property) it.next();
      if (p.getName().equalsIgnoreCase(PREFERRED_NAME_PROP)) {
        return p.getValue();
      }
    }
    return null;
  }

  private List retrieveDefinitions(DescLogicConcept dlc) {
    List definitions = new ArrayList();
    Vector propVect = dlc.getPropertyCollection();
    for (int x = 0; x < propVect.size(); x++) {
      Property p = (Property) propVect.get(x);
      if (p.getName().equalsIgnoreCase(DEFINITION_PROPERTY_NAME)) {
        //definitions.add(p.getValue());
        String definition = this.retrieveDefinitionValue(p.getValue());
        String definitionSource = this.retrieveDefinitionSource(p.getValue());
        Definition def = new Definition();
        Source src = new Source();
        def.setDefinition(definition);
        src.setAbbreviation(definitionSource);
        def.setSource(src);
        definitions.add(def);
      }
    }

    return definitions;
  }

  private String retrieveDefinitionSource(String termStr) {
    String source = "";

    int length = 0; //<def-source>,  <def-definition>
    length = termStr.length();
    int iStartDefSource = 0;
    int iEndDefSource = 0;

    if (length > 0) {
      iStartDefSource = termStr.lastIndexOf("<def-source>");
      iStartDefSource = iStartDefSource + ("<def-source>").length();
      iEndDefSource = termStr.indexOf("</def-source>");
      if ((iStartDefSource > 1) && (iEndDefSource > 1)) {
        source = termStr.substring(iStartDefSource, iEndDefSource);
      }
    }

    return source;
  }

  private String retrieveDefinitionValue(String termStr) {
    String definition = "";

    int length = 0; //<def-source>,  <def-definition>
    length = termStr.length();
    int iStartDef = 0;
    int iEndDef = 0;

    if (length > 0) {
      iStartDef = termStr.lastIndexOf("<def-definition>");
      iStartDef = iStartDef + ("<def-definition>").length();
      iEndDef = termStr.indexOf("</def-definition>");

      if ((iStartDef > 1) && (iEndDef > 1)) {
        definition = termStr.substring(iStartDef, iEndDef);
      }
    }

    return definition;
  }

  /**
   * @param args
   */
  public static void main(String[] args) {
    EVSQueryService testAction = new EVSQueryService("http://cabio.nci.nih.gov/cacore30/server/HTTPServer");
    try {
      //testAction.findConceptsBySynonym("gene", 100);
//       testAction.findConceptsByCode("C41095", true, 100);

      List<EVSConcept> cons = testAction.findConceptsByPreferredName("name", false);

      for(EVSConcept con : cons) {
        System.out.println(con.getPreferredName());
      }

//       ApplicationService evsService = ApplicationService.getRemoteInstance("http://cabio.nci.nih.gov/cacore30/server/HTTPServer");

//       gov.nih.nci.evs.domain.DescLogicConcept concept = new gov.nih.nci.evs.domain.DescLogicConcept();
//       DetachedCriteria criteria = DetachedCriteria.forClass(gov.nih.nci.evs.domain.DescLogicConcept.class, "concept");

//       criteria.add(Expression.eq("name", "Name"));
      
//       List<DescLogicConcept> l = evsService.query(criteria, gov.nih.nci.evs.domain.DescLogicConcept.class.getName());

//       for(DescLogicConcept con : l) {
//         System.out.println(con.getName());
//       }

    }
    catch (Exception e) {
      e.printStackTrace();
    }
  }
}