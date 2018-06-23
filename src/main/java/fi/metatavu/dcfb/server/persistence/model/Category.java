package fi.metatavu.dcfb.server.persistence.model;

import java.time.OffsetDateTime;
import java.util.UUID;

import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.validator.constraints.NotEmpty;

import fi.metatavu.dcfb.server.search.handlers.CategoryIndexHandler;

/**
 * JPA entity for storing items
 * 
 * @author Antti Leppä
 */
@Entity
@EntityListeners(CategoryIndexHandler.class)
@Cacheable(true)
@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL)
public class Category {

  @Id
  private UUID id;

  @ManyToOne
  private Category parent;

  @ManyToOne (optional = false)
  private LocalizedEntry title;

  @Column(nullable = false, unique = true)
  @NotNull
  @NotEmpty
  private String slug;

  @Column (nullable = false)
  private OffsetDateTime createdAt;

  @Column (nullable = false)
  private OffsetDateTime modifiedAt;

  @Column (nullable = false)
  private UUID lastModifier;

  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public Category getParent() {
    return parent;
  }

  public void setParent(Category parent) {
    this.parent = parent;
  }

  public LocalizedEntry getTitle() {
    return title;
  }

  public void setTitle(LocalizedEntry title) {
    this.title = title;
  }

  public String getSlug() {
    return slug;
  }

  public void setSlug(String slug) {
    this.slug = slug;
  }

  /**
   * @param createdAt the createdAt to set
   */
  public void setCreatedAt(OffsetDateTime createdAt) {
    this.createdAt = createdAt;
  }

  /**
   * @return the createdAt
   */
  public OffsetDateTime getCreatedAt() {
    return createdAt;
  }

  /**
   * @param lastModifier the lastModifier to set
   */
  public void setLastModifier(UUID lastModifier) {
    this.lastModifier = lastModifier;
  }

  /**
   * @return the lastModifier
   */
  public UUID getLastModifier() {
    return lastModifier;
  }

  /**
   * @param modifiedAt the modifiedAt to set
   */
  public void setModifiedAt(OffsetDateTime modifiedAt) {
    this.modifiedAt = modifiedAt;
  }

  /**
   * @return the modifiedAt
   */
  public OffsetDateTime getModifiedAt() {
    return modifiedAt;
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
