package fi.metatavu.dcfb.server.rest;

import java.security.Principal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;

import com.github.slugify.Slugify;

import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.LocaleUtils;
import org.apache.commons.lang3.StringUtils;
import org.jboss.resteasy.spi.ResteasyProviderFactory;
import org.keycloak.KeycloakPrincipal;
import org.keycloak.KeycloakSecurityContext;
import org.keycloak.authorization.client.AuthzClient;
import org.keycloak.authorization.client.ClientAuthorizationContext;
import org.keycloak.representations.AccessToken;
import org.keycloak.representations.AccessToken.Access;
import org.slf4j.Logger;

import fi.metatavu.dcfb.server.localization.LocalizedValueController;
import fi.metatavu.dcfb.server.persistence.model.LocalizedEntry;
import fi.metatavu.dcfb.server.persistence.model.LocalizedType;
import fi.metatavu.dcfb.server.rest.model.BadRequest;
import fi.metatavu.dcfb.server.rest.model.Forbidden;
import fi.metatavu.dcfb.server.rest.model.InternalServerError;
import fi.metatavu.dcfb.server.rest.model.LocalizedValue;
import fi.metatavu.dcfb.server.rest.model.NotFound;
import fi.metatavu.dcfb.server.rest.model.NotImplemented;

/**
 * Abstract base class for all API services
 * 
 * @author Antti Leppä
 */
public abstract class AbstractApi {

  protected static final String NOT_FOUND_MESSAGE = "Not found";
  protected static final String UNAUTHORIZED = "Unauthorized";

  private static final String REALM_ADMIN = "admin";
  private static final String REALM_USER = "user";

  @Inject
  private Logger logger;
  
  @Inject
  private LocalizedValueController localizedValueController;  
  
  /**
   * Creates localized entry from values
   * 
   * @param localizedValues localized values
   * @return created entry
   */
  protected LocalizedEntry createLocalizedEntry(List<LocalizedValue> localizedValues) {
    return localizedValueController.setEntryValues(localizedValueController.createEntry(), getValues(localizedValues));
  }
  
  /**
   * Updates localized entry with given values
   * 
   * @param entry entry to be updated
   * @param localizedValues localized values
   * @return updated entry
   */
  protected LocalizedEntry updateLocalizedEntry(LocalizedEntry entry, List<LocalizedValue> localizedValues) {
    return localizedValueController.setEntryValues(entry, getValues(localizedValues));
  }
  
  /**
   * Validates localized list
   * 
   * @param localizedValues localized values
   * @return whether the values in list are vaild or nor
   */
  protected boolean isValidLocalizedList(List<LocalizedValue> localizedValues) {
    if (localizedValues == null) {
      return true;
    }

    try {
      for (int i = 0; i < localizedValues.size(); i++) {
        LocalizedValue localizedValue = localizedValues.get(i);
        if (LocaleUtils.toLocale(localizedValue.getLanguage()) == null) {
          return false;
        }
      }
    } catch (IllegalArgumentException e) {
      logger.warn("Error parsing localized value list", e);
      return false;
    }
    
    return true;
  }

  /**
   * Slugifys localized string
   * 
   * @param localizedValues localized string
   * @return slug
   */
  protected String slugifyLocalized(List<LocalizedValue> localizedValues) {
    return slugify(getFirstLocalized(localizedValues));
  }

  /**
   * Slugifys a text
   * 
   * @param text text
   * @return slug
   */
  protected String slugify(String text) {
    if (StringUtils.isEmpty(text)) {
      return UUID.randomUUID().toString();
    }
    
    Slugify slugify = new Slugify();
    return slugify.slugify(text);
  }

  /**
   * Returns list parameter as <E> translated by given translate function.
   * 
   * @param parameter list parameter as string list
   * @param translate translate function
   * @return list of <E>
   */
  @SuppressWarnings ("squid:S1168")
  protected <E> List<E> getListParameter(List<String> parameter, Function<String, E> translate) {
    if (parameter == null) {
      return null;
    }

    List<String> merged = new ArrayList<>();
    
    parameter.stream()
      .filter(StringUtils::isNoneEmpty)
      .forEach(filter -> merged.addAll(Arrays.asList(StringUtils.split(filter, ','))));

    return merged.stream()
      .map(translate::apply)
      .collect(Collectors.toList());
  }

