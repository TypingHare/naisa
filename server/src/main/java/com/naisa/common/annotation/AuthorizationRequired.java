package com.naisa.common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that a controller method requires authorization.
 *
 * <p>Authorization is implemented in {@code AuthorizationInterceptor}.
 *
 * @author James Chen
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AuthorizationRequired {}
