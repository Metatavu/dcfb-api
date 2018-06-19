package fi.metatavu.dcfb.server.search.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Annotation for declaring how indexed field is handled
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface Field {

  /**
   * Analyzer
   */
  String analyzer() default "";
  
  /**
   * Type. Defaults to sring
   */
  String type() default "string";

  /**
   * Index. Defaults to analyzed
   */
  String index() default "analyzed";
  
  /**
   * Store. Defaults to false
   */
  boolean store() default false;
  
}