  /**
   * Returns list parameter as <E> translated by given translate function.
   * 
   * @param parameter list parameter as string
   * @param translate translate function
   * @return list of <E>
   */
  @SuppressWarnings ("squid:S1168")
  protected <E> List<E> getListParameter(String parameter, Function<String, E> translate) {
    if (parameter == null) {
      return null;
    }

    return getListParameter(Arrays.asList(StringUtils.split(parameter, ',')), translate);
  }

  /**
   * Parses CSV enum parameter from string list into enum list
   * 
   * @param enumType target enum class
   * @param parameters string values
   * @return list of enums
   * @throws IllegalArgumentException if parameters contain invalid values
   */
  @SuppressWarnings ("squid:S1168")
  protected <T extends Enum<T>> List<T> getEnumListParameter(Class<T> enumType, List<String> parameter) {
    return getListParameter(parameter, name -> Enum.valueOf(enumType, name));
  }

  /**
   * Return current HttpServletRequest
   * 
   * @return current http servlet request
   */
  protected HttpServletRequest getHttpServletRequest() {
    return ResteasyProviderFactory.getContextData(HttpServletRequest.class);
  }
  
  /**
   * Returns logged user id
   * 
   * @return logged user id
   */
  protected UUID getLoggerUserId() {
    HttpServletRequest httpServletRequest = getHttpServletRequest();
    String remoteUser = httpServletRequest.getRemoteUser();
    if (remoteUser == null) {
      return null;
    }
    
    return UUID.fromString(remoteUser);
  }
  
  /**
   * Constructs ok response
   * 
   * @param entity payload
   * @return response
   */
  protected Response createOk(Object entity) {
    return Response
      .status(Response.Status.OK)
      .entity(entity)
      .build();
  }
  
  /**
   * Constructs ok response
   * 
   * @param entity payload
   * @param totalHits total hits
   * @return response
   */
  protected Response createOk(Object entity, Long totalHits) {
    return Response
      .status(Response.Status.OK)
      .entity(entity)
      .header("Total-Results", totalHits)
      .build();
  }
  
  /**
   * Constructs no content response
   * 
   * @return response
   */
  protected Response createNoContent() {
    return Response
      .status(Response.Status.NO_CONTENT)
      .build();
  }

  /**
   * Constructs bad request response
   * 
   * @param message message
   * @return response
   */
  protected Response createBadRequest(String message) {
    BadRequest entity = new BadRequest();
    entity.setCode(Response.Status.BAD_REQUEST.getStatusCode());
    entity.setMessage(message);
    return Response
      .status(Response.Status.BAD_REQUEST)
      .entity(entity)
      .build();
  }

  /**
   * Constructs not found response
   * 
   * @param message message
   * @return response
   */
  protected Response createNotFound(String message) {
    NotFound entity = new NotFound();
    entity.setCode(Response.Status.NOT_FOUND.getStatusCode());
    entity.setMessage(message);
    return Response
      .status(Response.Status.NOT_FOUND)
      .entity(entity)
      .build();
  }

  /**
   * Constructs not implemented response
   * 
   * @param message message
   * @return response
   */
  protected Response createNotImplemented(String message) {
    NotImplemented entity = new NotImplemented();
    entity.setCode(Response.Status.NOT_IMPLEMENTED.getStatusCode());
    entity.setMessage(message);
    return Response
      .status(Response.Status.NOT_IMPLEMENTED)
      .entity(entity)
      .build();
  }

