package fi.metatavu.dcfb.server.persistence.model;

import java.time.OffsetDateTime;
import java.util.Currency;
import java.util.UUID;

import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.validator.constraints.NotEmpty;

/**
 * JPA entity for storing items
 * 
 * @author Antti Leppä
 */
@Entity
@Cacheable(true)
@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL)
public class Item {

  @Id
  private UUID id;

  @ManyToOne (optional = false)
  private LocalizedEntry title;
  
  @ManyToOne (optional = false)
  private LocalizedEntry description;
  
  @ManyToOne (optional = false)
  private Category category;
  
  @Column(nullable = false)
  @NotNull
  @NotEmpty
  private String slug;

  @Column (nullable = false)
  private OffsetDateTime createdAt;

  @Column (nullable = false)
  private OffsetDateTime modifiedAt;

  @Column
  private OffsetDateTime expiresAt;

  @Column (nullable = false)
  private UUID lastModifier;

  @Column(nullable = false)
  @NotNull
  @NotEmpty
  private String unitPrice;
  
  @Column (nullable = false)
  private Currency priceCurrency;

  @Column (nullable = false)
  private Long amount;

  @Column(nullable = false)
  @NotNull
  @NotEmpty
  private String unit;
  
  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
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
  
  public UUID getLastModifier() {
    return lastModifier;
  }
  
  public void setLastModifier(UUID lastModifier) {
    this.lastModifier = lastModifier;
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
