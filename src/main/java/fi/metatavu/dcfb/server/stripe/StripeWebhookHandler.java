package fi.metatavu.dcfb.server.stripe;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;

import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Charge;
import com.stripe.model.Event;
import com.stripe.model.StripeObject;
import com.stripe.net.ApiResource;
import com.stripe.net.Webhook;

import fi.metatavu.dcfb.server.items.ItemController;
import fi.metatavu.dcfb.server.persistence.model.Item;
import fi.metatavu.dcfb.server.persistence.model.ItemReservation;
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
    
    String itemReservationIdStr = metadata.get(StripeConsts.STRIPE_CHARGE_ITEM_RESERVATION_ID);
    
    if (StringUtils.isNotBlank(itemReservationIdStr)) {
      UUID itemReservationId = null;
      try {
        itemReservationId = UUID.fromString(itemReservationIdStr);
      } catch (IllegalArgumentException e) {
        throw new WebhookException("Received charge with invalid itemId", e);
      }
      
      ItemReservation itemReservation = itemController.findItemReservation(itemReservationId);
      if (itemReservation == null) {
        throw new WebhookException("Received charge with non existing item reservation");
      }
      
      Item item = itemReservation.getItem();
      if (item == null) {
        throw new WebhookException("Received charge with non existing item");
      }
      
      itemController.updateItemSoldAmount(item, item.getSoldAmount() + itemReservation.getAmount(), item.getLastModifier());
      itemController.deleteItemReservation(itemReservation);
    } else {
      throw new WebhookException("Received charge without item details");
    }
  }

  /**
   * Returns if stripe has been configured to run in live mode or not
   */
  private boolean inLiveMode() {
    return systemSettingController.getSettingValueBoolean(StripeConsts.STRIPE_LIVE_MODE_SETTING);
  }

}
