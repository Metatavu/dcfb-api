package fi.metatavu.dcfb.server.webhooks;

import javax.servlet.http.HttpServletRequest;

/**
 * Interface describing webhook handler
 *
 * @author Heikki Kurhinen 
 */
public interface WebhookHandler {

  /**
   * @return Returns webhook handler type
   */
  public String getType();

  /**
   * Handles webhook request
   * 
   * @param request http servlet request
   */
  public void handle(HttpServletRequest request) throws WebhookException;

}