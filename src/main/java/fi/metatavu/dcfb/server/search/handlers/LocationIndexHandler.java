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

import fi.metatavu.dcfb.server.items.LocationController;
import fi.metatavu.dcfb.server.items.LocalizedValueController;
import fi.metatavu.dcfb.server.persistence.model.Location;
import fi.metatavu.dcfb.server.search.index.IndexableLocation;

/**
 * Index handler for locations
 */
@ApplicationScoped
public class LocationIndexHandler extends AbstractIndexableHandler<Location, IndexableLocation> {

  @Inject
  private Logger logger;

  @Inject
  private LocationController locationController;

  @Inject
  private LocalizedValueController localizedValueController;

  @Inject
  private Event<LocationIndexEvent> locationIndexEvent;
  
  /**
   * Location update event listener
   * 
   * @param persistedEntity persisted JPA entity
   */
  @PostUpdate
  @PostPersist
  public void onLocationUpdate(Location persistedEntity) {
    locationIndexEvent.fire(new LocationIndexEvent(persistedEntity.getId()));
  }
  
  /**
   * Location index event listener
   * 
   * @param event index event
   */
  @Transactional (value = TxType.REQUIRES_NEW)
  public void onLocationIndex(@Observes (during = TransactionPhase.AFTER_SUCCESS) LocationIndexEvent event) {
    Location entity = locationController.findLocation(event.getId());
    if (entity != null) {
      index(entity);
    } else {
      logger.error("Could not find location with id {}", event.getId());
    }
  }

  @Override
  protected String getType() {
    return IndexableLocation.TYPE;
  }
  
  /**
   * Creates indexable location from JPA entity
   * 
   * @param location JPA location
   * @return indexable location
   */
  @Override
  public IndexableLocation createIndexable(Location location) {
    Locale localeFi = new Locale("fi");
    Locale localeSv = new Locale("sv");
    Locale localeEn = new Locale("en");
    
    List<String> nameFi = localizedValueController.getValues(location.getName(), localeFi);
    List<String> nameSv = localizedValueController.getValues(location.getName(), localeSv);
    List<String> nameEn = localizedValueController.getValues(location.getName(), localeEn);
    
    List<String> additionalInformationsFi = localizedValueController.getValues(location.getAdditionalInformations(), localeFi);
    List<String> additionalInformationsSv = localizedValueController.getValues(location.getAdditionalInformations(), localeSv);
    List<String> additionalInformationsEn = localizedValueController.getValues(location.getAdditionalInformations(), localeEn);
    
    UUID id = location.getId();
    String slug = location.getSlug();
    OffsetDateTime createdAt = location.getCreatedAt();
    OffsetDateTime modifiedAt = location.getModifiedAt();
    
    return new IndexableLocation(id, 
      nameFi, 
      nameSv, 
      nameEn, 
      additionalInformationsFi, 
      additionalInformationsSv, 
      additionalInformationsEn,
      slug, 
      createdAt, 
      modifiedAt);
  }

}
