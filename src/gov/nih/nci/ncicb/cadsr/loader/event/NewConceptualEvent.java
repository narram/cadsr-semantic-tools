package gov.nih.nci.ncicb.cadsr.loader.event;

import java.util.List;
import java.util.ArrayList;

/**
 * Events with a concept should extend this event
 *
 * @author <a href="mailto:ludetc@mail.nih.gov">Christophe Ludet</a>
 */
public class NewConceptualEvent implements LoaderEvent {

  /**
   * Tagged Value name for Documentation
   */
  public static final String TV_DOCUMENTATION = "documentation";
  public static final String TV_DESCRIPTION = "description";

  private String description;
  private boolean reviewed = false;

  private List<NewConceptEvent> concepts = new ArrayList();


  /**
   * Get the Description value.
   * @return the Description value.
   */
  public String getDescription() {
    return description;
  }

  public boolean isReviewed() 
  {
    return reviewed;
  }
  
  public void setReviewed(boolean b) 
  {
    reviewed = b;
  }
  
  /**
   * Describe <code>getConcepts</code> method here.
   *
   * @return a <code>List</code> of NewConceptEvent
   */
  public List<NewConceptEvent> getConcepts() {
    return concepts;
  }

  public void addConcept(NewConceptEvent concept) {
    concepts.add(concept);
  }
  
  /**
   * Set the Description value.
   * @param newDescription The new Description value.
   */
  public void setDescription(String newDescription) {
    this.description = newDescription;
  }
  

}