package fi.metatavu.dcfb.server;

import static org.awaitility.Awaitility.await;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

import feign.FeignException;
import fi.metatavu.dcfb.client.Address;
import fi.metatavu.dcfb.client.Coordinate;
import fi.metatavu.dcfb.client.Location;
import fi.metatavu.dcfb.client.LocationListSort;
import fi.metatavu.dcfb.client.LocationsApi;

@SuppressWarnings ("squid:S1192")
public class LocationsTestsIT extends AbstractIntegrationTest {
  
  @Test
  public void testCreateLocation() throws IOException, URISyntaxException {
    TestDataBuilder dataBuilder = new TestDataBuilder(this, USER_1_USERNAME, USER_1_PASSWORD);
    try {
      Location createdLocation = dataBuilder.createSimpleLocation();

      assertNotNull(createdLocation);
      assertNotNull(createdLocation.getId());
      assertNotNull(createdLocation.getAddress());
      assertNotNull(createdLocation.getCoordinate());

      assertEquals("Finland", createdLocation.getAddress().getCountry());
      assertEquals("10000", createdLocation.getAddress().getPostalCode());
      assertEquals("Example", createdLocation.getAddress().getPostOffice());
      assertEquals("Example street", createdLocation.getAddress().getStreetAddress());

      assertEquals("epsg4326", createdLocation.getCoordinate().getCrs());
      assertEquals("27.273488", createdLocation.getCoordinate().getLongitude());
      assertEquals("61.685807", createdLocation.getCoordinate().getLatitude());

      assertEquals(Locale.ENGLISH.getLanguage(), createdLocation.getName().get(0).getLanguage());
      assertEquals("simple location", createdLocation.getName().get(0).getValue());
      assertEquals("SINGLE", createdLocation.getName().get(0).getType());
    } finally {
      dataBuilder.clean();
    }
  }

  @Test
  public void testFindLocation() throws IOException, URISyntaxException {
    TestDataBuilder dataBuilder = new TestDataBuilder(this, USER_1_USERNAME, USER_1_PASSWORD);
    try {
      Location simpleLocation = dataBuilder.createSimpleLocation();
      LocationsApi locationsApi = dataBuilder.getLocationsApi();
      assertEquals(simpleLocation.toString(), locationsApi.findLocation(simpleLocation.getId()).toString());
    } finally {
      dataBuilder.clean();
    }
  }

  @Test
  public void testSearchLocations() throws IOException, URISyntaxException {
    TestDataBuilder dataBuilder = new TestDataBuilder(this, USER_1_USERNAME, USER_1_PASSWORD);
    try {
      LocationsApi locationsApi = dataBuilder.getLocationsApi();

      Location simpleLocation = dataBuilder.createSimpleLocation();
      
      waitLocationCount(locationsApi, 1);
      
      List<Location> locations = locationsApi.listLocations("simple", null, null, null, null);
      assertEquals(1, locations.size());
      assertEquals(simpleLocation.toString(), locations.get(0).toString());
    } finally {
      dataBuilder.clean();
    }
  }
  
  @Test
  public void testSearchLocationsLimit() throws IOException, URISyntaxException {
    TestDataBuilder dataBuilder = new TestDataBuilder(this, USER_1_USERNAME, USER_1_PASSWORD);
    try {
      LocationsApi locationsApi = dataBuilder.getLocationsApi();

      dataBuilder.createSimpleLocation();
      dataBuilder.createSimpleLocation();
      dataBuilder.createSimpleLocation();
      dataBuilder.createSimpleLocation();
      dataBuilder.createSimpleLocation();
      waitLocationCount(locationsApi, 5);
      
      assertEquals(3, locationsApi.listLocations(null, null, null, 2l, null).size());
      assertEquals(2, locationsApi.listLocations(null, null, null, 3l, 60l).size());
      assertEquals(2, locationsApi.listLocations(null, null, null, 1l, 2l).size());
      assertEquals(2, locationsApi.listLocations(null, null, null, 0l, 2l).size());
      assertEquals(3, locationsApi.listLocations(null, null, null, null, 3l).size());
    } finally {
      dataBuilder.clean();
    }
  }

