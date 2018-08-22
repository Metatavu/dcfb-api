package fi.metatavu.dcfb.server.stripe;

/**
 * Class for stripe constants
 * 
 * @author Heikki Kurhinen
 */
public class StripeConsts {
  
  private StripeConsts() {
    // Empty constructor
  }

  public static final String STRIPE_API_KEY_SETTING = "stripe.api-key";
  
  public static final String STRIPE_SIGNING_KEY_SETTING = "stripe.webhook-signing-secret";

  public static final String STRIPE_LIVE_MODE_SETTING = "stripe.live-mode";

  public static final String STRIPE_WEBHOOK_TYPE = "stripe";

  public static final String STRIPE_SIGNATURE_HEADER = "Stripe-Signature";
  
  public static final String STRIPE_CHARGE_ITEM_RESERVATION_ID = "item-reservation-id";

}
