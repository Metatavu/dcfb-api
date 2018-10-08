package fi.metatavu.dcfb.server.items;

import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Currency;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;

import fi.metatavu.dcfb.server.categories.CategoryController;
import fi.metatavu.dcfb.server.localization.LocalizedValueController;
import fi.metatavu.dcfb.server.persistence.dao.ItemDAO;
import fi.metatavu.dcfb.server.persistence.dao.ItemImageDAO;
import fi.metatavu.dcfb.server.persistence.dao.ItemMetaDAO;
import fi.metatavu.dcfb.server.persistence.dao.ItemReservationDAO;
import fi.metatavu.dcfb.server.persistence.dao.ItemUserDAO;
import fi.metatavu.dcfb.server.persistence.model.Category;
import fi.metatavu.dcfb.server.persistence.model.Item;
import fi.metatavu.dcfb.server.persistence.model.ItemImage;
import fi.metatavu.dcfb.server.persistence.model.ItemMeta;
import fi.metatavu.dcfb.server.persistence.model.ItemReservation;
import fi.metatavu.dcfb.server.persistence.model.ItemUser;
import fi.metatavu.dcfb.server.persistence.model.LocalizedEntry;
import fi.metatavu.dcfb.server.persistence.model.Location;
import fi.metatavu.dcfb.server.rest.model.ItemListSort;
import fi.metatavu.dcfb.server.search.handlers.ItemIndexEvent;
import fi.metatavu.dcfb.server.search.handlers.ItemIndexHandler;
import fi.metatavu.dcfb.server.search.searchers.ItemSearcher;
import fi.metatavu.dcfb.server.search.searchers.SearchResult;

@ApplicationScoped
public class ItemController {
  
  private static long RESERVATION_EXPIRE_MINUTES = 5l;

  @Inject
  private CategoryController categoryController;

  @Inject
  private ItemSearcher itemSearcher;

  @Inject
  private ItemIndexHandler itemIndexHandler;
  
  @Inject
  private ItemDAO itemDAO;

  @Inject
  private ItemImageDAO itemImageDAO;

  @Inject
  private ItemMetaDAO itemMetaDAO;
  
  @Inject
  private ItemUserDAO itemUserDAO;

  @Inject
  private ItemReservationDAO itemReservationDAO;

  @Inject
  private LocalizedValueController localizedValueController;

  @Inject
  private Event<ItemIndexEvent> itemIndexEvent;
  
  /**
   * Create item
   *
   * @param title title
   * @param description description
   * @param category category
   * @param location location
   * @param slug slug
   * @param expiresAt expiresAt
   * @param unitPrice unitPrice
   * @param priceCurrency priceCurrency
   * @param amount amount
   * @param unit unit
   * @param visibilityLimited is items visibility limited to only specific users
   * @param soldAmount sold amount
   * @param allowPurchaseContactSeller whether item is allowed to purchase directly from the seller
   * @param allowPurchaseCreditCard whether item is allowed to purchase directly with credit card
   * @param deliveryTime delivery time
   * @param contactEmail contact email
   * @param contactPhone contact phone
   * @param termsOfDelivery terms of delivery
   * @param allowDelivery allow delivery
   * @param allowPickup allow pick up
   * @param deliveryPrice delivery price
   * @param deliveryCurrency  delivery currency
   * @param sellerId sellerId
   * @param modifiedId modifiedId
   * @return created item
   */
  @SuppressWarnings ("squid:S00107")
  public Item createItem(String typeOfBusiness, LocalizedEntry title, LocalizedEntry description, Category category, Location location, String slug, OffsetDateTime expiresAt, String unitPrice, Currency priceCurrency, Long amount, String unit, boolean visibilityLimited, UUID resourceId, Long soldAmount, Boolean allowPurchaseContactSeller, Boolean allowPurchaseCreditCard, Integer deliveryTime, String contactEmail, String contactPhone, String termsOfDelivery,Boolean allowDelivery, Boolean allowPickup, String deliveryPrice, Currency deliveryCurrency, String businessName, String businessCode, UUID sellerId, UUID modifier) {
    return itemDAO.create(UUID.randomUUID(), typeOfBusiness, title, description, category, location, getUniqueSlug(slug), expiresAt, unitPrice, 
        priceCurrency, amount, unit, visibilityLimited, resourceId, soldAmount, allowPurchaseContactSeller, allowPurchaseCreditCard,
        deliveryTime, contactEmail, contactPhone, termsOfDelivery, allowDelivery, allowPickup, deliveryPrice, deliveryCurrency, 
        businessName, businessCode, sellerId, modifier);
  }

