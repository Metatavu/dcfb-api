package fi.metatavu.dcfb.server.locations;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class LocationConsts {

  public static final String COORDINATE_REFERENCE_SYSTEM = "epsg4326";
  public static final List<String> SUPPORTED_COORDINATE_REFERENCE_SYSTEMS = Collections.unmodifiableList(Arrays.asList( COORDINATE_REFERENCE_SYSTEM )); 

  private LocationConsts() {
    // Private constructor
  }

}