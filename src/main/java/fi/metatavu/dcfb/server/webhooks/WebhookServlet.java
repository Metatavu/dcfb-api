package fi.metatavu.dcfb.server.webhooks;

import java.io.IOException;
import java.io.Writer;
import java.util.Iterator;

import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.transaction.Transactional;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;

/**
 * Servlet handling receiving webhooks
 * 
 * @author Heikki Kurhinen
 */
@WebServlet (urlPatterns = "/webhooks/*")
@Transactional
public class WebhookServlet extends HttpServlet {

  private static final long serialVersionUID = 8456693177050096253L;

  @Inject
  private Logger logger;
  
  @Inject
  @Any
  private Instance<WebhookHandler> webhookHandlers;
  
  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    String type = StringUtils.stripStart(request.getPathInfo(), "/");
    if (StringUtils.isBlank(type)) {
      sendResponse(response, HttpServletResponse.SC_NOT_FOUND, "Type not defined");
      return;
    }

    logger.info("Received webhook of type: {}", type);

    Iterator<WebhookHandler> webhookHandlerIterator = webhookHandlers.iterator();
    try {
      while (webhookHandlerIterator.hasNext()) {
        WebhookHandler webhookHandler = webhookHandlerIterator.next();
        if (StringUtils.equals(webhookHandler.getType(), type)) {
          webhookHandler.handle(request);
          sendResponse(response, HttpServletResponse.SC_OK, "OK");
          return;
        }
      }
    } catch (WebhookException e) {
      logger.error("Error processing webhook", e);
      sendResponse(response, HttpServletResponse.SC_BAD_REQUEST, "webhook processing failed");
      return;
    }
    
    sendResponse(response, HttpServletResponse.SC_NOT_FOUND, "Not found");
  }

  /**
   * Writes http response
   * 
   * @param response http servlet responose
   * @param status response http status code
   * @param message message to send
   * 
   */
  private void sendResponse(HttpServletResponse response, int status, String message) {
    try (Writer writer = response.getWriter()) {
      writer.write(message);
      response.setStatus(status);
    } catch (IOException e) {
      logger.error("Could not write response", e);
    }
  }
}
