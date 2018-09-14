package fi.metatavu.dcfb.server.localization;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
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
    if (entry == null) {
      return null;
    }
    
    LocalizedValue localizedValue = localizedValueDAO.findByEntryLocaleAndType(entry, locale, type);
    return localizedValue != null ? localizedValue.getValue() : null;
  }
  
  /**
   * Returns values for entry and locale
   * 
   * @param entry entry
   * @param locale locale
   * @return values
   */
  public List<String> getValues(LocalizedEntry entry, Locale locale) {
    if (entry == null) {
      return Collections.emptyList();
    }
    
    return localizedValueDAO.listByEntryAndLocale(entry, locale).stream()
      .map(LocalizedValue::getValue)
      .collect(Collectors.toList());
  }
  
  /**
   * Returns any value for entry and type
   * 
   * @param entry entry
   * @param type type
   * @return value
   */
  public String getAnyValue(LocalizedEntry entry, String type) {
    if (entry == null) {
      return null;
    }
    
    List<LocalizedValue> result = localizedValueDAO.listByEntryAndType(entry, type, 0, 1);
    if (result.isEmpty()) {
      return null;
    }
    
    return result.get(0).getValue();
  }
  
  public List<LocalizedValue> listLocalizedValues(LocalizedEntry entry) {
    return localizedValueDAO.listByEntry(entry);
  }

  /**
   * Deletes localized entry
   * 
   * @param entry entry
   */
  public void deleteEntry(LocalizedEntry entry) {
    localizedValueDAO.listByEntry(entry).stream().forEach(localizedValueDAO::delete);
    localizedEntryDAO.delete(entry);
  }
  
}
