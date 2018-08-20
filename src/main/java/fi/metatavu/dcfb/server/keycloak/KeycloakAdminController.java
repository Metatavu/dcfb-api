package fi.metatavu.dcfb.server.keycloak;

import java.util.UUID;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.representations.idm.UserRepresentation;

import fi.metatavu.dcfb.server.settings.SystemSettingController;

/**
 * Controller for keycloak administration tasks
 * 
 * @author Heikki Kurhinen
 * @author Antti leppä
 */
@ApplicationScoped
public class KeycloakAdminController {

  @Inject
  private SystemSettingController systemSettingController;

  /**
   * Checks if user has some attribute present
   * 
   * @param userId Keycloak user id
   * @param attributeName name of the attribute
   * @return true if user has attribute, false otherwise
   */
  public boolean userHasAttribute(UUID userId, String attributeName) {
    UserRepresentation userRepresentation = getUser(userId);
    if (userRepresentation == null) {
      return false;
    }
    
    return userRepresentation.getAttributes().containsKey(attributeName);
   }
  
  /**
   * Gets user from keycloak server with user id
   * 
   * @param userId user id
   * @return user representation or null if not found
   */
  public UserRepresentation getUser(UUID userId) {
    Keycloak client = getClient();
    String realm = systemSettingController.getSettingValue(KeycloakConsts.KEYCLOAK_ADMIN_REALM_SETTING);
    RealmResource realmResource = client.realm(realm);
    UserResource userRessource = realmResource.users().get(userId.toString());
    if (userRessource == null) {
      return null;
    }
    
    return userRessource.toRepresentation();
  }

  /**
   * Constructs keycloak admin client
   */
  private Keycloak getClient() {
    return KeycloakBuilder.builder()
      .serverUrl(systemSettingController.getSettingValue(KeycloakConsts.KEYCLOAK_ADMIN_SERVER_URL_SETTING))
      .realm(systemSettingController.getSettingValue(KeycloakConsts.KEYCLOAK_ADMIN_REALM_SETTING))
      .grantType(OAuth2Constants.PASSWORD)
      .clientId(systemSettingController.getSettingValue(KeycloakConsts.KEYCLOAK_ADMIN_CLIENT_ID_SETTING))
      .clientSecret(systemSettingController.getSettingValue(KeycloakConsts.KEYCLOAK_ADMIN_USERNAME_SETTING))
      .password(systemSettingController.getSettingValue(KeycloakConsts.KEYCLOAK_ADMIN_PASSWORD_SETTING))
      .build();
  }

}
