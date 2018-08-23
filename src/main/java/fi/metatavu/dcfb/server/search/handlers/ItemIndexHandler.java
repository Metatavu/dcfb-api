package fi.metatavu.dcfb.server.search.handlers;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.stream.Collectors;

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

import fi.metatavu.dcfb.server.items.ItemController;
import fi.metatavu.dcfb.server.localization.LocalizedValueController;
import fi.metatavu.dcfb.server.persistence.model.Item;
import fi.metatavu.dcfb.server.search.index.GeoPoint;
import fi.metatavu.dcfb.server.search.index.IndexableItem;

/**
 * Index handler for items
 */
@ApplicationScoped
public class ItemIndexHandler extends AbstractIndexableHandler<Item, IndexableItem> {

  @Inject
  private Logger logger;

  @Inject
  private ItemController itemController;

  @Inject
  private LocalizedValueController localizedValueController;

  @Inject
  private Event<ItemIndexEvent> itemIndexEvent;
  
  /**
   * Item update event  listener
   * 
   * @param persistedEntity persisted JPA entity
   */
  @PostUpdate
  @PostPersist
  public void onItemUpdate(Item persistedEntity) {
    itemIndexEvent.fire(new ItemIndexEvent(persistedEntity.getId()));
  }
  
  /**
   * Item index event listener
   * 
   * @param event index event
   */
  @Transactional (value = TxType.REQUIRES_NEW)
  public void onItemIndex(@Observes (during = TransactionPhase.AFTER_SUCCESS) ItemIndexEvent event) {
    Item entity = itemController.findItem(event.getId());
    if (entity != null) {
      index(entity);
    } else {
      logger.error("Could not find item with id {}", event.getId());
    }
  }

  @Override
  protected String getType() {
    return IndexableItem.TYPE;
  }
  
  /**
   * Creates indexable item from JPA entity
   * 
   * @param item JPA item
   * @return indexable item
   */
  @Override
  public IndexableItem createIndexable(Item item) {
    Locale localeFi = new Locale("fi");
    Locale localeSv = new Locale("sv");
    Locale localeEn = new Locale("en");
    
    List<String> titleFi = localizedValueController.getValues(item.getTitle(), localeFi);
    List<String> titleSv = localizedValueController.getValues(item.getTitle(), localeSv);
    List<String> titleEn = localizedValueController.getValues(item.getTitle(), localeEn);

    List<String> descriptionFi = localizedValueController.getValues(item.getDescription(), localeFi);
    List<String> descriptionSv = localizedValueController.getValues(item.getDescription(), localeSv);
    List<String> descriptionEn = localizedValueController.getValues(item.getDescription(), localeEn);

    List<String> allowedUserIds = itemController.listItemUsers(item).stream().map(itemUser -> itemUser.getUserId().toString()).collect(Collectors.toList());
    boolean visibilityLimited = item.getVisibilityLimited();

    UUID categoryId = item.getCategory() != null ? item.getCategory().getId() : null;
    UUID locationId = item.getLocation() != null ? item.getLocation().getId() : null;
    String slug = item.getSlug();
    OffsetDateTime createdAt = item.getCreatedAt();
    OffsetDateTime modifiedAt = item.getModifiedAt();
    OffsetDateTime expiresAt = item.getExpiresAt();
    GeoPoint geoPoint = createGeoPoint(item.getLocation());
    
    return new IndexableItem(item.getId(),
        geoPoint,
        titleFi, 
        titleSv, 
        titleEn, 
        descriptionFi, 
        descriptionSv, 
        descriptionEn, 
        categoryId,
        locationId,
        slug,
        allowedUserIds,
        visibilityLimited,
        createdAt, 
        modifiedAt, 
        expiresAt);
  }
}
