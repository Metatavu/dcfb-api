package fi.metatavu.dcfb.server.rest.translate;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;

import fi.metatavu.dcfb.server.persistence.model.Category;
import fi.metatavu.dcfb.server.rest.model.Image;
import fi.metatavu.dcfb.server.rest.model.Item;
import fi.metatavu.dcfb.server.rest.model.Price;

/**
 * Translator for items
 * 
 * @author Antti Leppä
 */
@ApplicationScoped
public class ItemTranslator extends AbstractTranslator {
  
  /**
   * Translates JPA item object into REST item object
   * 
   * @param item JPA item object
   * @return REST item
   */
  public Item translateItem(fi.metatavu.dcfb.server.persistence.model.Item item) {
    if (item == null) {
      return null;
    }
    
    Category category = item.getCategory();
    Price unitPrice = new Price();
    unitPrice.setCurrency(item.getPriceCurrency().getCurrencyCode());
    unitPrice.setPrice(item.getUnitPrice());
    
    Item result = new Item();
    result.setAmount(item.getAmount());
    result.setCategoryId(category != null ? category.getId() : null);
    result.setCreatedAt(item.getCreatedAt());
    result.setDescription(translatelocalizedValue(item.getDescription()));
    result.setExpiresAt(item.getExpiresAt());
    result.setId(item.getId());
    result.setImages(getItemImageIds(item));
    result.setModifiedAt(item.getModifiedAt());
    result.setSlug(item.getSlug());
    result.setTitle(translatelocalizedValue(item.getTitle()));
    result.setUnit(item.getUnit());
    result.setUnitPrice(unitPrice);
    
    return result;
  }
  
  /**
   * Translates list of JPA items into REST items
   * 
   * @param items JPA items
   * @return REST items
   */
  public List<Item> translateItems(List<fi.metatavu.dcfb.server.persistence.model.Item> items) {
    return items.stream().map(this::translateItem).collect(Collectors.toList());
  }

  private List<Image> getItemImageIds(fi.metatavu.dcfb.server.persistence.model.Item item) {
    // TODO Implement
    return Collections.emptyList();
  }

}
