package fi.metatavu.dcfb.server.persistence.dao;

import java.time.OffsetDateTime;
import java.util.Currency;
import java.util.UUID;

import fi.metatavu.dcfb.server.persistence.model.Category;
import fi.metatavu.dcfb.server.persistence.model.Item;
import fi.metatavu.dcfb.server.persistence.model.LocalizedEntry;

/**
 * DAO for Item entity
 * 
 * @author Antti Leppä
 */
public class ItemDAO extends AbstractDAO<Item> {

  /**
  * Creates new item
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
  * @return created item
  * @param lastModifier modifier
  */
  @SuppressWarnings ("squid:S00107")
  public Item create(UUID id, LocalizedEntry title, LocalizedEntry description, Category category, String slug, OffsetDateTime expiresAt, String unitPrice, Currency priceCurrency, Long amount, String unit, UUID lastModifier) {
    Item item = new Item();
    item.setId(id);
    item.setTitle(title);
    item.setDescription(description);
    item.setCategory(category);
    item.setSlug(slug);
    item.setExpiresAt(expiresAt);
    item.setUnitPrice(unitPrice);
    item.setPriceCurrency(priceCurrency);
    item.setAmount(amount);
    item.setUnit(unit);
    item.setLastModifier(lastModifier);
    return persist(item);
  }

  /**
  * Updates title
  *
  * @param title title
  * @param lastModifier modifier
  * @return updated item
  */
  public Item updateTitle(Item item, LocalizedEntry title, UUID lastModifier) {
    item.setLastModifier(lastModifier);
    item.setTitle(title);
    return persist(item);
  }

  /**
  * Updates description
  *
  * @param description description
  * @param lastModifier modifier
  * @return updated item
  */
  public Item updateDescription(Item item, LocalizedEntry description, UUID lastModifier) {
    item.setLastModifier(lastModifier);
    item.setDescription(description);
    return persist(item);
  }

  /**
  * Updates category
  *
  * @param category category
  * @param lastModifier modifier
  * @return updated item
  */
  public Item updateCategory(Item item, Category category, UUID lastModifier) {
    item.setLastModifier(lastModifier);
    item.setCategory(category);
    return persist(item);
  }

  /**
  * Updates slug
  *
  * @param slug slug
  * @param lastModifier modifier
  * @return updated item
  */
  public Item updateSlug(Item item, String slug, UUID lastModifier) {
    item.setLastModifier(lastModifier);
    item.setSlug(slug);
    return persist(item);
  }

  /**
  * Updates expiresAt
  *
  * @param expiresAt expiresAt
  * @param lastModifier modifier
  * @return updated item
  */
  public Item updateExpiresAt(Item item, OffsetDateTime expiresAt, UUID lastModifier) {
    item.setLastModifier(lastModifier);
    item.setExpiresAt(expiresAt);
    return persist(item);
  }

  /**
  * Updates unitPrice
  *
  * @param unitPrice unitPrice
  * @param lastModifier modifier
  * @return updated item
  */
  public Item updateUnitPrice(Item item, String unitPrice, UUID lastModifier) {
    item.setLastModifier(lastModifier);
    item.setUnitPrice(unitPrice);
    return persist(item);
  }

  /**
  * Updates priceCurrency
  *
  * @param priceCurrency priceCurrency
  * @param lastModifier modifier
  * @return updated item
  */
  public Item updatePriceCurrency(Item item, Currency priceCurrency, UUID lastModifier) {
    item.setLastModifier(lastModifier);
    item.setPriceCurrency(priceCurrency);
    return persist(item);
  }

  /**
  * Updates amount
  *
  * @param amount amount
  * @param lastModifier modifier
  * @return updated item
  */
  public Item updateAmount(Item item, Long amount, UUID lastModifier) {
    item.setLastModifier(lastModifier);
    item.setAmount(amount);
    return persist(item);
  }

  /**
  * Updates unit
  *
  * @param unit unit
  * @param lastModifier modifier
  * @return updated item
  */
  public Item updateUnit(Item item, String unit, UUID lastModifier) {
    item.setLastModifier(lastModifier);
    item.setUnit(unit);
    return persist(item);
  }

}
