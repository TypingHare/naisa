package com.naisa.config;

import java.net.URI;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Comparator;
import java.util.List;

import jakarta.annotation.Priority;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.flyway.autoconfigure.FlywayMigrationStrategy;
import org.springframework.boot.jdbc.autoconfigure.DataSourceProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import com.naisa.test.seed.DatabaseSeeder;

/**
 * Recreates and seeds the local test database during startup.
 *
 * <p>
 * This configuration is used only when the {@code test} profile is active.
 *
 * @author James Chen, Codex
 */
@Configuration(proxyBeanMethods = false)
@Profile("test")
public class TestDatabaseConfiguration {
    private static final Logger LOGGER = LoggerFactory.getLogger(TestDatabaseConfiguration.class);

    /**
     * Replaces Spring Boot's default Flyway startup behavior with database recreation and
     * migration.
     *
     * The returned strategy performs the following steps: (1) recreates the test database by
     * forcibly dropping the existing database and creating a new one, and (2) executes all Flyway
     * migration files against the newly created database.
     *
     * <p>
     * PostgreSQL passwords should be stored in {@code .pgpass} for security. If the database
     * username differs from the operating system username, {@code spring.datasource.username} must
     * be set.
     *
     * <p>
     * If the port is missing from the database URL, it defaults to 5432.
     *
     * <p>
     * It is assumed that the host is always {@code localhost} for test databases.
     *
     * @param username   the PostgreSQL username, or a blank value to use the driver's default
     * @param properties the datasource properties containing the test database URL
     * @return the strategy that recreates and migrates the test database
     * @author James Chen, Codex
     */
    @Bean
    private FlywayMigrationStrategy testDatabaseMigrationStrategy(
        @Value("${spring.datasource.username:}") final String username,
        final DataSourceProperties properties
    ) {
        return flyway -> {
            // Recreate the test database.
            final URI uri = URI.create(properties.getUrl().substring("jdbc:".length()));
            final int port = uri.getPort();
            final String databaseName = uri.getPath().substring(1);
            recreateDatabase(
                username.isBlank() ? null : username,
                port == -1 ? 5432 : port,
                databaseName
            );

            // Run migration files.
            flyway.migrate();
        };
    }

    /**
     * Creates a runner that seeds the test database after Flyway migrations and Hibernate
     * initialization have completed.
     *
     * <p>
     * Every registered {@link DatabaseSeeder} must be annotated with {@link Priority}. The runner
     * checks all seeders before inserting any data and fails if an annotation is missing. Seeders
     * run sequentially in ascending priority order (lower values run first). Seeders with the same
     * priority run in alphabetical order of their fully qualified class names.
     *
     * <p>
     * The {@code generateEntities()} method of each seeder receives the same base seed from
     * {@code test.seeder.seed}, which defaults to 471 when the property is absent.
     *
     * <p>
     * For each seeder, the runner calls {@code generateAndSeed()} with the count from
     * {@code test.seeder.count}, which defaults to 100 when the property is absent.
     *
     * @param seed    the base seed passed to each database seeder for repeatable data generation
     * @param count   the number of entities requested from each database seeder
     * @param seeders the database seeders supplied by Spring
     * @return an application runner that invokes each seeder during startup
     * @throws RuntimeException if any {@link DatabaseSeeder} subclass lacks a {@link Priority}
     *                          annotation
     * @author James Chen, Codex
     */
    @Bean
    private ApplicationRunner seedTestDatabase(
        @Value("${test.seeder.seed:471}") final long seed,
        @Value("${test.seeder.count:100}") final int count,
        final List<DatabaseSeeder<?>> seeders
    ) {
        return args -> {
            for (final DatabaseSeeder<?> seeder : seeders) {
                final Class<?> seederClass = seeder.getClass();
                if (seederClass.getAnnotation(Priority.class) == null) {
                    throw new RuntimeException(
                        String.format(
                            "Database seeder %s is not annotated by %s",
                            seederClass.getName(),
                            Priority.class.getName()
                        )
                    );
                }
            }

            final List<DatabaseSeeder<?>> orderedSeeders = seeders.stream().sorted(
                Comparator.comparingInt(
                    seeder -> seeder.getClass().getAnnotation(Priority.class).value()
                ).thenComparing(seeder -> seeder.getClass().getName())
            ).toList();
            for (final DatabaseSeeder<?> seeder : orderedSeeders) {
                seeder.generateAndSeed(seed, count);
            }
        };
    }

    /**
     * Recreates the database.
     *
     * <p>
     * This method connects to the {@code postgres} database and executes the following two SQL
     * statements:
     *
     * {@snippet lang = sql :
     *     DROP DATABASE IF EXISTS "db_name" WITH (FORCE);
     *     CREATE DATABASE "db_name";
     * }
     *
     * Here, {@code db_name} represents the given database name. This operation permanently deletes
     * all records in the database. Therefore, this method should only be called in a test
     * environment.
     *
     * <p>
     * The test database will be dropped forcibly. If it does not exist, the DROP statement is a
     * no-op.
     *
     * <p>
     * The PostgreSQL server is assumed to run on {@code localhost}, and the database name must not
     * contain double quotes. No password is supplied explicitly, allowing the driver to look it up
     * in {@code .pgpass}. The entry must match the {@code postgres} maintenance database or use a
     * wildcard for the database name.
     *
     * @param username     the PostgreSQL username, or {@code null} to use the driver's default
     * @param port         the port on which the PostgreSQL server is listening
     * @param databaseName the name of the database
     * @throws IllegalStateException if connecting to PostgreSQL or recreating the database fails
     * @author James Chen, Codex
     */
    private static void recreateDatabase(
        final String username,
        final int port,
        final String databaseName
    ) {
        try (Connection connection = DriverManager.getConnection(
            String.format("jdbc:postgresql://localhost:%d/postgres", port),
            username,
            null
        ); Statement statement = connection.createStatement()) {
            connection.setAutoCommit(true);
            statement.executeUpdate(
                String.format("DROP DATABASE IF EXISTS \"%s\" WITH (FORCE)", databaseName)
            );
            LOGGER.info("Dropped database {}", databaseName);
            statement.executeUpdate(String.format("CREATE DATABASE \"%s\"", databaseName));
            LOGGER.info("Created database {}", databaseName);
        } catch (SQLException exception) {
            throw new IllegalStateException("Could not recreate the test database.", exception);
        }
    }
}
