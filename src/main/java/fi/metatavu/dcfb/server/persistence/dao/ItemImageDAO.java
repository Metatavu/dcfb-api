package fi.metatavu.dcfb.server.persistence.dao;

import java.util.List;
import java.util.UUID;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import fi.metatavu.dcfb.server.persistence.model.Item;
import fi.metatavu.dcfb.server.persistence.model.ItemImage;
import fi.metatavu.dcfb.server.persistence.model.ItemImage_;

/**
 * DAO for ItemImage entity
 * 
 * @author Antti Leppä
 */
public class ItemImageDAO extends AbstractDAO<ItemImage> {

  /**
  * Creates new itemImage
  *
  * @param url URL
  * @param contentType contentType
  * @param item item
  * @return created itemImage
  */
  public ItemImage create(UUID id, String url, String contentType, Item item) {
    ItemImage itemImage = new ItemImage();
    itemImage.setId(id);
    itemImage.setUrl(url);
    itemImage.setContentType(contentType);
    itemImage.setItem(item);
    return persist(itemImage);
  }
  
  /**
   * Lists images by item
   * 
   * @param item item
   * @return images
   */
  public List<ItemImage> listByItem(Item item) {
    EntityManager entityManager = getEntityManager();

    CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
    CriteriaQuery<ItemImage> criteria = criteriaBuilder.createQuery(ItemImage.class);
    Root<ItemImage> root = criteria.from(ItemImage.class);
    criteria.select(root);
    criteria.where(criteriaBuilder.equal(root.get(ItemImage_.item), item));
    
    return entityManager.createQuery(criteria).getResultList();
  }  

  /**
  * Updates URL
  *
  * @param itemImage item image
  * @param url URL
  * @return updated itemImage
  */
  public ItemImage updateUrl(ItemImage itemImage, String url) {
    itemImage.setUrl(url);
    return persist(itemImage);
  }

  /**
  * Updates contentType
  *
  * @param itemImage item image
  * @param contentType contentType
  * @return updated itemImage
  */
  public ItemImage updateContentType(ItemImage itemImage, String contentType) {
    itemImage.setContentType(contentType);
    return persist(itemImage);
  }

}
