package fi.metatavu.dcfb.server.rest;

import java.util.List;
import java.util.UUID;

import javax.ejb.Stateful;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.core.Response;

import org.apache.commons.lang3.StringUtils;

import fi.metatavu.dcfb.server.items.CategoryController;
import fi.metatavu.dcfb.server.persistence.model.Category;
import fi.metatavu.dcfb.server.persistence.model.LocalizedEntry;
import fi.metatavu.dcfb.server.rest.translate.CategoryTranslator;

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
    
    if (parent == null && payload.getParentId() != null) {
      return createBadRequest("Invalid parent id");
    }
    
    LocalizedEntry title = createLocalizedEntry(payload.getTitle());
    
    return createOk(categoryTranslator.translateCategory(categoryController.createCategory(parent, title, slug)));
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
  public Response listCategories(Long firstResult, Long maxResults) throws Exception {
    List<Category> categories = categoryController.listCategories(firstResult, maxResults);
    return createOk(categoryTranslator.translateCategories(categories));
  }

  @Override
  public Response updateCategory(UUID categoryId, fi.metatavu.dcfb.server.rest.model.Category payload) throws Exception {
    Category parent = payload.getParentId() != null ? categoryController.findCategory(payload.getParentId()) : null;
    String slug = StringUtils.isNotBlank(payload.getSlug()) ? payload.getSlug() : slugifyLocalized(payload.getTitle());
    
    if (parent == null && payload.getParentId() != null) {
      return createBadRequest("Invalid parent id");
    }
    
    Category category = categoryController.findCategory(categoryId);
    if (category == null) {
      return createNotFound(NOT_FOUND_MESSAGE);
    }

    LocalizedEntry title = updateLocalizedEntry(category.getTitle(), payload.getTitle());
    
    return createOk(categoryTranslator.translateCategory(categoryController.updateCategory(category, parent, title, slug)));
  }


}
