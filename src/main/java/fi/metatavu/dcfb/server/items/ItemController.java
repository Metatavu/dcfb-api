package fi.metatavu.dcfb.server.items;

import java.time.OffsetDateTime;
import java.util.Currency;
import java.util.UUID;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import fi.metatavu.dcfb.server.persistence.dao.ItemDAO;
import fi.metatavu.dcfb.server.persistence.model.Category;
import fi.metatavu.dcfb.server.persistence.model.Item;
import fi.metatavu.dcfb.server.persistence.model.LocalizedEntry;

@ApplicationScoped
public class ItemController {
  
  @Inject
  private ItemDAO itemDAO;
  
  /**
   * Create item
   *
   * @param title title
   * @param description description
   * @param category category
   * @param slug slug
   * @param expiresAt expiresAt
   * @param unitPrice unitPrice
   * @param priceCurrency priceCurrency
   * @param amount amount
   * @param unit unit
   * @param modifier modifier
   * @return created item
   */
  @SuppressWarnings ("squid:S00107")
  public Item createItem(LocalizedEntry title, LocalizedEntry description, Category category, String slug, OffsetDateTime expiresAt, String unitPrice, Currency priceCurrency, Long amount, String unit, UUID modifier) {
    return itemDAO.create(UUID.randomUUID(), title, description, category, slug, expiresAt, unitPrice, priceCurrency, amount, unit, modifier);
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
   * @param modifier modifier
   * @return updated item
   */
  @SuppressWarnings ("squid:S00107")
  public Item updateItem(Item item, LocalizedEntry title, LocalizedEntry description, Category category, String slug, OffsetDateTime expiresAt, String unitPrice, Currency priceCurrency, Long amount, String unit, UUID modifier) {
    itemDAO.updateTitle(item, title, modifier);
    itemDAO.updateDescription(item, description, modifier);
    itemDAO.updateCategory(item, category, modifier);
    itemDAO.updateSlug(item, slug, modifier);
    itemDAO.updateExpiresAt(item, expiresAt, modifier);
    itemDAO.updateUnitPrice(item, unitPrice, modifier);
    itemDAO.updatePriceCurrency(item, priceCurrency, modifier);
    itemDAO.updateAmount(item, amount, modifier);
    itemDAO.updateUnit(item, unit, modifier);
    return item;
  }

  /**
   * Deletes an item
   * 
   * @param item item to be deleted
   */
  public void deleteItem(Item item) {
    itemDAO.delete(item);
  }
}
