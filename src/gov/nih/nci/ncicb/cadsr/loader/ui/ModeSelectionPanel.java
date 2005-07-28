package gov.nih.nci.ncicb.cadsr.loader.ui;

import gov.nih.nci.ncicb.cadsr.loader.UserSelections;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import gov.nih.nci.ncicb.cadsr.loader.util.*;

public class ModeSelectionPanel extends JPanel 
{

  private JRadioButton reportOption, curateOption, reviewOption;
  private ButtonGroup group;
  private JPanel _this = this;

  private UserSelections userSelections = UserSelections.getInstance();
  private UserPreferences prefs = UserPreferences.getInstance();
  
  public ModeSelectionPanel()
  {
    initUI();
  }

  public String getSelection() {
    return group.getSelection().getActionCommand();
  }

  private void initUI() {

    this.setLayout(new BorderLayout());
    
    JPanel infoPanel = new JPanel();
    infoPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
    infoPanel.setBackground(Color.WHITE);
    infoPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK));

    infoPanel.add(new JLabel(new ImageIcon(Thread.currentThread().getContextClassLoader().getResource("xmi.gif"))));

    JLabel infoLabel = new JLabel("<html>Please select the mode you want <br>the Workbench to run in</html>");
    infoPanel.add(infoLabel);
    
    this.add(infoPanel, BorderLayout.NORTH);
    
    group = new ButtonGroup();
    
    reportOption = new JRadioButton("Generate Semantic Annotation Report Only");
    reportOption.setActionCommand(RunMode.GenerateReport.toString());
        
    curateOption = new JRadioButton("Curate Semantic Annotation");
    curateOption.setActionCommand(RunMode.Curator.toString());
       
    reviewOption = new JRadioButton("Review Annotated Model");
    reviewOption.setActionCommand(RunMode.Reviewer.toString());

    if(prefs.getModeSelection() == null) 
      reportOption.setSelected(true);
    else if(prefs.getModeSelection().equals(RunMode.GenerateReport.toString()))
      reportOption.setSelected(true);
    else if(prefs.getModeSelection().equals(RunMode.Curator.toString()))
      curateOption.setSelected(true);
    else if(prefs.getModeSelection().equals(RunMode.Reviewer.toString()))
      reviewOption.setSelected(true);
    else
      reportOption.setSelected(true);
      
    group.add(reportOption);
    group.add(curateOption);
    group.add(reviewOption);

    JPanel buttonPanel = new JPanel();
    buttonPanel.setLayout(new GridLayout(0, 1));

    buttonPanel.add
      (new JLabel("<html>Semantic Integration Workbench can be run in 3 modes<br>Which would you like to use?</html>"));

    buttonPanel.add(reportOption);
    buttonPanel.add(curateOption);
    buttonPanel.add(reviewOption);

    this.setLayout(new BorderLayout());
    this.add(infoPanel, BorderLayout.NORTH);
    this.add(buttonPanel, BorderLayout.CENTER);

  }

  private void insertInBag(JPanel bagComp, Component comp, int x, int y) {

    insertInBag(bagComp, comp, x, y, 1, 1);

  }

  private void insertInBag(JPanel bagComp, Component comp, int x, int y, int width, int height) {
    JPanel p = new JPanel();
    p.add(comp);

    bagComp.add(p, new GridBagConstraints(x, y, width, height, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
  }


}