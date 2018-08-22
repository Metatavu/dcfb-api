package fi.metatavu.dcfb.server.items;

import javax.ejb.Schedule;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@Startup
@Singleton
@ApplicationScoped
public class ItemReservationExpirationScheduler {
  
  @Inject
  private ItemController itemController;

  @Schedule(hour = "*", minute = "*", info = "Every minute")
  public void deleteExpiredReservations() {
    itemController.deleteExpiredReservations();
  }
  
}
