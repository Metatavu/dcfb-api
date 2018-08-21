package fi.metatavu.dcfb.server.rest;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.Currency;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.ejb.Stateful;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.core.Response;

import org.apache.commons.lang3.StringUtils;
import org.keycloak.authorization.client.AuthzClient;
import org.keycloak.representations.idm.authorization.ResourceRepresentation;
import org.keycloak.representations.idm.authorization.ScopeRepresentation;
import org.slf4j.Logger;

import com.stripe.model.Product;

import fi.metatavu.dcfb.server.categories.CategoryController;
import fi.metatavu.dcfb.server.items.ItemController;
import fi.metatavu.dcfb.server.keycloak.KeycloakAdminController;
import fi.metatavu.dcfb.server.keycloak.KeycloakConsts;
import fi.metatavu.dcfb.server.locations.LocationController;
import fi.metatavu.dcfb.server.persistence.model.Category;
import fi.metatavu.dcfb.server.persistence.model.ItemUser;
import fi.metatavu.dcfb.server.persistence.model.LocalizedEntry;
import fi.metatavu.dcfb.server.persistence.model.Location;
import fi.metatavu.dcfb.server.rest.model.Image;
import fi.metatavu.dcfb.server.rest.model.Item;
import fi.metatavu.dcfb.server.rest.model.ItemListSort;
import fi.metatavu.dcfb.server.rest.model.Meta;
import fi.metatavu.dcfb.server.rest.translate.ItemTranslator;
import fi.metatavu.dcfb.server.search.searchers.SearchResult;
import fi.metatavu.dcfb.server.stripe.StripeController;

/**
 * Items REST Service implementation
 * 
 * @author Antti Leppä
 */
@RequestScoped
@Stateful
public class ItemsApiImpl extends AbstractApi implements ItemsApi {

  public static final String SCOPE_ITEM_VIEW = "item:view";

  public static final String SCOPE_ITEM_MANAGE = "item:manage";

  public static final String ITEM_RESOURCE_TYPE = "urn:dcfbapi:resources:item";

  private static final String RESOURCE_VISIBILITY_ATTR = "visibility";

  private static final String RESOURCE_VISIBILITY_USERS_ATTR = "visibleTo";

  private static final String RESOURCE_VISIBILITY_PUBLIC = "public";

  private static final String RESOURCE_VISIBILITY_PRIVATE = "private";

  @Inject
  private StripeController stripeController;
  
  @Inject
  private KeycloakAdminController keycloakAdminController;

  @Inject
  private CategoryController categoryController;

  @Inject
  private LocationController locationController;

  @Inject
  private ItemController itemController;

  @Inject
  private ItemTranslator itemTranslator;

  @Inject
  private Logger logger;