  /**
   * Finds an item
   * 
   * @param itemId item id
   * @return item or null if not found
   */
  public Item findItem(UUID itemId) {
    return itemDAO.findById(itemId);
  }

  /**
   * Update item
   *
   * @param title                      title
   * @param description                description
   * @param category                   category
   * @param visibilityLimited          visibilityLimited
   * @param location                   location
   * @param slug                       slug
   * @param expiresAt                  expiresAt
   * @param unitPrice                  unitPrice
   * @param priceCurrency              priceCurrency
   * @param amount                     amount
   * @param unit                       unit
   * @param soldAmount                 soldAmount
   * @param allowPurchaseContactSeller allowPurchaseContactSeller
   * @param allowPurchaseCreditCard    allowPurchaseCreditCard
   * @param deliveryTime               deliveryTime
   * @param contactEmail               contactEmail
   * @param contactPhone               contactPhone
   * @param termsOfDelivery            termsOfDelivery
   * @param allowDelivery              allow delivery
   * @param allowPickup                allow pick up
   * @param deliveryPrice              delivery price
   * @param deliveryCurrency           delivery currency
   * @param sellerId                   sellerId
   * @param modifier                   modifier

   * @return updated item
   */
  public Item updateItem(Item item, LocalizedEntry title, LocalizedEntry description, Category category, boolean visibilityLimited, Location location, String slug, OffsetDateTime expiresAt, String unitPrice, Currency priceCurrency, Long amount, String unit, Long soldAmount, Boolean allowPurchaseContactSeller, Boolean allowPurchaseCreditCard, Integer deliveryTime, String contactEmail, String contactPhone, String termsOfDelivery,Boolean allowDelivery, Boolean allowPickup, String deliveryPrice, Currency deliveryCurrency, String businessName, String businessCode, UUID sellerId, UUID modifier) {
    itemDAO.updateTitle(item, title, modifier);
    itemDAO.updateDescription(item, description, modifier);
    itemDAO.updateCategory(item, category, modifier);
    itemDAO.updateVisibilityLimited(item, visibilityLimited, modifier);
    itemDAO.updateLocation(item, location, modifier);
    itemDAO.updateSlug(item, slug, modifier);
    itemDAO.updateExpiresAt(item, expiresAt, modifier);
    itemDAO.updateUnitPrice(item, unitPrice, modifier);
    itemDAO.updatePriceCurrency(item, priceCurrency, modifier);
    itemDAO.updateAmount(item, amount, modifier);
    itemDAO.updateUnit(item, unit, modifier);
    itemDAO.updateSellerId(item, sellerId, modifier);
    itemDAO.updateSoldAmount(item, soldAmount, modifier);
    itemDAO.updateAllowPurchaseContactSeller(item, allowPurchaseContactSeller, modifier);
    itemDAO.updateAllowPurchaseCreditCard(item, allowPurchaseCreditCard, modifier);
    itemDAO.updateDeliveryTime(item, deliveryTime, modifier);
    itemDAO.updateContactEmail(item, contactEmail, modifier);
    itemDAO.updateContactPhone(item, contactPhone, modifier);
    itemDAO.updateTermsOfDelivery(item, termsOfDelivery, modifier);
    itemDAO.updateAllowDelivery(item, allowDelivery, modifier);
    itemDAO.updateAllowPickup(item, allowPickup, modifier);
    itemDAO.updateDeliveryPrice(item, deliveryPrice, modifier);
    itemDAO.updateDeliveryCurrency(item, deliveryCurrency, modifier);
    itemDAO.updateBusinessCode(item, businessCode, modifier);
    itemDAO.updateBusinessName(item, businessName, modifier);
    
    return item;
  }

