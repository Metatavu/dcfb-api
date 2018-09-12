package fi.metatavu.dcfb.server.persistence.dao;

import java.util.List;
import java.util.UUID;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import fi.metatavu.dcfb.server.persistence.model.Category;
import fi.metatavu.dcfb.server.persistence.model.Category_;
import fi.metatavu.dcfb.server.persistence.model.LocalizedEntry;

/**
 * DAO for Category entity
 * 
 * @author Antti Leppä
 */
public class CategoryDAO extends AbstractDAO<Category> {

  /**
  * Creates new category
  *
  * @param parent parent
  * @param title title
  * @param slug slug
  * @param lastModifier last modifier
  * @return created category
  */
  public Category create(UUID id, Category parent, LocalizedEntry title, String slug, UUID lastModifier) {
    Category category = new Category();
    category.setId(id);
    category.setParent(parent);
    category.setTitle(title);
    category.setSlug(slug);
    category.setLastModifier(lastModifier);

    return persist(category);
  }

  /**
   * Finds category by slug
   * 
   * @param slug
   * @return category
   */
  public Category findBySlug(String slug) {
    EntityManager entityManager = getEntityManager();

    CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
    CriteriaQuery<Category> criteria = criteriaBuilder.createQuery(Category.class);
    Root<Category> root = criteria.from(Category.class);
    criteria.select(root);
    criteria.where(criteriaBuilder.equal(root.get(Category_.slug), slug));
    return getSingleResult(entityManager.createQuery(criteria));
  }

  /**
   * List categories by parent category
   * 
   * @param parent
   * @return list of categories
   */
  public List<Category> listByParent(Category parent) {
    EntityManager entityManager = getEntityManager();

    CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
    CriteriaQuery<Category> criteria = criteriaBuilder.createQuery(Category.class);
    Root<Category> root = criteria.from(Category.class);
    criteria.select(root);
    criteria.where(criteriaBuilder.equal(root.get(Category_.parent), parent));
    return entityManager.createQuery(criteria).getResultList();
  }

  /**
  * Updates parent
  *
  * @param parent parent
  * @param lastModifier last modifier
  * @return updated category
  */
  public Category updateParent(Category category, Category parent, UUID lastModifier) {
    category.setParent(parent);
    category.setLastModifier(lastModifier);
    return persist(category);
  }

  /**
  * Updates title
  *
  * @param title title
  * @param lastModifier last modifier
  * @return updated category
  */
  public Category updateTitle(Category category, LocalizedEntry title, UUID lastModifier) {
    category.setTitle(title);
    category.setLastModifier(lastModifier);
    return persist(category);
  }

  /**
  * Updates slug
  *
  * @param slug slug
  * @param lastModifier last modifier
  * @return updated category
  */
  public Category updateSlug(Category category, String slug, UUID lastModifier) {
    category.setSlug(slug);
    category.setLastModifier(lastModifier);
    return persist(category);
  }


}
