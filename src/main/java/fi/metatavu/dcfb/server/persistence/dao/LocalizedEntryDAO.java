package fi.metatavu.dcfb.server.persistence.dao;

import java.util.UUID;

import fi.metatavu.dcfb.server.persistence.model.LocalizedEntry;

/**
 * DAO for LocalizedEntry
 * 
 * @author Antti Leppä
 */
public class LocalizedEntryDAO extends AbstractDAO<LocalizedEntry> {
  
  /**
  * Creates new localizedEntry
  *
  * @param lastModifier modifier
  */
  public LocalizedEntry create(UUID id) {
    LocalizedEntry localizedEntry = new LocalizedEntry();
    localizedEntry.setId(id);
    return persist(localizedEntry);
  }
  
}