  /**
   * Update item sold amount value
   *
   * @param item item
   * @param soldAmount sold amount
   * @param modifier modifier
   * 
   * @return updated item
   */
  public Item updateItemSoldAmount(Item item, Long soldAmount, UUID modifier) {
    itemDAO.updateSoldAmount(item, soldAmount, modifier);
    return item;
  }
  
  /**
   * Deletes an item
   * 
   * @param item item to be deleted
   */
  public void deleteItem(Item item) {
    itemMetaDAO.listByItem(item).stream().forEach(itemMetaDAO::delete);
    deleteItemImages(item);
    deleteItemUsers(item);
    deleteItemReservations(item);
    itemDAO.delete(item);
    itemIndexHandler.deleteIndexable(item.getId());
  }

  /**
   * Creates an item image
   * 
   * @param item item
   * @param contentType image content type
   * @param url image URL
   * @return created item image
   */
  public ItemImage createImageItem(Item item, String contentType, String url) {
    return  itemImageDAO.create(UUID.randomUUID(), url, contentType, item);
  }

  /**
   * Lists item images
   * 
   * @param item item
   * @return images
   */
  public List<ItemImage> listItemImages(Item item) {
    return itemImageDAO.listByItem(item);
  }
  
  /**
   * Deletes all images related to specified item
   * 
   * @param item item
   */
  public void deleteItemImages(Item item) {
    listItemImages(item).stream().forEach(itemImageDAO::delete);
  }

    /**
   * Creates an item user
   * 
   * @param item item
   * @param userId user id
   * 
   * @return created item user
   */
  public ItemUser createItemUser(Item item, UUID userId) {
    return  itemUserDAO.create(UUID.randomUUID(), userId, item);
  }

  /**
   * Lists item users
   * 
   * @param item item
   * @return users
   */
  public List<ItemUser> listItemUsers(Item item) {
    return itemUserDAO.listByItem(item);
  }
  
  /**
   * Deletes all users related to specified item
   * 
   * @param item item
   */
  public void deleteItemUsers(Item item) {
    listItemUsers(item).stream().forEach(itemUserDAO::delete);
  }
  
  /**
   * Deletes all item reservations
   * 
   * @param item item
   */
  public void deleteItemReservations(Item item) {
    itemReservationDAO.listByItem(item).stream()
      .forEach(this::deleteItemReservation);
  }

  /**
   * Searches items
   * 
   * @param nearLat prefer items near geo point
   * @param nearLon prefer items near geo point
   * @param sellerIds seller ids. Ignored if null
   * @param categories filter by categories. Ignored if null
   * @param locations filter by locations. Ignored if null
   * @param search Search by free-text. Ignored if null
   * @param includeExhausted whether to include items without any items left
   * @param firstResult result offset
   * @param maxResults maximum number of results returned
   * @return search result
   */
  @SuppressWarnings ("squid:S00107")
  public SearchResult<Item> searchItems(Double nearLat, Double nearLon, List<UUID> sellerIds, List<Category> categories, List<Location> locations, String search, 
      UUID currentUserId, boolean includeExhausted, Long firstResult, Long maxResults, List<ItemListSort> sorts) {
    
    List<UUID> categoryIds = categoryController.listTreeCategories(categories).stream()
      .map(Category::getId)
      .collect(Collectors.toList());

    List<UUID> locationIds = locations == null ? null : locations.stream()
      .map(Location::getId)
      .collect(Collectors.toList());

    SearchResult<UUID> searchResult = itemSearcher.searchItems(nearLat, nearLon, sellerIds,categoryIds, locationIds, 
        search,  currentUserId, includeExhausted, firstResult, maxResults, sorts);

    List<Item> items = searchResult.getResult().stream()
      .map(itemDAO::findById)
      .filter(Objects::nonNull)
      .collect(Collectors.toList());

    return new SearchResult<>(items, searchResult.getTotalHits());
  }

