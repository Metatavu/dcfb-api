package fi.metatavu.dcfb.server.persistence.model;

import java.util.UUID;

import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Type;

/**
 * JPA entity for storing item user ids
 * 
 * @author Heikki Kurhinen
 */
@Entity
@Cacheable(true)
@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL)
public class ItemUser {

  @Id
  @Type(type="org.hibernate.type.PostgresUUIDType")
  private UUID id;

  @Column(nullable = false)
  private UUID userId;

  @ManyToOne (optional = false)
  private Item item;

  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public void setUserId(UUID userId) {
    this.userId = userId;
  }

  public UUID getUserId() {
    return userId;
  }

  public Item getItem() {
    return item;
  }
  
  public void setItem(Item item) {
    this.item = item;
  }

}