  @Override
  public Response createItem(Item payload) throws Exception {
    if (!isRealmUser()) {
      return createForbidden("Anonymous users can not create items");
    }

    UUID sellerId = payload.getSellerId();
    if (!isRealmAdmin() && !sellerId.equals(getLoggerUserId())) {
      return createForbidden(UNAUTHORIZED);
    }
    
    if (!keycloakAdminController.userHasAttribute(sellerId, KeycloakConsts.KEYCLOAK_STRIPE_ACCOUNT_ATTRIBUTE)) {
      return createBadRequest("Users without stripe account id cannot create items");
    }
    
    if (!isValidLocalizedList(payload.getTitle())) {
      return createBadRequest("Invalid title");
    }

    if (!isValidLocalizedList(payload.getDescription())) {
      return createBadRequest("Invalid description");
    }

    Category category = categoryController.findCategory(payload.getCategoryId());
    if (category == null) {
      return createBadRequest(String.format("Invalid category %s", payload.getCategoryId()));
    }

    Location location = payload.getLocationId() != null ? locationController.findLocation(payload.getLocationId()) : null;
    if (payload.getLocationId() != null && location == null) {
      return createBadRequest(String.format("Invalid location %s", payload.getLocationId()));
    }
    
    Currency priceCurrency = null;
    try {
      priceCurrency = Currency.getInstance(payload.getUnitPrice().getCurrency());
    } catch (IllegalArgumentException e) {
      logger.warn("Failed to parse currency", e);
      return createBadRequest(String.format("Invalid currency %s", e.getMessage()));
    }

    LocalizedEntry title = createLocalizedEntry(payload.getTitle());
    LocalizedEntry description = createLocalizedEntry(payload.getDescription());
    String slug = StringUtils.isNotBlank(payload.getSlug()) ? payload.getSlug() : slugifyLocalized(payload.getTitle());
    OffsetDateTime expiresAt = payload.getExpiresAt();
    String unitPrice = payload.getUnitPrice().getPrice();
    Long amount = payload.getAmount();
    String unit = payload.getUnit();
    UUID modifier = getLoggerUserId();
    Boolean visibilityLimited = Boolean.FALSE;
    Long soldAmount = payload.getSoldAmount();
    
    if (payload.isVisibilityLimited() != null) {
      visibilityLimited = payload.isVisibilityLimited();
    } 
    
    fi.metatavu.dcfb.server.persistence.model.Item item = itemController.createItem(
        title, 
        description, 
        category, 
        location,
        slug, 
        expiresAt, 
        unitPrice, 
        priceCurrency, 
        amount, 
        unit,
        visibilityLimited,
        null,
        sellerId,
        soldAmount,
        modifier);
//    
//    Product stripeProduct = stripeController.createItemProduct(item);
//    if (stripeProduct == null) {
//      return createInternalServerError("Failed to create Stripe product");
//    }
//    
//    itemController.updateItemStripeProductId(item, stripeProduct.getId(), modifier);

    createImages(payload, item);
    List<ItemUser> itemUsers = createItemUsers(payload, item);
    ResourceRepresentation resource = createProtectedResource(item, itemUsers);
    if (resource != null) {
      itemController.setResourceId(item, UUID.fromString(resource.getId()), modifier);
    }
    setItemMetas(item, payload.getMeta());
    
    return createOk(itemTranslator.translateItem(item));
  }

  @Override
  public Response deleteItem(UUID itemId) throws Exception {

    if (!isRealmUser()) {
      return createForbidden("Anonymous users can not delete items");
    }

    fi.metatavu.dcfb.server.persistence.model.Item item = itemController.findItem(itemId);
    if (item == null) {
      return createNotFound(NOT_FOUND_MESSAGE);
    }
    
    itemController.deleteItem(item);
    deleteProtectedResource(item);

    return createNoContent();
  }

  @Override
  public Response findItem(UUID itemId) throws Exception {

    fi.metatavu.dcfb.server.persistence.model.Item item = itemController.findItem(itemId);
    if (item == null) {
      return createNotFound(NOT_FOUND_MESSAGE);
    }

    return createOk(itemTranslator.translateItem(item));
  }
  
  @Override
  public Response listItems(String categoryIdsParam, String locationIdsParam, String userIds, String search, List<String> sort, Long firstResult, Long maxResults) throws Exception {

    // TODO: userIds
    
    List<Category> categories = null;
    List<Location> locations = null;

    try {
      categories = getListParameter(categoryIdsParam, param -> {
        UUID id = UUID.fromString(param);
        Category category = categoryController.findCategory(id);
        if (category == null) {
          throw new IllegalArgumentException(String.format("Could not find category %s", id));
        }
        
        return category;
      });

      locations = getListParameter(locationIdsParam, param -> {
        UUID id = UUID.fromString(param);
        Location location = locationController.findLocation(id);
        if (location == null) {
          throw new IllegalArgumentException(String.format("Could not find location %s", id));
        }
        
        return location;
      });

    } catch (IllegalArgumentException e) {
      logger.warn("Failed to parse list parameters", e);
      return createBadRequest(e.getMessage());
    }
    
    List<ItemListSort> sorts = null;
    try {
      sorts = getEnumListParameter(ItemListSort.class, sort);
    } catch (IllegalArgumentException e) {
      logger.warn("Failed to parse enum parameters", e);
      return createBadRequest(e.getMessage());
    }

    SearchResult<fi.metatavu.dcfb.server.persistence.model.Item> searchResult = itemController.searchItems(categories, locations, search, getLoggerUserId(), firstResult, maxResults, sorts);

    return createOk(itemTranslator.translateItems(searchResult.getResult()), searchResult.getTotalHits());
  }

