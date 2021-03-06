package fi.metatavu.dcfb.server;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import static org.awaitility.Awaitility.*;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

import feign.FeignException;
import fi.metatavu.dcfb.client.CategoriesApi;
import fi.metatavu.dcfb.client.Category;
import fi.metatavu.dcfb.client.CategoryListSort;

@SuppressWarnings ("squid:S1192")
public class CategoriesTestsIT extends AbstractIntegrationTest {
  
  @Test
  public void testCreateCategory() throws IOException, URISyntaxException {
    TestDataBuilder dataBuilder = new TestDataBuilder(this, USER_1_USERNAME, USER_1_PASSWORD);
    try {
      Category parentCategory = dataBuilder.createSimpleCategory();
      
      Category category = new Category();
      category.setParentId(parentCategory.getId());
      category.setTitle(dataBuilder.createLocalized(Locale.GERMAN, "PLURAL", "new category"));
      category.setMeta(Arrays.asList(
        dataBuilder.createMeta("test-1", "test value 1"),
        dataBuilder.createMeta("test-2", "test value 2")
      ));

      Category createdCategory = dataBuilder.createCategory(category);

      Map<String, String> metaMap = mapMetas(category.getMeta());
      
      assertNotNull(createdCategory);
      assertNotNull(createdCategory.getId());
      assertEquals(parentCategory.getId(), createdCategory.getParentId());
      assertEquals(1, createdCategory.getTitle().size());
      assertEquals(Locale.GERMAN.getLanguage(), createdCategory.getTitle().get(0).getLanguage());
      assertEquals("PLURAL", createdCategory.getTitle().get(0).getType());
      assertEquals("new category", createdCategory.getTitle().get(0).getValue());
      assertEquals(2, createdCategory.getMeta().size());
      assertEquals("test value 1", metaMap.get("test-1"));
      assertEquals("test value 2", metaMap.get("test-2"));

    } finally {
      dataBuilder.clean();
    }
  }

  @Test
  public void testCreateCategoryAsUser() throws IOException, URISyntaxException {
    TestDataBuilder dataBuilder = new TestDataBuilder(this, USER_1_USERNAME, USER_1_PASSWORD);
    try {
      CategoriesApi categoriesApi = dataBuilder.getCategoriesApi();
      
      try {
        Category category = new Category();
        category.setTitle(dataBuilder.createLocalized(Locale.GERMAN, "PLURAL", "new category"));
        categoriesApi.createCategory(category);
        fail("Creating categories as user should be forbidden");
      } catch (FeignException e) {
        assertEquals(403, e.status());
      }

    } finally {
      dataBuilder.clean();
    }
  }

  @Test
  public void testCreateCategoryAsAnonymous() throws IOException, URISyntaxException {
    TestDataBuilder dataBuilder = new TestDataBuilder(this, USER_1_USERNAME, USER_1_PASSWORD);
    try {
      CategoriesApi categoriesApi = dataBuilder.getAnonymousCategoriesApi();
      
      try {
        Category category = new Category();
        category.setTitle(dataBuilder.createLocalized(Locale.GERMAN, "PLURAL", "new category"));
        categoriesApi.createCategory(category);
        fail("Creating categories as anonymous should be forbidden");
      } catch (FeignException e) {
        assertEquals(403, e.status());
      }

    } finally {
      dataBuilder.clean();
    }
  }
  
  @Test
  public void testFindCategory() throws IOException, URISyntaxException {
    TestDataBuilder dataBuilder = new TestDataBuilder(this, USER_1_USERNAME, USER_1_PASSWORD);
    try {
      CategoriesApi categoriesApi = dataBuilder.getAnonymousCategoriesApi();
      Category createdCategory = dataBuilder.createSimpleCategory();
      assertEquals(createdCategory.toString(), categoriesApi.findCategory(createdCategory.getId()).toString());
    } finally {
      dataBuilder.clean();
    }
  }

