package fi.metatavu.dcfb.server.keycloak;

/**
 * Class for keycloak constants
 * 
 * @author Heikki Kurhinen
 * @author Antti Leppä
 */
@SuppressWarnings ("squid:S2068")
public class KeycloakConsts {
  
  private KeycloakConsts() {
  }

  public static final String KEYCLOAK_STRIPE_ACCOUNT_ATTRIBUTE = "stripe-account-id";
  
  public static final String KEYCLOAK_ADMIN_REALM_SETTING = "keycloak-admin-realm";
  
  public static final String KEYCLOAK_ADMIN_CLIENT_ID_SETTING = "keycloak-admin-client-id";
  
  public static final String KEYCLOAK_ADMIN_CLIENT_SECRET_SETTING = "keycloak-admin-client-secret";
  
  public static final String KEYCLOAK_ADMIN_SERVER_URL_SETTING = "keycloak-admin-server-url";
  
  public static final String KEYCLOAK_ADMIN_USERNAME_SETTING = "keycloak-admin-username";
  
  public static final String KEYCLOAK_ADMIN_PASSWORD_SETTING = "keycloak-admin-password";
}