  @Override
  public Response updateItem(UUID itemId, Item payload) throws Exception {

    if (!isRealmUser()) {
      return createForbidden("Anonymous users can not update items");
    }

    fi.metatavu.dcfb.server.persistence.model.Item item = itemController.findItem(itemId);
    if (item == null) {
      return createNotFound(NOT_FOUND_MESSAGE);
    }
    
    LocalizedEntry title = updateLocalizedEntry(item.getTitle(), payload.getTitle());
    LocalizedEntry description = updateLocalizedEntry(item.getDescription(), payload.getDescription());
    String slug = payload.getSlug();
    OffsetDateTime expiresAt = payload.getExpiresAt();
    String unitPrice = payload.getUnitPrice().getPrice();
    Long amount = payload.getAmount();
    String unit = payload.getUnit();
    UUID modifier = getLoggerUserId();
    boolean visibilityLimited = payload.isVisibilityLimited();
    UUID sellerId = payload.getSellerId();
    Long soldAmount = payload.getSoldAmount();

    Category category = categoryController.findCategory(payload.getCategoryId());
    if (category == null) {
      return createBadRequest(String.format("Invalid category %s", payload.getCategoryId()));
    }

    Location location = payload.getLocationId() != null ? locationController.findLocation(payload.getLocationId()) : null;
    if (payload.getLocationId() != null && location == null) {
      return createBadRequest(String.format("Invalid location %s", payload.getLocationId()));
    }
    
    Currency priceCurrency = null;
    try {
      priceCurrency = Currency.getInstance(payload.getUnitPrice().getCurrency());
    } catch (IllegalArgumentException e) {
      logger.warn("Failed to parse list parameters", e);
      return createBadRequest(String.format("Invalid currency %s", e.getMessage()));
    }
    
    item = itemController.updateItem(item, 
        title, 
        description, 
        category,
        location,
        slug, 
        expiresAt, 
        unitPrice, 
        priceCurrency, 
        amount, 
        unit,
        visibilityLimited, 
        sellerId,
        soldAmount,
        modifier);
    
    itemController.deleteItemImages(item);
    itemController.deleteItemUsers(item);
    List<ItemUser> itemUsers = createItemUsers(payload, item);
    updateProtectedResource(item, itemUsers);
    createImages(payload, item);
    setItemMetas(item, payload.getMeta());
    
    Product stripeProduct = stripeController.updateItemProduct(item);
    if (stripeProduct == null) {
      return createInternalServerError("Failed to update Stripe product");
    }
    
    return createOk(itemTranslator.translateItem(item));
  }

  /**
   * Creates item images from REST request
   * 
   * @param payload REST request
   * @param item item
   */
  private void createImages(Item payload, fi.metatavu.dcfb.server.persistence.model.Item item) {
    if (payload.getImages() != null) {
      payload.getImages().stream().forEach(image -> createImage(item, image));
    }
  }

  /**
   * Creates item image from REST object
   * 
   * @param payload REST object
   * @param item item
   */
  private void createImage(fi.metatavu.dcfb.server.persistence.model.Item item, Image image) {
    itemController.createImageItem(item, image.getType(), image.getUrl());
  }

  /**
   * Creates item users
   * 
   * @param payload REST object
   * @param item item
   */
  private List<ItemUser> createItemUsers(Item payload, fi.metatavu.dcfb.server.persistence.model.Item item) {
    if (payload.getVisibleToUsers() == null) {
      return Collections.emptyList();
    }

    return payload.getVisibleToUsers().stream().map(userId -> itemController.createItemUser(item, userId)).collect(Collectors.toList());
  }

