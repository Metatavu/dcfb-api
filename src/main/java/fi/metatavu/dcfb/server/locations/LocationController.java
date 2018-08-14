package fi.metatavu.dcfb.server.locations;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import fi.metatavu.dcfb.server.persistence.dao.LocationDAO;
import fi.metatavu.dcfb.server.persistence.model.Location;
import fi.metatavu.dcfb.server.rest.model.LocationListSort;
import fi.metatavu.dcfb.server.persistence.model.LocalizedEntry;
import fi.metatavu.dcfb.server.search.handlers.LocationIndexHandler;
import fi.metatavu.dcfb.server.search.searchers.LocationSearcher;
import fi.metatavu.dcfb.server.search.searchers.SearchResult;

@ApplicationScoped
public class LocationController {

  @Inject
  private LocationSearcher locationSearcher;

  @Inject
  private LocationIndexHandler locationIndexHandler;
  
  @Inject
  private LocationDAO locationDAO;

  /**
    * Creates new location
    *
    * @param slug slug
    * @param additionalInformations additionalInformations
    * @param country country
    * @param latitude latitude
    * @param longitude longitude
    * @param name name
    * @param postalCode postalCode
    * @param postOffice postOffice
    * @param streetAddress streetAddress
    * @param createdAt createdAt
    * @param modifiedAt modifiedAt
    * @param lastModifier modifier
    * @return created location
    */
  @SuppressWarnings ("squid:S00107")
  public Location createLocation(String slug, LocalizedEntry additionalInformations, String country, BigDecimal latitude, BigDecimal longitude, LocalizedEntry name, String postalCode, String postOffice, String streetAddress, UUID lastModifier) {
    return locationDAO.create(UUID.randomUUID(), getUniqueSlug(slug), additionalInformations, country, latitude, longitude, name, postalCode, postOffice, streetAddress, lastModifier);
  }

  /**
   * Finds an location
   * 
   * @param locationId location id
   * @return location or null if not found
   */
  public Location findLocation(UUID locationId) {
    return locationDAO.findById(locationId);
  }
  
  /**
   * Update location
   *
   * @param additionalInformations additionalInformations
   * @param country country
   * @param latitude latitude
   * @param longitude longitude
   * @param name name
   * @param postalCode postalCode
   * @param postOffice postOffice
   * @param streetAddress streetAddress
   * @param modifier modifier
   * @return updated location
   */
  @SuppressWarnings ("squid:S00107")
  public Location updateLocation(Location location, LocalizedEntry additionalInformations, String country, BigDecimal latitude, BigDecimal longitude, LocalizedEntry name, String postalCode, String postOffice, String streetAddress, UUID modifier) {
    locationDAO.updateAdditionalInformations(location, additionalInformations, modifier);
    locationDAO.updateCountry(location, country, modifier);
    locationDAO.updateLatitude(location, latitude, modifier);
    locationDAO.updateLongitude(location, longitude, modifier);
    locationDAO.updateName(location, name, modifier);
    locationDAO.updatePostalCode(location, postalCode, modifier);
    locationDAO.updatePostOffice(location, postOffice, modifier);
    locationDAO.updateStreetAddress(location, streetAddress, modifier);
    return location;
  }
  
  /**
   * Deletes a location
   * 
   * @param location location to be deleted
   */
  public void deleteLocation(Location location) {
    locationDAO.delete(location);
    locationIndexHandler.deleteIndexable(location.getId());
  }

  /**
   * Searches locations
   * 
   * @param slug filter by slug. Ignored if null
   * @param search Search by free-text. Ignored if null
   * @param firstResult result offset
   * @param maxResults maximum number of results returned
   * @return search result
   */
  public SearchResult<Location> searchLocations(String slug, String search, Long firstResult, Long maxResults, List<LocationListSort> sorts) {
    SearchResult<UUID> searchResult = locationSearcher.searchLocations(slug, search, firstResult, maxResults, sorts);

    List<Location> locations = searchResult.getResult().stream()
      .map(locationDAO::findById)
      .filter(Objects::nonNull)
      .collect(Collectors.toList());

    return new SearchResult<>(locations, searchResult.getTotalHits());
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

    while (locationDAO.findBySlug(result) != null) {
      iteration++;
      result = String.format("%s-%d", slug, iteration);
    }

	  return result;
  }

}