package gov.nih.nci.ncicb.cadsr.loader.ui;

import gov.nih.nci.ncicb.cadsr.domain.*;
import gov.nih.nci.ncicb.cadsr.loader.event.ReviewEvent;
import gov.nih.nci.ncicb.cadsr.loader.event.ReviewListener;

import gov.nih.nci.ncicb.cadsr.loader.ui.tree.*;

import gov.nih.nci.ncicb.cadsr.loader.util.LookupUtil;
import gov.nih.nci.ncicb.cadsr.loader.util.StringUtil;
import java.awt.event.ItemEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;





public class UMLElementViewPanel extends JPanel
  implements ActionListener, KeyListener, ItemListener {

  private Concept[] concepts;

  private boolean unsavedChanges = false;

  private JPanel _this = this;

  private UMLNode node;

  private static final String ADD = "ADD",
    DELETE = "DELETE",
    SAVE = "SAVE";

  private JButton addButton, deleteButton, saveButton;
  private JCheckBox reviewButton;
  private List<ReviewListener> reviewListeners = new ArrayList();
  
//  public UMLElementViewPanel(Concept[] concepts) {
//    this.concepts = concepts;
//    initUI();
//  }
  
  public UMLElementViewPanel(UMLNode node) 
  {
    this.node = node;
    initConcepts();
    initUI();
  }

  public void updateNode(UMLNode node) 
  {
    this.node = node;
    initConcepts();
    updateConcepts(concepts);
  }

  private void initConcepts() 
  {
    String[] conceptCodes = null;
      if(node instanceof ClassNode) {
        ObjectClass oc = (ObjectClass)node.getUserObject();
        conceptCodes = oc.getPreferredName().split("-");
        
      } else if(node instanceof AttributeNode) {
        Property prop = ((DataElement)node.getUserObject()).getDataElementConcept().getProperty();
        conceptCodes = prop.getPreferredName().split("-");
      } 
      
      if(StringUtil.isEmpty(conceptCodes[0])) {
        conceptCodes = new String[0];
      }

      concepts = new Concept[conceptCodes.length];

      for(int i=0; i<concepts.length; 
          concepts[i] = LookupUtil.lookupConcept(conceptCodes[i++])
          );
      if((concepts.length > 0) && (concepts[0] == null))
        concepts = new Concept[0];
    
  }

  private void updateConcepts(Concept[] concepts) {
    this.concepts = concepts;
    this.removeAll();
    initUI();
  }

  public Concept[] getConcepts() {
    return null;
  }

  public boolean haveUnsavedChanges() {
    return unsavedChanges;
  }

  private void initUI() {
    this.setLayout(new BorderLayout());

//     JPanel scrollPanel = new JPanel();

    JPanel gridPanel = new JPanel(new GridLayout(-1, 1));
    JScrollPane scrollPane = new JScrollPane(gridPanel);

    ConceptUI[] conceptUIs = new ConceptUI[concepts.length];
    JPanel[] conceptPanels = new JPanel[concepts.length];

    for(int i = 0; i<concepts.length; i++) {
      conceptUIs[i] = new ConceptUI(concepts[i]);

      String title = i == 0?"Primary Concept":"Qualifier Concept";

      conceptPanels[i] = new JPanel();
      conceptPanels[i].setBorder
        (BorderFactory.createTitledBorder(title));

      conceptPanels[i].setLayout(new BorderLayout());

      JPanel mainPanel = new JPanel(new GridBagLayout());

      insertInBag(mainPanel, conceptUIs[i].labels[0], 0, 0);
      insertInBag(mainPanel, conceptUIs[i].labels[1], 0, 1);
      insertInBag(mainPanel, conceptUIs[i].labels[2], 0, 2);
      insertInBag(mainPanel, conceptUIs[i].labels[3], 0, 3);

      insertInBag(mainPanel, conceptUIs[i].code, 1, 0, 2, 1);
      insertInBag(mainPanel, conceptUIs[i].name, 1, 1, 2, 1);
      insertInBag(mainPanel, conceptUIs[i].defScrollPane, 1, 2, 2, 1);
      insertInBag(mainPanel, conceptUIs[i].defSource, 1, 3,1, 1);

      JButton evsButton = new JButton("Evs Link");
      insertInBag(mainPanel, evsButton, 2, 3);

      conceptPanels[i].add(mainPanel, BorderLayout.CENTER);
      gridPanel.add(conceptPanels[i]);

      conceptUIs[i].code.addKeyListener(this);
      conceptUIs[i].name.addKeyListener(this);
      conceptUIs[i].defSource.addKeyListener(this);
    }

    

    addButton = new JButton("Add");
    deleteButton = new JButton("Remove");
    saveButton = new JButton("Apply");
    reviewButton = new JCheckBox("Reviewed");
    
    reviewButton.setSelected(((ReviewableUMLNode)node).isReviewed());

    addButton.setActionCommand(ADD);
    deleteButton.setActionCommand(DELETE);
    saveButton.setActionCommand(SAVE);

    addButton.addActionListener(this);
    deleteButton.addActionListener(this);
    saveButton.addActionListener(this);
    reviewButton.addItemListener(this);
    
    if(concepts.length < 2)
      deleteButton.setEnabled(false);

    saveButton.setEnabled(false);

    JPanel buttonPanel = new JPanel();
    buttonPanel.add(addButton);
    buttonPanel.add(deleteButton);
    buttonPanel.add(saveButton);
    buttonPanel.add(reviewButton);

//     scrollPanel.add(buttonPanel, BorderLayout.SOUTH);

//     scrollPanel.add(gridPanel);

//     scrollPane.setViewportView(gridPanel);

    this.add(scrollPane, BorderLayout.CENTER);
    this.add(buttonPanel, BorderLayout.SOUTH);

  }

  public void keyTyped(KeyEvent evt) {
    System.out.println("typed");
    saveButton.setEnabled(true);
  }
  public void keyPressed(KeyEvent evt) {
    System.out.println("pressed");
  }
  public void keyReleased(KeyEvent evt) {
    System.out.println("released");
  }

  
  
  public void actionPerformed(ActionEvent evt) {
    JButton button = (JButton)evt.getSource();
    if(button.getActionCommand().equals(SAVE)) {
      
    } else if(button.getActionCommand().equals(ADD)) {
      Concept[] newConcepts = new Concept[concepts.length + 1];
      for(int i = 0; i<concepts.length; i++) {
        newConcepts[i] = concepts[i];
      }
      Concept concept = DomainObjectFactory.newConcept();
      concept.setPreferredName("");
      concept.setLongName("");
      concept.setDefinitionSource("");
      concept.setPreferredDefinition("");
      newConcepts[newConcepts.length - 1] = concept;
      concepts = newConcepts;

      _this.removeAll();
      initUI();
    } else if(button.getActionCommand().equals(DELETE)) {
      Concept[] newConcepts = new Concept[concepts.length - 1];
      for(int i = 0; i<newConcepts.length; i++) {
        newConcepts[i] = concepts[i];
      }
      concepts = newConcepts;

      _this.removeAll();
      initUI();
    } else if(button.getActionCommand().equals(SAVE)) {
      saveButton.setEnabled(false);
    } 
  }
  
  public void itemStateChanged(ItemEvent e) {
    if(e.getStateChange() == ItemEvent.SELECTED
       || e.getStateChange() == ItemEvent.DESELECTED
       ) {
      ReviewEvent event = new ReviewEvent();
      event.setUserObject(node);
      
      event.setReviewed(ItemEvent.SELECTED == e.getStateChange());
      
      fireReviewEvent(event);
      

    }
  }
  
  public void fireReviewEvent(ReviewEvent event) {
    for(ReviewListener l : reviewListeners)
      l.reviewChanged(event);
  }
  

  public void addReviewListener(ReviewListener listener) {
    reviewListeners.add(listener);
  }
  
  public static void main(String[] args) {
    JFrame frame = new JFrame();
    frame.setLayout(new BorderLayout());

    Concept con = DomainObjectFactory.newConcept();
    con.setPreferredName("C12345");
    con.setPreferredDefinition("A definition of this concept");
    con.setDefinitionSource("NCI_GLOSS");
    con.setLongName("NewConceptName");

    Concept[] concepts = new Concept[1];
    concepts[0] = con;

    ObjectClass oc = DomainObjectFactory.newObjectClass();
    oc.setPreferredName(con.getPreferredName());
    oc.setLongName("com.anwar.ATrivialObject");
    
    ClassNode node = new ClassNode(oc);
    frame.add(new UMLElementViewPanel(node));
    
    frame.setSize(500, 400);

    frame.show();

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

class ConceptUI {
  JLabel[] labels = new JLabel[] {
    new JLabel("Concept Code"),
    new JLabel("Concept Preferred Name"),
    new JLabel("Concept Definition"),
    new JLabel("Concept Definition Source")
  };

  JTextField code = new JTextField(10);
  JTextField name = new JTextField(20);
  JTextArea def = new JTextArea();
  JTextField defSource = new JTextField(10);

  JScrollPane defScrollPane;

  public ConceptUI(Concept concept) {
    initUI(concept);
  }

  private void initUI(Concept concept) {
    def.setFont(new Font("Serif", Font.ITALIC, 16));
    def.setLineWrap(true);
    def.setWrapStyleWord(true);
    defScrollPane = new JScrollPane(def);
    defScrollPane
      .setVerticalScrollBarPolicy
      (JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
    defScrollPane.setPreferredSize(new Dimension(400, 100));

    code.setText(concept.getPreferredName());
    name.setText(concept.getLongName());
    def.setText(concept.getPreferredDefinition());
    defSource.setText(concept.getDefinitionSource());
  }

}