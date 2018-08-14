package fi.metatavu.dcfb.server.persistence.dao;

import java.util.List;
import java.util.UUID;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import fi.metatavu.dcfb.server.persistence.model.Item;
import fi.metatavu.dcfb.server.persistence.model.ItemUser;
import fi.metatavu.dcfb.server.persistence.model.ItemUser_;

/**
 * DAO for ItemUser entity
 * 
 * @author Heikki Kurhinen 
 */
public class ItemUserDAO extends AbstractDAO<ItemUser> {

  /**
  * Creates new itemImage
  *
  * @param id id
  * @param userId user id
  * @param item item
  * @return created itemImage
  */
  public ItemUser create(UUID id, UUID userId, Item item) {
    ItemUser itemUser = new ItemUser();
    itemUser.setId(id);
    itemUser.setItem(item);
    itemUser.setUserId(userId);
    return persist(itemUser);
  }
  
  /**
   * Lists users by item
   * 
   * @param item item
   * @return users 
   */
  public List<ItemUser> listByItem(Item item) {
    EntityManager entityManager = getEntityManager();

    CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
    CriteriaQuery<ItemUser> criteria = criteriaBuilder.createQuery(ItemUser.class);
    Root<ItemUser> root = criteria.from(ItemUser.class);
    criteria.select(root);
    criteria.where(criteriaBuilder.equal(root.get(ItemUser_.item), item));
    
    return entityManager.createQuery(criteria).getResultList();
  }  

}
