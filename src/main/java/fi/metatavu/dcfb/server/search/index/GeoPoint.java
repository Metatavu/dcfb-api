package fi.metatavu.dcfb.server.search.index;

import java.math.BigDecimal;

public class GeoPoint {
  
  private BigDecimal lat;
  private BigDecimal lon;
  
  public GeoPoint() {
  }
  
  public GeoPoint(BigDecimal lat, BigDecimal lon) {
    super();
    this.lat = lat;
    this.lon = lon;
  }

  public BigDecimal getLat() {
    return lat;
  }
  
  public BigDecimal getLon() {
    return lon;
  }
  
}
