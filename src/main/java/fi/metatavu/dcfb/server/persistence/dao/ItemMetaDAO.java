package fi.metatavu.dcfb.server.persistence.dao;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import fi.metatavu.dcfb.server.persistence.model.Item;
import fi.metatavu.dcfb.server.persistence.model.ItemMeta;
import fi.metatavu.dcfb.server.persistence.model.ItemMeta_;

/**
 * DAO for ItemMeta entity
 * 
 * @author Antti Leppä
 */
public class ItemMetaDAO extends AbstractDAO<ItemMeta> {
  
  /**
   * Creates new itemMeta
   *
   * @param item item
   * @param key key
   * @param value value
   * @return created itemMeta
   */
  public ItemMeta create(UUID id, Item item, String key, String value) {
    ItemMeta itemMeta = new ItemMeta();
    itemMeta.setId(id);
    itemMeta.setItem(item);
    itemMeta.setKey(key);
    itemMeta.setValue(value);
    return persist(itemMeta);
  }

  /**
   * Finds item meta by item and key
   * 
   * @return item meta or null if not found
   */
  public ItemMeta findByItemAndKey(Item item, String key) {
    EntityManager entityManager = getEntityManager();

    CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
    CriteriaQuery<ItemMeta> criteria = criteriaBuilder.createQuery(ItemMeta.class);
    Root<ItemMeta> root = criteria.from(ItemMeta.class);
    criteria.select(root);
    criteria.where(
      criteriaBuilder.and(  
        criteriaBuilder.equal(root.get(ItemMeta_.key), key),
        criteriaBuilder.equal(root.get(ItemMeta_.item), item)
      )
    );

    return getSingleResult(entityManager.createQuery(criteria));
  }

  /**
   * List item meta by item
   * 
   * @return list of item metas
   */
  public List<ItemMeta> listByItem(Item item) {
    EntityManager entityManager = getEntityManager();

    CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
    CriteriaQuery<ItemMeta> criteria = criteriaBuilder.createQuery(ItemMeta.class);
    Root<ItemMeta> root = criteria.from(ItemMeta.class);
    criteria.select(root);
    criteria.where(
      criteriaBuilder.equal(root.get(ItemMeta_.item), item)
    );

    return entityManager.createQuery(criteria).getResultList();
  }

  /**
   * List item metas where key is not
   * 
   * @return item meta or null if not found
   */
  public List<ItemMeta> listByKeyNotIn(Item item, Collection<String> keys) {
    if (keys == null || keys.isEmpty()) {
      return Collections.emptyList();
    }

    EntityManager entityManager = getEntityManager();

    CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
    CriteriaQuery<ItemMeta> criteria = criteriaBuilder.createQuery(ItemMeta.class);
    Root<ItemMeta> root = criteria.from(ItemMeta.class);
    criteria.select(root);
    criteria.where(
      criteriaBuilder.and(
        criteriaBuilder.equal(root.get(ItemMeta_.item), item),
        criteriaBuilder.not(root.get(ItemMeta_.key).in(keys))
      )
    );

    return entityManager.createQuery(criteria).getResultList();
  }

   /**
   * Updates item
   *
   * @param item item
   * @return updated itemMeta
   */
   public ItemMeta updateItem(ItemMeta itemMeta, Item item) {
     itemMeta.setItem(item);
     return persist(itemMeta);
   }

   /**
   * Updates key
   *
   * @param key key
   * @return updated itemMeta
   */
   public ItemMeta updateKey(ItemMeta itemMeta, String key) {
     itemMeta.setKey(key);
     return persist(itemMeta);
   }

   /**
   * Updates value
   *
   * @param value value
   * @return updated itemMeta
   */
   public ItemMeta updateValue(ItemMeta itemMeta, String value) {
     itemMeta.setValue(value);
     return persist(itemMeta);
   }
   
}
