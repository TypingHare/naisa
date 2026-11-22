package com.naisa.util;

import org.springframework.http.HttpMethod;

/**
 * Identifies an API route by its HTTP method and request path.
 *
 * @author James Chen
 */
public record Route(HttpMethod method, String path) {
    @Override
    public final String toString() {
        return method.toString().toUpperCase() + " " + path;
    }
}
