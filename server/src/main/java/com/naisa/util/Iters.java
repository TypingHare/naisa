package com.naisa.util;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;

/**
 * A utility class containing a collection of helper functions for iteration operations.
 *
 * @author James Chen
 */
public final class Iters {
    private Iters() {
    }

    /**
     * Maps each element of a collection to an element in a new list.
     *
     * @param <T>        source element type
     * @param <R>        result element type
     * @param collection collection to map
     * @param mapper     function applied to each element
     * @return an unmodifiable list of mapped elements in encounter order
     * @author James Chen
     */
    public static <T, R> List<R> map(final Collection<T> collection, final Function<T, R> mapper) {
        return collection.stream().map(mapper).toList();
    }

    /**
     * Sums the values obtained by mapping each element.
     *
     * @param <T>        source element type
     * @param collection collection to sum
     * @param mapper     function mapping each element to a non-null number
     * @return the sum, or 0.0 for an empty collection
     */
    public static <T> double sum(final Collection<T> collection, final Function<T, Double> mapper) {
        return collection.stream().map(mapper).reduce(0.0, Double::sum);
    }

    /**
     * Calculates the arithmetic mean of the values obtained by mapping each element.
     *
     * @param <T>        source element type
     * @param collection collection to average
     * @param mapper     function mapping each element to a non-null number
     * @return the arithmetic mean, or {@link Double#NaN} for an empty collection
     */
    public static <T> double average(
        final Collection<T> collection,
        final Function<T, Double> mapper
    ) {
        return sum(collection, mapper) / collection.size();
    }
}
