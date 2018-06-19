package fi.metatavu.dcfb.server.rest;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Currency;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.ejb.Stateful;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.core.Response;

import org.apache.commons.lang3.StringUtils;

import fi.metatavu.dcfb.server.items.CategoryController;
import fi.metatavu.dcfb.server.items.ItemController;
import fi.metatavu.dcfb.server.persistence.model.Category;
import fi.metatavu.dcfb.server.persistence.model.LocalizedEntry;
import fi.metatavu.dcfb.server.rest.model.Image;
import fi.metatavu.dcfb.server.rest.model.Item;
import fi.metatavu.dcfb.server.rest.model.ItemListSort;
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
  private ItemController itemController;

  @Inject
  private ItemTranslator itemTranslator;

  @Override
  public Response createItem(Item payload) throws Exception {
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
    
    Currency priceCurrency = null;
    try {
      priceCurrency = payload.getUnitPrice() != null ? Currency.getInstance(payload.getUnitPrice().getCurrency()) : null;
    } catch (IllegalArgumentException e) {
      return createBadRequest(String.format("Invalid currency %s", e.getMessage()));
    }
    
    LocalizedEntry title = createLocalizedEntry(payload.getTitle());
    LocalizedEntry description = createLocalizedEntry(payload.getDescription());
    String slug = StringUtils.isNotBlank(payload.getSlug()) ? payload.getSlug() : slugifyLocalized(payload.getTitle());
    OffsetDateTime expiresAt = payload.getExpiresAt();
    String unitPrice = payload.getUnitPrice() != null ? payload.getUnitPrice().getPrice() : null;
    Long amount = payload.getAmount();
    String unit = payload.getUnit();
    UUID modifier = getLoggerUserId();
    
    fi.metatavu.dcfb.server.persistence.model.Item item = itemController.createItem(
        title, 
        description, 
        category, 
        slug, 
        expiresAt, 
        unitPrice, 
        priceCurrency, 
        amount, 
        unit, 
        modifier);

    createImages(payload, item);
    
    return createOk(itemTranslator.translateItem(item));
  }

  @Override
  public Response deleteItem(UUID itemId) throws Exception {
    fi.metatavu.dcfb.server.persistence.model.Item item = itemController.findItem(itemId);
    if (item == null) {
      return createNotFound(NOT_FOUND_MESSAGE);
    }
    
    itemController.deleteItem(item);

    return createNoContent();
  }

  @Override
  public Response findItem(UUID itemId) throws Exception {
    fi.metatavu.dcfb.server.persistence.model.Item item = itemController.findItem(itemId);
    if (item == null) {
      return createNotFound(NOT_FOUND_MESSAGE);
    }

    return createOk(itemTranslator.translateItem(item));
  }

  @Override
  public Response listItems(String categoryIdsParam, String search, List<String> sort, Long firstResult, Long maxResults) throws Exception {
    List<Category> categories = null;

    if (categoryIdsParam != null) {
      try {      
        categories = Arrays.stream(StringUtils.split(categoryIdsParam, ','))
          .map(UUID::fromString)
          .map(categoryId -> {
            Category category = categoryController.findCategory(categoryId);
            if (category == null) {
              throw new IllegalArgumentException(String.format("Could not find category %s", categoryId));
            }
            
            return category;
          })
          .collect(Collectors.toList());
      } catch (IllegalArgumentException e) {
        return createBadRequest(e.getMessage());
      }
    }

    List<ItemListSort> sorts = null;
    try {
      sorts = getEnumListParameter(ItemListSort.class, sort);
    } catch (IllegalArgumentException e) {
      return createBadRequest(e.getMessage());
    }

    SearchResult<fi.metatavu.dcfb.server.persistence.model.Item> searchResult = itemController.searchItems(categories, search, firstResult, maxResults, sorts);

    return createOk(itemTranslator.translateItems(searchResult.getResult()), searchResult.getTotalHits());
  }

  @Override
  public Response updateItem(UUID itemId, Item payload) throws Exception {
    fi.metatavu.dcfb.server.persistence.model.Item item = itemController.findItem(itemId);
    if (item == null) {
      return createNotFound(NOT_FOUND_MESSAGE);
    }
    
    LocalizedEntry title = updateLocalizedEntry(item.getTitle(), payload.getTitle());
    LocalizedEntry description = updateLocalizedEntry(item.getDescription(), payload.getDescription());
    String slug = payload.getSlug();
    OffsetDateTime expiresAt = payload.getExpiresAt();
    String unitPrice = payload.getUnitPrice() != null ? payload.getUnitPrice().getPrice() : null;
    Long amount = payload.getAmount();
    String unit = payload.getUnit();
    UUID modifier = getLoggerUserId();

    Category category = categoryController.findCategory(payload.getCategoryId());
    if (category == null) {
      return createBadRequest(String.format("Invalid category %s", payload.getCategoryId()));
    }
    
    Currency priceCurrency = null;
    try {
      priceCurrency = payload.getUnitPrice() != null ? Currency.getInstance(payload.getUnitPrice().getCurrency()) : null;
    } catch (IllegalArgumentException e) {
      return createBadRequest(String.format("Invalid currency %s", e.getMessage()));
    }
    
    item = itemController.updateItem(item, 
        title, 
        description, 
        category, 
        slug, 
        expiresAt, 
        unitPrice, 
        priceCurrency, 
        amount, 
        unit, 
        modifier);
    
    itemController.deleteItemImages(item);
    createImages(payload, item);
    
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

}
