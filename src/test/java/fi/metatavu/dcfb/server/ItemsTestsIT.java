package fi.metatavu.dcfb.server;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;
import static org.awaitility.Awaitility.*;

import java.io.IOException;
import java.net.URISyntaxException;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

import feign.FeignException;
import fi.metatavu.dcfb.client.Category;
import fi.metatavu.dcfb.client.Image;
import fi.metatavu.dcfb.client.Item;
import fi.metatavu.dcfb.client.ItemListSort;
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
  public void testSearchItems() throws IOException, URISyntaxException {
    TestDataBuilder dataBuilder = new TestDataBuilder(this, USER_1_USERNAME, USER_1_PASSWORD);
    try {
      ItemsApi itemsApi = dataBuilder.getItemApi();

      Category simpleCategory = dataBuilder.createSimpleCategory();
      Item simpleItem = dataBuilder.createSimpleItem(simpleCategory.getId());
      
      await().atMost(1, TimeUnit.MINUTES).until(() -> {
        return itemsApi.listItems(Collections.emptyMap()).size() == 1;
      });
      
      List<Item> items = itemsApi.listItems(null, "simple", null, null, null);
      assertEquals(1, items.size());
      assertEquals(simpleItem.toString(), items.get(0).toString());
    } finally {
      dataBuilder.clean();
    }
  }
  
  @Test
  public void testSearchItemsByCategory() throws IOException, URISyntaxException {
    TestDataBuilder dataBuilder = new TestDataBuilder(this, USER_1_USERNAME, USER_1_PASSWORD);
    try {
      ItemsApi itemsApi = dataBuilder.getItemApi();

      Category simpleCategory1 = dataBuilder.createSimpleCategory();
      Category simpleCategory2 = dataBuilder.createSimpleCategory();

      Item simpleItem = dataBuilder.createSimpleItem(simpleCategory1.getId());
      
      await().atMost(1, TimeUnit.MINUTES).until(() -> {
        return itemsApi.listItems(Collections.emptyMap()).size() == 1;
      });
      
      List<Item> items1Items = itemsApi.listItems(simpleCategory1.getId().toString(), null, null, null, null);
      assertEquals(1, items1Items.size());
      assertEquals(simpleItem.toString(), items1Items.get(0).toString());

      List<Item> items2Items = itemsApi.listItems(simpleCategory2.getId().toString(), null, null, null, null);
      assertEquals(0, items2Items.size());
      
      List<Item> items3Items = itemsApi.listItems(simpleCategory1.getId().toString() + "," + simpleCategory2.getId().toString(), null, null, null, null); 
      assertEquals(1, items3Items.size());
      assertEquals(simpleItem.toString(), items1Items.get(0).toString());

      try {
        itemsApi.listItems("not-uuid", null, null, null, null); 
        fail("List with invalid uuid should return bad request");
      } catch (FeignException e) {
        assertEquals(400, e.status());
      }

      try {
        itemsApi.listItems(UUID.randomUUID().toString(), null, null, null, null);
        fail("List with incorrect uuid should return bad request");
      } catch (FeignException e) {
        assertEquals(400, e.status());
      }
    } finally {
      dataBuilder.clean();
    }
  }
  
  @Test
  public void testSearchItemsLimit() throws IOException, URISyntaxException {
    TestDataBuilder dataBuilder = new TestDataBuilder(this, USER_1_USERNAME, USER_1_PASSWORD);
    try {
      ItemsApi itemsApi = dataBuilder.getItemApi();

      Category simpleCategory = dataBuilder.createSimpleCategory();
      dataBuilder.createSimpleItem(simpleCategory.getId());
      dataBuilder.createSimpleItem(simpleCategory.getId());
      dataBuilder.createSimpleItem(simpleCategory.getId());
      dataBuilder.createSimpleItem(simpleCategory.getId());
      dataBuilder.createSimpleItem(simpleCategory.getId());
      waitItemCount(itemsApi, 5);
      
      assertEquals(3, itemsApi.listItems(null, null, null, 2l, null).size());
      assertEquals(2, itemsApi.listItems(null, null, null, 3l, 60l).size());
      assertEquals(2, itemsApi.listItems(null, null, null, 1l, 2l).size());
      assertEquals(2, itemsApi.listItems(null, null, null, 0l, 2l).size());
      assertEquals(3, itemsApi.listItems(null, null, null, null, 3l).size());
    } finally {
      dataBuilder.clean();
    }
  }

  @Test
  public void testSearchItemsSortDates() throws IOException, URISyntaxException {
    TestDataBuilder dataBuilder = new TestDataBuilder(this, USER_1_USERNAME, USER_1_PASSWORD);
    try {
      ItemsApi itemsApi = dataBuilder.getItemApi();

      Category simpleCategory = dataBuilder.createSimpleCategory();
      Item item1 = dataBuilder.createSimpleItem(simpleCategory.getId());
      waitItemCount(itemsApi, 1);
      dataBuilder.createSimpleItem(simpleCategory.getId());
      waitItemCount(itemsApi, 2);
      dataBuilder.createSimpleItem(simpleCategory.getId());
      waitItemCount(itemsApi, 3);
      dataBuilder.createSimpleItem(simpleCategory.getId());
      waitItemCount(itemsApi, 4);
      Item item5 = dataBuilder.createSimpleItem(simpleCategory.getId());
      waitItemCount(itemsApi, 5);

      List<Item> itemsCreatedAsc = itemsApi.listItems(null, null, Arrays.asList(ItemListSort.CREATED_AT_ASC.toString()), null, null);
      List<Item> itemsCreatedDesc = itemsApi.listItems(null, null, Arrays.asList(ItemListSort.CREATED_AT_DESC.toString()), null, null);
      List<Item> itemsModifiedAsc = itemsApi.listItems(null, null, Arrays.asList(ItemListSort.MODIFIED_AT_ASC.toString()), null, null);
      List<Item> itemsModifiedDesc = itemsApi.listItems(null, null, Arrays.asList(ItemListSort.MODIFIED_AT_DESC.toString()), null, null);
      
      assertEquals(item1.getId(), itemsCreatedAsc.get(0).getId());
      assertEquals(item5.getId(), itemsCreatedAsc.get(4).getId());
      assertEquals(item5.getId(), itemsCreatedDesc.get(0).getId());
      assertEquals(item1.getId(), itemsCreatedDesc.get(4).getId());
      assertEquals(item1.getId(), itemsModifiedAsc.get(0).getId());
      assertEquals(item5.getId(), itemsModifiedAsc.get(4).getId());
      assertEquals(item5.getId(), itemsModifiedDesc.get(0).getId());
      assertEquals(item1.getId(), itemsModifiedDesc.get(4).getId());
    } finally {
      dataBuilder.clean();
    }
  }

  @Test
  public void testSearchItemsSortScore() throws IOException, URISyntaxException {
    TestDataBuilder dataBuilder = new TestDataBuilder(this, USER_1_USERNAME, USER_1_PASSWORD);
    try {
      ItemsApi itemsApi = dataBuilder.getItemApi();

      Category simpleCategory = dataBuilder.createSimpleCategory();

      fi.metatavu.dcfb.client.Item payload1 = new fi.metatavu.dcfb.client.Item();
      payload1.setTitle(dataBuilder.createLocalized("example title"));
      payload1.setDescription(dataBuilder.createLocalized("test description"));
      payload1.setCategoryId(simpleCategory.getId());
      payload1.setUnit("mm");
      payload1.setUnitPrice(dataBuilder.createSimplePrice());
      payload1.setAmount(1l);
      payload1.setUnit("unit");
      Item item1 = dataBuilder.createItem(payload1);

      waitItemCount(itemsApi, 1);

      fi.metatavu.dcfb.client.Item payload2 = new fi.metatavu.dcfb.client.Item();
      payload2.setTitle(dataBuilder.createLocalized("test title"));
      payload2.setDescription(dataBuilder.createLocalized("test description"));
      payload2.setCategoryId(simpleCategory.getId());
      payload2.setUnitPrice(dataBuilder.createSimplePrice());
      payload2.setAmount(2l);
      payload2.setUnit("unit");
      Item item2 = dataBuilder.createItem(payload2);
      
      waitItemCount(itemsApi, 2);

      List<Item> itemsScoreAsc = itemsApi.listItems(null, "test", Arrays.asList(ItemListSort.SCORE_ASC.toString()), null, null);
      List<Item> itemsScoreDesc = itemsApi.listItems(null, "test", Arrays.asList(ItemListSort.SCORE_DESC.toString()), null, null);
      
      assertEquals(item1.getId(), itemsScoreAsc.get(0).getId());
      assertEquals(item2.getId(), itemsScoreAsc.get(1).getId());
      assertEquals(item2.getId(), itemsScoreDesc.get(0).getId());
      assertEquals(item1.getId(), itemsScoreDesc.get(1).getId());
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

  private void waitItemCount(ItemsApi itemsApi, int count) {
    await().atMost(1, TimeUnit.MINUTES).until(() -> {
      return itemsApi.listItems(Collections.emptyMap()).size() == count;
    });
  }

}
