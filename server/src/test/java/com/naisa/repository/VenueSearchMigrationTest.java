package com.naisa.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;
import javax.sql.DataSource;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Checks fresh installation and upgrade without changing existing database tables.
 *
 * @author Andrew Hsu, Codex
 */
@SpringBootTest
class VenueSearchMigrationTest {
  @Autowired private DataSource dataSource;

  /**
   * Adds nullable price metadata and surface type without classifying existing prices.
   *
   * @param upgrade whether to insert a stored price before V2 runs
   * @author Andrew Hsu, Codex
   */
  @ParameterizedTest
  @ValueSource(booleans = {false, true})
  void supportsFreshInstallAndV1Upgrade(final boolean upgrade) {
    final JdbcTemplate jdbc = new JdbcTemplate(dataSource);
    final String schema = "search_test_" + UUID.randomUUID().toString().replace("-", "");
    jdbc.execute("CREATE SCHEMA " + schema);
    try {
      if (upgrade) {
        Flyway.configure()
            .dataSource(dataSource)
            .defaultSchema(schema)
            .schemas(schema)
            .target("1")
            .load()
            .migrate();
        insertPrice(jdbc, schema);
      }
      Flyway.configure()
          .dataSource(dataSource)
          .defaultSchema(schema)
          .schemas(schema)
          .load()
          .migrate();
      if (!upgrade) {
        insertPrice(jdbc, schema);
      }
      final Map<String, Object> row =
          jdbc.queryForMap(
              "SELECT price, currency, price_unit, surface_type FROM " + schema + ".venue_sports");
      assertEquals(new BigDecimal("12.50"), row.get("price"));
      assertNull(row.get("currency"));
      assertNull(row.get("price_unit"));
      assertNull(row.get("surface_type"));
      assertEquals(3, columnLength(jdbc, schema, "currency"));
      assertEquals(32, columnLength(jdbc, schema, "price_unit"));
      assertEquals(64, columnLength(jdbc, schema, "surface_type"));
      assertEquals(
          2,
          jdbc.queryForObject(
              "SELECT count(*) FROM " + schema + ".flyway_schema_history WHERE success",
              Integer.class));
    } finally {
      jdbc.execute("DROP SCHEMA " + schema + " CASCADE");
    }
  }

  /**
   * Stores a V1 price without new metadata.
   *
   * @param jdbc the database connection helper
   * @param schema the test schema
   * @author Andrew Hsu, Codex
   */
  private void insertPrice(final JdbcTemplate jdbc, final String schema) {
    jdbc.update(
        "INSERT INTO " + schema + ".venues "
            + "(id, name, address, latitude, longitude, timezone) "
            + "VALUES (1, 'Migration venue', '1 Test Street', 0, 0, 'UTC')");
    jdbc.update("INSERT INTO " + schema + ".sports (id, name) VALUES (1, 'Tennis')");
    jdbc.update(
        "INSERT INTO " + schema + ".venue_sports "
            + "(venue_id, sport_id, price, is_indoor) VALUES (1, 1, 12.50, false)");
  }

  /**
   * Reads the declared length of a new nullable column.
   *
   * @param jdbc the database connection helper
   * @param schema the test schema
   * @param column the column name
   * @return the declared maximum length
   * @author Andrew Hsu, Codex
   */
  private int columnLength(final JdbcTemplate jdbc, final String schema, final String column) {
    return jdbc.queryForObject(
        """
        SELECT character_maximum_length FROM information_schema.columns
        WHERE table_schema = ? AND table_name = 'venue_sports' AND column_name = ?
        """,
        Integer.class,
        schema,
        column);
  }
}
