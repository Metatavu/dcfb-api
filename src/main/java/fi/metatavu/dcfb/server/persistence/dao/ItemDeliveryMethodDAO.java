package fi.metatavu.dcfb.server.persistence.dao;

import java.util.Currency;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import fi.metatavu.dcfb.server.persistence.model.Item;
import fi.metatavu.dcfb.server.persistence.model.ItemDeliveryMethod;
import fi.metatavu.dcfb.server.persistence.model.ItemDeliveryMethod_;
import fi.metatavu.dcfb.server.persistence.model.LocalizedEntry;

/**
 * DAO for ItemDeliveryMethod entity
 * 
 * @author Antti Leppä
 */
public class ItemDeliveryMethodDAO extends AbstractDAO<ItemDeliveryMethod> {

  /**
  * Creates new itemDeliveryMethod
  *
  * @param item item
  * @param currency currency
  * @param price price
  * @param title title
  * @param lastModifier modifier
  */
  public ItemDeliveryMethod create(Item item, Currency currency, String price, LocalizedEntry title) {
    ItemDeliveryMethod itemDeliveryMethod = new ItemDeliveryMethod();
    itemDeliveryMethod.setItem(item);
    itemDeliveryMethod.setCurrency(currency);
    itemDeliveryMethod.setPrice(price);
    itemDeliveryMethod.setTitle(title);
    return persist(itemDeliveryMethod);
  }
  
  /**
   * List item delivery methods by item
   * 
   * @return item delivery methods
   */
  public List<ItemDeliveryMethod> listByItem(Item item) {
    EntityManager entityManager = getEntityManager();

    CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
    CriteriaQuery<ItemDeliveryMethod> criteria = criteriaBuilder.createQuery(ItemDeliveryMethod.class);
    Root<ItemDeliveryMethod> root = criteria.from(ItemDeliveryMethod.class);
    criteria.select(root);
    criteria.where(
      criteriaBuilder.equal(root.get(ItemDeliveryMethod_.item), item)
    );

    return entityManager.createQuery(criteria).getResultList();
  }
  
  /**
  * Updates item
  *
  * @param item item
  * @return updated itemDeliveryMethod
  */
  public ItemDeliveryMethod updateItem(ItemDeliveryMethod itemDeliveryMethod, Item item) {
    itemDeliveryMethod.setItem(item);
    return persist(itemDeliveryMethod);
  }

  /**
  * Updates currency
  *
  * @param currency currency
  * @return updated itemDeliveryMethod
  */
  public ItemDeliveryMethod updateCurrency(ItemDeliveryMethod itemDeliveryMethod, Currency currency) {
    itemDeliveryMethod.setCurrency(currency);
    return persist(itemDeliveryMethod);
  }

  /**
  * Updates price
  *
  * @param price price
  * @return updated itemDeliveryMethod
  */
  public ItemDeliveryMethod updatePrice(ItemDeliveryMethod itemDeliveryMethod, String price) {
    itemDeliveryMethod.setPrice(price);
    return persist(itemDeliveryMethod);
  }

  /**
  * Updates title
  *
  * @param title title
  * @return updated itemDeliveryMethod
  */
  public ItemDeliveryMethod updateTitle(ItemDeliveryMethod itemDeliveryMethod, LocalizedEntry title) {
    itemDeliveryMethod.setTitle(title);
    return persist(itemDeliveryMethod);
  }

}
