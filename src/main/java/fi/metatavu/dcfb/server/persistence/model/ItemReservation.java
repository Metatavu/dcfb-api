package fi.metatavu.dcfb.server.persistence.model;

import java.time.OffsetDateTime;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;

import org.hibernate.annotations.Type;

/**
 * JPA entity for storing items
 * 
 * @author Antti Leppä
 */
@Entity
public class ItemReservation {

  @Id
  @Type(type="org.hibernate.type.PostgresUUIDType")
  private UUID id;

  @ManyToOne (optional = false)
  private Item item;
  
  @Column (nullable = false)
  private OffsetDateTime createdAt;

  @Column (nullable = false)
  private OffsetDateTime modifiedAt;

  @Column (nullable = false)
  private OffsetDateTime expiresAt;
  
  @Column (nullable = false)
  private Long amount;

  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }
  
  public Item getItem() {
    return item;
  }
  
  public void setItem(Item item) {
    this.item = item;
  }
  
  public OffsetDateTime getCreatedAt() {
    return createdAt;
  }
  
  public void setCreatedAt(OffsetDateTime createdAt) {
    this.createdAt = createdAt;
  }
  
  public OffsetDateTime getExpiresAt() {
    return expiresAt;
  }
  
  public void setExpiresAt(OffsetDateTime expiresAt) {
    this.expiresAt = expiresAt;
  }
  
  public OffsetDateTime getModifiedAt() {
    return modifiedAt;
  }
  
  public void setModifiedAt(OffsetDateTime modifiedAt) {
    this.modifiedAt = modifiedAt;
  }

  public Long getAmount() {
    return amount;
  }
  
  public void setAmount(Long amount) {
    this.amount = amount;
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
