package fi.metatavu.dcfb.server.items;

import java.time.OffsetDateTime;
import java.util.Currency;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import fi.metatavu.dcfb.server.persistence.dao.ItemMetaDAO;
import fi.metatavu.dcfb.server.persistence.dao.ItemUserDAO;
import fi.metatavu.dcfb.server.persistence.dao.ItemDAO;
import fi.metatavu.dcfb.server.persistence.dao.ItemImageDAO;
import fi.metatavu.dcfb.server.persistence.model.Category;
import fi.metatavu.dcfb.server.persistence.model.ItemMeta;
import fi.metatavu.dcfb.server.persistence.model.ItemUser;
import fi.metatavu.dcfb.server.persistence.model.Item;
import fi.metatavu.dcfb.server.persistence.model.ItemImage;
import fi.metatavu.dcfb.server.persistence.model.LocalizedEntry;
import fi.metatavu.dcfb.server.persistence.model.Location;
import fi.metatavu.dcfb.server.rest.model.ItemListSort;
import fi.metatavu.dcfb.server.search.handlers.ItemIndexHandler;
import fi.metatavu.dcfb.server.search.searchers.ItemSearcher;
import fi.metatavu.dcfb.server.search.searchers.SearchResult;

@ApplicationScoped
public class ItemController {

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
   * @param modifier modifier
   * @return created item
   */
  @SuppressWarnings ("squid:S00107")
  public Item createItem(LocalizedEntry title, LocalizedEntry description, Category category, Location location, String slug, OffsetDateTime expiresAt, String unitPrice, Currency priceCurrency, Long amount, String unit, boolean visibilityLimited, UUID resourceId, UUID modifier) {
    return itemDAO.create(UUID.randomUUID(), title, description, category, location, getUniqueSlug(slug), expiresAt, unitPrice, priceCurrency, amount, unit, visibilityLimited, resourceId, modifier);
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
   * @param item item
   * @param title title
   * @param description description
   * @param category category
   * @param slug slug
   * @param expiresAt expiresAt
   * @param unitPrice unitPrice
   * @param priceCurrency priceCurrency
   * @param amount amount
   * @param unit unit
   * @param visibilityLimited visibility limited
   * @param modifier modifier
   * @return updated item
   */
  @SuppressWarnings ("squid:S00107")
  public Item updateItem(Item item, LocalizedEntry title, LocalizedEntry description, Category category, Location location, String slug, OffsetDateTime expiresAt, String unitPrice, Currency priceCurrency, Long amount, String unit, boolean visibilityLimited, UUID modifier) {
    itemDAO.updateTitle(item, title, modifier);
    itemDAO.updateDescription(item, description, modifier);
    itemDAO.updateCategory(item, category, modifier);
    itemDAO.updateLocation(item, location, modifier);
    itemDAO.updateSlug(item, slug, modifier);
    itemDAO.updateExpiresAt(item, expiresAt, modifier);
    itemDAO.updateUnitPrice(item, unitPrice, modifier);
    itemDAO.updatePriceCurrency(item, priceCurrency, modifier);
    itemDAO.updateAmount(item, amount, modifier);
    itemDAO.updateVisibilityLimited(item, visibilityLimited, modifier);
    itemDAO.updateUnit(item, unit, modifier);
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
   * Searches items
   * 
   * @param categories filter by categories. Ignored if null
   * @param locations filter by locations. Ignored if null
   * @param search Search by free-text. Ignored if null
   * @param firstResult result offset
   * @param maxResults maximum number of results returned
   * @return search result
   */
  public SearchResult<Item> searchItems(List<Category> categories, List<Location> locations, String search, UUID currentUserId, Long firstResult, Long maxResults, List<ItemListSort> sorts) {
    List<UUID> categoryIds = categories == null ? null : categories.stream()
      .map(Category::getId)
      .collect(Collectors.toList());

    List<UUID> locationIds = locations == null ? null : locations.stream()
      .map(Location::getId)
      .collect(Collectors.toList());

    SearchResult<UUID> searchResult = itemSearcher.searchItems(categoryIds, locationIds, search, currentUserId, firstResult, maxResults, sorts);

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
