package gov.nih.nci.ncicb.cadsr.loader.ui;

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

import java.io.File;

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
  private Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    
    /**
     * This constructor accepts a reference to the Wizard component that created it,
     * which it uses to update the button components and access the WizardModel.
     * @param w A callback to the Wizard component that created this controller.
     */    
    public WizardController(Wizard w) {
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
        if(descriptor.getPanelDescriptorIdentifier().equals(LoginPanelDescriptor.IDENTIFIER)) {
          final LoginPanel panel = (LoginPanel)descriptor.getPanelComponent();
          final ProgressLoginPanelDescriptor thisDesc =
            (ProgressLoginPanelDescriptor)model
            .getPanelDescriptor(nextPanelDescriptor);

          final SwingWorker worker = new SwingWorker() {
              public Object construct() {
                try {
                  boolean workOffline = (Boolean)UserSelections.getInstance()
                    .getProperty("WORK_OFFLINE");
                  
                  ProgressEvent evt = null;

                  if(!workOffline) {
                    evt = new ProgressEvent();
                    evt.setMessage("Sending credentials...");
                    thisDesc.newProgressEvent(evt);
                    
                    LoginContext lc = new LoginContext("UML_Loader", panel);    
                    lc.login();
                    username = panel.getUsername();
                    panel.setErrorMessage("");
                  } else {
                    try {
                      Thread.currentThread().sleep(100);
                    } catch (Exception e){
                    } // end of try-catch
                  }
                  
                  evt = new ProgressEvent();
                  evt.setGoal(100);
                  evt.setStatus(100);
                  evt.setMessage("Done");
                  thisDesc.newProgressEvent(evt);
                } catch (Exception e){
                  ProgressEvent evt = new ProgressEvent();
                  evt.setStatus(-1);
                  evt.setGoal(-1);
                  evt.setMessage("Failed");
                  thisDesc.newProgressEvent(evt);

                  username = null;
                  panel.setErrorMessage("Login / Password incorrect");
                } // end of try-catch
                return null;
              }
            };
          worker.start(); 
        }

        if(descriptor.getPanelDescriptorIdentifier().equals(FileSelectionPanelDescriptor.IDENTIFIER)) {
          FileSelectionPanel panel = 
            (FileSelectionPanel)descriptor.getPanelComponent();
          filename = panel.getSelection();
        }

        if(descriptor.getPanelDescriptorIdentifier().equals(SemanticConnectorPanelDescriptor.IDENTIFIER)) {
          SemanticConnectorPanel panel = 
            (SemanticConnectorPanel)descriptor.getPanelComponent();

          final ProgressSemanticConnectorPanelDescriptor thisDesc =
            (ProgressSemanticConnectorPanelDescriptor)model
            .getPanelDescriptor(nextPanelDescriptor);

          final SwingWorker worker = new SwingWorker() {
              public Object construct() {
                XMIParser  parser = new XMIParser();
                ElementsLists elements = new ElementsLists();
                UMLHandler listener = new UMLDefaultHandler(elements);
                parser.setEventHandler(listener);
                parser.addProgressListener(thisDesc);
                parser.parse(filename);
                
                Validator validator = new UMLValidator(elements);
                validator.validate();

                new TreeBuilder().buildTree(elements);
                
                return null;
              }
            };
          worker.start(); 

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
    
  private void putToCenter(Component comp) {
    comp.setLocation((screenSize.width - comp.getSize().width) / 2, (screenSize.height - comp.getSize().height) / 2);
  }
  
}
