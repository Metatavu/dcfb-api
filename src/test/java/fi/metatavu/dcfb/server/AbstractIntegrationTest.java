package fi.metatavu.dcfb.server;

import static io.restassured.RestAssured.given;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang3.math.NumberUtils;

import fi.metatavu.dcfb.ApiClient;
import fi.metatavu.dcfb.client.CategoriesApi;
import fi.metatavu.dcfb.client.ItemsApi;


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
  protected static final String DEFAULT_KEYCLOAK_CLIENT_ID = "ui";
  protected static final UUID REALM1_USER_1_ID = UUID.fromString("c72e219c-71a0-4f5e-9b06-5dafe5394e27");
  
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
   * Resolves an access token for realm, username and password
   * 
   * @param username username
   * @param password password
   * @return an access token
   * @throws IOException thrown on communication failure
   */
  protected String getAccessToken(String username, String password) throws IOException {
    return getAccessToken(DEFAULT_KEYCLOAK_CLIENT_ID, username, password);
  }

  /**
   * Resolves an admin access token for realm
   * 
   * @return an access token
   * @throws IOException thrown on communication failure
   */
  protected String getAdminToken() throws IOException {
    return getAccessToken(DEFAULT_KEYCLOAK_CLIENT_ID, ADMIN_USERNAME, ADMIN_PASSWORD); 
  }

  /**
   * Resolves an access token for realm, client, username and password
   * 
   * @param clientId clientId
   * @param username username
   * @param password password
   * @return an access token
   * @throws IOException thrown on communication failure
   */
  protected String getAccessToken(String clientId, String username, String password) throws IOException {
    String path = String.format("/auth/realms/%s/protocol/openid-connect/token", KEYCLOAK_REALM);
    
    String response = given()
      .baseUri(AUTH_SERVER_URL)
      .formParam("client_id", clientId)
      .formParam("grant_type", "password")
      .formParam("username", username)
      .formParam("password", password)
      .post(path)
      .getBody()
      .asString();

    Map<String, Object> responseMap = readJsonMap(response);
    return (String) responseMap.get("access_token");
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
  
}