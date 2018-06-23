package fi.metatavu.dcfb.server.search.handlers;

import java.util.UUID;

/**
 * Index location event
 */
public class LocationIndexEvent {
  
  private UUID id;
  
  /**
   * Constructor
   */
  public LocationIndexEvent() {
    // Zero-argument constructor
  }
  
  /**
   * Constructor
   * 
   * @param id location id
   */
  public LocationIndexEvent(UUID id) {
    super();
    this.id = id;
  }

  /**
   * Retruns location id
   * 
   * @return location id
   */
  public UUID getId() {
    return id;
  }
  
  /**
   * Sets location id
   * 
   * @param id location id
   */
  public void setId(UUID id) {
    this.id = id;
  }

}
