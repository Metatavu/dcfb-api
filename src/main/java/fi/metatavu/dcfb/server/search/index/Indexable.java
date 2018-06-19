package fi.metatavu.dcfb.server.search.index;

import java.util.UUID;

/**
 * Interface that describes indexable class
 */
public interface Indexable {

  /**
   * Returns elastic search type
   * 
   * @return elastic search type
   */
  public String getType();
  
  /**
   * Returns id in elastic search
   * 
   * @return id in elastic search
   */
  public UUID getId();
  
}