  @Test
  public void testListCategory() throws IOException, URISyntaxException {
    TestDataBuilder dataBuilder = new TestDataBuilder(this, USER_1_USERNAME, USER_1_PASSWORD);
    try {
      CategoriesApi categoriesApi = dataBuilder.getAnonymousCategoriesApi();
      Category category1 = dataBuilder.createSimpleCategory();

      waitCategoryCount(categoriesApi, 1);
      
      Category category2 = dataBuilder.createSimpleCategory();
      
      waitCategoryCount(categoriesApi, 2);
      
      List<Category> list = categoriesApi.listCategories(null, null, null, null, null, null);
      assertEquals(2, list.size());
      assertEquals(category1.toString(), list.get(0).toString());
      assertEquals(category2.toString(), list.get(1).toString());
    } finally {
      dataBuilder.clean();
    }
  }

  @Test
  public void testSearchCategoriesByText() throws IOException, URISyntaxException {
    TestDataBuilder dataBuilder = new TestDataBuilder(this, USER_1_USERNAME, USER_1_PASSWORD);
    try {
      CategoriesApi categoriesApi = dataBuilder.getAnonymousCategoriesApi();

      Category simpleCategory = dataBuilder.createSimpleCategory();
      
      waitCategoryCount(categoriesApi, 1);
      
      List<Category> categories = categoriesApi.listCategories(null, "simple", null, null, null, null);
      assertEquals(1, categories.size());
      assertEquals(simpleCategory.toString(), categories.get(0).toString());
    } finally {
      dataBuilder.clean();
    }
  }

  @Test
  public void testSearchCategoriesBySlug() throws IOException, URISyntaxException {
    TestDataBuilder dataBuilder = new TestDataBuilder(this, USER_1_USERNAME, USER_1_PASSWORD);
    try {
      CategoriesApi categoriesApi = dataBuilder.getAnonymousCategoriesApi();

      Category simpleCategory = dataBuilder.createSimpleCategory();
      
      waitCategoryCount(categoriesApi, 1);
      
      List<Category> categories = categoriesApi.listCategories(null, null, "simple-category", null, null, null);
      assertEquals(1, categories.size());
      assertEquals(simpleCategory.toString(), categories.get(0).toString());

      assertEquals(0, categoriesApi.listCategories(null, null, "simple", null, null, null).size());

    } finally {
      dataBuilder.clean();
    }
  }
  
  @Test
  public void testSearchCategoriesByParent() throws IOException, URISyntaxException {
    TestDataBuilder dataBuilder = new TestDataBuilder(this, USER_1_USERNAME, USER_1_PASSWORD);
    try {
      CategoriesApi categoriesApi = dataBuilder.getAnonymousCategoriesApi();

      Category parentCategory = dataBuilder.createSimpleCategory();

      Category childPayload = new Category();
      childPayload.setParentId(parentCategory.getId());
      childPayload.setSlug("child");
      childPayload.setTitle(dataBuilder.createLocalized("child"));
      Category childCategory = dataBuilder.createCategory(childPayload);
      
      waitCategoryCount(categoriesApi, 2);
      
      List<Category> categories1Categories = categoriesApi.listCategories(parentCategory.getId(), null, null, null, null, null);
      assertEquals(1, categories1Categories.size());
      assertEquals(childCategory.toString(), categories1Categories.get(0).toString());

      assertEquals(0, categoriesApi.listCategories(childCategory.getId(), null, null, null, null, null).size());

      try {
        Map<String, Object> queryParams = new HashMap<>();
        queryParams.put("parentId", "not-uuid");
        categoriesApi.listCategories(queryParams); 
        fail("List with invalid uuid should return not found");
      } catch (FeignException e) {
        assertEquals(404, e.status());
      }

      try {
        categoriesApi.listCategories(UUID.randomUUID(), null, null, null, null, null);
        fail("List with incorrect uuid should return bad request");
      } catch (FeignException e) {
        assertEquals(400, e.status());
      }
    } finally {
      dataBuilder.clean();
    }
  }
  
