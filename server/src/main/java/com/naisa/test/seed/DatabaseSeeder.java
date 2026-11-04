package com.naisa.test.seed;

/**
 * Represents a database seeder.
 *
 * <p>
 * Concrete seeders must be registered as Spring beans and annotated with
 * {@link jakarta.annotation.Priority}. The test database runner invokes them in ascending priority
 * order, breaking ties by fully qualified class name.
 *
 * @author James Chen
 */
public abstract class DatabaseSeeder {
    /**
     * Inserts this seeder's initial data into the database.
     */
    public abstract void seed();
}
