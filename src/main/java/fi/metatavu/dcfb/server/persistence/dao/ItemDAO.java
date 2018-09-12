package fi.metatavu.dcfb.server.persistence.dao;

import java.time.OffsetDateTime;
import java.util.Currency;
import java.util.UUID;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import fi.metatavu.dcfb.server.persistence.model.Category;
import fi.metatavu.dcfb.server.persistence.model.Item;
import fi.metatavu.dcfb.server.persistence.model.Item_;
import fi.metatavu.dcfb.server.persistence.model.LocalizedEntry;
import fi.metatavu.dcfb.server.persistence.model.Location;

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
  * @param soldAmount amount of items sold
  * @param allowPurchaseContactSeller whether item is allowed to purchase directly from the seller
  * @param allowPurchaseCreditCard whether item is allowed to purchase directly with credit card
  * @param lastModifier modifier
  * @param sellerId sellerId
  * @return created item
  */
  @SuppressWarnings ("squid:S00107")
  public Item create(UUID id, LocalizedEntry title, LocalizedEntry description, Category category, Location location, String slug, OffsetDateTime expiresAt, String unitPrice, Currency priceCurrency, Long amount, String unit, boolean visibilityLimited, UUID resourceId, Long soldAmount, Boolean allowPurchaseContactSeller, Boolean allowPurchaseCreditCard, UUID sellerId, UUID lastModifier) {
    Item item = new Item();
    item.setId(id);
    item.setTitle(title);
    item.setDescription(description);
    item.setCategory(category);
    item.setLocation(location);
    item.setSlug(slug);
    item.setExpiresAt(expiresAt);
    item.setUnitPrice(unitPrice);
    item.setPriceCurrency(priceCurrency);
    item.setAmount(amount);
    item.setUnit(unit);
    item.setVisibilityLimited(visibilityLimited);
    item.setResourceId(resourceId);
    item.setSoldAmount(soldAmount);
    item.setSellerId(sellerId);
    item.setAllowPurchaseContactSeller(allowPurchaseContactSeller);
    item.setAllowPurchaseCreditCard(allowPurchaseCreditCard);
    item.setLastModifier(lastModifier);
    return persist(item);
  }

  /**
   * Finds location by slug
   * 
   * @param slug
   * @return location
   */
  public Item findBySlug(String slug) {
    EntityManager entityManager = getEntityManager();

    CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
    CriteriaQuery<Item> criteria = criteriaBuilder.createQuery(Item.class);
    Root<Item> root = criteria.from(Item.class);
    criteria.select(root);
    criteria.where(criteriaBuilder.equal(root.get(Item_.slug), slug));
    return getSingleResult(entityManager.createQuery(criteria));
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
  * Updates location
  *
  * @param location location
  * @param lastModifier modifier
  * @return updated item
  */
  public Item updateLocation(Item item, Location location, UUID lastModifier) {
    item.setLastModifier(lastModifier);
    item.setLocation(location);
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
  
  /**
  * Updates visibility limited
  *
  * @param item item to update  
  * @param visbilityLimited visibility limited
  * @param lastModifier modifier
  * @return updated item
  */
  public Item updateVisibilityLimited(Item item, boolean visibilityLimited, UUID lastModifier) {
    item.setLastModifier(lastModifier);
    item.setVisibilityLimited(visibilityLimited);
    return persist(item);
  }

  /**
   * Updates resourceId
   * @param item item to update  
   * @param resourceId resource id
   * @param lastModifier modifier
   */
  public Item updateResourceId(Item item, UUID resourceId, UUID lastModifier) {
    item.setResourceId(resourceId);
    item.setLastModifier(lastModifier);
    return persist(item);
  }

  /**
   * Updates soldAmount
   * 
   * @param item item to update  
   * @param soldAmount sold amount
   * @param lastModifier modifier
   */
  public Item updateSoldAmount(Item item, Long soldAmount, UUID lastModifier) {
    item.setSoldAmount(soldAmount);
    item.setLastModifier(lastModifier);
    return persist(item);
  }

  /**
   * Updates sellerId
   * @param item item to update  
   * @param sellerId seller id
   * @param lastModifier modifier
   */
  public Item updateSellerId(Item item, UUID sellerId, UUID lastModifier) {
    item.setSellerId(sellerId);
    item.setLastModifier(lastModifier);
    return persist(item);
  }

  /**
   * Updates allowPurchaseContactSeller
   * @param item item to update  
   * @param allowPurchaseContactSeller whether item is allowed to purchase directly from the seller
   * @param lastModifier modifier
   */
  public Item updateAllowPurchaseContactSellerId(Item item, Boolean allowPurchaseContactSeller, UUID lastModifier) {
    item.setAllowPurchaseContactSeller(allowPurchaseContactSeller);
    item.setLastModifier(lastModifier);
    return persist(item);
  }

  /**
   * Updates allowPurchaseContactSeller
   * @param item item to update  
   * @param allowPurchaseContactSeller whether item is allowed to purchase directly with credit card
   * @param lastModifier modifier
   */
  public Item updateAllowPurchaseCreditCard(Item item, Boolean allowPurchaseCreditCard, UUID lastModifier) {
    item.setAllowPurchaseContactSeller(allowPurchaseCreditCard);
    item.setLastModifier(lastModifier);
    return persist(item);
  }
  
}
