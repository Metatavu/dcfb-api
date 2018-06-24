package fi.metatavu.dcfb.server.rest;

import java.time.OffsetDateTime;
import java.util.Currency;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.ejb.Stateful;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.core.Response;

import org.apache.commons.lang3.StringUtils;

import fi.metatavu.dcfb.server.categories.CategoryController;
import fi.metatavu.dcfb.server.items.ItemController;
import fi.metatavu.dcfb.server.locations.LocationController;
import fi.metatavu.dcfb.server.persistence.model.Category;
import fi.metatavu.dcfb.server.persistence.model.LocalizedEntry;
import fi.metatavu.dcfb.server.persistence.model.Location;
import fi.metatavu.dcfb.server.rest.model.Image;
import fi.metatavu.dcfb.server.rest.model.Item;
import fi.metatavu.dcfb.server.rest.model.ItemListSort;
import fi.metatavu.dcfb.server.rest.model.Meta;
import fi.metatavu.dcfb.server.rest.translate.ItemTranslator;
import fi.metatavu.dcfb.server.search.searchers.SearchResult;

/**
 * Items REST Service implementation
 * 
 * @author Antti Leppä
 */
@RequestScoped
@Stateful
public class ItemsApiImpl extends AbstractApi implements ItemsApi {

  @Inject
  private CategoryController categoryController;

  @Inject
  private LocationController locationController;

  @Inject
  private ItemController itemController;

  @Inject
  private ItemTranslator itemTranslator;

  @Override
  public Response createItem(Item payload) throws Exception {
    if (!isRealmUser()) {
      return createForbidden("Anonymous users can not create items");
    }

    if (!isValidLocalizedList(payload.getTitle())) {
      return createBadRequest("Invalid title");
    }

    if (!isValidLocalizedList(payload.getDescription())) {
      return createBadRequest("Invalid description");
    }

    Category category = categoryController.findCategory(payload.getCategoryId());
    if (category == null) {
      return createBadRequest(String.format("Invalid category %s", payload.getCategoryId()));
    }

    Location location = payload.getLocationId() != null ? locationController.findLocation(payload.getLocationId()) : null;
    if (payload.getLocationId() != null && location == null) {
      return createBadRequest(String.format("Invalid location %s", payload.getLocationId()));
    }
    
    Currency priceCurrency = null;
    try {
      priceCurrency = Currency.getInstance(payload.getUnitPrice().getCurrency());
    } catch (IllegalArgumentException e) {
      return createBadRequest(String.format("Invalid currency %s", e.getMessage()));
    }
    
    LocalizedEntry title = createLocalizedEntry(payload.getTitle());
    LocalizedEntry description = createLocalizedEntry(payload.getDescription());
    String slug = StringUtils.isNotBlank(payload.getSlug()) ? payload.getSlug() : slugifyLocalized(payload.getTitle());
    OffsetDateTime expiresAt = payload.getExpiresAt();
    String unitPrice = payload.getUnitPrice().getPrice();
    Long amount = payload.getAmount();
    String unit = payload.getUnit();
    UUID modifier = getLoggerUserId();
    
    fi.metatavu.dcfb.server.persistence.model.Item item = itemController.createItem(
        title, 
        description, 
        category, 
        location,
        slug, 
        expiresAt, 
        unitPrice, 
        priceCurrency, 
        amount, 
        unit, 
        modifier);

    createImages(payload, item);
    setItemMetas(item, payload.getMeta());
    
    return createOk(itemTranslator.translateItem(item));
  }

  @Override
  public Response deleteItem(UUID itemId) throws Exception {
    // TODO: Add resource based permission check

    if (!isRealmUser()) {
      return createForbidden("Anonymous users can not delete items");
    }

    fi.metatavu.dcfb.server.persistence.model.Item item = itemController.findItem(itemId);
    if (item == null) {
      return createNotFound(NOT_FOUND_MESSAGE);
    }
    
    itemController.deleteItem(item);

    return createNoContent();
  }

  @Override
  public Response findItem(UUID itemId) throws Exception {
    // TODO: Add resource based permission check

    fi.metatavu.dcfb.server.persistence.model.Item item = itemController.findItem(itemId);
    if (item == null) {
      return createNotFound(NOT_FOUND_MESSAGE);
    }

    return createOk(itemTranslator.translateItem(item));
  }

