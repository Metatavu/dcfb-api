package fi.metatavu.dcfb.server.search.io;

import java.util.ArrayList;
import java.util.List;

import fi.metatavu.dcfb.server.search.index.Indexable;

/**
 * Register indexable event
 */
public class RegisterIndexableEvent {
  
  private List<Class<? extends Indexable>> indexables;
  
  /**
   * Constructor
   */
  public RegisterIndexableEvent() {
    this.indexables = new ArrayList<>();
  }
  
  /**
   * Registers indexable
   * 
   * @param indexable
   */
  public void registerIndexable(Class<? extends Indexable> indexable) {
    indexables.add(indexable);
  }

  /**
   * Returns indexables
   * 
   * @return the indexables
   */
  public List<Class<? extends Indexable>> getIndexables() {
    return indexables;
  }

}
