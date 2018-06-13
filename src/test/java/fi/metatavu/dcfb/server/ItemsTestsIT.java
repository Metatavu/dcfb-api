package fi.metatavu.dcfb.server;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.net.URISyntaxException;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Collections;
import java.util.Locale;

import org.junit.Test;

import feign.FeignException;
import fi.metatavu.dcfb.client.Category;
import fi.metatavu.dcfb.client.Image;
import fi.metatavu.dcfb.client.Item;
import fi.metatavu.dcfb.client.ItemsApi;
import fi.metatavu.dcfb.client.Price;

@SuppressWarnings ("squid:S1192")
public class ItemsTestsIT extends AbstractIntegrationTest {
  
  private static final ZoneId TIMEZONE = ZoneId.of("Europe/Helsinki");
  
  @Test
  public void testCreateItem() throws IOException, URISyntaxException {
    TestDataBuilder dataBuilder = new TestDataBuilder(this, USER_1_USERNAME, USER_1_PASSWORD);
    try {
      Category simpleCategory = dataBuilder.createSimpleCategory();
      
      Price price = new Price();
      price.setCurrency("USD");
      price.setPrice("15.00");
      
      Image image = new Image();
      image.setType("image/jpeg");
      image.setUrl("https://www.example.com/jpeg.jpg");
      
      fi.metatavu.dcfb.client.Item item = new fi.metatavu.dcfb.client.Item();
      item.setAmount(25l);
      item.setCategoryId(simpleCategory.getId());
      item.setDescription(dataBuilder.createLocalized(Locale.FRENCH, "PLURAL", "created description"));
      item.setExpiresAt(getOffsetDateTime(2020, 5, 4, TIMEZONE));
      item.setImages(Collections.emptyList());
      item.setTitle(dataBuilder.createLocalized(Locale.JAPAN, "PLURAL", "created title"));
      item.setUnit("Unit of Fake");
      item.setUnitPrice(price);
      item.setImages(Arrays.asList(image));
      
      Item createdItem = dataBuilder.createItem(item);
      
      assertNotNull(createdItem);
      assertNotNull(createdItem.getId());
      assertEquals(new Long(25l), createdItem.getAmount());
      assertEquals(simpleCategory.getId(), createdItem.getCategoryId());
      assertEquals("15.00", createdItem.getUnitPrice().getPrice());
      assertEquals("USD", createdItem.getUnitPrice().getCurrency());
      assertEquals(1, createdItem.getDescription().size());
      assertEquals(Locale.FRENCH.getLanguage(), createdItem.getDescription().get(0).getLanguage());
      assertEquals("created description", createdItem.getDescription().get(0).getValue());
      assertEquals("PLURAL", createdItem.getDescription().get(0).getType());
      assertEquals(getOffsetDateTime(2020, 5, 4, TIMEZONE).toInstant(), createdItem.getExpiresAt().toInstant());
      assertEquals(Locale.JAPAN.getLanguage(), createdItem.getTitle().get(0).getLanguage());
      assertEquals("created title", createdItem.getTitle().get(0).getValue());
      assertEquals("PLURAL", createdItem.getTitle().get(0).getType());
      assertEquals("Unit of Fake", createdItem.getUnit());
      assertEquals("USD", createdItem.getUnitPrice().getCurrency());
      assertEquals("15.00", createdItem.getUnitPrice().getPrice());
      assertEquals(1, createdItem.getImages().size());
      assertEquals("image/jpeg", createdItem.getImages().get(0).getType());
      assertEquals("https://www.example.com/jpeg.jpg", createdItem.getImages().get(0).getUrl());
      
    } finally {
      dataBuilder.clean();
    }
  }
  
  @Test
  public void testFindItem() throws IOException, URISyntaxException {
    TestDataBuilder dataBuilder = new TestDataBuilder(this, USER_1_USERNAME, USER_1_PASSWORD);
    try {
      Category simpleCategory = dataBuilder.createSimpleCategory();
      Item simpleItem = dataBuilder.createSimpleItem(simpleCategory.getId());
      ItemsApi itemApi = dataBuilder.getItemApi();
      assertEquals(simpleItem.toString(), itemApi.findItem(simpleItem.getId()).toString());
    } finally {
      dataBuilder.clean();
    }
  }

