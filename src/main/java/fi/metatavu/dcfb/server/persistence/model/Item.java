package fi.metatavu.dcfb.server.persistence.model;

import java.time.OffsetDateTime;
import java.util.Currency;
import java.util.UUID;

import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Type;
import org.hibernate.validator.constraints.NotEmpty;

import fi.metatavu.dcfb.server.search.handlers.ItemIndexHandler;

/**
 * JPA entity for storing items
 * 
 * @author Antti Leppä
 */
@Entity
@EntityListeners(ItemIndexHandler.class)
@Cacheable(true)
@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL)
public class Item {

  @Id
  @Type(type="org.hibernate.type.PostgresUUIDType")
  private UUID id;
  
  @NotNull
  @NotEmpty
  @Column (nullable = false)
  private String typeOfBusiness;

  @ManyToOne (optional = false)
  private LocalizedEntry title;
  
  @ManyToOne (optional = false)
  private LocalizedEntry description;
  
  @ManyToOne (optional = false)
  private Category category;

  @ManyToOne
  private Location location;
  
  @Column(nullable = false, unique = true)
  @NotNull
  @NotEmpty
  private String slug;
  
  private String businessName;
    
  private String businessCode;
  
  @Column (nullable = false)
  private OffsetDateTime createdAt;

  @Column (nullable = false)
  private OffsetDateTime modifiedAt;

  @Column
  private OffsetDateTime expiresAt;

  @Column (nullable = false)
  @Type(type="org.hibernate.type.PostgresUUIDType")
  private UUID lastModifier;

  private String unitPrice;
  
  private Currency priceCurrency;

  @Column (nullable = false)
  private Long amount;

  @Column (nullable = false)
  private Long soldAmount;
  
  @Column(nullable = false)
  @NotNull
  @NotEmpty
  private String unit;

  @Column (nullable = false)
  private boolean visibilityLimited;

  @Column (nullable = true)
  @Type(type="org.hibernate.type.PostgresUUIDType")
  private UUID resourceId;

  @Column (nullable = true)
  @Type(type="org.hibernate.type.PostgresUUIDType")
  private UUID sellerId;
  
  @Column (nullable = false)
  private Boolean allowPurchaseCreditCard;
  
  @Column (nullable = false)
  private Boolean allowPurchaseContactSeller;

  private Integer deliveryTime;

  private String contactEmail;
  
  private String contactPhone;

  private String deliveryPrice;
  
  private Currency deliveryCurrency;

  private Boolean allowDelivery;

  private Boolean allowPickup;

