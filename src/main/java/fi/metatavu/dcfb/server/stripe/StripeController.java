package fi.metatavu.dcfb.server.stripe;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Product;

import org.slf4j.Logger;

import fi.metatavu.dcfb.server.localization.LocalizedValueController;
import fi.metatavu.dcfb.server.persistence.model.Item;
import fi.metatavu.dcfb.server.settings.SystemSettingController;

/**
 * Controller for Stripe functions
 * 
 * @author Heikki Kurhinen
 */
@ApplicationScoped
public class StripeController {
  
  @Inject
  private LocalizedValueController localizedValueController;  

  @Inject
  private SystemSettingController systemSettingController;

  @Inject
  private Logger logger;

  @PostConstruct
  @SuppressWarnings ("squid:S2696")
  public void init() {
    Stripe.apiKey = systemSettingController.getSettingValue(StripeConsts.STRIPE_API_KEY_SETTING);
  }
  
  /**
   * Creates item product into the Stripe
   * 
   * @param item item
   * @return created product
   */
  public Product createItemProduct(Item item) {
    try {
      String title = localizedValueController.getAnyValue(item.getTitle(), "single");
      
      Map<String, Object> productParams = new HashMap<>();
      Map<String, Object> productMetadataParams = new HashMap<>();
   
      productMetadataParams.put("dcfbItemId", item.getId().toString());
      productParams.put("name", title);
      productParams.put("type", "good");
      productParams.put("metadata", productMetadataParams);
      
      return Product.create(productParams);
    } catch (StripeException e) {
      logger.error("Error creating stripe product", e);
    }
    
    return null;
  }

  /**
   * Updates item product into the Stripe
   * 
   * @param item item
   * @return updated product
   */
  public Product updateItemProduct(Item item) {
    try {
      String title = localizedValueController.getAnyValue(item.getTitle(), "single");
      
      Map<String, Object> productParams = new HashMap<>();
      productParams.put("name", title);
      
      Product product = Product.retrieve(item.getStripeProductId());
      if (product != null) {
        product.update(productParams);
      }
      
      return product;
    } catch (StripeException e) {
      logger.error("Error creating stripe product", e);
    }
    
    return null;
  }

}