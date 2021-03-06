/*L
 * Copyright Oracle Inc, SAIC, SAIC-F
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/cadsr-semantic-tools/LICENSE.txt for details.
 */

package gov.nih.nci.ncicb.cadsr.loader.ui;

import gov.nih.nci.ncicb.cadsr.domain.PermissibleValue;

import gov.nih.nci.ncicb.cadsr.loader.util.PropertyAccessor;

import java.util.List;

import java.awt.Color;
import java.awt.Component;
import java.awt.ComponentOrientation;
import java.awt.FlowLayout;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;

import java.util.*;

//import org.apache.log4j.Logger;

public class PVCompareDialog extends JDialog implements ActionListener{

  private JLabel lvdLabel = new JLabel("Local Value Domains");
  private JLabel cadsrLabel = new JLabel("caDSR Value Domains");

  private JLabel explainLabel;

  private JScrollPane lvdScrollPane = null;
  private JScrollPane cadsrScrollPane = null;
  
  private JButton closeButton = null;
  private JPanel mainPanel = null;
  private JList lvdPermissibleValueJList = null;
  private JList cadsrPermissibleValueJList = null;
  private List<PermissibleValue> localPermVals = null;
  private List<PermissibleValue> cadsrPermVals = null;

  //private Logger logger = Logger.getLogger(PVCompareDialog.class.getName());
  
