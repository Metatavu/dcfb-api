package fi.metatavu.dcfb.server.search.index;

import java.util.UUID;

/**
 * Abstract base class for indexables
 */
public abstract class AbstractIndexable implements Indexable {

  private UUID id;
  
  /**
   * Constructor
   */
  public AbstractIndexable() {
    // Zero-argument constructor
  }

  /**
   * Constructor
   *  
   * @param id indexable id
   */
  public AbstractIndexable(UUID id) {
    this.id = id;
  }

  /**
   * Retruns indexable id
   * 
   * @return indexable id
   */
  public UUID getId() {
    return id;
  }

  /**
   * Sets indexable id
   * 
   * @param id indexable id
   */
  public void setId(UUID id) {
    this.id = id;
  }
  
}
