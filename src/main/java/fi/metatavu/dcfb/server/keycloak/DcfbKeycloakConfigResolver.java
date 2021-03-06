package fi.metatavu.dcfb.server.keycloak;

import java.io.FileInputStream;
import java.io.IOException;

import org.keycloak.adapters.KeycloakConfigResolver;
import org.keycloak.adapters.KeycloakDeployment;
import org.keycloak.adapters.KeycloakDeploymentBuilder;
import org.keycloak.adapters.spi.HttpFacade.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DcfbKeycloakConfigResolver implements KeycloakConfigResolver {

  private static Logger logger = LoggerFactory.getLogger(DcfbKeycloakConfigResolver.class.getName());

  @Override
  public KeycloakDeployment resolve(Request request) {
    String configFilePath = System.getProperty("keycloak.config-path");
    FileInputStream configStream;
    try {
      configStream = new FileInputStream(configFilePath);
      try {
        return KeycloakDeploymentBuilder.build(configStream);
      } finally {
        configStream.close();
      }
    } catch (IOException e) {
      logger.warn("Failed to read config file", e);
    }

    return null;
  }

}