  @Test
  public void testSearchCategoriesLimit() throws IOException, URISyntaxException {
    TestDataBuilder dataBuilder = new TestDataBuilder(this, USER_1_USERNAME, USER_1_PASSWORD);
    try {
      CategoriesApi categoriesApi = dataBuilder.getAnonymousCategoriesApi();

      dataBuilder.createSimpleCategory();
      dataBuilder.createSimpleCategory();
      dataBuilder.createSimpleCategory();
      dataBuilder.createSimpleCategory();
      dataBuilder.createSimpleCategory();
      
      await().atMost(1, TimeUnit.MINUTES).until(() -> {
        return categoriesApi.listCategories(Collections.emptyMap()).size() == 5;
      });
      
      assertEquals(3, categoriesApi.listCategories(null, null, null, null, 2l, null).size());
      assertEquals(2, categoriesApi.listCategories(null, null, null, null, 3l, 60l).size());
      assertEquals(2, categoriesApi.listCategories(null, null, null, null, 1l, 2l).size());
      assertEquals(2, categoriesApi.listCategories(null, null, null, null, 0l, 2l).size());
      assertEquals(3, categoriesApi.listCategories(null, null, null, null, null, 3l).size());
    } finally {
      dataBuilder.clean();
    }
  }

  @Test
  public void testSearchCategoriesSortDates() throws IOException, URISyntaxException {
    TestDataBuilder dataBuilder = new TestDataBuilder(this, USER_1_USERNAME, USER_1_PASSWORD);
    try {
      CategoriesApi categoriesApi = dataBuilder.getAnonymousCategoriesApi();

      Category category1 = dataBuilder.createSimpleCategory();
      waitCategoryCount(categoriesApi, 1);
      dataBuilder.createSimpleCategory();      
      waitCategoryCount(categoriesApi, 2);
      dataBuilder.createSimpleCategory();
      waitCategoryCount(categoriesApi, 3);
      dataBuilder.createSimpleCategory();
      waitCategoryCount(categoriesApi, 4);
      Category category5 = dataBuilder.createSimpleCategory();
      waitCategoryCount(categoriesApi, 5);
      
      List<Category> categoriesCreatedAsc = categoriesApi.listCategories(null, null, null, Arrays.asList(CategoryListSort.CREATED_AT_ASC.toString()), null, null);
      List<Category> categoriesCreatedDesc = categoriesApi.listCategories(null, null, null, Arrays.asList(CategoryListSort.CREATED_AT_DESC.toString()), null, null);
      List<Category> categoriesModifiedAsc = categoriesApi.listCategories(null, null, null, Arrays.asList(CategoryListSort.MODIFIED_AT_ASC.toString()), null, null);
      List<Category> categoriesModifiedDesc = categoriesApi.listCategories(null, null, null, Arrays.asList(CategoryListSort.MODIFIED_AT_DESC.toString()), null, null);
      
      assertEquals(category1.getId(), categoriesCreatedAsc.get(0).getId());
      assertEquals(category5.getId(), categoriesCreatedAsc.get(4).getId());
      assertEquals(category5.getId(), categoriesCreatedDesc.get(0).getId());
      assertEquals(category1.getId(), categoriesCreatedDesc.get(4).getId());
      assertEquals(category1.getId(), categoriesModifiedAsc.get(0).getId());
      assertEquals(category5.getId(), categoriesModifiedAsc.get(4).getId());
      assertEquals(category5.getId(), categoriesModifiedDesc.get(0).getId());
      assertEquals(category1.getId(), categoriesModifiedDesc.get(4).getId());
    } finally {
      dataBuilder.clean();
    }
  }

