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
import fi.metatavu.dcfb.server.rest.model.ItemReservation;
import fi.metatavu.dcfb.server.rest.model.Meta;
import fi.metatavu.dcfb.server.rest.model.Price;
import fi.metatavu.dcfb.server.rest.translate.ItemTranslator;
import fi.metatavu.dcfb.server.search.searchers.SearchResult;

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
    
    if (payload.getPaymentMethods() == null) {
      return createBadRequest("PaymentMethods is required");
    }

    UUID sellerId = payload.getSellerId();
    if (!isRealmAdmin() && !sellerId.equals(getLoggerUserId())) {
      return createForbidden(UNAUTHORIZED);
    }
    
    if (sellerId == null) {
      return createBadRequest("Seller is required");
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
    
    Currency priceCurrency = getPriceCurrency(payload.getUnitPrice());
    if (priceCurrency == null) {
      return createBadRequest(String.format("Invalid currency %s", payload.getUnitPrice().getCurrency()));
    }
    
    Boolean allowPurchaseContactSeller = payload.getPaymentMethods().isAllowContactSeller();
    Boolean allowPurchaseCreditCard = payload.getPaymentMethods().isAllowCreditCard();
    
    if (allowPurchaseCreditCard && !keycloakAdminController.userHasAttribute(sellerId, KeycloakConsts.KEYCLOAK_STRIPE_ACCOUNT_ATTRIBUTE)) {
      return createBadRequest("Users without stripe account id cannot create items with credit card payment");
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
    Integer deliveryTime = payload.getDeliveryTime();
    String contactEmail = payload.getContactEmail();
    String contactPhone = payload.getContactPhone();
    String termsOfDelivery = payload.getTermsOfDelivery();
    Boolean allowDelivery = payload.isAllowDelivery() != null ? payload.isAllowDelivery() : false;
    Boolean allowPickup = payload.isAllowPickup() != null ? payload.isAllowPickup() : false;
    String deliveryPrice = payload.getDeliveryPrice() != null ? payload.getDeliveryPrice().getPrice() : null;
    Currency deliveryCurrency = getPriceCurrency(payload.getDeliveryPrice());
    String businessCode = payload.getBusinessCode();
    String businessName = payload.getBusinessName();

    Long soldAmount = payload.getSoldAmount();
    if (soldAmount == null) {
      soldAmount = 0l;
    }
    
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
        soldAmount,
        allowPurchaseContactSeller,
        allowPurchaseCreditCard,
        deliveryTime,
        contactEmail,
        contactPhone,
        termsOfDelivery,
        allowDelivery,
        allowPickup,
        deliveryPrice,
        deliveryCurrency,
        businessName, 
        businessCode,
        sellerId,
        modifier);

    createImages(payload, item);
    List<ItemUser> itemUsers = createItemUsers(payload, item);
    ResourceRepresentation resource = createProtectedResource(item, itemUsers);
    if (resource != null) {
      itemController.setResourceId(item, UUID.fromString(resource.getId()), modifier);
    }
    setItemMetas(item, payload.getMeta());
    return createOk(itemTranslator.translateItem(item));
  }

  private Currency getPriceCurrency(Price price) {
    if (price == null || price.getCurrency() == null) {
      return null;
    }

    try {
      return Currency.getInstance(price.getCurrency());
    } catch (IllegalArgumentException e) {
      logger.warn("Failed to parse currency", e);
    }
    
    return null;
  }

  @Override
  public Response createItemReservation(UUID itemId, ItemReservation payload) throws Exception {
    if (!isRealmUser()) {
      return createForbidden("Anonymous users can not create item reservations");
    }

    fi.metatavu.dcfb.server.persistence.model.Item item = itemController.findItem(itemId);
    if (item == null) {
      return createNotFound(NOT_FOUND_MESSAGE);
    }
    
    long itemsLeft = item.getAmount() - (item.getSoldAmount() + itemController.countReservedAmountByItem(item));
    if (itemsLeft < payload.getAmount()) {
      return createBadRequest("Not enough items left in the stock");
    }
    
    fi.metatavu.dcfb.server.persistence.model.ItemReservation itemReservation = itemController.createResevation(item, payload.getAmount());
    return createOk(itemTranslator.translateItemReservation(itemReservation));
  }
  
  @Override
  public Response findItemReservation(UUID itemId, UUID itemReservationId) throws Exception {
    if (!isRealmUser()) {
      return createForbidden("Anonymous users can not find item reservations");
    }
    
    fi.metatavu.dcfb.server.persistence.model.Item item = itemController.findItem(itemId);
    if (item == null) {
      return createNotFound(NOT_FOUND_MESSAGE);
    }

    fi.metatavu.dcfb.server.persistence.model.ItemReservation itemReservation = itemController.findItemReservation(itemReservationId);
    if (itemReservation == null) {
      return createNotFound(NOT_FOUND_MESSAGE);
    }
    
    if (!itemReservation.getItem().getId().equals(item.getId())) {
      return createNotFound(NOT_FOUND_MESSAGE);  
    }
    
    return createOk(itemTranslator.translateItemReservation(itemReservation));
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
  public Response listItems(String categoryIdsParam, String locationIdsParam, String userIdsParam, String search, Double nearLat,
      Double nearLon, Boolean includeExhausted, List<String> sort, Long firstResult, Long maxResults) throws Exception {
    
    List<UUID> userIds = getListParameter(userIdsParam, UUID::fromString);
    
    if (!isRealmAdmin() && (userIds != null && !userIds.isEmpty())) {
      if (userIds.size() > 1) {
        return createForbidden("You don't have permission filter by this user id");
      }
      
      UUID userId = userIds.get(0);
      if (!getLoggerUserId().equals(userId)) {
        return createForbidden("You don't have permission filter by this user id");
      }
    }
    
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

    SearchResult<fi.metatavu.dcfb.server.persistence.model.Item> searchResult = itemController.searchItems(nearLat, nearLon, 
      userIds, categories, locations, search, getLoggerUserId(), includeExhausted != null ? includeExhausted.booleanValue() : false, 
      firstResult, maxResults, sorts);

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
    Integer deliveryTime = payload.getDeliveryTime();
    String contactEmail = payload.getContactEmail();
    String contactPhone = payload.getContactPhone();
    String termsOfDelivery = payload.getTermsOfDelivery();
    Boolean allowPurchaseContactSeller = payload.getPaymentMethods().isAllowContactSeller();
    Boolean allowPurchaseCreditCard = payload.getPaymentMethods().isAllowCreditCard();
    Boolean allowDelivery = payload.isAllowDelivery() != null ? payload.isAllowDelivery() : false;
    Boolean allowPickup = payload.isAllowPickup() != null ? payload.isAllowPickup() : false;
    String deliveryPrice = payload.getDeliveryPrice() != null ? payload.getDeliveryPrice().getPrice() : null;
    Currency deliveryCurrency = getPriceCurrency(payload.getDeliveryPrice());
    String businessCode = payload.getBusinessCode();
    String businessName = payload.getBusinessName();

    Category category = categoryController.findCategory(payload.getCategoryId());
    if (category == null) {
      return createBadRequest(String.format("Invalid category %s", payload.getCategoryId()));
    }

    Location location = payload.getLocationId() != null ? locationController.findLocation(payload.getLocationId()) : null;
    if (payload.getLocationId() != null && location == null) {
      return createBadRequest(String.format("Invalid location %s", payload.getLocationId()));
    }
    
    Currency priceCurrency = getPriceCurrency(payload.getUnitPrice());
    if (priceCurrency == null) {
      return createBadRequest(String.format("Invalid currency %s", payload.getUnitPrice().getCurrency()));
    }

    item = itemController.updateItem(item, 
        title, 
        description, 
        category,
        visibilityLimited, 
        location,
        slug, 
        expiresAt, 
        unitPrice, 
        priceCurrency, 
        amount, 
        unit,
        soldAmount,
        allowPurchaseContactSeller,
        allowPurchaseCreditCard,
        deliveryTime,
        contactEmail,
        contactPhone,
        termsOfDelivery,
        allowDelivery,
        allowPickup,
        deliveryPrice,
        deliveryCurrency,
        businessName, 
        businessCode,
        sellerId,
        modifier);
    
    itemController.deleteItemImages(item);
    itemController.deleteItemUsers(item);
    List<ItemUser> itemUsers = createItemUsers(payload, item);
    updateProtectedResource(item, itemUsers);
    createImages(payload, item);
    setItemMetas(item, payload.getMeta());
    
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
