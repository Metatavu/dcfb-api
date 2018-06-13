package fi.metatavu.dcfb.server;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Locale;

import org.junit.Test;

import feign.FeignException;
import fi.metatavu.dcfb.client.CategoriesApi;
import fi.metatavu.dcfb.client.Category;

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
      Category createdCategory = dataBuilder.createCategory(category);
      
      assertNotNull(createdCategory);
      assertNotNull(createdCategory.getId());
      assertEquals(parentCategory.getId(), createdCategory.getParentId());
      assertEquals(1, createdCategory.getTitle().size());
      assertEquals(Locale.GERMAN.getLanguage(), createdCategory.getTitle().get(0).getLanguage());
      assertEquals("PLURAL", createdCategory.getTitle().get(0).getType());
      assertEquals("new category", createdCategory.getTitle().get(0).getValue());
    } finally {
      dataBuilder.clean();
    }
  }

  @Test
  public void testFindCategory() throws IOException, URISyntaxException {
    TestDataBuilder dataBuilder = new TestDataBuilder(this, USER_1_USERNAME, USER_1_PASSWORD);
    try {
      CategoriesApi categoryApi = dataBuilder.getCategoryApi();
      Category createdCategory = dataBuilder.createSimpleCategory();
      assertEquals(createdCategory.toString(), categoryApi.findCategory(createdCategory.getId()).toString());
    } finally {
      dataBuilder.clean();
    }
  }

  @Test
  public void testListCategory() throws IOException, URISyntaxException {
    TestDataBuilder dataBuilder = new TestDataBuilder(this, USER_1_USERNAME, USER_1_PASSWORD);
    try {
      CategoriesApi categoryApi = dataBuilder.getCategoryApi();
      Category category1 = dataBuilder.createSimpleCategory();
      Category category2 = dataBuilder.createSimpleCategory();
      List<Category> list = categoryApi.listCategories(null, null);

      assertEquals(2, list.size());
      assertEquals(category1.toString(), list.get(0).toString());
      assertEquals(category2.toString(), list.get(1).toString());
    } finally {
      dataBuilder.clean();
    }
  }

  @Test
  public void testUpdateCategory() throws IOException, URISyntaxException {
    TestDataBuilder dataBuilder = new TestDataBuilder(this, USER_1_USERNAME, USER_1_PASSWORD);
    try {
      CategoriesApi categoryApi = dataBuilder.getCategoryApi();
      Category parentCategory = dataBuilder.createSimpleCategory();
      Category category = dataBuilder.createSimpleCategory();
      
      assertNull(category.getParentId());
      assertEquals(1, category.getTitle().size());
      assertEquals(Locale.ENGLISH.getLanguage(), category.getTitle().get(0).getLanguage());
      assertEquals("SINGLE", category.getTitle().get(0).getType());
      assertEquals("simple category", category.getTitle().get(0).getValue());
      
      category.setParentId(parentCategory.getId());
      category.setTitle(dataBuilder.createLocalized(Locale.KOREA, "PLURAL", "updated category"));
      
      Category updatedCategory = categoryApi.updateCategory(category.getId(), category);
      assertEquals(parentCategory.getId(), updatedCategory.getParentId());
      assertEquals(1, updatedCategory.getTitle().size());
      assertEquals(Locale.KOREA.getLanguage(), updatedCategory.getTitle().get(0).getLanguage());
      assertEquals("PLURAL", updatedCategory.getTitle().get(0).getType());
      assertEquals("updated category", updatedCategory.getTitle().get(0).getValue());
    } finally {
      dataBuilder.clean();
    }
  }
  
  @Test
  public void testDeleteCategory() throws IOException, URISyntaxException {
    TestDataBuilder dataBuilder = new TestDataBuilder(this, USER_1_USERNAME, USER_1_PASSWORD);
    try {
      CategoriesApi adminCategoryApi = dataBuilder.getAdminCategoryApi();
      
      Category simpleCategory = dataBuilder.createSimpleCategory();
      assertNotNull(adminCategoryApi.findCategory(simpleCategory.getId()));
      adminCategoryApi.deleteCategory(simpleCategory.getId());
      
      try {
        adminCategoryApi.findCategory(simpleCategory.getId());
        fail("Item should not be found");
      } catch (FeignException e) {
        assertEquals(404, e.status());
      }
      
      dataBuilder.excludeCategoryFromClean(simpleCategory);
    } finally {
      dataBuilder.clean();
    }
  }

}