  @Test
  public void testSearchCategoriesSortScore() throws IOException, URISyntaxException {
    TestDataBuilder dataBuilder = new TestDataBuilder(this, USER_1_USERNAME, USER_1_PASSWORD);
    try {
      CategoriesApi categoriesApi = dataBuilder.getAnonymousCategoriesApi();

      fi.metatavu.dcfb.client.Category payload1 = new fi.metatavu.dcfb.client.Category();
      payload1.setTitle(dataBuilder.createLocalized("example title test"));
      Category category1 = dataBuilder.createCategory(payload1);
      waitCategoryCount(categoriesApi, 1);

      fi.metatavu.dcfb.client.Category payload2 = new fi.metatavu.dcfb.client.Category();
      payload2.setTitle(dataBuilder.createLocalized("test title test"));
      Category category2 = dataBuilder.createCategory(payload2);
      waitCategoryCount(categoriesApi, 2);

      List<Category> categoriesScoreAsc = categoriesApi.listCategories(null, "test", null, Arrays.asList(CategoryListSort.SCORE_ASC.toString()), null, null);
      List<Category> categoriesScoreDesc = categoriesApi.listCategories(null, "test", null, Arrays.asList(CategoryListSort.SCORE_DESC.toString()), null, null);
      
      assertEquals(category1.getId(), categoriesScoreAsc.get(0).getId());
      assertEquals(category2.getId(), categoriesScoreAsc.get(1).getId());
      assertEquals(category2.getId(), categoriesScoreDesc.get(0).getId());
      assertEquals(category1.getId(), categoriesScoreDesc.get(1).getId());
    } finally {
      dataBuilder.clean();
    }
  }

  @Test
  public void testUpdateCategory() throws IOException, URISyntaxException {
    TestDataBuilder dataBuilder = new TestDataBuilder(this, USER_1_USERNAME, USER_1_PASSWORD);
    try {
      CategoriesApi categoriesApi = dataBuilder.getAdminCategoriesApi();
      Category parentCategory = dataBuilder.createSimpleCategory();

      Category payload = new Category();
      payload.setTitle(dataBuilder.createLocalized("simple category"));
      payload.setMeta(Arrays.asList(
        dataBuilder.createMeta("test-1", "test value 1"),
        dataBuilder.createMeta("test-2", "test value 2")
      ));

      Category category = dataBuilder.createCategory(payload);
      
      Map<String, String> metaMap = mapMetas(category.getMeta());
      assertNull(category.getParentId());
      assertEquals(1, category.getTitle().size());
      assertEquals(Locale.ENGLISH.getLanguage(), category.getTitle().get(0).getLanguage());
      assertEquals("SINGLE", category.getTitle().get(0).getType());
      assertEquals("simple category", category.getTitle().get(0).getValue());
      assertEquals(2, category.getMeta().size());
      assertEquals("test value 1", metaMap.get("test-1"));
      assertEquals("test value 2", metaMap.get("test-2"));

      category.setParentId(parentCategory.getId());
      category.setTitle(dataBuilder.createLocalized(Locale.KOREA, "PLURAL", "updated category"));
      category.setMeta(Arrays.asList(
        dataBuilder.createMeta("test-1", "test value 1"),
        dataBuilder.createMeta("test-3", "test value 3"),
        dataBuilder.createMeta("test-4", "test value 4")
      ));

      Category updatedCategory = categoriesApi.updateCategory(category.getId(), category);
      Map<String, String> updatedMetaMap = mapMetas(updatedCategory.getMeta());
      assertEquals(parentCategory.getId(), updatedCategory.getParentId());
      assertEquals(1, updatedCategory.getTitle().size());
      assertEquals(Locale.KOREA.getLanguage(), updatedCategory.getTitle().get(0).getLanguage());
      assertEquals("PLURAL", updatedCategory.getTitle().get(0).getType());
      assertEquals("updated category", updatedCategory.getTitle().get(0).getValue());
      assertEquals(3, updatedCategory.getMeta().size());
      assertEquals("test value 1", updatedMetaMap.get("test-1"));
      assertEquals("test value 3", updatedMetaMap.get("test-3"));
      assertEquals("test value 4", updatedMetaMap.get("test-4"));
    } finally {
      dataBuilder.clean();
    }
  }

