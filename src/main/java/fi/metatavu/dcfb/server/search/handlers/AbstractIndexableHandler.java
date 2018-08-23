package fi.metatavu.dcfb.server.search.handlers;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.UUID;

import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.slf4j.Logger;

import fi.metatavu.dcfb.server.persistence.model.Location;
import fi.metatavu.dcfb.server.search.index.GeoPoint;
import fi.metatavu.dcfb.server.search.index.Indexable;
import fi.metatavu.dcfb.server.search.io.IndexUpdater;

import fi.metatavu.dcfb.server.search.io.RegisterIndexableEvent;

/**
 * Abstract base class for index handlers
 */
public abstract class AbstractIndexableHandler<T, I extends Indexable> {

  @Inject
  private Logger logger;

  @Inject
  private IndexUpdater indexUpdater;

  public void onRegisterIndexableEvent(@Observes RegisterIndexableEvent event) {
    event.registerIndexable(getIndexableClass());
  }
  
  /**
   * Indexes an entity
   * 
   * @param entity
   */
  protected void index(T entity) {
    I indexable = createIndexable(entity);
    if (indexable != null) {
      indexUpdater.index(indexable);
    } else {
      logger.error("Failed to create indexable");
    }
  }

  public void deleteIndexable(UUID id) {
    indexUpdater.remove(getType(), id.toString());
  }
  
  /**
   * Creates an indexable from entity
   * 
   * @param entity JPA entity
   * @return indexable
   */
  protected abstract I createIndexable(T entity);

  /**
   * Returns elastic search type
   * 
   * @return elastic search type
   */
  protected abstract String getType();

  /**
   * Creates GeoPoint for a Location object
   * 
   * @param location location
   * @return GeoPoint
   */
  protected GeoPoint createGeoPoint(Location location) {
    if (location == null) {
      return null;
    }
    
    BigDecimal latitude = location.getLatitude();
    BigDecimal longitude = location.getLongitude();
    
    if (longitude == null || longitude == null) {
      return null;
    }
    
    return new GeoPoint(latitude, longitude);
  }

  /**
   * Resolves indexable class from generic type arguments
   * 
   * @return indexable class
   */
  private Class<? extends Indexable> getIndexableClass() {
    Type genericSuperclass = getClass().getGenericSuperclass();

    if (genericSuperclass instanceof ParameterizedType) {
      return getTypeArgument((ParameterizedType) genericSuperclass, 1);
    } else if ((genericSuperclass instanceof Class<?>) && (AbstractIndexableHandler.class.isAssignableFrom((Class<?>) genericSuperclass))) {
      return getTypeArgument((ParameterizedType) ((Class<?>) genericSuperclass).getGenericSuperclass(), 1);
    }

    logger.error("Failed to resolve indexable class");

    return null;
  }

  /**
   * Extracts nth parameterized type from parameterized type 
   * 
   * @param parameterizedType parameterized type
   * @param nth index of returned type
   * @return parameterized type
   */
  @SuppressWarnings("unchecked")
  private Class<? extends Indexable> getTypeArgument(ParameterizedType parameterizedType, int nth)  {
    return (Class<? extends Indexable>) parameterizedType.getActualTypeArguments()[nth];
  }
  
}
