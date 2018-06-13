package fi.metatavu.dcfb.server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.stream.Collectors;

import fi.metatavu.dcfb.client.CategoriesApi;
import fi.metatavu.dcfb.client.Category;
import fi.metatavu.dcfb.client.Item;
import fi.metatavu.dcfb.client.ItemsApi;
import fi.metatavu.dcfb.client.LocalizedValue;
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
  private String username;
  private String password;
  private String adminToken;
  private String accessToken;
  
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
  public CategoriesApi getCategoryApi() throws IOException {
    return test.getCategoriesApi(getAccessToken());
  }
  
  /**
   * Returns initialized categories API
   * 
   * @return initialized categories API
   * @throws IOException
   */
  public CategoriesApi getAdminCategoryApi() throws IOException {
    return test.getCategoriesApi(getAdminToken());
  }
  
  /**
   * Creates a simple item
   * 
   * @param categoryId category id
   * @return created item
   * @throws IOException
   */
  public fi.metatavu.dcfb.client.Item createSimpleItem(UUID categoryId) throws IOException {
    Price price = new Price();
    price.setCurrency("EUR");
    price.setPrice("10.00");
    
    fi.metatavu.dcfb.client.Item payload = new fi.metatavu.dcfb.client.Item();
    payload.setAmount(15l);
    payload.setCategoryId(categoryId);
    payload.setDescription(createLocalized("desc"));
    payload.setExpiresAt(null);
    payload.setImages(Collections.emptyList());
    payload.setTitle(createLocalized("simple item"));
    payload.setUnit("Fake");
    payload.setUnitPrice(price);
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
    Category result = getAdminCategoryApi().createCategory(payload);
    this.categories.add(0, result);
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
   * Cleans created test data
   * @throws IOException 
   */
  public void clean() throws IOException {
    ItemsApi itemApi = getAdminItemApi();
    CategoriesApi categoriesApi = getAdminCategoryApi();
    
    items.stream()
      .map(fi.metatavu.dcfb.client.Item::getId)
      .forEach(itemApi::deleteItem);
    
    categories.stream()
      .map(fi.metatavu.dcfb.client.Category::getId)
      .forEach(categoriesApi::deleteCategory);
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
