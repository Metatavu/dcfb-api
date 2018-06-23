package fi.metatavu.dcfb.server.search.handlers;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.enterprise.event.TransactionPhase;
import javax.inject.Inject;
import javax.persistence.PostPersist;
import javax.persistence.PostUpdate;
import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;

import org.slf4j.Logger;

import fi.metatavu.dcfb.server.categories.CategoryController;
import fi.metatavu.dcfb.server.localization.LocalizedValueController;
import fi.metatavu.dcfb.server.persistence.model.Category;
import fi.metatavu.dcfb.server.search.index.IndexableCategory;

/**
 * Index handler for categories
 */
@ApplicationScoped
public class CategoryIndexHandler extends AbstractIndexableHandler<Category, IndexableCategory> {

  @Inject
  private Logger logger;

  @Inject
  private CategoryController categoryController;

  @Inject
  private LocalizedValueController localizedValueController;

  @Inject
  private Event<CategoryIndexEvent> categoryIndexEvent;
  
  /**
   * Category update event listener
   * 
   * @param persistedEntity persisted JPA entity
   */
  @PostUpdate
  @PostPersist
  public void onCategoryUpdate(Category persistedEntity) {
    categoryIndexEvent.fire(new CategoryIndexEvent(persistedEntity.getId()));
  }
  
  /**
   * Category index event listener
   * 
   * @param event index event
   */
  @Transactional (value = TxType.REQUIRES_NEW)
  public void onCategoryIndex(@Observes (during = TransactionPhase.AFTER_SUCCESS) CategoryIndexEvent event) {
    Category entity = categoryController.findCategory(event.getId());
    if (entity != null) {
      index(entity);
    } else {
      logger.error("Could not find category with id {}", event.getId());
    }
  }

  @Override
  protected String getType() {
    return IndexableCategory.TYPE;
  }
  
  /**
   * Creates indexable category from JPA entity
   * 
   * @param category JPA category
   * @return indexable category
   */
  @Override
  public IndexableCategory createIndexable(Category category) {
    Locale localeFi = new Locale("fi");
    Locale localeSv = new Locale("sv");
    Locale localeEn = new Locale("en");
    
    List<String> titleFi = localizedValueController.getValues(category.getTitle(), localeFi);
    List<String> titleSv = localizedValueController.getValues(category.getTitle(), localeSv);
    List<String> titleEn = localizedValueController.getValues(category.getTitle(), localeEn);

    UUID parentId = category.getParent() != null ? category.getParent().getId() : null;
    String slug = category.getSlug();
    OffsetDateTime createdAt = category.getCreatedAt();
    OffsetDateTime modifiedAt = category.getModifiedAt();
    
    return new IndexableCategory(category.getId(), 
        parentId,
        titleFi, 
        titleSv, 
        titleEn, 
        slug,
        createdAt,
        modifiedAt);
  }

}