  @Lob
  @Type(type = "org.hibernate.type.TextType")
  private String termsOfDelivery;

  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }
  
  public String getTypeOfBusiness() {
    return typeOfBusiness;
  }
  
  public void setTypeOfBusiness(String typeOfBusiness) {
    this.typeOfBusiness = typeOfBusiness;
  }

  public LocalizedEntry getTitle() {
    return title;
  }

  public void setTitle(LocalizedEntry title) {
    this.title = title;
  }

  public LocalizedEntry getDescription() {
    return description;
  }

  public void setDescription(LocalizedEntry description) {
    this.description = description;
  }

  public Category getCategory() {
    return category;
  }

  public void setCategory(Category category) {
    this.category = category;
  }

  public void setVisibilityLimited(boolean visibilityLimited) {
    this.visibilityLimited = visibilityLimited;
  }

  public boolean getVisibilityLimited() {
    return visibilityLimited;
  }

  /**
   * @return the location
   */
  public Location getLocation() {
    return location;
  }

  /**
   * @param location the location to set
   */
  public void setLocation(Location location) {
    this.location = location;
  }

  public String getSlug() {
    return slug;
  }

  public void setSlug(String slug) {
    this.slug = slug;
  }

  public OffsetDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(OffsetDateTime createdAt) {
    this.createdAt = createdAt;
  }

  public OffsetDateTime getModifiedAt() {
    return modifiedAt;
  }

  public void setModifiedAt(OffsetDateTime modifiedAt) {
    this.modifiedAt = modifiedAt;
  }

  public OffsetDateTime getExpiresAt() {
    return expiresAt;
  }

  public void setExpiresAt(OffsetDateTime expiresAt) {
    this.expiresAt = expiresAt;
  }
  
  public String getUnitPrice() {
    return unitPrice;
  }
  
  public void setUnitPrice(String unitPrice) {
    this.unitPrice = unitPrice;
  }

  public Currency getPriceCurrency() {
    return priceCurrency;
  }

  public void setPriceCurrency(Currency priceCurrency) {
    this.priceCurrency = priceCurrency;
  }

  public Long getAmount() {
    return amount;
  }

  public void setAmount(Long amount) {
    this.amount = amount;
  }

  public String getUnit() {
    return unit;
  }

  public void setUnit(String unit) {
    this.unit = unit;
  }

  /**
   * @return the resourceId
   */
  public UUID getResourceId() {
    return resourceId;
  }

  /**
   * @param resourceId the resourceId to set
   */
  public void setResourceId(UUID resourceId) {
    this.resourceId = resourceId;
  }

  /**
   * @return the sellerId
   */
  public UUID getSellerId() {
    return sellerId;
  }

  /**
   * @param sellerId the sellerId to set
   */
  public void setSellerId(UUID sellerId) {
    this.sellerId = sellerId;
  }

  public UUID getLastModifier() {
    return lastModifier;
  }
  
  public void setLastModifier(UUID lastModifier) {
    this.lastModifier = lastModifier;
  }

  /**
   * @return the soldAmount
   */
  public Long getSoldAmount() {
    return soldAmount;
  }

  /**
   * @param soldAmount the soldAmount to set
   */
  public void setSoldAmount(Long soldAmount) {
    this.soldAmount = soldAmount;
  }
  
  public Boolean getAllowPurchaseContactSeller() {
    return allowPurchaseContactSeller;
  }
  
  public void setAllowPurchaseContactSeller(Boolean allowPurchaseContactSeller) {
    this.allowPurchaseContactSeller = allowPurchaseContactSeller;
  }
  
  public Boolean getAllowPurchaseCreditCard() {
    return allowPurchaseCreditCard;
  }
  
  public void setAllowPurchaseCreditCard(Boolean allowPurchaseCreditCard) {
    this.allowPurchaseCreditCard = allowPurchaseCreditCard;
  }
  
  public Integer getDeliveryTime() {
    return deliveryTime;
  }

  public void setDeliveryTime(Integer deliveryTime) {
    this.deliveryTime = deliveryTime;
  }

  public String getContactEmail() {
    return contactEmail;
  }

  public void setContactEmail(String contactEmail) {
    this.contactEmail = contactEmail;
  }

  public String getContactPhone() {
    return contactPhone;
  }

  public void setContactPhone(String contactPhone) {
    this.contactPhone = contactPhone;
  }

  public String getTermsOfDelivery() {
    return termsOfDelivery;
  }

  public void setTermsOfDelivery(String termsOfDelivery) {
    this.termsOfDelivery = termsOfDelivery;
  }

  /**
   * @return the deliveryCurrency
   */
  public Currency getDeliveryCurrency() {
    return deliveryCurrency;
  }

  /**
   * @param deliveryCurrency the deliveryCurrency to set
   */
  public void setDeliveryCurrency(Currency deliveryCurrency) {
    this.deliveryCurrency = deliveryCurrency;
  }

  /**
   * @return the deliveryPrice
   */
  public String getDeliveryPrice() {
    return deliveryPrice;
  }

  /**
   * @param deliveryPrice the deliveryPrice to set
   */
  public void setDeliveryPrice(String deliveryPrice) {
    this.deliveryPrice = deliveryPrice;
  }

  /**
   * @return the allowDelivery
   */
  public Boolean getAllowDelivery() {
    return allowDelivery;
  }

  /**
   * @param allowDelivery the allowDelivery to set
   */
  public void setAllowDelivery(Boolean allowDelivery) {
    this.allowDelivery = allowDelivery;
  }

  /**
   * @return the allowPickup
   */
  public Boolean getAllowPickup() {
    return allowPickup;
  }

  /**
   * @param allowPickup the allowPickup to set
   */
  public void setAllowPickup(Boolean allowPickup) {
    this.allowPickup = allowPickup;
  }
  
  public String getBusinessCode() {
    return businessCode;
  }
  
  public void setBusinessCode(String businessCode) {
    this.businessCode = businessCode;
  }
  
  public String getBusinessName() {
    return businessName;
  }
  
  public void setBusinessName(String businessName) {
    this.businessName = businessName;
  }

  @PrePersist
  public void onCreate() {
    setCreatedAt(OffsetDateTime.now());
    setModifiedAt(OffsetDateTime.now());
  }
  
  @PreUpdate
  public void onUpdate() {
    setModifiedAt(OffsetDateTime.now());
  }
  
}