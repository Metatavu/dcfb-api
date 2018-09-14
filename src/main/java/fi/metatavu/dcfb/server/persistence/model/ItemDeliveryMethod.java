package fi.metatavu.dcfb.server.persistence.model;

import java.util.Currency;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.Type;
import org.hibernate.validator.constraints.NotEmpty;

/**
 * JPA entity for item delivery
 * 
 * @author Antti Leppä
 */
@Entity
public class ItemDeliveryMethod {

  @Id
  @Type(type="org.hibernate.type.PostgresUUIDType")
  private UUID id;

  @ManyToOne
  private Item item;
  
  @ManyToOne (optional = false)
  private LocalizedEntry title;
  
  @Column(nullable = false)
  @NotNull
  @NotEmpty
  private String price;
  
  @Column (nullable = false)
  private Currency currency;

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
   * @return the item
   */
  public Item getItem() {
    return item;
  }

  /**
   * @param item the item to set
   */
  public void setItem(Item item) {
    this.item = item;
  }

  public Currency getCurrency() {
    return currency;
  }
  
  public void setCurrency(Currency currency) {
    this.currency = currency;
  }
  
  public String getPrice() {
    return price;
  }
  
  public void setPrice(String price) {
    this.price = price;
  }
  
  public LocalizedEntry getTitle() {
    return title;
  }
  
  public void setTitle(LocalizedEntry title) {
    this.title = title;
  }

}
