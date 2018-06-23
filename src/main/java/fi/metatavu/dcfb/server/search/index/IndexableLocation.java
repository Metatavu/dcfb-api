package fi.metatavu.dcfb.server.search.index;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import fi.metatavu.dcfb.server.search.annotations.Field;

/**
 * Indexable implementation for locations
 */
public class IndexableLocation extends AbstractIndexable {

  public static final String TYPE = "location";
  public static final String CREATED_AT_FIELD = "createdAt";
  public static final String MODIFIED_AT_FIELD = "modifiedAt";
  public static final String SLUG_FIELD = "slug";
  
  @Field(analyzer = "finnish")
  private List<String> nameFi;

  @Field(analyzer = "swedish")
  private List<String> nameSv;

  @Field(analyzer = "english")
  private List<String> nameEn;
  
  @Field(analyzer = "finnish")
  private List<String> additionalInformationsFi;

  @Field(analyzer = "swedish")
  private List<String> additionalInformationsSv;

  @Field(analyzer = "english")
  private List<String> additionalInformationsEn;

  @Field(index = "not_analyzed", store = true)
  private String slug;

  @Field(index = "not_analyzed", store = true, type = "date")
  private OffsetDateTime createdAt;

  @Field(index = "not_analyzed", store = true, type = "date")
  private OffsetDateTime modifiedAt;

  public IndexableLocation() {
    // Zero-argument constructor
  }

  /**
   * Constructor
   * 
   * @param id id
   * @param nameFi name in finnish
   * @param nameSv name in swedish
   * @param nameEn name in english
   * @param slug slug
   * @param createdAt created at
   * @param modifiedAt modified at
   */
  @SuppressWarnings ("squid:S00107")
  public IndexableLocation(UUID id, List<String> nameFi, List<String> nameSv, List<String> nameEn, 
    List<String> additionalInformationsFi, List<String> additionalInformationsSv, List<String> additionalInformationsEn,
    String slug, OffsetDateTime createdAt, OffsetDateTime modifiedAt) {
    super(id);
    this.nameFi = nameFi;
    this.nameSv = nameSv;
    this.nameEn = nameEn;
    this.additionalInformationsFi = additionalInformationsFi;
    this.additionalInformationsSv = additionalInformationsSv;
    this.additionalInformationsEn = additionalInformationsEn;
    this.slug = slug;
    this.createdAt = createdAt;
    this.modifiedAt = modifiedAt;
  }

  @Override
  public String getType() {
    return TYPE;
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

  /**
   * @return the nameEn
   */
  public List<String> getNameEn() {
    return nameEn;
  }

  /**
   * @param nameEn the nameEn to set
   */
  public void setNameEn(List<String> nameEn) {
    this.nameEn = nameEn;
  }

  /**
   * @return the nameFi
   */
  public List<String> getNameFi() {
    return nameFi;
  }

  /**
   * @param nameFi the nameFi to set
   */
  public void setNameFi(List<String> nameFi) {
    this.nameFi = nameFi;
  }

  /**
   * @return the nameSv
   */
  public List<String> getNameSv() {
    return nameSv;
  }

  /**
   * @param nameSv the nameSv to set
   */
  public void setNameSv(List<String> nameSv) {
    this.nameSv = nameSv;
  }

  /**
   * @return the additionalInformationsEn
   */
  public List<String> getAdditionalInformationsEn() {
    return additionalInformationsEn;
  }

  /**
   * @param additionalInformationsEn the additionalInformationsEn to set
   */
  public void setAdditionalInformationsEn(List<String> additionalInformationsEn) {
    this.additionalInformationsEn = additionalInformationsEn;
  }

  /**
   * @return the additionalInformationsFi
   */
  public List<String> getAdditionalInformationsFi() {
    return additionalInformationsFi;
  }

  /**
   * @param additionalInformationsFi the additionalInformationsFi to set
   */
  public void setAdditionalInformationsFi(List<String> additionalInformationsFi) {
    this.additionalInformationsFi = additionalInformationsFi;
  }

  /**
   * @return the additionalInformationsSv
   */
  public List<String> getAdditionalInformationsSv() {
    return additionalInformationsSv;
  }

  /**
   * @param additionalInformationsSv the additionalInformationsSv to set
   */
  public void setAdditionalInformationsSv(List<String> additionalInformationsSv) {
    this.additionalInformationsSv = additionalInformationsSv;
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
  
}