  /**
   * List metas by item
   * 
   * @param item item
   * @return item metas
   */
  public List<ItemMeta> listMetas(Item item) {
    return itemMetaDAO.listByItem(item); 
  }

  /**
   * Sets resource id value for an item
   * 
   * @param item item
   * @param resourceId resource id
   * @param value value
   * @return created or updatedmeta entity. Null if given value is null
   */
  public Item setResourceId(Item item, UUID resourceId, UUID lastModifier) {
    return itemDAO.updateResourceId(item, resourceId, lastModifier);
  }


  /**
   * Sets meta value for an item
   * 
   * @param item item
   * @param key key
   * @param value value
   * @return created or updatedmeta entity. Null if given value is null
   */
  public ItemMeta setMeta(Item item, String key, String value) {
    ItemMeta itemMeta = itemMetaDAO.findByItemAndKey(item, key);
    if (itemMeta == null) {
      if (value == null) {
        return null;
      }

      return itemMetaDAO.create(UUID.randomUUID(), item, key, value);
    } else {
      if (value == null) {
        itemMetaDAO.delete(itemMeta);
        return null;
      }

      return itemMetaDAO.updateValue(itemMeta, value);
    }
  }

  /**
   * Deletes category metas with keys not in set
   * 
   * @param category
   * @param keys
   */
  public void deleteMetasNotIn(Item item, Set<String> keys) {
    itemMetaDAO.listByKeyNotIn(item, keys).stream().forEach(itemMetaDAO::delete);
  }

  /**
   * Create new ItemReservation 
   * 
   * @param item item
   * @param amount amount
   * @return
   */
  public ItemReservation createResevation(Item item, Long amount) {
    ItemReservation result = itemReservationDAO.create(UUID.randomUUID(), item, OffsetDateTime.now().plus(RESERVATION_EXPIRE_MINUTES, ChronoUnit.MINUTES), amount);
    itemIndexEvent.fire(new ItemIndexEvent(item.getId()));
    return result;
  }

  /**
   * Finds an item reservation
   * 
   * @param itemId item reservation id
   * @return item reservation or null if not found
   */
  public ItemReservation findItemReservation(UUID itemReservationId) {
    return itemReservationDAO.findById(itemReservationId);
  }
  
  /**
   * Returns total amount of reservations for an item
   * 
   * @param item item
   * @return total amount of reservations for an item
   */
  public long countReservedAmountByItem(Item item) {
    return itemReservationDAO.listByItem(item).stream().mapToLong(ItemReservation::getAmount).sum();
  }
  
  /**
   * Deletes expired reservations
   */
  public void deleteExpiredReservations() {
    itemReservationDAO.listExpired().stream().forEach(itemReservationDAO::delete);
  } 

  /**
   * Deletes an item reservation
   * 
   * @param itemReservation item reservation
   */
  public void deleteItemReservation(ItemReservation itemReservation) {
    itemReservationDAO.delete(itemReservation);
  }
  
  /**
   * Generates an unique slug
   * 
   * @param slug preferred slug
   * @return unique slug
   */
  private String getUniqueSlug(String slug) {
    String result = slug;
    int iteration = 0;

    while (itemDAO.findBySlug(result) != null) {
      iteration++;
      result = String.format("%s-%d", slug, iteration);
    }

	  return result;
  }
}
