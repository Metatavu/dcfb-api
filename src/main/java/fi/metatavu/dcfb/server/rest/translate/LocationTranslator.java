package fi.metatavu.dcfb.server.rest.translate;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;

import fi.metatavu.dcfb.server.locations.LocationConsts;
import fi.metatavu.dcfb.server.rest.model.Address;
import fi.metatavu.dcfb.server.rest.model.Coordinate;
import fi.metatavu.dcfb.server.rest.model.Location;

/**
 * Translator for locations
 * 
 * @author Antti Leppä
 */
@ApplicationScoped
public class LocationTranslator extends AbstractTranslator {
  
  /**
   * Translates JPA location object into REST location object
   * 
   * @param location JPA location object
   * @return REST location
   */
  public Location translateLocation(fi.metatavu.dcfb.server.persistence.model.Location location) {
    if (location == null) {
      return null;
    }

    Address address = new Address();
    address.setAdditionalInformations(translatelocalizedValue(location.getAdditionalInformations()));
    address.setCountry(location.getCountry());
    address.setPostalCode(location.getPostalCode());
    address.setPostOffice(location.getPostOffice());
    address.setStreetAddress(location.getStreetAddress());

    Coordinate coordinate = new Coordinate();
    coordinate.setCrs(LocationConsts.COORDINATE_REFERENCE_SYSTEM);
    coordinate.setLatitude(translateCoordinateValue(location.getLatitude()));
    coordinate.setLongitude(translateCoordinateValue(location.getLongitude()));
    
    Location result = new Location();
    result.setAddress(address);
    result.setCoordinate(coordinate);
    result.setId(location.getId());
    result.setName(translatelocalizedValue(location.getName()));

    return result;
  }

  /**
   * Translates list of JPA locations into REST locations
   * 
   * @param items JPA locations
   * @return REST locations
   */
  public List<Location> translateLocations(List<fi.metatavu.dcfb.server.persistence.model.Location> locations) {
    return locations.stream().map(this::translateLocation).collect(Collectors.toList());
  }
  
  /**
   * Translates coordinate value into string
   * 
   * @param value value
   * @return value as string 
   */
  private String translateCoordinateValue(BigDecimal value) {
    if (value == null) {
      return null;
    }

    return value.toString();
  }

}
