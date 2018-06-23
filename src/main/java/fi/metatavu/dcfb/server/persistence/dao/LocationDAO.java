package fi.metatavu.dcfb.server.persistence.dao;

import java.math.BigDecimal;
import java.util.UUID;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import fi.metatavu.dcfb.server.persistence.model.Location;
import fi.metatavu.dcfb.server.persistence.model.Location_;
import fi.metatavu.dcfb.server.persistence.model.LocalizedEntry;

/**
 * DAO for Location entity
 * 
 * @author Antti Leppä
 */
public class LocationDAO extends AbstractDAO<Location> {
  
  /**
  * Creates new location
  *
  * @param id id
  * @param slug slug
  * @param additionalInformations additionalInformations
  * @param country country
  * @param latitude latitude
  * @param longitude longitude
  * @param name name
  * @param postalCode postalCode
  * @param postOffice postOffice
  * @param streetAddress streetAddress
  * @param lastModifier modifier
  * @return created location
  */
  @SuppressWarnings ("squid:S00107")
  public Location create(UUID id, String slug, LocalizedEntry additionalInformations, String country, BigDecimal latitude, BigDecimal longitude, LocalizedEntry name, String postalCode, String postOffice, String streetAddress, UUID lastModifier) {
    Location location = new Location();
    location.setId(id);
    location.setSlug(slug);
    location.setAdditionalInformations(additionalInformations);
    location.setCountry(country);
    location.setLatitude(latitude);
    location.setLongitude(longitude);
    location.setName(name);
    location.setPostalCode(postalCode);
    location.setPostOffice(postOffice);
    location.setStreetAddress(streetAddress);
    location.setLastModifier(lastModifier);
    return persist(location);
  }

  /**
   * Finds location by slug
   * 
   * @param slug
   * @return location
   */
  public Location findBySlug(String slug) {
    EntityManager entityManager = getEntityManager();

    CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
    CriteriaQuery<Location> criteria = criteriaBuilder.createQuery(Location.class);
    Root<Location> root = criteria.from(Location.class);
    criteria.select(root);
    criteria.where(criteriaBuilder.equal(root.get(Location_.slug), slug));
    return getSingleResult(entityManager.createQuery(criteria));
  }

  /**
  * Updates additionalInformations
  *
  * @param additionalInformations additionalInformations
  * @param lastModifier modifier
  * @return updated location
  */
  public Location updateAdditionalInformations(Location location, LocalizedEntry additionalInformations, UUID lastModifier) {
    location.setLastModifier(lastModifier);
    location.setAdditionalInformations(additionalInformations);
    return persist(location);
  }

  /**
  * Updates country
  *
  * @param country country
  * @param lastModifier modifier
  * @return updated location
  */
  public Location updateCountry(Location location, String country, UUID lastModifier) {
    location.setLastModifier(lastModifier);
    location.setCountry(country);
    return persist(location);
  }

  /**
  * Updates latitude
  *
  * @param latitude latitude
  * @param lastModifier modifier
  * @return updated location
  */
  public Location updateLatitude(Location location, BigDecimal latitude, UUID lastModifier) {
    location.setLastModifier(lastModifier);
    location.setLatitude(latitude);
    return persist(location);
  }

  /**
  * Updates longitude
  *
  * @param longitude longitude
  * @param lastModifier modifier
  * @return updated location
  */
  public Location updateLongitude(Location location, BigDecimal longitude, UUID lastModifier) {
    location.setLastModifier(lastModifier);
    location.setLongitude(longitude);
    return persist(location);
  }

  /**
  * Updates name
  *
  * @param name name
  * @param lastModifier modifier
  * @return updated location
  */
  public Location updateName(Location location, LocalizedEntry name, UUID lastModifier) {
    location.setLastModifier(lastModifier);
    location.setName(name);
    return persist(location);
  }

  /**
  * Updates postalCode
  *
  * @param postalCode postalCode
  * @param lastModifier modifier
  * @return updated location
  */
  public Location updatePostalCode(Location location, String postalCode, UUID lastModifier) {
    location.setLastModifier(lastModifier);
    location.setPostalCode(postalCode);
    return persist(location);
  }

  /**
  * Updates postOffice
  *
  * @param postOffice postOffice
  * @param lastModifier modifier
  * @return updated location
  */
  public Location updatePostOffice(Location location, String postOffice, UUID lastModifier) {
    location.setLastModifier(lastModifier);
    location.setPostOffice(postOffice);
    return persist(location);
  }

  /**
  * Updates streetAddress
  *
  * @param streetAddress streetAddress
  * @param lastModifier modifier
  * @return updated location
  */
  public Location updateStreetAddress(Location location, String streetAddress, UUID lastModifier) {
    location.setLastModifier(lastModifier);
    location.setStreetAddress(streetAddress);
    return persist(location);
  }
    
}
