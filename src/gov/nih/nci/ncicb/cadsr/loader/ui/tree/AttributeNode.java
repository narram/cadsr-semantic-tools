package gov.nih.nci.ncicb.cadsr.loader.ui.tree;

import gov.nih.nci.ncicb.cadsr.domain.DataElement;

import javax.swing.Icon;
import javax.swing.ImageIcon;

public class AttributeNode extends AbstractUMLNode {

  public AttributeNode(DataElement de) {
    display = de.getDataElementConcept().getProperty().getLongName();

    fullPath = de.getDataElementConcept().getObjectClass().getLongName()
      + display;
    userObject = de;
  }

  public Icon getIcon() {
    return new ImageIcon(Thread.currentThread().getContextClassLoader().getResource("tree-attribute.gif"));
  }

}