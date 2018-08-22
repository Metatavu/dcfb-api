package fi.metatavu.dcfb.server.persistence.dao;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import fi.metatavu.dcfb.server.persistence.model.Item;
import fi.metatavu.dcfb.server.persistence.model.ItemReservation;
import fi.metatavu.dcfb.server.persistence.model.ItemReservation_;

/**
 * DAO for ItemReservation
 * 
 * @author Antti Leppä
 * @author Heikki Kurhinen
 */
public class ItemReservationDAO extends AbstractDAO<ItemReservation> {

  /**
  * Creates new item reservation
  *
  * @param id id
  * @param item 
  * @return created item
  */
  @SuppressWarnings ("squid:S00107")
  public ItemReservation create(UUID id, Item item, OffsetDateTime expiresAt, Long amount) {
    ItemReservation itemReservation = new ItemReservation();
    itemReservation.setId(id);
    itemReservation.setExpiresAt(expiresAt);
    itemReservation.setAmount(amount);
    itemReservation.setItem(item);
    return persist(itemReservation);
  }
  
  /**
   * Lists reservations by item
   * 
   * @param item item
   * @return reservations 
   */
  public List<ItemReservation> listByItem(Item item) {
    EntityManager entityManager = getEntityManager();

    CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
    CriteriaQuery<ItemReservation> criteria = criteriaBuilder.createQuery(ItemReservation.class);
    Root<ItemReservation> root = criteria.from(ItemReservation.class);
    criteria.select(root);
    criteria.where(criteriaBuilder.equal(root.get(ItemReservation_.item), item));
    
    return entityManager.createQuery(criteria).getResultList();
  }  
  
  /**
   * Lists expired reservations
   * 
   * @return expired reservations
   */
  public List<ItemReservation> listExpired() {
    EntityManager entityManager = getEntityManager();

    CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
    CriteriaQuery<ItemReservation> criteria = criteriaBuilder.createQuery(ItemReservation.class);
    Root<ItemReservation> root = criteria.from(ItemReservation.class);
    criteria.select(root);
    criteria.where(criteriaBuilder.lessThan(root.get(ItemReservation_.expiresAt), OffsetDateTime.now()));
    
    return entityManager.createQuery(criteria).getResultList();
  }    

}
