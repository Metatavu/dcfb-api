package fi.metatavu.dcfb.server.search.handlers;

import java.util.UUID;

/**
 * Index item event
 */
public class ItemIndexEvent {
  
  private UUID id;
  
  /**
   * Constructor
   */
  public ItemIndexEvent() {
    // Zero-argument constructor
  }
  
  /**
   * Constructor
   * 
   * @param id item id
   */
  public ItemIndexEvent(UUID id) {
    super();
    this.id = id;
  }

  /**
   * Retruns item id
   * 
   * @return item id
   */
  public UUID getId() {
    return id;
  }
  
  /**
   * Sets item id
   * 
   * @param id item id
   */
  public void setId(UUID id) {
    this.id = id;
  }

}
