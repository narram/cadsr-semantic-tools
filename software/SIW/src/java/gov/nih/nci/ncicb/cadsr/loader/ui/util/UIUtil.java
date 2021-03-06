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
package gov.nih.nci.ncicb.cadsr.loader.ui.util;

import gov.nih.nci.ncicb.cadsr.domain.DataElement;
import gov.nih.nci.ncicb.cadsr.domain.Definition;
import gov.nih.nci.ncicb.cadsr.domain.DomainObjectFactory;
import gov.nih.nci.ncicb.cadsr.domain.ObjectClass;
import gov.nih.nci.ncicb.cadsr.domain.ValueMeaning;
import gov.nih.nci.ncicb.cadsr.loader.ui.tree.*;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.Insets;

import java.util.List;

import javax.swing.*;


public class UIUtil {

  private static Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
  private static String panelTitle = null;
  
  public static void putToCenter(Component comp) {
    comp.setLocation((screenSize.width - comp.getSize().width) / 2, (screenSize.height - comp.getSize().height) / 2);
  }
  
  public static JTextArea createDescription(UMLNode node) {
    setPanelTitle(node);
    JTextArea descriptionArea = new JTextArea(5, 54);

    if(node instanceof ClassNode) {
      ObjectClass oc = (ObjectClass) node.getUserObject();
      descriptionArea.setText(oc.getPreferredDefinition());
    } else if(node instanceof AttributeNode) {
        DataElement de = (DataElement) node.getUserObject();
        for(Definition def : (List<Definition>) de.getDefinitions()) {
            descriptionArea.setText(def.getDefinition());
            break;
        }
        if(de.getDefinitions().size() == 0){
            Definition altDef = DomainObjectFactory.newDefinition();
            altDef.setType(Definition.TYPE_UML_DE);
            altDef.setDefinition("");
            de.addDefinition(altDef);
        }
    } else if(node instanceof ValueMeaningNode) {
        ValueMeaning vm = (ValueMeaning)node.getUserObject();
        for(Definition def :  vm.getDefinitions()) {
            descriptionArea.setText(def.getDefinition());
            break;
        }
        if(vm.getDefinitions().size() == 0){
            Definition altDef = DomainObjectFactory.newDefinition();
            altDef.setType(Definition.TYPE_UML_VM);
            altDef.setDefinition("");
            vm.addDefinition(altDef);
        }
    }

    descriptionArea.setLineWrap(true);
    descriptionArea.setEditable(true);
    
    return descriptionArea;
 
  }
  
  private static void setPanelTitle(UMLNode node){
    panelTitle = "UML Class Documentation";
    if(node instanceof AttributeNode) {
        panelTitle = "UML Attribute Description";
    } else if(node instanceof ValueMeaningNode) {
        panelTitle = "UML ValueMeaning Description";
    }
  }
  
  public static String getPanelTitle(){
      return panelTitle;
  }

  public static void insertInBag(JPanel bagComp, Component comp, int x, int y) {
    insertInBag(bagComp, comp, x, y, 1, 1);
  }

  public static void insertInBag(JPanel bagComp, Component comp, int x, int y, int width, int height) {
    JPanel p = new JPanel();
    p.add(comp);

    bagComp.add(p, new GridBagConstraints(x, y, width, height, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
  }

  public static void insertInBag(JPanel bagComp, Component comp, int x, int y, int width, int height, int anchor) {
    JPanel p = new JPanel();
    p.add(comp);

    bagComp.add(p, new GridBagConstraints(x, y, width, height, 0.0, 0.0, anchor, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
  }

  
  
}