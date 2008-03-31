package gov.nih.nci.ncicb.cadsr.loader.ui;

import gov.nih.nci.ncicb.cadsr.domain.Representation;
import gov.nih.nci.ncicb.cadsr.domain.ConceptualDomain;
import gov.nih.nci.ncicb.cadsr.domain.DomainObjectFactory;
import gov.nih.nci.ncicb.cadsr.domain.ValueDomain;
import gov.nih.nci.ncicb.cadsr.loader.event.ElementChangeEvent;
import gov.nih.nci.ncicb.cadsr.loader.event.ElementChangeListener;
import gov.nih.nci.ncicb.cadsr.loader.ext.CadsrModule;
import gov.nih.nci.ncicb.cadsr.loader.ext.CadsrModuleListener;
import gov.nih.nci.ncicb.cadsr.loader.ui.tree.UMLNode;
import gov.nih.nci.ncicb.cadsr.loader.ui.tree.ValueDomainNode;
import gov.nih.nci.ncicb.cadsr.loader.ui.util.UIUtil;
import gov.nih.nci.ncicb.cadsr.loader.util.ConventionUtil;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public class ValueDomainViewPanel extends JPanel 
    implements Editable, CadsrModuleListener, ItemListener, DocumentListener
{
  private JLabel vdPrefDefTitleLabel = new JLabel("VD Preferred Definition"),
    vdDatatypeTitleLabel = new JLabel("VD Datatype"),
    vdTypeTitleLabel = new JLabel("VD Type"),
    vdCdIdTitleLabel = new JLabel("VD CD PublicId / Version"),
    vdRepIdTitleLabel = new JLabel("Representation PublicId / Version"),
    vdCdLongNameTitleLabel = new JLabel("VD CD Long Name");
  
  public JTextArea vdPrefDefValueTextField = new JTextArea();
  private CadsrDialog cadsrCDDialog;
  private CadsrDialog cadsrREPDialog;

  public JComboBox vdDatatypeValueCombobox = null;
  public JRadioButton vdTypeERadioButton = new JRadioButton("E");
  public JRadioButton vdTypeNRadioButton = new JRadioButton("N");
  public JRadioButton tmpInvisible = new JRadioButton("dummy");
  private ButtonGroup vdTypeRadioButtonGroup = new ButtonGroup();
  
  public JLabel vdCDPublicIdJLabel = null;
  public JLabel vdCdLongNameValueJLabel = null;
  private JButton searchButton = new JButton("Search");
  private JButton repSearchButton = new JButton("Search");
  public JLabel vdRepIdValueJLabel = null;
  
  private JScrollPane scrollPane;
  private List datatypeList = null;

  private ValueDomain vd, tempVD;
  private ApplyButtonPanel applyButtonPanel;
  private UMLNode umlNode;
  private CadsrModule cadsrModule;

  private boolean isInitialized = false;
  private List<ElementChangeListener> changeListeners = new ArrayList<ElementChangeListener>();
  private List<PropertyChangeListener> propChangeListeners = new ArrayList<PropertyChangeListener>();  

  
  public void addPropertyChangeListener(PropertyChangeListener l) {
      super.addPropertyChangeListener(l);;
      propChangeListeners.add(l);
  }


  public ValueDomainViewPanel() {

  }
  
  public void update(ValueDomain vd, UMLNode umlNode) 
  {
    this.vd = vd;
    this.umlNode = umlNode;

    tempVD = DomainObjectFactory.newValueDomain();
    tempVD.setConceptualDomain(vd.getConceptualDomain());
    tempVD.setPreferredDefinition(vd.getPreferredDefinition());
    tempVD.setRepresentation(vd.getRepresentation());
    tempVD.setDataType(vd.getDataType());
    tempVD.setVdType(vd.getVdType());
    vd = null;

    if(!isInitialized)
      initUI();

    initValues();
    applyButtonPanel.propertyChange(new PropertyChangeEvent(this, ButtonPanel.SETUP, null, true));
    applyButtonPanel.update();
  }
  
  private void initUI() 
  {
    isInitialized = true;

    this.setLayout(new BorderLayout());
    JPanel mainPanel = new JPanel(new GridBagLayout());
    applyButtonPanel = new ApplyButtonPanel(this, (ValueDomainNode)umlNode);
    addPropertyChangeListener(applyButtonPanel);

    this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
    populateDatatypeCombobox();
    this.setCursor(Cursor.getDefaultCursor());
 
    JPanel vdTypeRadioButtonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    vdTypeRadioButtonGroup.add(vdTypeERadioButton);
    vdTypeRadioButtonGroup.add(vdTypeNRadioButton);
    vdTypeRadioButtonGroup.add(tmpInvisible);
    vdTypeRadioButtonPanel.add(vdTypeERadioButton);
    vdTypeRadioButtonPanel.add(vdTypeNRadioButton);
    vdTypeRadioButtonPanel.add(tmpInvisible);
    tmpInvisible.setVisible(false);
      
    JPanel vdCDIdVersionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    vdCDPublicIdJLabel = new JLabel();
    vdCDIdVersionPanel.add(vdCDPublicIdJLabel);
    vdCDIdVersionPanel.add(searchButton);
    searchButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent evt) {
            cadsrCDDialog.setAlwaysOnTop(true);
            cadsrCDDialog.setVisible(true);
            ConceptualDomain cd = (ConceptualDomain)cadsrCDDialog.getAdminComponent();
            if(cd == null) return;
            tempVD.setConceptualDomain(cd);
            //initValues();
            setVdCdSearchedValues();
            firePropertyChangeEvent(new PropertyChangeEvent(this, ApplyButtonPanel.SAVE, null, true));
          }});
    
    repSearchButton.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent evt) {
             cadsrREPDialog.setAlwaysOnTop(true);
             cadsrREPDialog.setVisible(true);
             Representation rep = (Representation)cadsrREPDialog.getAdminComponent();
             if(rep == null) return;
             tempVD.setRepresentation(rep);
             //initValues();
             setRepSearchedValues();
             firePropertyChangeEvent(new PropertyChangeEvent(this, ApplyButtonPanel.SAVE, null, true));
           }});
    vdCdLongNameValueJLabel = new JLabel();

    JPanel vdCDRepPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    vdRepIdValueJLabel = new JLabel();
    vdCDRepPanel.add(vdRepIdValueJLabel);
    vdCDRepPanel.add(repSearchButton);
    
    vdPrefDefValueTextField.setLineWrap(true);
    vdPrefDefValueTextField.setWrapStyleWord(true);
    scrollPane = new JScrollPane(vdPrefDefValueTextField);
    scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

    scrollPane = new JScrollPane(vdPrefDefValueTextField);
    scrollPane.setPreferredSize(new Dimension(200, 100));
    
    UIUtil.insertInBag(mainPanel, vdPrefDefTitleLabel, 0, 1);
    UIUtil.insertInBag(mainPanel, scrollPane, 1, 1, 3, 1); 
    
    UIUtil.insertInBag(mainPanel, vdDatatypeTitleLabel, 0, 3);
    UIUtil.insertInBag(mainPanel, vdDatatypeValueCombobox, 1, 3);
    
    UIUtil.insertInBag(mainPanel, vdTypeTitleLabel, 0, 4);
    UIUtil.insertInBag(mainPanel, vdTypeRadioButtonPanel, 1, 4);
    
    UIUtil.insertInBag(mainPanel, vdCdIdTitleLabel, 0, 5);
    UIUtil.insertInBag(mainPanel, vdCDIdVersionPanel, 1, 5);
    
    UIUtil.insertInBag(mainPanel, vdCdLongNameTitleLabel, 0, 6);
    UIUtil.insertInBag(mainPanel, vdCdLongNameValueJLabel, 1, 6);

    UIUtil.insertInBag(mainPanel, vdRepIdTitleLabel, 0, 7);
    UIUtil.insertInBag(mainPanel, vdCDRepPanel, 1, 7);

    JScrollPane scrollPane = new JScrollPane(mainPanel);
    scrollPane.getVerticalScrollBar().setUnitIncrement(30);

    this.add(scrollPane, BorderLayout.CENTER);
    
  }
  
  private void initValues() 
  {
    vdPrefDefValueTextField.setText(tempVD.getPreferredDefinition());
    
    int datatypeIndex = tempVD.getDataType() == null ? -1 : getSelectedIndex(tempVD.getDataType());
    vdDatatypeValueCombobox.setSelectedIndex(datatypeIndex == -1 ? 0 : datatypeIndex);
        
      if(vd.getVdType() != null && tempVD != null && tempVD.getVdType() != null){
          if(!vd.getVdType().equals("null")){
              if(!vd.getVdType().equals(null)){
                if(tempVD.getVdType().equals("E")){
                    vdTypeERadioButton.setSelected(true);
                    vdTypeNRadioButton.setSelected(false);
                }
                else if(tempVD.getVdType().equals("N")){
                    vdTypeNRadioButton.setSelected(true);
                    vdTypeERadioButton.setSelected(false);
                }
              }else{
                tmpInvisible.setSelected(true);
                vdTypeERadioButton.setSelected(false);
                vdTypeNRadioButton.setSelected(false);
              }
         }else{
            tmpInvisible.setSelected(true);
            vdTypeERadioButton.setSelected(false);
            vdTypeNRadioButton.setSelected(false);
         }
      }else{
          tmpInvisible.setSelected(true);
          vdTypeERadioButton.setSelected(false);
          vdTypeNRadioButton.setSelected(false);
        }

    vdCDPublicIdJLabel.setText(ConventionUtil.publicIdVersion(tempVD.getConceptualDomain()));

    if(tempVD != null && tempVD.getConceptualDomain() != null && tempVD.getConceptualDomain().getLongName() != null
        && !tempVD.getConceptualDomain().getLongName().equals(""))
      vdCdLongNameValueJLabel.setText(tempVD.getConceptualDomain().getLongName());
    else
      vdCdLongNameValueJLabel.setText("Unable to lookup CD Long Name");

    vdRepIdValueJLabel.setText(ConventionUtil.publicIdVersion(tempVD.getRepresentation()));

    firePropertyChangeEvent(new PropertyChangeEvent(this, ApplyButtonPanel.SAVE, null, false));
    vdPrefDefValueTextField.getDocument().addDocumentListener(this);
    vdTypeERadioButton.addItemListener(this);
    vdTypeNRadioButton.addItemListener(this);
    vdDatatypeValueCombobox.addItemListener(this);
  }
  
  private void setVdCdSearchedValues(){
      vdCDPublicIdJLabel.setText(ConventionUtil.publicIdVersion(tempVD.getConceptualDomain()));

      if(tempVD.getConceptualDomain().getLongName() != null
          && !tempVD.getConceptualDomain().getLongName().equals(""))
        vdCdLongNameValueJLabel.setText(tempVD.getConceptualDomain().getLongName());
      else
        vdCdLongNameValueJLabel.setText("Unable to lookup CD Long Name");
  }
  
  private void setRepSearchedValues(){
      vdRepIdValueJLabel.setText(ConventionUtil.publicIdVersion(tempVD.getRepresentation()));
  }
  
  public static void main(String args[]) 
  {
//    JFrame frame = new JFrame();
//    ValueDomainViewPanel vdPanel = new ValueDomainViewPanel();
//    vdPanel.setVisible(true);
//    frame.add(vdPanel);
//    frame.setVisible(true);
//    frame.setSize(450, 350);
  }

    public void applyPressed() {   
        vd.setPublicId(null);
        vd.setConceptualDomain(tempVD.getConceptualDomain());
        vd.setRepresentation(tempVD.getRepresentation());
        if(vdDatatypeValueCombobox.getSelectedIndex() != 0){
            vd.setDataType(String.valueOf(vdDatatypeValueCombobox.getSelectedItem()));}
        if(vdPrefDefValueTextField.getText() != null || !vdPrefDefValueTextField.getText().equals("null") || vdPrefDefValueTextField.getText().length() > 0)
            vd.setPreferredDefinition(vdPrefDefValueTextField.getText());
        if(vdTypeERadioButton.isSelected())
            vd.setVdType("E");
        if(vdTypeNRadioButton.isSelected())
            vd.setVdType("N");
        vdTypeERadioButton.addItemListener(this);
        vdTypeNRadioButton.addItemListener(this);
        
        firePropertyChangeEvent(new PropertyChangeEvent(this, ApplyButtonPanel.SAVE, null, false));
        fireElementChangeEvent(new ElementChangeEvent((Object)umlNode));
    }

    public void setCadsrCDDialog(CadsrDialog cadsrCDDialog) {
      this.cadsrCDDialog = cadsrCDDialog;
    }

    public void setCadsrREPDialog(CadsrDialog cadsrREPDialog) {
      this.cadsrREPDialog = cadsrREPDialog;
    }

    public void setCadsrModule(CadsrModule cadsrModule){
      this.cadsrModule = cadsrModule;
    }
    
    private void populateDatatypeCombobox(){
        datatypeList = new ArrayList();
        Collection<String> collDatatypes = cadsrModule.getAllDatatypes();
        vdDatatypeValueCombobox = new JComboBox();
        vdDatatypeValueCombobox.addItem("Please Select The Datatype");
        datatypeList.add("Please Select The Datatype");
        Iterator itr = collDatatypes.iterator();
        while(itr.hasNext()){
            String datatypeValue = (String) itr.next();
            datatypeList.add(datatypeValue);
            vdDatatypeValueCombobox.addItem(datatypeValue);
        }
    }
    
    private int getSelectedIndex(String selectedString){
        for(int i=0; i<datatypeList.size(); i++)
            if(datatypeList.get(i)!= null 
                && datatypeList.get(i).toString().trim().equals(selectedString.trim()))
                return i;
        return -1;
    }
    public void addElementChangeListener(ElementChangeListener listener){
        changeListeners.add(listener);
    }
    private void fireElementChangeEvent(ElementChangeEvent event) {
      for(ElementChangeListener l : changeListeners)
        l.elementChanged(event);
    }
    private void firePropertyChangeEvent(PropertyChangeEvent evt) {
      for(PropertyChangeListener l : propChangeListeners) 
        l.propertyChange(evt);
    }

    public void itemStateChanged(ItemEvent e) {
        firePropertyChangeEvent(new PropertyChangeEvent(this, ApplyButtonPanel.SAVE, null, true));
    }

    public void insertUpdate(DocumentEvent e) {
        firePropertyChangeEvent(new PropertyChangeEvent(this, ApplyButtonPanel.SAVE, null, true));
    }

    public void removeUpdate(DocumentEvent e) {
        firePropertyChangeEvent(new PropertyChangeEvent(this, ApplyButtonPanel.SAVE, null, true));
    }

    public void changedUpdate(DocumentEvent e) {
        firePropertyChangeEvent(new PropertyChangeEvent(this, ApplyButtonPanel.SAVE, null, true));
    }
    public UMLNode getNode() 
    {
      return umlNode;
    }
    public String getPubId() {
        return tempVD.getPublicId();
    }
}
