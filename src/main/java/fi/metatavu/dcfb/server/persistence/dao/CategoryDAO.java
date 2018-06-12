package fi.metatavu.dcfb.server.persistence.dao;

import java.util.UUID;

import fi.metatavu.dcfb.server.persistence.model.Category;
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
  * @return created category
  */
  public Category create(UUID id, Category parent, LocalizedEntry title, String slug) {
    Category category = new Category();
    category.setId(id);
    category.setParent(parent);
    category.setTitle(title);
    category.setSlug(slug);
    return persist(category);
  }

  /**
  * Updates parent
  *
  * @param parent parent
  * @return updated category
  */
  public Category updateParent(Category category, Category parent) {
    category.setParent(parent);
    return persist(category);
  }

  /**
  * Updates title
  *
  * @param title title
  * @return updated category
  */
  public Category updateTitle(Category category, LocalizedEntry title) {
    category.setTitle(title);
    return persist(category);
  }

  /**
  * Updates slug
  *
  * @param slug slug
  * @return updated category
  */
  public Category updateSlug(Category category, String slug) {
    category.setSlug(slug);
    return persist(category);
  }


}
