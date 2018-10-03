package fi.metatavu.dcfb.server.rest.translate;

import java.util.List;
import java.util.stream.Collectors;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import fi.metatavu.dcfb.server.items.ItemController;
import fi.metatavu.dcfb.server.persistence.model.Category;
import fi.metatavu.dcfb.server.persistence.model.ItemImage;
import fi.metatavu.dcfb.server.persistence.model.ItemReservation;
import fi.metatavu.dcfb.server.persistence.model.ItemUser;
import fi.metatavu.dcfb.server.persistence.model.Location;
import fi.metatavu.dcfb.server.rest.model.Image;
import fi.metatavu.dcfb.server.rest.model.Item;
import fi.metatavu.dcfb.server.rest.model.ItemPaymentMethods;
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
    String businessCode = item.getBusinessCode();
    String businessName = item.getBusinessName();
    
    Price deliveryPrice = new Price();
    deliveryPrice.setCurrency(item.getDeliveryCurrency() != null ? item.getDeliveryCurrency().getCurrencyCode() : null);
    deliveryPrice.setPrice(item.getDeliveryPrice());

    ItemPaymentMethods paymentMethods = new ItemPaymentMethods();
    paymentMethods.setAllowContactSeller(item.getAllowPurchaseContactSeller());
    paymentMethods.setAllowCreditCard(item.getAllowPurchaseCreditCard());
    
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
    result.setSellerId(item.getSellerId());
    result.setSoldAmount(item.getSoldAmount());
    result.setReservedAmount(itemController.countReservedAmountByItem(item));
    result.setResourceId(item.getResourceId());
    result.setDeliveryTime(item.getDeliveryTime());
    result.setContactEmail(item.getContactEmail());
    result.setContactPhone(item.getContactPhone());
    result.setTermsOfDelivery(item.getTermsOfDelivery());
    result.setAllowDelivery(item.getAllowDelivery());
    result.setAllowPickup(item.getAllowPickup());
    result.setDeliveryPrice(deliveryPrice);
    result.setBusinessCode(businessCode);
    result.setBusinessName(businessName);
    result.setPaymentMethods(paymentMethods);
    result.setMeta(itemController.listMetas(item).stream().map(itemMeta -> {
      Meta meta = new Meta();
      meta.setKey(itemMeta.getKey());
      meta.setValue(itemMeta.getValue());
      return meta;
    }).collect(Collectors.toList()));

    return result;
  }
  
  /**
   * Translates JPA item reservation object into REST item reservation object
   * 
   * @param item JPA item reservation object
   * @return REST item reservation
   */
  public fi.metatavu.dcfb.server.rest.model.ItemReservation translateItemReservation(ItemReservation itemReservation) {
    if (itemReservation == null) {
      return null;
    }
    
    fi.metatavu.dcfb.server.rest.model.ItemReservation result = new fi.metatavu.dcfb.server.rest.model.ItemReservation();
    result.setAmount(itemReservation.getAmount());
    result.setId(itemReservation.getId());
    
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
