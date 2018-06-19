package fi.metatavu.dcfb.server.search.index;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import fi.metatavu.dcfb.server.search.annotations.Field;

/**
 * Indexable implementation for categories
 */
public class IndexableCategory extends AbstractIndexable {

  public static final String TYPE = "category";
  public static final String PARENT_ID_FIELD = "parentId";
  public static final String CREATED_AT_FIELD = "createdAt";
  public static final String MODIFIED_AT_FIELD = "modifiedAt";
  
  @Field(analyzer = "finnish")
  private List<String> titleFi;

  @Field(analyzer = "swedish")
  private List<String> titleSv;

  @Field(analyzer = "english")
  private List<String> titleEn;

  @Field(index = "not_analyzed", store = true)
  private UUID parentId;

  @Field(index = "not_analyzed", store = true)
  private String slug;

  @Field(index = "not_analyzed", store = true, type = "date")
  private OffsetDateTime createdAt;

  @Field(index = "not_analyzed", store = true, type = "date")
  private OffsetDateTime modifiedAt;

  public IndexableCategory() {
    // Zero-argument constructor
  }
  
  @SuppressWarnings ("squid:S00107")
  public IndexableCategory(UUID id, UUID parentId, List<String> titleFi, List<String> titleSv, List<String> titleEn, String slug, OffsetDateTime createdAt, OffsetDateTime modifiedAt) {
    super(id);
    this.titleFi = titleFi;
    this.titleSv = titleSv;
    this.titleEn = titleEn;
    this.parentId = parentId;
    this.slug = slug;
    this.createdAt = createdAt;
    this.modifiedAt = modifiedAt;
  }

  @Override
  public String getType() {
    return TYPE;
  }

  /**
   * @return the titleEn
   */
  public List<String> getTitleEn() {
    return titleEn;
  }

  /**
   * @param titleEn the titleEn to set
   */
  public void setTitleEn(List<String> titleEn) {
    this.titleEn = titleEn;
  }

  /**
   * @return the titleFi
   */
  public List<String> getTitleFi() {
    return titleFi;
  }

  /**
   * @param titleFi the titleFi to set
   */
  public void setTitleFi(List<String> titleFi) {
    this.titleFi = titleFi;
  }

  /**
   * @return the titleSv
   */
  public List<String> getTitleSv() {
    return titleSv;
  }

  /**
   * @param titleSv the titleSv to set
   */
  public void setTitleSv(List<String> titleSv) {
    this.titleSv = titleSv;
  }

  /**
   * @return the parentId
   */
  public UUID getParentId() {
    return parentId;
  }

  /**
   * @param parentId the parentId to set
   */
  public void setParentId(UUID parentId) {
    this.parentId = parentId;
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
   * @return the createdAt
   */
  public OffsetDateTime getCreatedAt() {
    return createdAt;
  }

  /**
   * @param createdAt the createdAt to set
   */
  public void setCreatedAt(OffsetDateTime createdAt) {
    this.createdAt = createdAt;
  }

  /**
   * @return the modifiedAt
   */
  public OffsetDateTime getModifiedAt() {
    return modifiedAt;
  }

  /**
   * @param modifiedAt the modifiedAt to set
   */
  public void setModifiedAt(OffsetDateTime modifiedAt) {
    this.modifiedAt = modifiedAt;
  }

}
