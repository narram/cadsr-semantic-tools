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
package gov.nih.nci.ncicb.cadsr.loader.ui;

import gov.nih.nci.ncicb.cadsr.loader.util.BeansAccessor;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.*;

import javax.security.auth.login.*;

import gov.nih.nci.ncicb.cadsr.loader.*;
import gov.nih.nci.ncicb.cadsr.loader.event.*;
import gov.nih.nci.ncicb.cadsr.loader.parser.*;
import gov.nih.nci.ncicb.cadsr.loader.validator.*;

import gov.nih.nci.ncicb.cadsr.loader.ui.tree.TreeBuilder;
import gov.nih.nci.ncicb.cadsr.loader.util.*;
import gov.nih.nci.ncicb.cadsr.loader.defaults.UMLDefaults;

import gov.nih.nci.ncicb.cadsr.semconn.SemanticConnectorException;
import gov.nih.nci.ncicb.cadsr.semconn.*;

import java.io.File;

import org.apache.log4j.Logger;


/**
 * This class is responsible for reacting to events generated by pushing any of the
 * three buttons, 'Next', 'Previous', and 'Cancel.' Based on what button is pressed,
 * the controller will update the model to show a new panel and reset the state of
 * the buttons as necessary.
 */
public class WizardController implements ActionListener {
    
  private Wizard wizard;
  private String username;
  private String filename;
  private String outputFile;

  private RunMode mode;

  private Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    
  private static Logger logger = Logger.getLogger(WizardController.class.getName());

  private UserSelections userSelections = UserSelections.getInstance();

  private UserPreferences prefs = UserPreferences.getInstance();

  private java.util.List<RunModeListener> runModeListeners = new ArrayList<RunModeListener>();

  private Parser xmiParser = null, preParser = null;
  private RoundtripAction roundtripAction = null;
  private Validator validator = null;

  public WizardController() {}

  /**
   * @param w A callback to the Wizard component that created this controller.
   */
  public void setWizard(Wizard w) {
    wizard = w;
  }
  
    /**
     * Calling method for the action listener interface. This class listens for actions
     * performed by the buttons in the Wizard class, and calls methods below to determine
     * the correct course of action.
     * @param evt The ActionEvent that occurred.
     */    
    public void actionPerformed(java.awt.event.ActionEvent evt) {
      if (evt.getActionCommand().equals(Wizard.CANCEL_BUTTON_ACTION_COMMAND))
        cancelButtonPressed();
      else if (evt.getActionCommand().equals(Wizard.BACK_BUTTON_ACTION_COMMAND))
        backButtonPressed();
      else if (evt.getActionCommand().equals(Wizard.NEXT_BUTTON_ACTION_COMMAND))
        nextButtonPressed();
    }
    
    
    
    private void cancelButtonPressed() {
        
        wizard.close(Wizard.CANCEL_RETURN_CODE);
    }