  /**
   * Sets meta values for a item
   * 
   * @param item
   * @param metas
   * @return item
   */
  private fi.metatavu.dcfb.server.persistence.model.Item setItemMetas(fi.metatavu.dcfb.server.persistence.model.Item item, List<Meta> metas) {
    if (metas == null) {
      return item;
    }

    Set<String> usedKeys = new HashSet<>(metas.size());

    for (Meta meta : metas) {
      String key = meta.getKey();
      usedKeys.add(key);
      itemController.setMeta(item, key, meta.getValue());  
    }

    itemController.deleteMetasNotIn(item, usedKeys);

    return item;
  }

  /**
   * Creates protected resource to keycloak
   * 
   * @param item Item to create the resource for
   * @param visibleToUsers list of users resource is visible to
   * 
   * @return create resource
   */
  private ResourceRepresentation createProtectedResource(fi.metatavu.dcfb.server.persistence.model.Item item, List<ItemUser> visibleToUsers) {
    HashSet<ScopeRepresentation> scopes = new HashSet<>();
    scopes.add(new ScopeRepresentation(SCOPE_ITEM_MANAGE));
    scopes.add(new ScopeRepresentation(SCOPE_ITEM_VIEW));

    ResourceRepresentation itemResource = new ResourceRepresentation(item.getSlug(), scopes, String.format("/v1/items/%s", item.getId()), ITEM_RESOURCE_TYPE);
    itemResource.setOwner(getLoggerUserId().toString());
    itemResource.setOwnerManagedAccess(true);

    itemResource.setAttributes(createResourceAttributes(item.getVisibilityLimited(), visibleToUsers.stream().map(ItemUser::getUserId).collect(Collectors.toList())));

    AuthzClient client = getAuthzClient();
    if (client == null) {
      logger.warn("Error getting authorization client, cannot create resource");
      return null;
    }

    return getAuthzClient().protection().resource().create(itemResource);
  }

  /**
   * Updates protected resource to keycloak
   * 
   * @param item Item to create the resource for
   * @param visibleToUsers list of users resource is visible to
   */
  private void updateProtectedResource(fi.metatavu.dcfb.server.persistence.model.Item item, List<ItemUser> visibleToUsers) {
    UUID resourceId = item.getResourceId();
    if (resourceId == null) {
      return;
    }
    
    AuthzClient client = getAuthzClient();
    ResourceRepresentation resource = client.protection().resource().findById(resourceId.toString());
    if (resource == null) {
      return;
    }

    resource.setAttributes(createResourceAttributes(item.getVisibilityLimited(), visibleToUsers.stream().map(ItemUser::getUserId).collect(Collectors.toList())));
    client.protection().resource().update(resource);
  }

  /**
   * Deletes protected resource
   * 
   * @param item Item the resource is connected to
   */
  private void deleteProtectedResource(fi.metatavu.dcfb.server.persistence.model.Item item) {
    UUID resourceId = item.getResourceId();
    if (resourceId == null) {
      return;
    }

    getAuthzClient().protection().resource().delete(resourceId.toString());
  }

  /**
   * Creates attributes for protected resource
   * 
   * @param visibilityLimited is item visibility limited
   * @param visibleToUsers list of users resource is visible to
   * 
   * @return resource attributes
   */
  private Map<String, List<String>> createResourceAttributes(boolean visibilityLimited, List<UUID> visibleToUsers) {
    HashMap<String, List<String>> attributes = new HashMap<>();
    String visibility = visibilityLimited ? RESOURCE_VISIBILITY_PRIVATE : RESOURCE_VISIBILITY_PUBLIC;
    attributes.put(RESOURCE_VISIBILITY_ATTR, Arrays.asList(visibility));
    attributes.put(RESOURCE_VISIBILITY_USERS_ATTR, visibleToUsers.stream().map(UUID::toString).collect(Collectors.toList()));
    return attributes;
  }

}
