package fi.metatavu.dcfb.server.webhooks;

/**
 * Wrapper class for exceptions thrown by webhook handlers
 * 
 * @author Heikki Kurhinen
 * 
 */
public class WebhookException extends Exception {

  private static final long serialVersionUID = 6270861111969736364L;

  public WebhookException () {
  }

  public WebhookException(String message) {
    super (message);
  }

  public WebhookException(Throwable cause) {
    super (cause);
  }

  public WebhookException(String message, Throwable cause) {
    super(message, cause);
  }
}