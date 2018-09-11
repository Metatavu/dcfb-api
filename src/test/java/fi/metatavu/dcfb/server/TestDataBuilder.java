package fi.metatavu.dcfb.server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.stream.Collectors;

import fi.metatavu.dcfb.client.Address;
import fi.metatavu.dcfb.client.CategoriesApi;
import fi.metatavu.dcfb.client.Category;
import fi.metatavu.dcfb.client.Coordinate;
import fi.metatavu.dcfb.client.Item;
import fi.metatavu.dcfb.client.ItemPaymentMethods;
import fi.metatavu.dcfb.client.ItemsApi;
import fi.metatavu.dcfb.client.LocalizedValue;
import fi.metatavu.dcfb.client.Location;
import fi.metatavu.dcfb.client.LocationsApi;
import fi.metatavu.dcfb.client.Meta;
import fi.metatavu.dcfb.client.Price;

/**
 * Builder for integration test data
 * 
 * @author Antti Leppä
 */
public class TestDataBuilder {

  private AbstractIntegrationTest test;
  private List<fi.metatavu.dcfb.client.Item> items;
  private List<fi.metatavu.dcfb.client.Category> categories;
  private List<fi.metatavu.dcfb.client.Location> locations;
  private String username;
  private String password;
  private String adminToken;
  private String accessToken;
  private String anonymousToken;
  
  /**
   * Constructor
   * 
   * @param test test class
   */
  public TestDataBuilder(AbstractIntegrationTest test, String username, String password) {
    this.test = test;
    this.username = username;
    this.password = password;
    this.items = new ArrayList<>();
    this.categories = new ArrayList<>();
    this.locations = new ArrayList<>();
  }
  
  /**
   * Returns initialized items API
   * 
   * @return initialized items API
   * @throws IOException
   */
  public ItemsApi getItemApi() throws IOException {
    return test.getItemsApi(getAccessToken());
  }
  
  /**
   * Returns initialized items API
   * 
   * @return initialized items API
   * @throws IOException
   */
  public ItemsApi getAdminItemApi() throws IOException {
    return test.getItemsApi(getAdminToken());
  }
  
  /**
   * Returns initialized categories API
   * 
   * @return initialized categories API
   * @throws IOException
   */
  public CategoriesApi getCategoriesApi() throws IOException {
    return test.getCategoriesApi(getAccessToken());
  }
  
  /**
   * Returns initialized categories API
   * 
   * @return initialized categories API
   * @throws IOException
   */
  public CategoriesApi getAnonymousCategoriesApi() throws IOException {
    return test.getCategoriesApi(getAnonymousToken());
  }
  
  /**
   * Returns initialized categories API
   * 
   * @return initialized categories API
   * @throws IOException
   */
  public CategoriesApi getAdminCategoriesApi() throws IOException {
    return test.getCategoriesApi(getAdminToken());
  }

  /**
   * Returns initialized locations API
   * 
   * @return initialized locations API
   * @throws IOException
   */
  public LocationsApi getLocationsApi() throws IOException {
    return test.getLocationsApi(getAccessToken());
  }
  
  /**
   * Returns initialized locations API
   * 
   * @return initialized locations API
   * @throws IOException
   */
  public LocationsApi getAdminLocationsApi() throws IOException {
    return test.getLocationsApi(getAdminToken());
  }

  /**
   * Creates a simple item
   * 
   * @param categoryId category id
   * @return created item
   * @throws IOException
   */
  public fi.metatavu.dcfb.client.Item createSimpleItem(UUID categoryId, UUID locationId) throws IOException {
    Price price = createSimplePrice();
    
    fi.metatavu.dcfb.client.Item payload = new fi.metatavu.dcfb.client.Item();
    payload.setAmount(15l);
    payload.setCategoryId(categoryId);
    payload.setDescription(createLocalized("desc"));
    payload.setExpiresAt(null);
    payload.setImages(Collections.emptyList());
    payload.setTitle(createLocalized("simple item"));
    payload.setUnit("Fake");
    payload.setUnitPrice(price);
    payload.setLocationId(locationId);
    payload.setSellerId(AbstractIntegrationTest.REALM1_USER_1_ID);
    payload.setPaymentMethods(createDefaultPaymentMethods());

    return createItem(payload);
  }

  /**
   * Creates an item
   * 
   * @param payload item payload
   * @return created item
   * @throws IOException
   */
  public fi.metatavu.dcfb.client.Item createItem(fi.metatavu.dcfb.client.Item payload) throws IOException {
    fi.metatavu.dcfb.client.Item result = getAdminItemApi().createItem(payload);
    this.items.add(0, result);
    return result;
  }

  /**
   * Creates simple category
   * 
   * @return created category
   * @throws IOException
   */
  public Category createSimpleCategory() throws IOException {
    Category payload = new Category();
    payload.setTitle(createLocalized("simple category"));
    return createCategory(payload);
  }
  
  /**
   * Creates category
   * 
   * @param payload category payload
   * @return created category
   * @throws IOException
   */
  public Category createCategory(Category payload) throws IOException {
    Category result = getAdminCategoriesApi().createCategory(payload);
    this.categories.add(0, result);
    return result;
  }

