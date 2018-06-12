package fi.metatavu.dcfb.server.rest.translate;

import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;

import fi.metatavu.dcfb.server.items.LocalizedValueController;
import fi.metatavu.dcfb.server.persistence.model.LocalizedEntry;
import fi.metatavu.dcfb.server.rest.model.LocalizedValue;

/**
 * Abstract translator class
 * 
 * @author Antti Leppä
 */
public abstract class AbstractTranslator {

  @Inject
  private LocalizedValueController localizedValueController;
  
  /**
   * Translates JPA localized entry into list of REST localized values
   * 
   * @param entry JPA localized entry
   * @return list of REST localized values
   */
  protected List<LocalizedValue> translatelocalizedValue(LocalizedEntry entry) {
    return localizedValueController.listLocalizedValues(entry).stream().map(localizedValues -> {
      LocalizedValue result = new LocalizedValue();
      
      result.setLanguage(localizedValues.getLocale().getLanguage());
      result.setType(localizedValues.getType().toString());
      result.setValue(localizedValues.getValue());
      
      return result;
    }).collect(Collectors.toList());
  }
  
}