  /**
   * Constructs internal server error response
   * 
   * @param message message
   * @return response
   */
  protected Response createInternalServerError(String message) {
    InternalServerError entity = new InternalServerError();
    entity.setCode(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
    entity.setMessage(message);
    return Response
      .status(Response.Status.INTERNAL_SERVER_ERROR)
      .entity(entity)
      .build();
  }

  /**
   * Constructs forbidden response
   * 
   * @param message message
   * @return response
   */
  protected Response createForbidden(String message) {
    Forbidden entity = new Forbidden();
    entity.setCode(Response.Status.FORBIDDEN.getStatusCode());
    entity.setMessage(message);
    return Response
      .status(Response.Status.FORBIDDEN)
      .entity(entity)
      .build();
  }

  /**
   * Returns whether logged user is in realm admin role
   * 
   * @return whether logged user is in realm admin role
   */
  protected boolean isRealmAdmin() {
    return hasRealmRole(REALM_ADMIN);
  }

  /**
   * Returns whether logged user is in realm user role
   * 
   * @return whether logged user is in realm user role
   */
  protected boolean isRealmUser() {
    return hasRealmRole(REALM_USER);
  }

  /**
   * Returns whether logged user has at least one of specified realm roles
   * 
   * @param role role
   * @return whether logged user has specified realm role or not
   */
  protected boolean hasRealmRole(String... roles) {
    KeycloakSecurityContext keycloakSecurityContext = getKeycloakSecurityContext();
    if (keycloakSecurityContext == null) {
      return false;
    }

    AccessToken token = keycloakSecurityContext.getToken();
    if (token == null) {
      return false;
    }

    Access realmAccess = token.getRealmAccess();
    if (realmAccess == null) {
      return false;
    }

    for (int i = 0; i < roles.length; i++) {
      if (realmAccess.isUserInRole(roles[i])) {
        return true;
      }
    }
    
    return false;
  }

  /**
   * Return keycloak authorization client
   */
  protected AuthzClient getAuthzClient() {
    ClientAuthorizationContext clientAuthorizationContext = getAuthorizationContext();
    if (clientAuthorizationContext == null) {
      return null;
    }

    return clientAuthorizationContext.getClient();
  }



  /**
   * Parses date time from string
   * 
   * @param timeString
   * @return
   */
  protected OffsetDateTime parseTime(String timeString) {
    if (StringUtils.isEmpty(timeString)) {
      return null;
    }
    
    return OffsetDateTime.parse(timeString);
  }

  /**
   * Converts list of localized values into a map
   * 
   * @param localizedValues list of localized values
   * @return map
   */
  private Map<Locale, Map<LocalizedType, String>> getValues(List<LocalizedValue> localizedValues) {
    Map<Locale, Map<LocalizedType, String>> result = new HashMap<>();
    
    if (localizedValues != null) {
      localizedValues.stream().forEach(localizedValue -> {
        Locale locale = LocaleUtils.toLocale(localizedValue.getLanguage());
        if (locale == null) {
          logger.error("Invalid locale {}, dropped", localizedValue.getLanguage());
          return;
        }
        
        LocalizedType type = EnumUtils.getEnum(LocalizedType.class, localizedValue.getType());
        if (type == null) {
          logger.error("Invalid type {}, dropped", localizedValue.getType());
          return;
        }
        
        if (!result.containsKey(locale)) {
          result.put(locale, new HashMap<>());
        }
        
        Map<LocalizedType, String> localeMap = result.get(locale);
        localeMap.put(type, localizedValue.getValue());
      });
    }

    return result;
  }
  
  /**
   * Parses CDT list into set of UUIDs.
   * 
   * @param cdt CDT list
   * @return set of UUID values
   */
  protected Set<UUID> parseUuidCDT(String cdt) {
    if (cdt == null) {
      return Collections.emptySet();
    }
    
    return Arrays.stream(StringUtils.split(cdt, ','))
      .map(UUID::fromString)
      .filter(Objects::nonNull)
      .collect(Collectors.toSet());
  }

  /**
   * Returns first non empty localized string from the list of localized values
   * 
   * @param localizedValues localized values
   * @return first string
   */
  private String getFirstLocalized(List<LocalizedValue> localizedValues) {
    if (localizedValues != null) {
      List<String> values = localizedValues.stream()
        .map(LocalizedValue::getValue)
        .filter(StringUtils::isNotEmpty)
        .collect(Collectors.toList());
      
      if (!values.isEmpty()) {
        return values.get(0);
      }
    }
    
    return null;
  }

  /**
   * Returns keycloak security context from request or null if not available
   */
  private KeycloakSecurityContext getKeycloakSecurityContext() {
    HttpServletRequest request = getHttpServletRequest();
    Principal userPrincipal = request.getUserPrincipal();
    KeycloakPrincipal<?> kcPrincipal = (KeycloakPrincipal<?>) userPrincipal;
    if (kcPrincipal == null) {
      return null;
    }
    
    return kcPrincipal.getKeycloakSecurityContext();
  }

  /**
   * Return keycloak authorization client context or null if not available 
   */
  private ClientAuthorizationContext getAuthorizationContext() {
    KeycloakSecurityContext keycloakSecurityContext = getKeycloakSecurityContext();
    if (keycloakSecurityContext == null) {
      return null;
    }

    return (ClientAuthorizationContext) keycloakSecurityContext.getAuthorizationContext();
  }

}