  @Test
  public void testUpdateCategoryAsUser() throws IOException, URISyntaxException {
    TestDataBuilder dataBuilder = new TestDataBuilder(this, USER_1_USERNAME, USER_1_PASSWORD);
    try {
      Category category = dataBuilder.createSimpleCategory();

      try {
        CategoriesApi categoriesApi = dataBuilder.getCategoriesApi();
        categoriesApi.updateCategory(category.getId(), category);
        fail("Updating categories as user should be forbidden");
      } catch (FeignException e) {
        assertEquals(403, e.status());
      }
    } finally {
      dataBuilder.clean();
    }
  }

  @Test
  public void testUpdateCategoryAsAnonymous() throws IOException, URISyntaxException {
    TestDataBuilder dataBuilder = new TestDataBuilder(this, USER_1_USERNAME, USER_1_PASSWORD);
    try {
      Category category = dataBuilder.createSimpleCategory();

      try {
        CategoriesApi categoriesApi = dataBuilder.getAnonymousCategoriesApi();
        categoriesApi.updateCategory(category.getId(), category);
        fail("Updating categories as user should be forbidden");
      } catch (FeignException e) {
        assertEquals(403, e.status());
      }
    } finally {
      dataBuilder.clean();
    }
  }
  
  @Test
  public void testDeleteCategory() throws IOException, URISyntaxException {
    TestDataBuilder dataBuilder = new TestDataBuilder(this, USER_1_USERNAME, USER_1_PASSWORD);
    try {
      CategoriesApi adminCategoriesApi = dataBuilder.getAdminCategoriesApi();
      
      Category simpleCategory = dataBuilder.createSimpleCategory();
      assertNotNull(adminCategoriesApi.findCategory(simpleCategory.getId()));
      adminCategoriesApi.deleteCategory(simpleCategory.getId());
      
      try {
        adminCategoriesApi.findCategory(simpleCategory.getId());
        fail("Item should not be found");
      } catch (FeignException e) {
        assertEquals(404, e.status());
      }
      
      dataBuilder.excludeCategoryFromClean(simpleCategory);
    } finally {
      dataBuilder.clean();
    }
  }

  @Test
  public void testDeleteCategoryAsUser() throws IOException, URISyntaxException {
    TestDataBuilder dataBuilder = new TestDataBuilder(this, USER_1_USERNAME, USER_1_PASSWORD);
    try {
      Category category = dataBuilder.createSimpleCategory();

      try {
        CategoriesApi categoriesApi = dataBuilder.getCategoriesApi();
        categoriesApi.deleteCategory(category.getId());
        fail("Deleting categories as user should be forbidden");
      } catch (FeignException e) {
        assertEquals(403, e.status());
      }
    } finally {
      dataBuilder.clean();
    }
  }

  @Test
  public void testDeleteCategoryAsAnonymous() throws IOException, URISyntaxException {
    TestDataBuilder dataBuilder = new TestDataBuilder(this, USER_1_USERNAME, USER_1_PASSWORD);
    try {
      Category category = dataBuilder.createSimpleCategory();

      try {
        CategoriesApi categoriesApi = dataBuilder.getAnonymousCategoriesApi();
        categoriesApi.deleteCategory(category.getId());
        fail("Deleting categories as user should be forbidden");
      } catch (FeignException e) {
        assertEquals(403, e.status());
      }
    } finally {
      dataBuilder.clean();
    }
  }

  private void waitCategoryCount(CategoriesApi categoriesApi, int count) {
    await().atMost(1, TimeUnit.MINUTES).until(() -> {
      return categoriesApi.listCategories(Collections.emptyMap()).size() == count;
    });
  }
  

}