  @Test
  public void testUpdateItem() throws IOException, URISyntaxException {
    TestDataBuilder dataBuilder = new TestDataBuilder(this, USER_1_USERNAME, USER_1_PASSWORD);
    try {
      ItemsApi itemApi = dataBuilder.getItemApi();
      
      Category simpleCategory = dataBuilder.createSimpleCategory();
      
      Price price = new Price();
      price.setCurrency("USD");
      price.setPrice("15.00");
      
      Image image = new Image();
      image.setType("image/png");
      image.setUrl("https://www.example.com/png.png");

      Item item = dataBuilder.createSimpleItem(simpleCategory.getId());
     
      item.setAmount(25l);
      item.setCategoryId(simpleCategory.getId());
      item.setDescription(dataBuilder.createLocalized(Locale.FRENCH, "PLURAL", "created description"));
      item.setExpiresAt(getOffsetDateTime(2020, 5, 4, TIMEZONE));
      item.setImages(Collections.emptyList());
      item.setTitle(dataBuilder.createLocalized(Locale.JAPAN, "PLURAL", "created title"));
      item.setUnit("Unit of Fake");
      item.setUnitPrice(price);
      item.setImages(Arrays.asList(image));
      
      Item updateItem = itemApi.updateItem(item.getId(), item);

      assertNotNull(updateItem);
      assertNotNull(updateItem.getId());
      assertEquals(new Long(25l), updateItem.getAmount());
      assertEquals(simpleCategory.getId(), updateItem.getCategoryId());
      assertEquals("15.00", updateItem.getUnitPrice().getPrice());
      assertEquals("USD", updateItem.getUnitPrice().getCurrency());
      assertEquals(1, updateItem.getDescription().size());
      assertEquals(Locale.FRENCH.getLanguage(), updateItem.getDescription().get(0).getLanguage());
      assertEquals("created description", updateItem.getDescription().get(0).getValue());
      assertEquals("PLURAL", updateItem.getDescription().get(0).getType());
      assertEquals(getOffsetDateTime(2020, 5, 4, TIMEZONE).toInstant(), updateItem.getExpiresAt().toInstant());
      assertEquals(Locale.JAPAN.getLanguage(), updateItem.getTitle().get(0).getLanguage());
      assertEquals("created title", updateItem.getTitle().get(0).getValue());
      assertEquals("PLURAL", updateItem.getTitle().get(0).getType());
      assertEquals("Unit of Fake", updateItem.getUnit());
      assertEquals("USD", updateItem.getUnitPrice().getCurrency());
      assertEquals("15.00", updateItem.getUnitPrice().getPrice());
      
      assertEquals(1, updateItem.getImages().size());
      assertEquals("image/png", updateItem.getImages().get(0).getType());
      assertEquals("https://www.example.com/png.png", updateItem.getImages().get(0).getUrl());
      
    } finally {
      dataBuilder.clean();
    }
  }
  

  @Test
  public void testDeleteItem() throws IOException, URISyntaxException {
    TestDataBuilder dataBuilder = new TestDataBuilder(this, USER_1_USERNAME, USER_1_PASSWORD);
    try {
      ItemsApi itemApi = dataBuilder.getItemApi();
      
      Category simpleCategory = dataBuilder.createSimpleCategory();
      Item item = dataBuilder.createSimpleItem(simpleCategory.getId());
      assertNotNull(itemApi.findItem(item.getId()));
      itemApi.deleteItem(item.getId());
      
      try {
        assertNull(itemApi.findItem(item.getId()));
        fail("Item should not be found");
      } catch (FeignException e) {
        assertEquals(404, e.status());
      }
      
      dataBuilder.excludeItemFromClean(item);
    } finally {
      dataBuilder.clean();
    }
  }

}
