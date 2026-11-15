package com.naisa.test.seed;

import java.util.List;
import java.util.Locale;
import java.util.Random;

import net.datafaker.Faker;

/**
 * Represents a database seeder.
 *
 * <p>
 * Concrete seeders must be registered as Spring beans and annotated with
 * {@link jakarta.annotation.Priority}. The test database runner invokes them in ascending priority
 * order, breaking ties by fully qualified class name.
 *
 * @param <T> the entity type
 * @author James Chen
 */
public abstract class DatabaseSeeder<T> {
    /**
     * Generates a list of entities.
     *
     * <p>
     * Implementations should generate reproducible entity objects with the provided seed and
     * requested count. They should initialize any random generators with the supplied seed to make
     * generation repeatable. Calling {@code getFaker()} to obtain a faker object and using it to
     * generate random data is recommended.
     *
     * @param seed  the seed to use for random data generation
     * @param count the number of entities to generate
     * @return the generated entities
     */
    public abstract List<T> generateEntities(long seed, int count);

    /**
     * Inserts a list of entities as initial data into the database.
     *
     * <p>
     * Implementations should insert the entities into the database and log the insertion after it
     * succeeds.
     *
     * @param entities the entities to insert
     * @author James Chen
     */
    public abstract void seed(List<T> entities);

    /**
     * Creates a faker object using the specified seed plus an offset.
     *
     * <p>
     * The offset is the hash code of the subclass's simple name. Adding this offset helps prevent
     * different seeders from generating identical sequences of seed data. For example, suppose two
     * tables have similar structures and each has an integer "age" as its first column. Without an
     * offset, both seeders would generate the same sequence of ages.
     *
     * @param seed the seed to use for random data generation
     * @return a faker object created using the combined seed
     * @author James Chen
     */
    protected Faker getFaker(final long seed) {
        final int offset = this.getClass().getSimpleName().hashCode();
        return new Faker(Locale.US, new Random(seed + offset));
    }

    /**
     * Generates the requested number of entities and inserts them into the database.
     *
     * @param seed  the base seed to use for random data generation
     * @param count the number of entities to generate and seed
     * @author James Chen
     */
    public void generateAndSeed(final long seed, final int count) {
        seed(generateEntities(seed, count));
    }
}