    private void nextButtonPressed() {
      WizardModel model = wizard.getModel();
      WizardPanelDescriptor descriptor = model.getCurrentPanelDescriptor();
      
      Object nextPanelDescriptor = descriptor.getNextPanelDescriptor();

      if(descriptor.getPanelDescriptorIdentifier().equals(ModeSelectionPanelDescriptor.IDENTIFIER)) {
        ModeSelectionPanel panel = 
          (ModeSelectionPanel)descriptor.getPanelComponent();
        
        mode = Enum.valueOf(RunMode.class, panel.getSelection());
        
        fireNewRunMode(mode);
        userSelections.setProperty("MODE", mode);

        wizard.setTitle(PropertyAccessor.getProperty("siw.title") + " -- " + mode.getTitleName());
                
        prefs.setModeSelection(mode.toString());
        
        // right here, we decide which module to use
        prefs.setUsePrivateApi(panel.usePrivateApi());

        FileSelectionPanelDescriptor fileDesc =
          (FileSelectionPanelDescriptor)model
          .getPanelDescriptor(FileSelectionPanelDescriptor.IDENTIFIER);
        fileDesc.init();
        
        ProgressFileSelectionPanelDescriptor desc =
            (ProgressFileSelectionPanelDescriptor)model
            .getPanelDescriptor(ProgressFileSelectionPanelDescriptor.IDENTIFIER);


        ModeSelectionPanelDescriptor mspDesc =
            (ModeSelectionPanelDescriptor)model
            .getPanelDescriptor(ModeSelectionPanelDescriptor.IDENTIFIER);
        

        if(mode.equals(RunMode.GenerateReport)) {
          desc.setNextPanelDescriptor(ReportConfirmPanelDescriptor.IDENTIFIER);
        } else if(mode.equals(RunMode.AnnotateXMI)) {
          desc.setNextPanelDescriptor(ReportConfirmPanelDescriptor.IDENTIFIER);
        } else if(mode.equals(RunMode.Reviewer)) {
          desc.setNextPanelDescriptor("FINISH");
        } else if(mode.equals(RunMode.Curator)) {
          desc.setNextPanelDescriptor("FINISH");
        } else if(mode.equals(RunMode.Roundtrip)) {
          desc.setNextPanelDescriptor(ReportConfirmPanelDescriptor.IDENTIFIER);
          desc.setBackPanelDescriptor(RoundtripPanelDescriptor.IDENTIFIER);
        } else if(mode.equals(RunMode.FixEa)) {
          desc.setNextPanelDescriptor(ReportConfirmPanelDescriptor.IDENTIFIER);
          desc.setBackPanelDescriptor(ModeSelectionPanelDescriptor.IDENTIFIER);
        }
        
      }

      if(descriptor.getPanelDescriptorIdentifier().equals(PackageFilterSelectionPanelDescriptor.IDENTIFIER)) {
        PackageFilterSelectionPanel panel = 
            (PackageFilterSelectionPanel)descriptor.getPanelComponent();
        Map temp = panel.getPackageFilter();
        UMLDefaults defaults = UMLDefaults.getInstance();
        defaults.setPackageFilter(temp);
      }
      if(descriptor.getPanelDescriptorIdentifier().equals(RoundtripPanelDescriptor.IDENTIFIER)) {
        RoundtripPanel panel = 
          (RoundtripPanel)descriptor.getPanelComponent();
        userSelections.setProperty("PROJECT_NAME", panel.getProjectName());
        userSelections.setProperty("PROJECT_VERSION", new Float(panel.getProjectVersion()));
      }

      if(descriptor.getPanelDescriptorIdentifier().equals(FileSelectionPanelDescriptor.IDENTIFIER)) {
        FileSelectionPanel panel = 
            (FileSelectionPanel)descriptor.getPanelComponent();
        filename = panel.getSelection();
        userSelections.setProperty("SKIP_VD_VALIDATION", panel.getSkipVdValidation());
        
        prefs.addRecentFile(filename);
        
        userSelections.setProperty("FILENAME", filename);
        
        try {
          if(panel.getChoosePackage()) {
            userSelections.setProperty("FILTER_CLASS_AND_PACKAGES", new Boolean(true));
            
//             wizard.getModel().setNextButtonEnabled(false);
            wizard.getDialog().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            preParser.parse(filename);
            wizard.getDialog().setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
//             wizard.getModel().setNextButtonEnabled(true);

            wizard.setCurrentPanel(nextPanelDescriptor);
            PackageClassFilterPanelDescriptor filterDesc =
              (PackageClassFilterPanelDescriptor)model
              .getPanelDescriptor(PackageClassFilterPanelDescriptor.IDENTIFIER);
            filterDesc.init();
            return;
          }


        } catch (ParserException e) {
          logger.error("Could not pre-parse file: " + e.getMessage());
        }
        
      }

      if(descriptor.getPanelDescriptorIdentifier().equals(FileSelectionPanelDescriptor.IDENTIFIER) || descriptor.getPanelDescriptorIdentifier().equals(PackageClassFilterPanelDescriptor.IDENTIFIER)) {

        if(descriptor.getPanelDescriptorIdentifier().equals(FileSelectionPanelDescriptor.IDENTIFIER)) {
          FileSelectionPanel panel = 
            (FileSelectionPanel)descriptor.getPanelComponent();
          filename = panel.getSelection();
          userSelections.setProperty("SKIP_VD_VALIDATION", panel.getSkipVdValidation());
          
          prefs.addRecentFile(filename);
          
          userSelections.setProperty("FILENAME", filename);
        }

          ReportConfirmPanelDescriptor reportDesc =
            (ReportConfirmPanelDescriptor)model
            .getPanelDescriptor(ReportConfirmPanelDescriptor.IDENTIFIER);
          final ReportConfirmPanel reportPanel = 
            (ReportConfirmPanel)reportDesc.getPanelComponent();
          

          final ProgressFileSelectionPanelDescriptor progressDesc =
            (ProgressFileSelectionPanelDescriptor)model
            .getPanelDescriptor(ProgressFileSelectionPanelDescriptor.IDENTIFIER);


           if(mode.equals(RunMode.GenerateReport)) {
             SwingWorker worker = new SwingWorker() {
                 public Object construct() {
                   try {
                     ProgressEvent evt = new ProgressEvent();

                     String filenameNoExt = filename.substring(filename.lastIndexOf("/")+1);
                     String inputXmi = filename;
                    
                     SemanticConnector sem = BeansAccessor.getSemanticConnector();
                     sem.setProgressListener(progressDesc);

                     evt.setMessage("Creating Semantic Connector Report. This may take a minute ...");
                     progressDesc.newProgressEvent(evt);
                     String outputXmi = inputXmi.substring(0, inputXmi.lastIndexOf("/") + 1)
                         + "FirstPass_" 
                         + inputXmi.substring(inputXmi.lastIndexOf("/") + 1, inputXmi.lastIndexOf("."))
                         + ".xmi";
                     sem.firstPass(inputXmi, outputXmi);
             
                     reportPanel.setFiles(inputXmi, outputXmi);
                    
                     evt.setCompleted(true);
                     progressDesc.newProgressEvent(evt);
                   } catch (ParserException e){
                     e.printStackTrace();
                     reportPanel.setFiles(null, "An error occured.");
                   } catch (Throwable t)  {// end of try-catch
                     t.printStackTrace();
                   }
                   return null;
                 }
               };
             worker.start(); 
           } else
          if(mode.equals(RunMode.Roundtrip)) {
            SwingWorker worker = new SwingWorker() {
                public Object construct() {
//                   RoundtripAction roundtripAction = BeansAccessor.getRoundtripAction();
                  
                  roundtripAction.addProgressListener(progressDesc);

                  String projectName = (String)userSelections.getProperty("PROJECT_NAME");
                  Float projectVersion = (Float)(userSelections.getProperty("PROJECT_VERSION"));
                  
                  File f = new File(filename);
                  outputFile = f.getParent() + "/roundtrip_" + f.getName();

                  roundtripAction.doRoundtrip(projectName, projectVersion, filename, outputFile);

                  reportPanel.setOutputText("Roundtrip was completed. The output file can be found here: <br>" + outputFile);

                  return null;

                } 
              };
            worker.start(); 

          } else if(mode.equals(RunMode.Reviewer) || mode.equals(RunMode.UnannotatedXmi) || mode.equals(RunMode.Curator)) {
            SwingWorker worker = new SwingWorker() {
                public Object construct() {
                  try {
                    if(mode.equals(RunMode.UnannotatedXmi))
                      userSelections.setProperty("HIDE_CONCEPT_ERRORS", new Boolean(true));

//                     XMIParser2 parser = new XMIParser2();
                    ElementsLists elements = ElementsLists.getInstance();
//                     UMLHandler listener = BeansAccessor.getUMLHandler();
//                     parser.setEventHandler(listener);
                    xmiParser.addProgressListener(progressDesc);
                    UMLDefaults defaults = UMLDefaults.getInstance();
                    defaults.initParams(filename);
                    
                    xmiParser.parse(filename);
                    
                    validator.addProgressListener(progressDesc);
                    validator.validate();

                    Set<ValidationFatal> fatals = ValidationItems.getInstance().getFatals();
                    if(fatals.size() > 0) {
                      String s = "<html><body><ul>";
                      for(ValidationFatal fat : fatals) {
                        s += "<li>" + fat.getMessage();
                      }
                      s += "</ul><br>Would you still like to continue?</body></html>";
                      int result = JOptionPane.showConfirmDialog(null, s, "Fatal Error in Model", JOptionPane.YES_NO_OPTION);
                      if (result == JOptionPane.NO_OPTION)
                        wizard.close(-1);
                    }
                    
                    TreeBuilder tb = TreeBuilder.getInstance();
                    tb.init();
                    tb.buildTree(elements);

                  } catch (ParserException e) {
                    logger.fatal("Could not parse: " + filename);
                    logger.fatal(e, e);
                    String msg = "Could not parse: \n" + filename + "\n";

                    Throwable cause = e.getCause();
                    while(cause != null) {
                      msg  = msg + cause.getMessage() + "\n";
                      cause = cause.getCause();
                    }

                    msg  = msg + "\nThe application will now close.";

                    JOptionPane.showMessageDialog((Frame)null,  msg, "Fatal Parsing Error", JOptionPane.ERROR_MESSAGE);
                    wizard.close(Wizard.ERROR_RETURN_CODE);
                  } catch (Exception e){
                    logger.error(e, e);
                    return null;
                  } // end of try-catch

                  wizard.close(Wizard.FINISH_RETURN_CODE);
                  return null;

                }
              };
            worker.start(); 
            
          }
        }
        
        if (nextPanelDescriptor instanceof WizardPanelDescriptor.FinishIdentifier) {
            wizard.close(Wizard.FINISH_RETURN_CODE);
        } else {        
            wizard.setCurrentPanel(nextPanelDescriptor);
        }

    }

