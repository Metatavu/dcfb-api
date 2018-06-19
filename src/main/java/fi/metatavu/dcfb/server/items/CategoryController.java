package fi.metatavu.dcfb.server.items;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import fi.metatavu.dcfb.server.persistence.dao.CategoryDAO;
import fi.metatavu.dcfb.server.persistence.model.Category;
import fi.metatavu.dcfb.server.persistence.model.LocalizedEntry;
import fi.metatavu.dcfb.server.rest.model.CategoryListSort;
import fi.metatavu.dcfb.server.search.handlers.CategoryIndexHandler;
import fi.metatavu.dcfb.server.search.searchers.CategorySearcher;
import fi.metatavu.dcfb.server.search.searchers.SearchResult;

/**
 * Category controller
 * 
 * @author Antti Leppä
 */
@ApplicationScoped
public class CategoryController {

  @Inject
  private CategorySearcher categorySearcher;

  @Inject
  private CategoryIndexHandler categoryIndexHandler;
  
  @Inject
  private CategoryDAO categoryDAO;

  /**
   * Creates new category
   * 
   * @param parent parent category
   * @param title title
   * @param slug slug
   * @param lastModifier last modifier
   * @return created category
   */
  public Category createCategory(Category parent, LocalizedEntry title, String slug, UUID lastModifier) {
    return categoryDAO.create(UUID.randomUUID(), parent, title, slug, lastModifier);
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
   * Searches categories
   * 
   * @param parent filter results by parent category. Ignored if null
   * @param search Search by free-text. Ignored if null
   * @param firstResult result offset
   * @param maxResults maximum number of results returned
   * @return search result
   */
  public SearchResult<Category> searchCategories(Category parent, String search, Long firstResult, Long maxResults, List<CategoryListSort> sorts) {
    SearchResult<UUID> searchResult = categorySearcher.searchCategories(parent != null ? parent.getId() : null, search, firstResult, maxResults, sorts);

    List<Category> categories = searchResult.getResult().stream()
      .map(categoryDAO::findById)
      .filter(Objects::nonNull)
      .collect(Collectors.toList());

    return new SearchResult<>(categories, searchResult.getTotalHits());
  }

  /**
   * Update category
   *
   * @param parent parent
   * @param title title
   * @param slug slug
   * @param lastModifier last modifier
   * @return updated category
   */
  public Category updateCategory(Category category, Category parent, LocalizedEntry title, String slug, UUID lastModifier) {
    categoryDAO.updateParent(category, parent, lastModifier);
    categoryDAO.updateTitle(category, title, lastModifier);
    categoryDAO.updateSlug(category, slug, lastModifier);
    return category;
  }

  /**
   * Deletes a category
   * 
   * @param category category
   */
  public void deleteCategory(Category category) {
    categoryDAO.delete(category);
    categoryIndexHandler.deleteIndexable(category.getId());
  }
  
}
