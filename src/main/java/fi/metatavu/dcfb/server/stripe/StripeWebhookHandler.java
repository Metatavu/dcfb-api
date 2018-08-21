package fi.metatavu.dcfb.server.stripe;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Charge;
import com.stripe.model.Event;
import com.stripe.model.Order;
import com.stripe.model.OrderItem;
import com.stripe.model.StripeObject;
import com.stripe.net.ApiResource;
import com.stripe.net.Webhook;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;

import fi.metatavu.dcfb.server.items.ItemController;
import fi.metatavu.dcfb.server.persistence.model.Item;
import fi.metatavu.dcfb.server.settings.SystemSettingController;
import fi.metatavu.dcfb.server.webhooks.WebhookException;
import fi.metatavu.dcfb.server.webhooks.WebhookHandler;

/**
 * Class that handles webhooks from stripe
 * 
 * @author Heikki Kurhinen
 */
@RequestScoped
public class StripeWebhookHandler implements WebhookHandler {
  
  @Inject
  private ItemController itemController;

  @Inject
  private SystemSettingController systemSettingController;

  @Inject
  private Logger logger;

  @Override
  public String getType() {
    return StripeConsts.STRIPE_WEBHOOK_TYPE;
  }

  @Override
  public void handle(HttpServletRequest request) throws WebhookException {
    String payload = null;
    String signatureHeader = request.getHeader(StripeConsts.STRIPE_SIGNATURE_HEADER);
    try {
      payload = request.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
    } catch (IOException e) {
      throw new WebhookException(e);
    }

    Event event = null;
    try {
      event = Webhook.constructEvent(payload, signatureHeader, systemSettingController.getSettingValue(StripeConsts.STRIPE_SIGNING_KEY_SETTING));
    } catch (SignatureVerificationException e) {
      throw new WebhookException(e);
    }

    if (inLiveMode() != event.getLivemode()) {
      throw new WebhookException("Operation mode mismatch");
    }

    switch(event.getType()) {
      case "order.payment_succeeded":
        handleOrderPaymentSucceeded(event);
        return;
      case "charge.succeeded":
        handleChargeSucceeded(event);
        return;
      default:
        logger.info("received webhook from stripe with type {}", event.getType());
    }
  }

  private void handleChargeSucceeded(Event event) throws WebhookException {
    StripeObject data = event.getData().getObject();
    Charge charge = ApiResource.GSON.fromJson(data.toJson(), Charge.class);
    Map<String, String> metadata = charge.getMetadata();
    
    String itemIdStr = metadata.get(StripeConsts.STRIPE_CHARGE_ITEM_ID);
    Long itemQuantity = NumberUtils.createLong(metadata.get(StripeConsts.STRIPE_CHARGE_ITEM_QUANTITY));
    
    if (StringUtils.isNotBlank(itemIdStr) && (itemQuantity != null)) {
      UUID itemId = null;
      try {
        itemId = UUID.fromString(itemIdStr);
      } catch (IllegalArgumentException e) {
        throw new WebhookException("Received charge with invalid itemId", e);
      }
      
      Item item = itemController.findItem(itemId);
      if (item == null) {
        throw new WebhookException("Received charge with non existing item");
      }
      
      itemController.updateItemSoldAmount(item, item.getSoldAmount() + itemQuantity, item.getLastModifier());
    } else {
      throw new WebhookException("Received charge without item details");
    }
  }

  /**
   * Handles webhook with type charge.succeeded
   * 
   * @param event webhook event
   */
  private void handleOrderPaymentSucceeded(Event event) {
    StripeObject data = event.getData().getObject();
    Order order = ApiResource.GSON.fromJson(data.toJson(), Order.class);
    List<OrderItem> orderItems = order.getItems();
    if (orderItems == null) {
      logger.warn("Received order.payment_succeeded webhook without items");
      return;
    }
    
    orderItems.stream().forEach((item) -> {
      // TODO: Handle
    });
  }

  /**
   * Returns if stripe has been configured to run in live mode or not
   */
  private boolean inLiveMode() {
    return systemSettingController.getSettingValueBoolean(StripeConsts.STRIPE_LIVE_MODE_SETTING);
  }

}
