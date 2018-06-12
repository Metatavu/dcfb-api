package fi.metatavu.dcfb.server.items;

import java.util.List;
import java.util.UUID;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import fi.metatavu.dcfb.server.persistence.dao.CategoryDAO;
import fi.metatavu.dcfb.server.persistence.model.Category;
import fi.metatavu.dcfb.server.persistence.model.LocalizedEntry;

/**
 * Category controller
 * 
 * @author Antti Leppä
 */
@ApplicationScoped
public class CategoryController {
  
  @Inject
  private CategoryDAO categoryDAO;

  /**
   * Creates new category
   * 
   * @param parent parent category
   * @param title title
   * @param slug slug
   * @return created category
   */
  public Category createCategory(Category parent, LocalizedEntry title, String slug) {
    return categoryDAO.create(UUID.randomUUID(), parent, title, slug);
  }
  
  /**
   * Find a category
   * 
   * @param id id
   * @return found category or null if not found
   */
  public Category findCategory(UUID id) {
    return categoryDAO.findById(id);
  }

  /**
   * Lists categories
   * 
   * @param firstResult first result
   * @param maxResults max results
   * @return list of categories
   */
  public List<Category> listCategories(Long firstResult, Long maxResults) {
    return categoryDAO.listAll(firstResult, maxResults);
  }
  
  /**
   * Update category
   *
   * @param parent parent
   * @param title title
   * @param slug slug
   * @return updated category
   */
  public Category updateCategory(Category category, Category parent, LocalizedEntry title, String slug) {
    categoryDAO.updateParent(category, parent);
    categoryDAO.updateTitle(category, title);
    categoryDAO.updateSlug(category, slug);
    return category;
  }

  /**
   * Deletes a category
   * 
   * @param category category
   */
  public void deleteCategory(Category category) {
    categoryDAO.delete(category);
  }
  
}