  @Test
  public void testSearchLocationsSortDates() throws IOException, URISyntaxException {
    TestDataBuilder dataBuilder = new TestDataBuilder(this, USER_1_USERNAME, USER_1_PASSWORD);
    try {
      LocationsApi locationsApi = dataBuilder.getLocationsApi();

      Location location1 = dataBuilder.createSimpleLocation();
      waitLocationCount(locationsApi, 1);
      dataBuilder.createSimpleLocation();
      waitLocationCount(locationsApi, 2);
      dataBuilder.createSimpleLocation();
      waitLocationCount(locationsApi, 3);
      dataBuilder.createSimpleLocation();
      waitLocationCount(locationsApi, 4);
      Location location5 = dataBuilder.createSimpleLocation();
      waitLocationCount(locationsApi, 5);

      List<Location> locationsCreatedAsc = locationsApi.listLocations(null, null, Arrays.asList(LocationListSort.CREATED_AT_ASC.toString()), null, null);
      List<Location> locationsCreatedDesc = locationsApi.listLocations(null, null, Arrays.asList(LocationListSort.CREATED_AT_DESC.toString()), null, null);
      List<Location> locationsModifiedAsc = locationsApi.listLocations(null, null, Arrays.asList(LocationListSort.MODIFIED_AT_ASC.toString()), null, null);
      List<Location> locationsModifiedDesc = locationsApi.listLocations(null, null, Arrays.asList(LocationListSort.MODIFIED_AT_DESC.toString()), null, null);
      
      assertEquals(location1.getId(), locationsCreatedAsc.get(0).getId());
      assertEquals(location5.getId(), locationsCreatedAsc.get(4).getId());
      assertEquals(location5.getId(), locationsCreatedDesc.get(0).getId());
      assertEquals(location1.getId(), locationsCreatedDesc.get(4).getId());
      assertEquals(location1.getId(), locationsModifiedAsc.get(0).getId());
      assertEquals(location5.getId(), locationsModifiedAsc.get(4).getId());
      assertEquals(location5.getId(), locationsModifiedDesc.get(0).getId());
      assertEquals(location1.getId(), locationsModifiedDesc.get(4).getId());
    } finally {
      dataBuilder.clean();
    }
  }
  
  @Test
  public void testUpdateLocation() throws IOException, URISyntaxException {
    TestDataBuilder dataBuilder = new TestDataBuilder(this, USER_1_USERNAME, USER_1_PASSWORD);
    try {
      LocationsApi locationsApi = dataBuilder.getLocationsApi();
      
      Address address = new Address();
      address.setAdditionalInformations(dataBuilder.createLocalized("simple test address"));
      address.setCountry("Finland");
      address.setPostalCode("10000");
      address.setPostOffice("Example");
      address.setStreetAddress("Example street");

      Coordinate coordinate = new Coordinate();
      coordinate.setCrs("epsg4326");
      coordinate.setLongitude("27.273488");
      coordinate.setLatitude("61.685807");

      fi.metatavu.dcfb.client.Location payload = new fi.metatavu.dcfb.client.Location();
      payload.setAddress(address);
      payload.setCoordinate(coordinate);
      payload.setName(dataBuilder.createLocalized("simple location"));

      Location location = dataBuilder.createLocation(payload);

      address.setCountry("Sweden");
      address.setPostalCode("12345");
      address.setPostOffice("Demo");
      address.setStreetAddress("Demo street");

      coordinate.setLongitude("37.273488");
      coordinate.setLatitude("71.685807");      
      
      location.setAddress(address);
      location.setCoordinate(coordinate);
      location.setName(dataBuilder.createLocalized("sample location"));

      Location updatedLocation = locationsApi.updateLocation(location.getId(), location);

      assertNotNull(updatedLocation);
      assertNotNull(updatedLocation.getId());
      assertNotNull(updatedLocation.getAddress());
      assertNotNull(updatedLocation.getCoordinate());

      assertEquals("Sweden", updatedLocation.getAddress().getCountry());
      assertEquals("12345", updatedLocation.getAddress().getPostalCode());
      assertEquals("Demo", updatedLocation.getAddress().getPostOffice());
      assertEquals("Demo street", updatedLocation.getAddress().getStreetAddress());

      assertEquals("epsg4326", updatedLocation.getCoordinate().getCrs());
      assertEquals("37.273488", updatedLocation.getCoordinate().getLongitude());
      assertEquals("71.685807", updatedLocation.getCoordinate().getLatitude());

      assertEquals(Locale.ENGLISH.getLanguage(), updatedLocation.getName().get(0).getLanguage());
      assertEquals("sample location", updatedLocation.getName().get(0).getValue());
      assertEquals("SINGLE", updatedLocation.getName().get(0).getType());
    } finally {
      dataBuilder.clean();
    }
  }

  @Test
  public void testDeleteLocation() throws IOException, URISyntaxException {
    TestDataBuilder dataBuilder = new TestDataBuilder(this, USER_1_USERNAME, USER_1_PASSWORD);
    try {
      LocationsApi locationsApi = dataBuilder.getLocationsApi();
      
      Location location = dataBuilder.createSimpleLocation();
      assertNotNull(locationsApi.findLocation(location.getId()));
      locationsApi.deleteLocation(location.getId());
      
      try {
        assertNull(locationsApi.findLocation(location.getId()));
        fail("Location should not be found");
      } catch (FeignException e) {
        assertEquals(404, e.status());
      }
      
      dataBuilder.excludeLocationFromClean(location);
    } finally {
      dataBuilder.clean();
    }
  }

  private void waitLocationCount(LocationsApi locationsApi, int count) {
    await().atMost(1, TimeUnit.MINUTES).until(() -> {
      return locationsApi.listLocations(Collections.emptyMap()).size() == count;
    });
  }
}
