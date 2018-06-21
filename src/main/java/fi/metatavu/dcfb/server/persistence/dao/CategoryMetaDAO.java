package fi.metatavu.dcfb.server.persistence.dao;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import fi.metatavu.dcfb.server.persistence.model.Category;
import fi.metatavu.dcfb.server.persistence.model.CategoryMeta;
import fi.metatavu.dcfb.server.persistence.model.CategoryMeta_;

/**
 * DAO for CategoryMeta entity
 * 
 * @author Antti Leppä
 */
public class CategoryMetaDAO extends AbstractDAO<CategoryMeta> {
  
  /**
   * Creates new categoryMeta
   *
   * @param category category
   * @param key key
   * @param value value
   * @return created categoryMeta
   */
  public CategoryMeta create(UUID id, Category category, String key, String value) {
    CategoryMeta categoryMeta = new CategoryMeta();
    categoryMeta.setId(id);
    categoryMeta.setCategory(category);
    categoryMeta.setKey(key);
    categoryMeta.setValue(value);
    return persist(categoryMeta);
  }

  /**
   * Finds category meta by category and key
   * 
   * @return category meta or null if not found
   */
  public CategoryMeta findByCategoryAndKey(Category category, String key) {
    EntityManager entityManager = getEntityManager();

    CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
    CriteriaQuery<CategoryMeta> criteria = criteriaBuilder.createQuery(CategoryMeta.class);
    Root<CategoryMeta> root = criteria.from(CategoryMeta.class);
    criteria.select(root);
    criteria.where(
      criteriaBuilder.and(  
        criteriaBuilder.equal(root.get(CategoryMeta_.key), key),
        criteriaBuilder.equal(root.get(CategoryMeta_.category), category)
      )
    );

    return getSingleResult(entityManager.createQuery(criteria));
  }

  /**
   * List category meta by category
   * 
   * @return list of category metas
   */
  public List<CategoryMeta> listByCategory(Category category) {
    EntityManager entityManager = getEntityManager();

    CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
    CriteriaQuery<CategoryMeta> criteria = criteriaBuilder.createQuery(CategoryMeta.class);
    Root<CategoryMeta> root = criteria.from(CategoryMeta.class);
    criteria.select(root);
    criteria.where(
      criteriaBuilder.equal(root.get(CategoryMeta_.category), category)
    );

    return entityManager.createQuery(criteria).getResultList();
  }

  /**
   * List category metas where key is not
   * 
   * @return category meta or null if not found
   */
  public List<CategoryMeta> listByKeyNotIn(Category category, Collection<String> keys) {
    if (keys == null || keys.isEmpty()) {
      return Collections.emptyList();
    }

    EntityManager entityManager = getEntityManager();

    CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
    CriteriaQuery<CategoryMeta> criteria = criteriaBuilder.createQuery(CategoryMeta.class);
    Root<CategoryMeta> root = criteria.from(CategoryMeta.class);
    criteria.select(root);
    criteria.where(
      criteriaBuilder.and(
        criteriaBuilder.equal(root.get(CategoryMeta_.category), category),
        criteriaBuilder.not(root.get(CategoryMeta_.key).in(keys))
      )
    );

    return entityManager.createQuery(criteria).getResultList();
  }

   /**
   * Updates category
   *
   * @param category category
   * @return updated categoryMeta
   */
   public CategoryMeta updateCategory(CategoryMeta categoryMeta, Category category) {
     categoryMeta.setCategory(category);
     return persist(categoryMeta);
   }

   /**
   * Updates key
   *
   * @param key key
   * @return updated categoryMeta
   */
   public CategoryMeta updateKey(CategoryMeta categoryMeta, String key) {
     categoryMeta.setKey(key);
     return persist(categoryMeta);
   }

   /**
   * Updates value
   *
   * @param value value
   * @return updated categoryMeta
   */
   public CategoryMeta updateValue(CategoryMeta categoryMeta, String value) {
     categoryMeta.setValue(value);
     return persist(categoryMeta);
   }
   
}
