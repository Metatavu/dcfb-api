package fi.metatavu.dcfb.server.items;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.Map.Entry;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import fi.metatavu.dcfb.server.persistence.dao.LocalizedEntryDAO;
import fi.metatavu.dcfb.server.persistence.dao.LocalizedValueDAO;
import fi.metatavu.dcfb.server.persistence.model.LocalizedEntry;
import fi.metatavu.dcfb.server.persistence.model.LocalizedType;
import fi.metatavu.dcfb.server.persistence.model.LocalizedValue;

@ApplicationScoped
public class LocalizedValueController {
  
  @Inject
  private LocalizedEntryDAO localizedEntryDAO;

  @Inject
  private LocalizedValueDAO localizedValueDAO;
  
  /**
   * Creates new localized entry
   * 
   * @return new localized entry
   */
  public LocalizedEntry createEntry() {
    return localizedEntryDAO.create(UUID.randomUUID());
  }
  
  /**
   * Sets localized entry values
   * 
   * @param entry entry
   * @param locale locale
   * @param values values
   * @return localized entry
   */
  public LocalizedEntry setEntryValues(LocalizedEntry entry, Map<Locale, Map<LocalizedType, String>> values) {
    localizedValueDAO.listByEntry(entry).stream().forEach(localizedValueDAO::delete);

    for (Entry<Locale, Map<LocalizedType, String>> localeEntry : values.entrySet()) {
      Locale locale = localeEntry.getKey();
      
      for (Entry<LocalizedType, String> valueEntry : localeEntry.getValue().entrySet()) {
        localizedValueDAO.create(UUID.randomUUID(), entry, locale, valueEntry.getKey(), valueEntry.getValue());
      }
    }
    
    return entry;
  } 
  
  /**
   * Returns value for entry, locale and type
   * 
   * @param entry entry
   * @param locale locale
   * @param type type
   * @return value or null if not found
   */
  public String getValue(LocalizedEntry entry, Locale locale, LocalizedType type) {
    LocalizedValue localizedValue = localizedValueDAO.findByEntryLocaleAndType(entry, locale, type); 
    return localizedValue != null ? localizedValue.getValue() : null;
  }
  
  public List<LocalizedValue> listLocalizedValues(LocalizedEntry entry) {
    return localizedValueDAO.listByEntry(entry);
  }
  
}