  /**
   * Creates a simple location
   * 
   * @return created location
   * @throws IOException
   */
  public fi.metatavu.dcfb.client.Location createSimpleLocation() throws IOException {
    return createSimpleLocation("61.685807", "27.273488");
  }
  
  public fi.metatavu.dcfb.client.Location createSimpleLocation(String latitude, String longitude) throws IOException {
    Address address = new Address();
    address.setAdditionalInformations(createLocalized("simple test address"));
    address.setCountry("Finland");
    address.setPostalCode("10000");
    address.setPostOffice("Example");
    address.setStreetAddress("Example street");

    Coordinate coordinate = new Coordinate();
    coordinate.setCrs("epsg4326");
    coordinate.setLongitude(longitude);
    coordinate.setLatitude(latitude);

    fi.metatavu.dcfb.client.Location payload = new fi.metatavu.dcfb.client.Location();
    payload.setAddress(address);
    payload.setCoordinate(coordinate);
    payload.setName(createLocalized("simple location"));

    return createLocation(payload);
  }

  /**
   * Creates an location
   * 
   * @param payload location payload
   * @return created location
   * @throws IOException
   */
  public fi.metatavu.dcfb.client.Location createLocation(fi.metatavu.dcfb.client.Location payload) throws IOException {
    fi.metatavu.dcfb.client.Location result = getAdminLocationsApi().createLocation(payload);
    this.locations.add(0, result);
    return result;
  }

  /**
   * Excludes item from cleanup
   * 
   * @param item item
   */
  public void excludeItemFromClean(Item item) {
    items = items.stream().filter((listItem) -> {
      return !listItem.getId().equals(item.getId());
    }).collect(Collectors.toList());
  }
  
  /**
   * Excludes category from cleanup
   * 
   * @param category category
   */
  public void excludeCategoryFromClean(Category category) {
    categories = categories.stream().filter((listItem) -> {
      return !listItem.getId().equals(category.getId());
    }).collect(Collectors.toList());
  }
  
  /**
   * Excludes location from cleanup
   * 
   * @param location location
   */
  public void excludeLocationFromClean(Location location) {
    locations = locations.stream().filter((listLocation) -> {
      return !listLocation.getId().equals(location.getId());
    }).collect(Collectors.toList());
  }

  /**
   * Cleans created test data
   * @throws IOException 
   */
  public void clean() throws IOException {
    ItemsApi itemApi = getAdminItemApi();
    CategoriesApi categoriesApi = getAdminCategoriesApi();
    LocationsApi locationsApi = getAdminLocationsApi();
    
    items.stream()
      .map(fi.metatavu.dcfb.client.Item::getId)
      .forEach(itemApi::deleteItem);
    
    categories.stream()
      .map(fi.metatavu.dcfb.client.Category::getId)
      .forEach(categoriesApi::deleteCategory);

    locations.stream()
      .map(fi.metatavu.dcfb.client.Location::getId)
      .forEach(locationsApi::deleteLocation);    
  }
  
  /**
   * Creates simple price
   * 
   * @return simple price
   */
  public Price createSimplePrice() {
	  Price price = new Price();
    price.setCurrency("EUR");
    price.setPrice("10.00");
	  return price;
  }  

  /**
   * Creates LocalizedValue instance for single locale and type
   * 
   * @param locale locale
   * @param type type
   * @param value string
   * @return created localized value
   */
  public List<LocalizedValue> createLocalized(Locale locale, String type, String value) {
    LocalizedValue result = new LocalizedValue();
    result.setLanguage(locale.getLanguage());
    result.setType(type);
    result.setValue(value);
    return Arrays.asList(result);
  }

  /**
   * Creates localized value for english locale and type single
   * 
   * @param value value
   * @return localized value
   */
  public List<LocalizedValue> createLocalized(String value) {
    return createLocalized(Locale.ENGLISH, "SINGLE", value);
  }

  /**
   * Creates meta value
   * 
   * @param key key
   * @param value value
   * @return meta
   */
  public Meta createMeta(String key, String value) {
    Meta result = new Meta();
    result.setKey(key);
    result.setValue(value);
    return result;
  }

  /**
   * Creates default payment methods object
   * 
   * @return default payment methods object
   */
  public ItemPaymentMethods createDefaultPaymentMethods() {
    ItemPaymentMethods paymentMethods = new ItemPaymentMethods();
    paymentMethods.setAllowContactSeller(true);
    paymentMethods.setAllowCreditCard(false);
    return paymentMethods;
  }
  
  /**
   * Returns admin token
   * 
   * @return admin token
   * @throws IOException
   */
  private String getAdminToken() throws IOException {
    if (adminToken == null) {
      adminToken = test.getAdminToken();
    }

    return adminToken;
  }
  
  /**
   * Returns anonymous token
   * 
   * @return anonymous token
   * @throws IOException
   */
  private String getAnonymousToken() throws IOException {
    if (anonymousToken == null) {
      anonymousToken = test.getAnonymousToken();
    }

    return anonymousToken;
  }

  /**
   * Returns user token
   * 
   * @return user token
   * @throws IOException
   */
  private String getAccessToken() throws IOException {
    if (accessToken == null) {
      accessToken = test.getAccessToken(username, password);
    }
    
    return accessToken;
  }
  
}
