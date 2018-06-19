package fi.metatavu.dcfb.server.search.handlers;

import java.util.UUID;

/**
 * Index category event
 */
public class CategoryIndexEvent {
  
  private UUID id;
  
  /**
   * Constructor
   */
  public CategoryIndexEvent() {
    // Zero-argument constructor
  }
  
  /**
   * Constructor
   * 
   * @param id category id
   */
  public CategoryIndexEvent(UUID id) {
    super();
    this.id = id;
  }

  /**
   * Retruns category id
   * 
   * @return category id
   */
  public UUID getId() {
    return id;
  }
  
  /**
   * Sets category id
   * 
   * @param id category id
   */
  public void setId(UUID id) {
    this.id = id;
  }

}