  @Override
  public Response listItems(String categoryIdsParam, String locationIdsParam, String search, List<String> sort, Long firstResult, Long maxResults) throws Exception {
    // TODO: Add resource based permission check

    List<Category> categories = null;
    List<Location> locations = null;

    try {
      categories = getListParameter(categoryIdsParam, param -> {
        UUID id = UUID.fromString(param);
        Category category = categoryController.findCategory(id);
        if (category == null) {
          throw new IllegalArgumentException(String.format("Could not find category %s", id));
        }
        
        return category;
      });

      locations = getListParameter(locationIdsParam, param -> {
        UUID id = UUID.fromString(param);
        Location location = locationController.findLocation(id);
        if (location == null) {
          throw new IllegalArgumentException(String.format("Could not find location %s", id));
        }
        
        return location;
      });

    } catch (IllegalArgumentException e) {
      return createBadRequest(e.getMessage());
    }
    
    List<ItemListSort> sorts = null;
    try {
      sorts = getEnumListParameter(ItemListSort.class, sort);
    } catch (IllegalArgumentException e) {
      return createBadRequest(e.getMessage());
    }

    SearchResult<fi.metatavu.dcfb.server.persistence.model.Item> searchResult = itemController.searchItems(categories, locations, search, firstResult, maxResults, sorts);

    return createOk(itemTranslator.translateItems(searchResult.getResult()), searchResult.getTotalHits());
  }

  @Override
  public Response updateItem(UUID itemId, Item payload) throws Exception {
    // TODO: Add resource based permission check

    if (!isRealmUser()) {
      return createForbidden("Anonymous users can not update items");
    }

    fi.metatavu.dcfb.server.persistence.model.Item item = itemController.findItem(itemId);
    if (item == null) {
      return createNotFound(NOT_FOUND_MESSAGE);
    }
    
    LocalizedEntry title = updateLocalizedEntry(item.getTitle(), payload.getTitle());
    LocalizedEntry description = updateLocalizedEntry(item.getDescription(), payload.getDescription());
    String slug = payload.getSlug();
    OffsetDateTime expiresAt = payload.getExpiresAt();
    String unitPrice = payload.getUnitPrice().getPrice();
    Long amount = payload.getAmount();
    String unit = payload.getUnit();
    UUID modifier = getLoggerUserId();

    Category category = categoryController.findCategory(payload.getCategoryId());
    if (category == null) {
      return createBadRequest(String.format("Invalid category %s", payload.getCategoryId()));
    }

    Location location = payload.getLocationId() != null ? locationController.findLocation(payload.getLocationId()) : null;
    if (payload.getLocationId() != null && location == null) {
      return createBadRequest(String.format("Invalid location %s", payload.getLocationId()));
    }
    
    Currency priceCurrency = null;
    try {
      priceCurrency = Currency.getInstance(payload.getUnitPrice().getCurrency());
    } catch (IllegalArgumentException e) {
      return createBadRequest(String.format("Invalid currency %s", e.getMessage()));
    }
    
    item = itemController.updateItem(item, 
        title, 
        description, 
        category,
        location,
        slug, 
        expiresAt, 
        unitPrice, 
        priceCurrency, 
        amount, 
        unit, 
        modifier);
    
    itemController.deleteItemImages(item);
    createImages(payload, item);
    setItemMetas(item, payload.getMeta());
    
    return createOk(itemTranslator.translateItem(item));
  }

  /**
   * Creates item images from REST request
   * 
   * @param payload REST request
   * @param item item
   */
  private void createImages(Item payload, fi.metatavu.dcfb.server.persistence.model.Item item) {
    if (payload.getImages() != null) {
      payload.getImages().stream().forEach(image -> createImage(item, image));
    }
  }

  /**
   * Creates item image from REST object
   * 
   * @param payload REST object
   * @param item item
   */
  private void createImage(fi.metatavu.dcfb.server.persistence.model.Item item, Image image) {
    itemController.createImageItem(item, image.getType(), image.getUrl());
  }

  /**
   * Sets meta values for a item
   * 
   * @param item
   * @param metas
   * @return item
   */
  private fi.metatavu.dcfb.server.persistence.model.Item setItemMetas(fi.metatavu.dcfb.server.persistence.model.Item item, List<Meta> metas) {
    if (metas == null) {
      return item;
    }

    Set<String> usedKeys = new HashSet<>(metas.size());

    for (Meta meta : metas) {
      String key = meta.getKey();
      usedKeys.add(key);
      itemController.setMeta(item, key, meta.getValue());  
    }

    itemController.deleteMetasNotIn(item, usedKeys);

    return item;
  }

}
