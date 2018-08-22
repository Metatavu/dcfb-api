package fi.metatavu.dcfb.server.persistence.dao;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import fi.metatavu.dcfb.server.persistence.model.LocalizedEntry;
import fi.metatavu.dcfb.server.persistence.model.LocalizedType;
import fi.metatavu.dcfb.server.persistence.model.LocalizedValue;
import fi.metatavu.dcfb.server.persistence.model.LocalizedValue_;

/**
 * DAO for LocalizedEntry
 * 
 * @author Antti Leppä
 */
public class LocalizedValueDAO extends AbstractDAO<LocalizedValue> {

  /**
  * Creates new localizedValue
  *
  * @param id id
  * @param entry entry
  * @param locale locale
  * @param type type
  * @param value value
  * @return created localizedValue
  */
  public LocalizedValue create(UUID id, LocalizedEntry entry, Locale locale, LocalizedType type, String value) {
    LocalizedValue localizedValue = new LocalizedValue();
    localizedValue.setId(id);
    localizedValue.setLocale(locale);
    localizedValue.setEntry(entry);
    localizedValue.setType(type);
    localizedValue.setValue(value);
    return persist(localizedValue);
  }
  
  /**
   * Lists values by entries
   * 
   * @param entry entry
   * @return values by entries
   */
  public List<LocalizedValue> listByEntry(LocalizedEntry entry) {
    EntityManager entityManager = getEntityManager();

    CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
    CriteriaQuery<LocalizedValue> criteria = criteriaBuilder.createQuery(LocalizedValue.class);
    Root<LocalizedValue> root = criteria.from(LocalizedValue.class);
    criteria.select(root);
    criteria.where(criteriaBuilder.equal(root.get(LocalizedValue_.entry), entry));
    
    return entityManager.createQuery(criteria).getResultList();
  }  

  /**
   * Finds value by entry, locale and type
   * 
   * @param entry entry
   * @param locale locale
   * @param type type
   * @return value
   */
  public LocalizedValue findByEntryLocaleAndType(LocalizedEntry entry, Locale locale, LocalizedType type) {
    EntityManager entityManager = getEntityManager();

    CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
    CriteriaQuery<LocalizedValue> criteria = criteriaBuilder.createQuery(LocalizedValue.class);
    Root<LocalizedValue> root = criteria.from(LocalizedValue.class);
    criteria.select(root);
    criteria.where(
      criteriaBuilder.and(
        criteriaBuilder.equal(root.get(LocalizedValue_.entry), entry),
        criteriaBuilder.equal(root.get(LocalizedValue_.locale), locale),
        criteriaBuilder.equal(root.get(LocalizedValue_.type), type)        
      )    
    );
    
    TypedQuery<LocalizedValue> query = entityManager.createQuery(criteria);
    
    return getSingleResult(query);
  }

  /**
   * LocalizedValue by entry and locale
   * 
   * @param entry entry
   * @param locale locale
   * @return value
   */
  public List<LocalizedValue> listByEntryAndLocale(LocalizedEntry entry, Locale locale) {
    EntityManager entityManager = getEntityManager();

    CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
    CriteriaQuery<LocalizedValue> criteria = criteriaBuilder.createQuery(LocalizedValue.class);
    Root<LocalizedValue> root = criteria.from(LocalizedValue.class);
    criteria.select(root);
    criteria.where(
      criteriaBuilder.and(
        criteriaBuilder.equal(root.get(LocalizedValue_.entry), entry),
        criteriaBuilder.equal(root.get(LocalizedValue_.locale), locale)    
      )    
    );
    
    TypedQuery<LocalizedValue> query = entityManager.createQuery(criteria);
    
    return query.getResultList();
  }

  /**
   * LocalizedValue by entry and type
   * 
   * @param entry entry
   * @param type type
   * @param firstResult 
   * @param maxResults 
   * @return value
   */
  public List<LocalizedValue> listByEntryAndType(LocalizedEntry entry, String type, Integer firstResult, Integer maxResults) {
    EntityManager entityManager = getEntityManager();

    CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
    CriteriaQuery<LocalizedValue> criteria = criteriaBuilder.createQuery(LocalizedValue.class);
    Root<LocalizedValue> root = criteria.from(LocalizedValue.class);
    criteria.select(root);
    criteria.where(
      criteriaBuilder.and(
        criteriaBuilder.equal(root.get(LocalizedValue_.entry), entry),
        criteriaBuilder.equal(root.get(LocalizedValue_.type), type)    
      )    
    );
    
    TypedQuery<LocalizedValue> query = entityManager.createQuery(criteria);
    
    if (firstResult != null) {
      query.setFirstResult(firstResult);
    }
    
    if (maxResults != null) {
      query.setMaxResults(maxResults);
    }
    
    return query.getResultList();
  }

  /**
  * Updates entry
  *
  * @param localizedValue localized value
  * @param entry entry
  * @return updated localizedValue
  */
  public LocalizedValue updateEntry(LocalizedValue localizedValue, LocalizedEntry entry) {
    localizedValue.setEntry(entry);
    return persist(localizedValue);
  }

  /**
  * Updates type
  *
  * @param localizedValue localized value
  * @param type type
  * @return updated localizedValue
  */
  public LocalizedValue updateType(LocalizedValue localizedValue, LocalizedType type) {
    localizedValue.setType(type);
    return persist(localizedValue);
  }

  /**
  * Updates value
  *
  * @param localizedValue localized value
  * @param value value
  * @return updated localizedValue
  */
  public LocalizedValue updateValue(LocalizedValue localizedValue, String value) {
    localizedValue.setValue(value);
    return persist(localizedValue);
  }

}
