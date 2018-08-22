package fi.metatavu.dcfb.server;

import static io.restassured.RestAssured.given;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.math.NumberUtils;
import org.junit.After;
import org.junit.Before;

import fi.metatavu.dcfb.ApiClient;
import fi.metatavu.dcfb.client.CategoriesApi;
import fi.metatavu.dcfb.client.ItemsApi;
import fi.metatavu.dcfb.client.LocationsApi;


/**
 * Abstract base class for integration tests
 * 
 * @author Heikki Kurhinen
 * @author Antti Leppä
 */
@SuppressWarnings ("squid:S1192")
public abstract class AbstractIntegrationTest extends AbstractTest {
  
  protected static final String KEYCLOAK_REALM = "dcfb";
  protected static final String ADMIN_USERNAME = "dcfb-admin";
  protected static final String ADMIN_PASSWORD = "test";
  protected static final String USER_1_USERNAME = "user1";
  protected static final String USER_1_PASSWORD = "test";
  protected static final String REALM_1 = "test-1";
  protected static final String BASE_URL = "/v1";
  protected static final String AUTH_SERVER_URL = "http://localhost:8280";
  protected static final String KEYCLOAK_CLIENT_ID = "ui";
  protected static final String KEYCLOAK_CLIENT_SECRET = "71926dcf-e676-4a3b-babc-a3900d92492e";
  protected static final UUID REALM1_USER_1_ID = UUID.fromString("c72e219c-71a0-4f5e-9b06-5dafe5394e27");

  @Before
  public void setupKeycloakSettings() {
    insertSystemSetting("keycloak-admin-realm", KEYCLOAK_REALM);
    insertSystemSetting("keycloak-admin-server-url" , String.format("%s/auth", AUTH_SERVER_URL));
    insertSystemSetting("keycloak-admin-client-secret", "0e0facfe-8922-48d3-b3d3-8cbc50bd2ada");
    insertSystemSetting("keycloak-admin-client-id", "api");
    insertSystemSetting("keycloak-admin-username", ADMIN_USERNAME);
    insertSystemSetting("keycloak-admin-password", ADMIN_PASSWORD);
  }
  
  @After
  public void teardownKeycloakSettings() {
    deleteSystemSettings("keycloak-admin-realm", "keycloak-admin-server-url", "keycloak-admin-client-secret", "keycloak-admin-client-id", "keycloak-admin-username", "keycloak-admin-password", "test");
  }
  
  /**
   * Returns API base path
   * 
   * @return API base path
   */
  protected String getBasePath() {
   return String.format("http://%s:%d", getHost(), getPort());
  }
  
  /**
   * Returns API host
   * 
   * @return API host
   */
  protected String getHost() {
    return System.getProperty("it.host");
  }
  
  /**
   * Returns API port
   * 
   * @return API port
   */
  protected Integer getPort() {
    return NumberUtils.createInteger(System.getProperty("it.port.http"));
  }

  /**
   * Returns WireMock port
   * 
   * @return WireMock port
   */
  protected int getWireMockPort() {
    return getPort() + 1;
  }
  
  /**
   * Returns WireMock base path
   * 
   * @return WireMock base path
   */
  protected String getWireMockBasePath() {
    return String.format("http://%s:%d", getHost(), getWireMockPort());
  }

  /**
   * Flushes JPA cache
   */
  protected void flushCache() {
    given()
      .baseUri(getBasePath())
      .get("/system/jpa/cache/flush")
      .then();
  }
  
  /**
   * Returns items API authenticated by the given access token
   * 
   * @param accessToken access token
   * @return items API authenticated by the given access token
   */
  protected ItemsApi getItemsApi(String accessToken) {
    ApiClient apiClient = getApiClient(accessToken);
    return apiClient.buildClient(ItemsApi.class);
  }
  
  /**
   * Returns categories API authenticated by the given access token
   * 
   * @param accessToken access token
   * @return categories API authenticated by the given access token
   */
  protected CategoriesApi getCategoriesApi(String accessToken) {
    ApiClient apiClient = getApiClient(accessToken);
    return apiClient.buildClient(CategoriesApi.class);
  }
  
  /**
   * Returns locations API authenticated by the given access token
   * 
   * @param accessToken access token
   * @return locations API authenticated by the given access token
   */
  protected LocationsApi getLocationsApi(String accessToken) {
    ApiClient apiClient = getApiClient(accessToken);
    return apiClient.buildClient(LocationsApi.class);
  }
  

