package fi.metatavu.dcfb.server.rest;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.ejb.Stateful;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.core.Response;

import org.apache.commons.lang3.StringUtils;

import fi.metatavu.dcfb.server.items.CategoryController;
import fi.metatavu.dcfb.server.persistence.model.Category;
import fi.metatavu.dcfb.server.persistence.model.LocalizedEntry;
import fi.metatavu.dcfb.server.rest.model.CategoryListSort;
import fi.metatavu.dcfb.server.rest.model.Meta;
import fi.metatavu.dcfb.server.rest.translate.CategoryTranslator;
import fi.metatavu.dcfb.server.search.searchers.SearchResult;

/**
 * Items REST Service implementation
 * 
 * @author Antti Leppä
 */
@RequestScoped
@Stateful
public class CategoriesApiImpl extends AbstractApi implements CategoriesApi {
  
  @Inject
  private CategoryController categoryController;

  @Inject
  private CategoryTranslator categoryTranslator;

  @Override
  public Response createCategory(fi.metatavu.dcfb.server.rest.model.Category payload) throws Exception {
    Category parent = payload.getParentId() != null ? categoryController.findCategory(payload.getParentId()) : null;
    String slug = StringUtils.isNotBlank(payload.getSlug()) ? payload.getSlug() : slugifyLocalized(payload.getTitle());
    UUID lastModifier = getLoggerUserId();

    if (parent == null && payload.getParentId() != null) {
      return createBadRequest("Invalid parent id");
    }
    
    LocalizedEntry title = createLocalizedEntry(payload.getTitle());
    
    return createOk(categoryTranslator.translateCategory(setCategoryMetas(categoryController.createCategory(parent, title, slug, lastModifier), payload.getMeta())));
  }

  @Override
  public Response deleteCategory(UUID categoryId) throws Exception {
    Category category = categoryController.findCategory(categoryId);
    if (category == null) {
      return createNotFound(NOT_FOUND_MESSAGE);
    }
    
    categoryController.deleteCategory(category);
    
    return createNoContent();
  }

  @Override
  public Response findCategory(UUID categoryId) throws Exception {
    Category category = categoryController.findCategory(categoryId);
    if (category == null) {
      return createNotFound(NOT_FOUND_MESSAGE);
    }
    
    return createOk(categoryTranslator.translateCategory(category));
  }

  @Override
  public Response listCategories(UUID parentId, String search, String slug, List<String> sort, Long firstResult, Long maxResults) throws Exception {
    Category parent = parentId != null ? categoryController.findCategory(parentId) : null;
    if (parent == null && parentId != null) {
      return createBadRequest(String.format("Category %s not found", parentId));
    }

    List<CategoryListSort> sorts = null;
    try {
      sorts = getEnumListParameter(CategoryListSort.class, sort);
    } catch (IllegalArgumentException e) {
      return createBadRequest(e.getMessage());
    }

    SearchResult<Category> searchResult = categoryController.searchCategories(parent, slug, search, firstResult, maxResults, sorts);
   
    return createOk(categoryTranslator.translateCategories(searchResult.getResult()), searchResult.getTotalHits());
  }

  @Override
  public Response updateCategory(UUID categoryId, fi.metatavu.dcfb.server.rest.model.Category payload) throws Exception {
    Category parent = payload.getParentId() != null ? categoryController.findCategory(payload.getParentId()) : null;
    String slug = StringUtils.isNotBlank(payload.getSlug()) ? payload.getSlug() : slugifyLocalized(payload.getTitle());
    UUID lastModifier = getLoggerUserId();

    if (parent == null && payload.getParentId() != null) {
      return createBadRequest("Invalid parent id");
    }
    
    Category category = categoryController.findCategory(categoryId);
    if (category == null) {
      return createNotFound(NOT_FOUND_MESSAGE);
    }

    LocalizedEntry title = updateLocalizedEntry(category.getTitle(), payload.getTitle());
    
    return createOk(categoryTranslator.translateCategory(setCategoryMetas(categoryController.updateCategory(category, parent, title, slug, lastModifier), payload.getMeta())));
  }

  /**
   * Sets meta values for a category
   * 
   * @param category
   * @param metas
   * @return category
   */
  private Category setCategoryMetas(Category category, List<Meta> metas) {
    if (metas == null) {
      return category;
    }

    Set<String> usedKeys = new HashSet<>(metas.size());

    for (Meta meta : metas) {
      String key = meta.getKey();
      usedKeys.add(key);
      categoryController.setMeta(category, key, meta.getValue());  
    }

    categoryController.deleteMetasNotIn(category, usedKeys);

    return category;
  }


}
