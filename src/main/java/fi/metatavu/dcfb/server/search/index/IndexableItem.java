package fi.metatavu.dcfb.server.search.index;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import fi.metatavu.dcfb.server.search.annotations.Field;

/**
 * Indexable implementation for items
 */
public class IndexableItem extends AbstractIndexable {

  public static final String TYPE = "item";
  public static final String CATEGORY_ID_FIELD = "categoryId";
  public static final String LOCATION_ID_FIELD = "locationId";
  public static final String CREATED_AT_FIELD = "createdAt";
  public static final String MODIFIED_AT_FIELD = "modifiedAt";
  
  @Field(analyzer = "finnish")
  private List<String> titleFi;

  @Field(analyzer = "swedish")
  private List<String> titleSv;

  @Field(analyzer = "english")
  private List<String> titleEn;

  @Field(analyzer = "finnish")
  private List<String> descriptionFi;

  @Field(analyzer = "swedish")
  private List<String> descriptionSv;

  @Field(analyzer = "english")
  private List<String> descriptionEn;

  @Field(index = "not_analyzed", store = true)
  private UUID categoryId;

  @Field(index = "not_analyzed", store = true)
  private UUID locationId;

  @Field(index = "not_analyzed", store = true)
  private String slug;

  @Field(index = "not_analyzed", store = true, type = "date")
  private OffsetDateTime createdAt;

  @Field(index = "not_analyzed", store = true, type = "date")
  private OffsetDateTime modifiedAt;

  @Field(index = "not_analyzed", store = true, type = "date")
  private OffsetDateTime expiresAt;

  public IndexableItem() {
    // Zero-argument constructor
  }
  
  @SuppressWarnings ("squid:S00107")
  public IndexableItem(UUID id, List<String> titleFi, List<String> titleSv, List<String> titleEn, 
      List<String> descriptionFi, List<String> descriptionSv, List<String> descriptionEn, 
      UUID categoryId, UUID locationId, String slug, OffsetDateTime createdAt, OffsetDateTime modifiedAt,
      OffsetDateTime expiresAt) {
    super(id);
    this.titleFi = titleFi;
    this.titleSv = titleSv;
    this.titleEn = titleEn;
    this.descriptionFi = descriptionFi;
    this.descriptionSv = descriptionSv;
    this.descriptionEn = descriptionEn;
    this.categoryId = categoryId;
    this.locationId = locationId;
    this.slug = slug;
    this.createdAt = createdAt;
    this.modifiedAt = modifiedAt;
    this.expiresAt = expiresAt;
  }

  @Override
  public String getType() {
    return TYPE;
  }

  public List<String> getTitleFi() {
    return titleFi;
  }

  public void setTitleFi(List<String> titleFi) {
    this.titleFi = titleFi;
  }

  public List<String> getTitleSv() {
    return titleSv;
  }

  public void setTitleSv(List<String> titleSv) {
    this.titleSv = titleSv;
  }

  public List<String> getTitleEn() {
    return titleEn;
  }

  public void setTitleEn(List<String> titleEn) {
    this.titleEn = titleEn;
  }

  public List<String> getDescriptionFi() {
    return descriptionFi;
  }

  public void setDescriptionFi(List<String> descriptionFi) {
    this.descriptionFi = descriptionFi;
  }

  public List<String> getDescriptionSv() {
    return descriptionSv;
  }

  public void setDescriptionSv(List<String> descriptionSv) {
    this.descriptionSv = descriptionSv;
  }

  public List<String> getDescriptionEn() {
    return descriptionEn;
  }

  public void setDescriptionEn(List<String> descriptionEn) {
    this.descriptionEn = descriptionEn;
  }

  public UUID getCategoryId() {
    return categoryId;
  }

  /**
   * @param categoryId the categoryId to set
   */
  public void setCategoryId(UUID categoryId) {
    this.categoryId = categoryId;
  }

  /**
   * @return the locationId
   */
  public UUID getLocationId() {
    return locationId;
  }

  /**
   * @param locationId the locationId to set
   */
  public void setLocationId(UUID locationId) {
    this.locationId = locationId;
  }

  public String getSlug() {
    return slug;
  }

  /**
   * @param slug the slug to set
   */
  public void setSlug(String slug) {
    this.slug = slug;
  }

  public OffsetDateTime getCreatedAt() {
    return createdAt;
  }

  /**
   * @param createdAt the createdAt to set
   */
  public void setCreatedAt(OffsetDateTime createdAt) {
    this.createdAt = createdAt;
  }

  public OffsetDateTime getModifiedAt() {
    return modifiedAt;
  }

  /**
   * @param modifiedAt the modifiedAt to set
   */
  public void setModifiedAt(OffsetDateTime modifiedAt) {
    this.modifiedAt = modifiedAt;
  }

  public OffsetDateTime getExpiresAt() {
    return expiresAt;
  }

  /**
   * @param expiresAt the expiresAt to set
   */
  public void setExpiresAt(OffsetDateTime expiresAt) {
    this.expiresAt = expiresAt;
  }

}
