package fi.metatavu.dcfb.server;

import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TestName;
import org.postgresql.util.PGobject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fi.metatavu.dcfb.client.Meta;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/**
 * Abstract base class for all tests
 * 
 * @author Antti Leppä
 * @author Heikki Kurhinen
 */
public abstract class AbstractTest {
  
  private static Logger logger = LoggerFactory.getLogger(AbstractTest.class.getName());
  
  @Rule
  public TestName testName = new TestName();
  
  @Before
  @SuppressWarnings ("squid:S106")
  public void printName() {
    System.out.println(String.format("> %s", testName.getMethodName()));
  }
  
  /**
   * Returns object mapper with default modules and settings
   * 
   * @return object mapper
   */
  protected ObjectMapper getObjectMapper() {
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.registerModule(new JavaTimeModule());
    objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    
    return objectMapper;
  }
  
  /**
   * Reads JSON src into Map
   * 
   * @param src input
   * @return map
   * @throws IOException throws IOException when there is error when reading the input 
   */
  protected Map<String, Object> readJsonMap(InputStream src) throws IOException {
    return getObjectMapper().readValue(src, new TypeReference<Map<String, Object>>() {});
  }

  /**
   * Reads JSON src into Map
   * 
   * @param src input
   * @return map
   * @throws IOException throws IOException when there is error when reading the input 
   */
  protected Map<String, Object> readJsonMap(String src) throws IOException {
    return getObjectMapper().readValue(src, new TypeReference<Map<String, Object>>() {});
  }
  
  /**
   * Executes a select statement into test database
   * 
   * @param sql sql
   * @param eachRow method to call for each row
   * @param params params
   */
  protected <T> List<T> executeSelect(String sql, Function<ResultSet, T> eachRow, Object... params) {
    List<T> result = new ArrayList<>();
     
    try (Connection connection = getConnection()) {
      connection.setAutoCommit(true);
      try (PreparedStatement statement = connection.prepareStatement(sql)) {
        applyStatementParams(statement, params);
        ResultSet resultSet = statement.executeQuery();
        while (resultSet.next()) {
          result.add(eachRow.apply(resultSet));
        }
      }
    } catch (Exception e) {
      logger.error("Failed to execute insert", e);
      fail(e.getMessage());
    }
    
    return result;
  }
  
  /**
   * Executes a select statement into test database
   * 
   * @param sql sql
   * @param eachRow method to call for each row
   * @param params params
   */
  protected <T> T executeSelectSingle(String sql, Function<ResultSet, T> eachRow, Object... params) {
    List<T> result = executeSelect(sql, eachRow, params);
    if (result.isEmpty()) {
      return null;
    }
    
    return result.get(0);
  }
  
  /**
   * Executes an update statement into test database
   * 
   * @param sql sql
   * @param params params
   */
  protected void executeUpdate(String sql, Object... params) {
    executeInsert(sql, params);
  }
  
  /**
   * Executes an insert statement into test database
   * 
   * @param sql sql
   * @param params params
   */
  protected void executeInsert(String sql, Object... params) {
    try (Connection connection = getConnection()) {
      connection.setAutoCommit(true);
      try (PreparedStatement statement = connection.prepareStatement(sql)) {
        applyStatementParams(statement, params);
        statement.execute();
      }
    } catch (Exception e) {
      logger.error("Failed to execute insert", e);
      fail(e.getMessage());
    }
  }
  
  /**
   * Executes a delete statement
   * 
   * @param sql sql 
   * @param params params
   */
  protected void executeDelete(String sql, Object... params) {
    try (Connection connection = getConnection()) {
      connection.setAutoCommit(true);
      try (PreparedStatement statement = connection.prepareStatement(sql)) {
        applyStatementParams(statement, params);
        statement.execute();
      }
    } catch (Exception e) {
      logger.error("Failed to execute delete", e);
      fail(e.getMessage());
    }
  }

  /**
   * Returns offset date time
   * 
   * @param year year
   * @param month month
   * @param dayOfMonth day 
   * @param zone zone
   * @return offset date time
   */
  protected OffsetDateTime getOffsetDateTime(int year, int month, int dayOfMonth, ZoneId zone) {
    return getZonedDateTime(year, month, dayOfMonth, 0, 0, 0, zone).toOffsetDateTime();
  }

  /**
   * Parses offset date time from string
   * 
   * @param string string
   * @return parsed offset date time
   */
  protected OffsetDateTime parseOffsetDateTime(String string) {
    return OffsetDateTime.parse(string);
  }

  /**
   * Returns ISO formatted date string
   * 
   * @param year year
   * @param month month
   * @param dayOfMonth day 
   * @param zone zone
   * @return ISO formatted date string
   */
  protected String getIsoDateTime(int year, int month, int dayOfMonth, ZoneId zone) {
    return DateTimeFormatter.ISO_DATE_TIME.format(getOffsetDateTime(year, month, dayOfMonth, zone));
  }
  
  /**
   * Returns zoned date time
   * 
   * @param year year
   * @param month month
   * @param dayOfMonth day 
   * @param hour hour
   * @param minute minute
   * @param second second
   * @param zone zone
   * @return zoned date time
   */
  protected ZonedDateTime getZonedDateTime(int year, int month, int dayOfMonth, int hour, int minute, int second, ZoneId zone) {
    return ZonedDateTime.of(year, month, dayOfMonth, hour, minute, second, 0, zone);
  }

  /**
   * Maps list of metas into a map
   * 
   * @param metas
   * @return map
   */
  protected Map<String, String> mapMetas(List<Meta> metas) {
    Map<String, String> result = new HashMap<>(metas.size());

    metas.stream().forEach(meta -> {
      result.put(meta.getKey(), meta.getValue());
    });

	  return result;
  }

  /**
   * Returns test database connection
   * 
   * @return test database connection
   */
  private Connection getConnection() {
    String username = System.getProperty("it.jdbc.username");
    String password = System.getProperty("it.jdbc.password");
    String url = System.getProperty("it.jdbc.url");
    try {
      Class.forName(System.getProperty("it.jdbc.driver")).newInstance();
    } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
      logger.error("Failed to load JDBC driver", e);
      fail(e.getMessage());
    }

    try {
      return DriverManager.getConnection(url, username, password);
    } catch (SQLException e) {
      logger.error("Failed to get connection", e);
      fail(e.getMessage());
    }
    
    return null;
  }
  
  /**
   * Applies params into sql statement
   * 
   * @param statement statement
   * @param params params
   * @throws SQLException
   */
  private void applyStatementParams(PreparedStatement statement, Object... params) throws SQLException {
    for (int i = 0, l = params.length; i < l; i++) {
      Object param = params[i];
      if (param instanceof List) {
        statement.setObject(i + 1, ((List<?>) param).toArray());
      } else if (param instanceof UUID) {
        PGobject pgObject = new PGobject();
        pgObject.setType("uuid");
        pgObject.setValue(param.toString());
        statement.setObject(i + 1, pgObject);
      } else {
        statement.setObject(i + 1, params[i]);
      }
    }
  }
 
}