    private void backButtonPressed() {
 
        WizardModel model = wizard.getModel();
        WizardPanelDescriptor descriptor = model.getCurrentPanelDescriptor();
 
        //  Get the descriptor that the current panel identifies as the previous
        //  panel, and display it.
        
        Object backPanelDescriptor = descriptor.getBackPanelDescriptor();        
        wizard.setCurrentPanel(backPanelDescriptor);
        
    }

    
    void resetButtonsToPanelRules() {
    
        //  Reset the buttons to support the original panel rules,
        //  including whether the next or back buttons are enabled or
        //  disabled, or if the panel is finishable.
        
        WizardModel model = wizard.getModel();
        WizardPanelDescriptor descriptor = model.getCurrentPanelDescriptor();
        
        //  If the panel in question has another panel behind it, enable
        //  the back button. Otherwise, disable it.
        
        model.setBackButtonText(Wizard.DEFAULT_BACK_BUTTON_TEXT);
        
        if (descriptor.getBackPanelDescriptor() != null)
            model.setBackButtonEnabled(Boolean.TRUE);
        else
            model.setBackButtonEnabled(Boolean.FALSE);

        //  If the panel in question has one or more panels in front of it,
        //  enable the next button. Otherwise, disable it.
 
        if (descriptor.getNextPanelDescriptor() != null)
            model.setNextButtonEnabled(Boolean.TRUE);
        else
            model.setNextButtonEnabled(Boolean.FALSE);
 
        //  If the panel in question is the last panel in the series, change
        //  the Next button to Finish and enable it. Otherwise, set the text
        //  back to Next.
        
        if (descriptor.getNextPanelDescriptor() instanceof WizardPanelDescriptor.FinishIdentifier) {
            model.setNextButtonText(Wizard.DEFAULT_FINISH_BUTTON_TEXT);
            model.setNextButtonEnabled(Boolean.TRUE);
        } else
            model.setNextButtonText(Wizard.DEFAULT_NEXT_BUTTON_TEXT);
        
    }

  public void setRunModeListeners(java.util.List<RunModeListener> listeners) {
    this.runModeListeners = listeners;
  }

  private void fireNewRunMode(RunMode runMode) {
    for(RunModeListener l : runModeListeners)
      l.setRunMode(runMode);
  }
    
  private void putToCenter(Component comp) {
    comp.setLocation((screenSize.width - comp.getSize().width) / 2, (screenSize.height - comp.getSize().height) / 2);
  }

  public void setRoundtripAction(RoundtripAction action) {
    this.roundtripAction = action;
  }
  public void setXmiParser(gov.nih.nci.ncicb.cadsr.loader.parser.Parser parser) {
    this.xmiParser = parser;
  }

  public void setPreParser(gov.nih.nci.ncicb.cadsr.loader.parser.Parser parser) {
    this.preParser = parser;
  }

  public void setValidator(Validator validator) {
    this.validator = validator;
  }
  
}
