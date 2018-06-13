package fi.metatavu.dcfb.server.rest.translate;

import java.util.List;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;

import fi.metatavu.dcfb.server.rest.model.Category;

/**
 * Translator for categories
 * 
 * @author Antti Leppä
 */
@ApplicationScoped
public class CategoryTranslator extends AbstractTranslator {

  /**
   * Translates JPA category object into REST category object
   * 
   * @param category JPA item object
   * @return REST category
   */
  public Category translateCategory(fi.metatavu.dcfb.server.persistence.model.Category category) {
    if (category == null) {
      return null;
    }
    
    Category result = new Category();
    result.setId(category.getId());
    result.setParentId(category.getParent() != null ? category.getParent().getId() : null);
    result.setSlug(category.getSlug());
    result.setTitle(translatelocalizedValue(category.getTitle()));
    
    return result;
  }

  /**
   * Translates list of category objects into list of REST category objects
   * 
   * @param categories list of JPA categories
   * @return list of REST categories
   */
  public List<Category> translateCategories(List<fi.metatavu.dcfb.server.persistence.model.Category> categories) {
    return categories.stream().map(this::translateCategory).collect(Collectors.toList());
  }

}