  /**
   * Returns API client authenticated by the given access token
   * 
   * @param accessToken access token
   * @return API client authenticated by the given access token
   */
  private ApiClient getApiClient(String accessToken) {
    ApiClient apiClient = new ApiClient("bearer", String.format("Bearer %s", accessToken));
    String basePath = String.format("http://%s:%d/v1", getHost(), getPort());
    apiClient.setBasePath(basePath);
    return apiClient;
  }

  /**
   * Resolves an admin access token for realm
   * 
   * @return an access token
   * @throws IOException thrown on communication failure
   */
  protected String getAdminToken() throws IOException {
    return getAccessToken(ADMIN_USERNAME, ADMIN_PASSWORD); 
  }

  /**
   * Resolves an access token for username and password
   * 
   * @param username username
   * @param password password
   * @return an access token
   * @throws IOException thrown on communication failure
   */
  protected String getAccessToken(String username, String password) throws IOException {
    String path = String.format("/auth/realms/%s/protocol/openid-connect/token", KEYCLOAK_REALM);
    String response = given()
      .baseUri(AUTH_SERVER_URL)
      .formParam("client_id", KEYCLOAK_CLIENT_ID)
      .formParam("client_secret", KEYCLOAK_CLIENT_SECRET)
      .formParam("grant_type", "password")
      .formParam("username", username)
      .formParam("password", password)
      .post(path)
      .getBody()
      .asString();

    Map<String, Object> responseMap = readJsonMap(response);
    String result = (String) responseMap.get("access_token");
    assertNotNull(result);
    return result;
  }

  /**
   * Resolves an anonymous access token (service account)
   * 
   * @return an access token
   * @throws IOException thrown on communication failure
   */
  protected String getAnonymousToken() throws IOException {
    String path = String.format("/auth/realms/%s/protocol/openid-connect/token", KEYCLOAK_REALM);
    String passwordEncoded = Base64.encodeBase64String(String.format("%s:%s", KEYCLOAK_CLIENT_ID, KEYCLOAK_CLIENT_SECRET).getBytes());
    String authorization = String.format("Basic %s", passwordEncoded);
    String response = given()
      .baseUri(AUTH_SERVER_URL)
      .header("Authorization", authorization)
      .formParam("grant_type", "client_credentials")
      .post(path)
      .getBody()
      .asString();
      
    Map<String, Object> responseMap = readJsonMap(response);
    String result = (String) responseMap.get("access_token");
    assertNotNull(result);
    return result;
  }
  
  /**
   * Starts a mailgun mocker
   * 
   * @return mailgun mocker
   */
  protected MailgunMocker startMailgunMocker() {
    String domain = "domain.example.com";
    String path = "mgapi";
    String apiKey = "fakekey";
    String senderEmail = "dcfb-test@example.com";
    String senderName = "DCFB Test";

    executeInsert("INSERT INTO SystemSetting (id, settingkey, value) VALUES (?, ?, ?)", UUID.randomUUID().toString(), "mailgun-apiurl", String.format("%s/%s",getWireMockBasePath(), path));
    executeInsert("INSERT INTO SystemSetting (id, settingkey, value) VALUES (?, ?, ?)", UUID.randomUUID().toString(), "mailgun-domain", domain);
    executeInsert("INSERT INTO SystemSetting (id, settingkey, value) VALUES (?, ?, ?)", UUID.randomUUID().toString(), "mailgun-apikey", apiKey);
    executeInsert("INSERT INTO SystemSetting (id, settingkey, value) VALUES (?, ?, ?)", UUID.randomUUID().toString(), "mailgun-sender-email", senderEmail);
    executeInsert("INSERT INTO SystemSetting (id, settingkey, value) VALUES (?, ?, ?)", UUID.randomUUID().toString(), "mailgun-sender-name", senderName);
    
    MailgunMocker mailgunMocker = new MailgunMocker(String.format("/%s", path), domain, apiKey);
    mailgunMocker.startMock();
    return mailgunMocker;
  }

  /**
   * Stops a malgun mocker
   * 
   * @param mailgunMocker mocker
   */
  protected void stopMailgunMocker(MailgunMocker mailgunMocker) {
    mailgunMocker.stopMock();
    executeDelete("DELETE FROM SystemSetting WHERE settingKey in ('mailgun-apiurl', 'mailgun-domain', 'mailgun-apikey', 'mailgun-sender-email', 'mailgun-sender-name')");
  }
  
  private void insertSystemSetting(String key, String value) {
    executeInsert("INSERT INTO SystemSetting (id, settingkey, value) VALUES (?, ?, ?)", UUID.randomUUID().toString(), key, value);    
  }
  
  private void deleteSystemSettings(String... keys) {
    executeDelete("DELETE FROM SystemSetting WHERE settingKey in (?)", (Object[]) keys);    
  }
}