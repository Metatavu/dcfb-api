package fi.metatavu.dcfb.server.categories;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import fi.metatavu.dcfb.server.persistence.dao.CategoryDAO;
import fi.metatavu.dcfb.server.persistence.dao.CategoryMetaDAO;
import fi.metatavu.dcfb.server.persistence.model.Category;
import fi.metatavu.dcfb.server.persistence.model.CategoryMeta;
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

  @Inject
  private CategoryMetaDAO categoryMetaDAO;

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
    return categoryDAO.create(UUID.randomUUID(), parent, title, getUniqueSlug(slug), lastModifier);
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
   * @param slug filter results by slug. Ignored if null
   * @param search Search by free-text. Ignored if null
   * @param firstResult result offset
   * @param maxResults maximum number of results returned
   * @return search result
   */
  public SearchResult<Category> searchCategories(Category parent, String slug, String search, Long firstResult, Long maxResults, List<CategoryListSort> sorts) {
    SearchResult<UUID> searchResult = categorySearcher.searchCategories(parent != null ? parent.getId() : null, slug, search, firstResult, maxResults, sorts);

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
    categoryMetaDAO.listByCategory(category).stream().forEach(categoryMetaDAO::delete);
    categoryDAO.delete(category);
    categoryIndexHandler.deleteIndexable(category.getId());
  }

  /**
   * List metas by category
   * 
   * @param category category
   * @return category metas
   */
  public List<CategoryMeta> listMetas(Category category) {
    return categoryMetaDAO.listByCategory(category); 
  }

  /**
   * Sets meta value for a category
   * 
   * @param category category
   * @param key key
   * @param value value
   * @return created or updatedmeta entity. Null if given value is null
   */
  public CategoryMeta setMeta(Category category, String key, String value) {
    CategoryMeta categoryMeta = categoryMetaDAO.findByCategoryAndKey(category, key);
    if (categoryMeta == null) {
      if (value == null) {
        return null;
      }

      return categoryMetaDAO.create(UUID.randomUUID(), category, key, value);
    } else {
      if (value == null) {
        categoryMetaDAO.delete(categoryMeta);
        return null;
      }

      return categoryMetaDAO.updateValue(categoryMeta, value);
    }
  }

  /**
   * Deletes category metas with keys not in set
   * 
   * @param category
   * @param keys
   */
  public void deleteMetasNotIn(Category category, Set<String> keys) {
    categoryMetaDAO.listByKeyNotIn(category, keys).stream().forEach(categoryMetaDAO::delete);
  }
  
  /**
   * Generates an unique slug
   * 
   * @param slug preferred slug
   * @return unique slug
   */
  private String getUniqueSlug(String slug) {
    String result = slug;
    int iteration = 0;

    while (categoryDAO.findBySlug(result) != null) {
      iteration++;
      result = String.format("%s-%d", slug, iteration);
    }

	  return result;
  }
}
