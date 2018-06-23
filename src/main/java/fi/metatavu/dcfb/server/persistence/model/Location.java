package fi.metatavu.dcfb.server.persistence.model;

import java.math.BigDecimal;
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

import fi.metatavu.dcfb.server.search.handlers.LocationIndexHandler;

/**
 * JPA entity for storing locations
 * 
 * @author Antti Leppä
 */
@Entity
@EntityListeners(LocationIndexHandler.class)
@Cacheable(true)
@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL)
public class Location {

  @Id
  private UUID id;

  @Column(nullable = false, unique = true)
  @NotNull
  @NotEmpty
  private String slug;

  @ManyToOne (optional = false)
  private LocalizedEntry name;

  private BigDecimal latitude;

  private BigDecimal longitude;

  private String streetAddress;

  private String postalCode;

  private String postOffice;

  private String country;

  @ManyToOne (optional = false)
  private LocalizedEntry additionalInformations;

  @Column (nullable = false)
  private OffsetDateTime createdAt;

  @Column (nullable = false)
  private OffsetDateTime modifiedAt;

  @Column (nullable = false)
  private UUID lastModifier;

  /**
   * @return the id
   */
  public UUID getId() {
    return id;
  }

  /**
   * @param id the id to set
   */
  public void setId(UUID id) {
    this.id = id;
  }

  /**
   * @return the slug
   */
  public String getSlug() {
    return slug;
  }

  /**
   * @param slug the slug to set
   */
  public void setSlug(String slug) {
    this.slug = slug;
  }

  /**
   * @return the additionalInformations
   */
  public LocalizedEntry getAdditionalInformations() {
    return additionalInformations;
  }

  /**
   * @param additionalInformations the additionalInformations to set
   */
  public void setAdditionalInformations(LocalizedEntry additionalInformations) {
    this.additionalInformations = additionalInformations;
  }

  /**
   * @return the country
   */
  public String getCountry() {
    return country;
  }

  /**
   * @param country the country to set
   */
  public void setCountry(String country) {
    this.country = country;
  }

  /**
   * @return the latitude
   */
  public BigDecimal getLatitude() {
    return latitude;
  }

  /**
   * @param latitude the latitude to set
   */
  public void setLatitude(BigDecimal latitude) {
    this.latitude = latitude;
  }

  /**
   * @return the longitude
   */
  public BigDecimal getLongitude() {
    return longitude;
  }

  /**
   * @param longitude the longitude to set
   */
  public void setLongitude(BigDecimal longitude) {
    this.longitude = longitude;
  }

  /**
   * @return the name
   */
  public LocalizedEntry getName() {
    return name;
  }

  /**
   * @param name the name to set
   */
  public void setName(LocalizedEntry name) {
    this.name = name;
  }

  /**
   * @return the postalCode
   */
  public String getPostalCode() {
    return postalCode;
  }

  /**
   * @param postalCode the postalCode to set
   */
  public void setPostalCode(String postalCode) {
    this.postalCode = postalCode;
  }

  /**
   * @return the postOffice
   */
  public String getPostOffice() {
    return postOffice;
  }

  /**
   * @param postOffice the postOffice to set
   */
  public void setPostOffice(String postOffice) {
    this.postOffice = postOffice;
  }

  /**
   * @return the streetAddress
   */
  public String getStreetAddress() {
    return streetAddress;
  }

  /**
   * @param streetAddress the streetAddress to set
   */
  public void setStreetAddress(String streetAddress) {
    this.streetAddress = streetAddress;
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
