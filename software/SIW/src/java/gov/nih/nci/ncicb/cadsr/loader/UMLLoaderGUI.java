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

package gov.nih.nci.ncicb.cadsr.loader;

import gov.nih.nci.ncicb.cadsr.loader.ui.*;

import java.awt.Component;
import java.awt.Frame;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.*;
import java.awt.Dimension;
import java.awt.Toolkit;

import java.util.*;

import gov.nih.nci.ncicb.cadsr.loader.event.*;
import gov.nih.nci.ncicb.cadsr.loader.util.*;

import java.awt.event.ComponentAdapter;

import java.awt.event.ComponentEvent;

import org.apache.log4j.*;

/** 
 * The main starting point for the UI. 
 * <br>
 *
 * @author <a href="mailto:chris.ludet@oracle.com">Christophe Ludet</a>
 * 
 */
public class UMLLoaderGUI 
{

  private static Logger logger = Logger.getLogger(UMLLoaderGUI.class.getName());
  private Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

  private UserSelections userSelections = UserSelections.getInstance();

  private RoundtripPanelDescriptor roundtripDesc;
  private GMEDefaultsPanelDescriptor gmeDefaultsDesc;

  private List<WizardPanelDescriptor> descriptors;

  private MainFrame mainFrame;

  private Appender appender;

  private List<RunModeListener> runModeListeners = new ArrayList<RunModeListener>();

  private UserPreferences prefs;
  
  public UMLLoaderGUI()
  {
  }

  public void start() {
    InitClass initClass = new InitClass(this);
    Thread t = new Thread(initClass);
    t.setPriority(Thread.MIN_PRIORITY);
//     t.start();

    logger.getParent().addAppender(appender);

    System.setProperty("java.security.auth.login.config", Thread.currentThread().getContextClassLoader().getResource("jaas.config").toExternalForm());

    Frame f = new Frame();
    Wizard wizard = new Wizard(f);

    wizard.getDialog().setTitle(PropertyAccessor.getProperty("siw.title"));
    
    WizardPanelDescriptor modeSelDesc = new ModeSelectionPanelDescriptor();
    wizard.registerWizardPanel(ModeSelectionPanelDescriptor.IDENTIFIER, modeSelDesc);

    WizardPanelDescriptor packageDesc = new PackageFilterSelectionPanelDescriptor();
    wizard.registerWizardPanel(PackageFilterSelectionPanelDescriptor.IDENTIFIER, packageDesc);

    WizardPanelDescriptor packageClassFilterDesc = new PackageClassFilterPanelDescriptor();
    wizard.registerWizardPanel(PackageClassFilterPanelDescriptor.IDENTIFIER, packageClassFilterDesc);


    WizardPanelDescriptor descriptor2 = new FileSelectionPanelDescriptor();
    wizard.registerWizardPanel(FileSelectionPanelDescriptor.IDENTIFIER, descriptor2);

//     WizardPanelDescriptor fpDescForRoundtrip = new FileSelectionPanelDescriptor(FileSelectionPanelDescriptor.MODE_ROUNDTRIP);
//     wizard.registerWizardPanel(FileSelectionPanelDescriptor.ROUNDTRIP_IDENTIFIER, fpDescForRoundtrip);

//     WizardPanelDescriptor fpDescForPackageFilter = new FileSelectionPanelDescriptor(FileSelectionPanelDescriptor.MODE_SC);
//     wizard.registerWizardPanel(FileSelectionPanelDescriptor.PACKAGE_FILTER_IDENTIFIER, fpDescForPackageFilter);


//     wizard.registerWizardPanel(RoundtripPanelDescriptor.IDENTIFIER, roundtripDesc);

//     wizard.registerWizardPanel(GMEDefaultsPanelDescriptor.IDENTIFIER, gmeDefaultsDesc);

    for(WizardPanelDescriptor _desc : descriptors) {
      wizard.registerWizardPanel(_desc.getPanelDescriptorIdentifier(), _desc);
    }

    WizardPanelDescriptor fileProgress = new ProgressFileSelectionPanelDescriptor();
    wizard.registerWizardPanel(ProgressFileSelectionPanelDescriptor.IDENTIFIER, fileProgress);


    WizardPanelDescriptor reportConfirmDesc = new ReportConfirmPanelDescriptor();
    wizard.registerWizardPanel(ReportConfirmPanelDescriptor.IDENTIFIER, reportConfirmDesc);


    WizardPanelDescriptor validationDesc = new ValidationPanelDescriptor();
    wizard.registerWizardPanel(ValidationPanelDescriptor.IDENTIFIER, validationDesc);

    WizardPanelDescriptor descriptor3 = new SemanticConnectorPanelDescriptor();
    wizard.registerWizardPanel(SemanticConnectorPanelDescriptor.IDENTIFIER, descriptor3);


    WizardPanelDescriptor descriptor4 = new ProgressSemanticConnectorPanelDescriptor();
    wizard.registerWizardPanel(ProgressSemanticConnectorPanelDescriptor.IDENTIFIER, descriptor4);
    
    wizard.setCurrentPanel(ModeSelectionPanelDescriptor.IDENTIFIER);
    int wizResult = wizard.showModalDialog();

    if(wizResult != 0) {
      System.exit(0);
    }
      
    RunMode mode = (RunMode)userSelections.getProperty("MODE");

    if(!(mode.equals(RunMode.Reviewer) || mode.equals(RunMode.Curator) || mode.equals(RunMode.UnannotatedXmi))) 
      System.exit(0);

    mainFrame.init();
    putToCenter(mainFrame);
    mainFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
    mainFrame.addWindowListener(new WindowAdapter(){
        public void windowClosing(WindowEvent e){
         mainFrame.exit();
        }
      });
    mainFrame.addWindowStateListener(new WindowAdapter(){
        public void windowStateChanged(WindowEvent e) {
          mainFrame.stateChanged(e);
        }
      });
    mainFrame.addComponentListener(new ComponentAdapter() {
        public void componentResized(ComponentEvent evt) {
          mainFrame.componentResized(evt);
        }
      });
    
    mainFrame.setWorkingTitle(null);

    mainFrame.setVisible(true);

  }

  public void setMainFrame(MainFrame frame) {
    this.mainFrame = frame;
  }

  public void setCustomAppender(Appender a) {
    this.appender = a;
  }

//   /**
//    * 
//    * @param args
//    */
//   public static void main(String[] args)
//   {
//     try
//     {
//       UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
//     }
//     catch(Exception e)
//     {
//       e.printStackTrace();
//     }

//     new UMLLoaderGUI();
//   }

//   public void setRoundtripDescriptor(RoundtripPanelDescriptor desc) {
//     roundtripDesc = desc;
//   }

//   public void setGmeDefaulsDescriptor(GMEDefaultsPanelDescriptor desc) {
//     gmeDefaultsDesc = desc;
//   }

  public void setPanelDescriptors(List<WizardPanelDescriptor> descriptors) {
    this.descriptors = descriptors;
  }

  public void setUserPreferences(UserPreferences preferences) {
    prefs = preferences;
  }
  
  private void putToCenter(Component comp) {
    comp.setLocation((screenSize.width - comp.getSize().width) / 2, (screenSize.height - comp.getSize().height) / 2);
  }

}