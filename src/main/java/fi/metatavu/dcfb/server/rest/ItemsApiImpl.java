package fi.metatavu.dcfb.server.rest;

import java.time.OffsetDateTime;
import java.util.Currency;
import java.util.UUID;

import javax.ejb.Stateful;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.core.Response;

import org.apache.commons.lang3.StringUtils;

import fi.metatavu.dcfb.server.items.CategoryController;
import fi.metatavu.dcfb.server.items.ItemController;
import fi.metatavu.dcfb.server.persistence.model.Category;
import fi.metatavu.dcfb.server.persistence.model.LocalizedEntry;
import fi.metatavu.dcfb.server.rest.model.Item;
import fi.metatavu.dcfb.server.rest.translate.ItemTranslator;

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
  public Response listItems(String categoryIdsParam, String search, Long firstResult, Long maxResults) throws Exception {
    // TODO: implement
    
    return null;
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

    return createOk(itemTranslator.translateItem(item));
  }

}
