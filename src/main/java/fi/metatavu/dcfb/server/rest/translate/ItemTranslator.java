package fi.metatavu.dcfb.server.rest.translate;

import java.util.List;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import fi.metatavu.dcfb.server.items.ItemController;
import fi.metatavu.dcfb.server.persistence.model.Category;
import fi.metatavu.dcfb.server.persistence.model.ItemImage;
import fi.metatavu.dcfb.server.persistence.model.ItemUser;
import fi.metatavu.dcfb.server.persistence.model.Location;
import fi.metatavu.dcfb.server.rest.model.Image;
import fi.metatavu.dcfb.server.rest.model.Item;
import fi.metatavu.dcfb.server.rest.model.Meta;
import fi.metatavu.dcfb.server.rest.model.Price;

/**
 * Translator for items
 * 
 * @author Antti Leppä
 */
@ApplicationScoped
public class ItemTranslator extends AbstractTranslator {
  
  @Inject
  private ItemController itemController;
  
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
    Location location = item.getLocation();
    Price unitPrice = new Price();
    unitPrice.setCurrency(item.getPriceCurrency() != null ? item.getPriceCurrency().getCurrencyCode() : null);
    unitPrice.setPrice(item.getUnitPrice());
    
    Item result = new Item();
    result.setAmount(item.getAmount());
    result.setCategoryId(category != null ? category.getId() : null);
    result.setLocationId(location != null ? location.getId() : null);
    result.setCreatedAt(item.getCreatedAt());
    result.setDescription(translatelocalizedValue(item.getDescription()));
    result.setExpiresAt(item.getExpiresAt());
    result.setId(item.getId());
    result.setImages(getItemImages(item));
    result.setModifiedAt(item.getModifiedAt());
    result.setSlug(item.getSlug());
    result.setTitle(translatelocalizedValue(item.getTitle()));
    result.setUnit(item.getUnit());
    result.setUnitPrice(unitPrice);
    result.setVisibilityLimited(item.getVisibilityLimited());
    result.setVisibleToUsers(itemController.listItemUsers(item).stream().map(ItemUser::getUserId).collect(Collectors.toList()));
    result.setMeta(itemController.listMetas(item).stream().map(itemMeta -> {
      Meta meta = new Meta();
      meta.setKey(itemMeta.getKey());
      meta.setValue(itemMeta.getValue());
      return meta;
    }).collect(Collectors.toList()));

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

  /**
   * Returns translated list of item images
   * 
   * @param item item
   * @return translated images
   */
  private List<Image> getItemImages(fi.metatavu.dcfb.server.persistence.model.Item item) {
    return itemController.listItemImages(item).stream().map(this::translateItemImage).collect(Collectors.toList());
  }
  
  /**
   * Translates JPA item image into REST entity
   * 
   * @param itemImage JPA item image
   * @return REST entity
   */
  private Image translateItemImage(ItemImage itemImage) {
    Image result = new Image();
    
    result.setId(itemImage.getId());
    result.setType(itemImage.getContentType());
    result.setUrl(itemImage.getUrl());
    
    return result;
  }

}