  public PVCompareDialog(List<PermissibleValue> localPVs, List<PermissibleValue> cadsrPVs) {
    
    super((JFrame)null, true);
    
    setTitle(PropertyAccessor.getProperty("pv.compare.title"));
    
    localPermVals = localPVs;
    cadsrPermVals = cadsrPVs;
      
    mainPanel = new JPanel();
    mainPanel.setLayout(new GridBagLayout());

    if((localPVs != null) && (localPVs.size() > 0))
        lvdScrollPane = new JScrollPane(getLVDPermissibleValuesList(localPVs));
    else    
        lvdScrollPane = new JScrollPane(new JLabel("<HTML><B color=RED>&nbsp;&nbsp;No Data Found  </B></HTML>"));
    if((cadsrPVs != null) && (cadsrPVs.size() > 0))
        cadsrScrollPane = new JScrollPane(getCadsrPermissibleValuesList(cadsrPVs));
    else    
        cadsrScrollPane = new JScrollPane(new JLabel("<HTML><B color=RED>&nbsp;&nbsp;No Data Found  </B></HTML>"));

    closeButton = new JButton(" Close ");
    closeButton.addActionListener(this);

    explainLabel = new JLabel("<html><u color=BLUE>Explain this</u></html>");
    ToolTipManager.sharedInstance().registerComponent(explainLabel);
    ToolTipManager.sharedInstance().setDismissDelay(3600000);
    explainLabel.setToolTipText(PropertyAccessor.getProperty("pv.compare.help.label"));

    mainPanel.add(explainLabel, new GridBagConstraints(0, 0, 2, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets(10, 0, 5, 10), 2, 2));

    mainPanel.add(lvdLabel, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets(10, 0, 5, 10), 2, 2));
    mainPanel.add(lvdScrollPane, new GridBagConstraints(0, 2, 1, 1, 0.0, 1.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets(5, 0, 10, 10), 2, 2));
    mainPanel.add(cadsrLabel, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets(10, 0, 5, 10), 2, 2));
    mainPanel.add(cadsrScrollPane, new GridBagConstraints(1, 2, 1, 1, 0.0, 1.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets(5, 0, 10, 10), 2, 2));
    mainPanel.add(closeButton, new GridBagConstraints(1, 3, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(10, 0, 10, 0), 2, 2));
    
    this.add(mainPanel);
    this.setSize(450, 350);
  }

  private JList getLVDPermissibleValuesList(List<PermissibleValue> lvdPermissibleValueList){
    if(lvdPermissibleValueList.size() > 0){
      DefaultListModel permissibleValueListModel = new DefaultListModel();
      lvdPermissibleValueJList = new JList(permissibleValueListModel);
      lvdPermissibleValueJList.setCellRenderer(new MyCellRenderer());

      Set<String> values = new TreeSet(new StringComparator());
      for(PermissibleValue pm : lvdPermissibleValueList) {
        values.add(pm.getValue());
      }
 
      for(String value : values){
        ListItem li = null;
        if(containsLvd(value)) // It exists in other list, show it in white
          li = new ListItem(Color.white, value);
        else // It doesn't exist in other list, show it in yellow
          li = new ListItem(Color.yellow, value);
         permissibleValueListModel.addElement(li);
      }
      lvdPermissibleValueJList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
      lvdPermissibleValueJList.setSelectedIndex(0);
    }
    return lvdPermissibleValueJList;
  }
  private boolean containsLvd(String argValue){
    boolean exists = false;
    if(cadsrPermVals.size() > 0)
      for(PermissibleValue pm : cadsrPermVals)
        if(pm.getValue().equals(argValue)){
          exists = true;
          break;
        }
    return exists;
  }
        
  private JList getCadsrPermissibleValuesList(List<PermissibleValue> cadsrPermissibleValueList){
    if(cadsrPermissibleValueList.size() > 0){
      DefaultListModel permissibleValueListModel = new DefaultListModel();
      cadsrPermissibleValueJList = new JList(permissibleValueListModel);
      cadsrPermissibleValueJList.setCellRenderer(new MyCellRenderer());

      Set<String> values = new TreeSet(new StringComparator());
      for(PermissibleValue pm : cadsrPermissibleValueList) {
        values.add(pm.getValue());
      }

      for(String value : values){
        ListItem li = null;
        if(containsCadsr(value)) // It exists in other list, show it in white
          li = new ListItem(Color.white, value);
        else // It doesn't exist in other list, show it in yellow
          li = new ListItem(Color.yellow, value);
        permissibleValueListModel.addElement(li);
      }
      cadsrPermissibleValueJList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
      cadsrPermissibleValueJList.setSelectedIndex(0);
    }
    return cadsrPermissibleValueJList;
  }

  private boolean containsCadsr(String argValue){
    boolean exists = false;
    if(localPermVals.size() > 0)
      for(PermissibleValue pm : localPermVals)
        if(pm.getValue().equals(argValue)){
          exists = true;
          break;
        }
    return exists;
  }
        
  public void actionPerformed(ActionEvent e) {
    this.setVisible(false);
  }

  private static class ListItem {
    private Color color;
    private String value;
    public ListItem(Color c, String s) {
      color = c;
      value = s;
    }
    public Color getColor() {
      return color;
    }
    public String getValue() {
      return value;
    }
  }

  private static class MyCellRenderer extends JPanel implements ListCellRenderer {
    private JLabel lbl;
    public MyCellRenderer() {
      setOpaque(true);
      lbl = new JLabel();
      FlowLayout fl = new FlowLayout();
      fl.setAlignment(FlowLayout.TRAILING);
      setLayout(fl);
      setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
      add(lbl);
    }
    public Component getListCellRendererComponent(JList list, Object value, int index, boolean iss, boolean chf) {
      lbl.setText(((ListItem) value).getValue());
      setBackground(((ListItem) value).getColor());
      if (iss) 
        setBorder(BorderFactory.createLineBorder(Color.blue, 2));
      else 
        setBorder(BorderFactory.createLineBorder(list.getBackground(), 2));

      return this;
    }
  }

  private class StringComparator implements Comparator<String> {
    public int compare(String s1, String s2) {
      return (s1.toLowerCase()).compareTo(s2.toLowerCase());
    }
    
    public boolean equals(Object o) {
      return this.equals(o);
    }
  }

  //    public static void main(String[] args) {
  //      PVCompareDialog pvPanel = new PVCompareDialog(null, null);
  //      pvPanel.setVisible(true);
  //    }
}
