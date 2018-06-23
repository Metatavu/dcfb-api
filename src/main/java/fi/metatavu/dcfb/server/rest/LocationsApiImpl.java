package fi.metatavu.dcfb.server.rest;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import javax.ejb.Stateful;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.core.Response;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import fi.metatavu.dcfb.server.locations.LocationConsts;
import fi.metatavu.dcfb.server.locations.LocationController;
import fi.metatavu.dcfb.server.persistence.model.LocalizedEntry;
import fi.metatavu.dcfb.server.rest.model.Address;
import fi.metatavu.dcfb.server.rest.model.Coordinate;
import fi.metatavu.dcfb.server.rest.model.Location;
import fi.metatavu.dcfb.server.rest.model.LocationListSort;
import fi.metatavu.dcfb.server.rest.translate.LocationTranslator;
import fi.metatavu.dcfb.server.search.searchers.SearchResult;

/**
 * Locations REST Service implementation
 * 
 * @author Antti Leppä
 */
@RequestScoped
@Stateful
public class LocationsApiImpl extends AbstractApi implements LocationsApi {

  @Inject
  private LocationController locationController;

  @Inject
  private LocationTranslator locationTranslator;

  @Override
  public Response createLocation(Location payload) throws Exception {
    if (!isValidLocalizedList(payload.getName())) {
      return createBadRequest("Invalid name");
    }

    String country = null;
    String postalCode = null;
    String postOffice = null;
    String streetAddress = null;
    LocalizedEntry additionalInformations = null;
    BigDecimal latitude = null;
    BigDecimal longitude = null;
    UUID lastModifier = getLoggerUserId();
    String slug = slugifyLocalized(payload.getName());

    Address addressPayload = payload.getAddress();
    if (addressPayload != null) {
      if (!isValidLocalizedList(addressPayload.getAdditionalInformations())) {
        return createBadRequest("Invalid address additional information");
      }

      country = addressPayload.getCountry();
      postalCode = addressPayload.getPostalCode();
      postOffice = addressPayload.getPostOffice();
      streetAddress = addressPayload.getStreetAddress();
      additionalInformations = createLocalizedEntry(addressPayload.getAdditionalInformations());
    }

    Coordinate coordinatePayload = payload.getCoordinate();
    if (coordinatePayload != null) {
      if (!LocationConsts.SUPPORTED_COORDINATE_REFERENCE_SYSTEMS.contains(coordinatePayload.getCrs())) {
        return createBadRequest(String.format("Unsupported coordinate reference system use one of %s", StringUtils.join(LocationConsts.SUPPORTED_COORDINATE_REFERENCE_SYSTEMS, ',')));
      }

      try {
        latitude = NumberUtils.createBigDecimal(coordinatePayload.getLatitude());
        longitude = NumberUtils.createBigDecimal(coordinatePayload.getLongitude());
      } catch (NumberFormatException e) {
        return createBadRequest(e.getMessage());
      }
    }

    LocalizedEntry name = createLocalizedEntry(payload.getName());

    fi.metatavu.dcfb.server.persistence.model.Location location = locationController.createLocation(
      slug,
      additionalInformations, 
      country, 
      latitude, 
      longitude, 
      name,
      postalCode, 
      postOffice, 
      streetAddress, 
      lastModifier);

    return createOk(locationTranslator.translateLocation(location));
  }

  @Override
  public Response findLocation(UUID locationId) throws Exception {
    fi.metatavu.dcfb.server.persistence.model.Location location = locationController.findLocation(locationId);
    if (location == null) {
      return createNotFound(NOT_FOUND_MESSAGE);
    }

    return createOk(locationTranslator.translateLocation(location));
  }

  @Override
  public Response listLocations(String search, String slug, List<String> sort, Long firstResult, Long maxResults) throws Exception {
    List<LocationListSort> sorts = null;
    try {
      sorts = getEnumListParameter(LocationListSort.class, sort);
    } catch (IllegalArgumentException e) {
      return createBadRequest(e.getMessage());
    }

    SearchResult<fi.metatavu.dcfb.server.persistence.model.Location> searchResult = locationController.searchLocations(slug, search, firstResult, maxResults, sorts);

    return createOk(locationTranslator.translateLocations(searchResult.getResult()), searchResult.getTotalHits());
  }

  @Override
  public Response updateLocation(UUID locationId, Location payload) throws Exception {
    fi.metatavu.dcfb.server.persistence.model.Location location = locationController.findLocation(locationId);
    if (location == null) {
      return createNotFound(NOT_FOUND_MESSAGE);
    }

    if (!isValidLocalizedList(payload.getName())) {
      return createBadRequest("Invalid name");
    }

    String country = null;
    String postalCode = null;
    String postOffice = null;
    String streetAddress = null;
    LocalizedEntry additionalInformations = null;
    BigDecimal latitude = null;
    BigDecimal longitude = null;
    UUID lastModifier = getLoggerUserId();

    Address addressPayload = payload.getAddress();
    if (addressPayload != null) {
      if (!isValidLocalizedList(addressPayload.getAdditionalInformations())) {
        return createBadRequest("Invalid address additional information");
      }

      country = addressPayload.getCountry();
      postalCode = addressPayload.getPostalCode();
      postOffice = addressPayload.getPostOffice();
      streetAddress = addressPayload.getStreetAddress();

      if (location.getAdditionalInformations() == null) {
        additionalInformations = createLocalizedEntry(addressPayload.getAdditionalInformations());
      } else {
        additionalInformations = updateLocalizedEntry(location.getAdditionalInformations(), addressPayload.getAdditionalInformations());
      }
    }

    Coordinate coordinatePayload = payload.getCoordinate();
    if (coordinatePayload != null) {
      if (!ArrayUtils.contains(LocationConsts.SUPPORTED_COORDINATE_REFERENCE_SYSTEMS, coordinatePayload.getCrs())) {
        return createBadRequest(String.format("Unsupported coordinate reference system use one of %s", StringUtils.join(LocationConsts.SUPPORTED_COORDINATE_REFERENCE_SYSTEMS, ',')));
      }

      try {
        latitude = NumberUtils.createBigDecimal(coordinatePayload.getLatitude());
        longitude = NumberUtils.createBigDecimal(coordinatePayload.getLongitude());
      } catch (NumberFormatException e) {
        return createBadRequest(e.getMessage());
      }
    }

    LocalizedEntry name = updateLocalizedEntry(location.getName(), payload.getName());
 
    fi.metatavu.dcfb.server.persistence.model.Location updatedLocation = locationController.updateLocation(
      location,
      additionalInformations, 
      country, 
      latitude, 
      longitude, 
      name, 
      postalCode, 
      postOffice, 
      streetAddress, 
      lastModifier);

    return createOk(locationTranslator.translateLocation(updatedLocation));
  }

  @Override
  public Response deleteLocation(UUID locationId) throws Exception {
    fi.metatavu.dcfb.server.persistence.model.Location location = locationController.findLocation(locationId);
    if (location == null) {
      return createNotFound(NOT_FOUND_MESSAGE);
    }
    
    locationController.deleteLocation(location);

    return createNoContent();
  }

}
